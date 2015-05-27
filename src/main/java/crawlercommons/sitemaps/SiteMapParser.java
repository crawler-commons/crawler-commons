/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.sitemaps;

import static org.apache.tika.mime.MediaType.APPLICATION_XML;
import static org.apache.tika.mime.MediaType.TEXT_PLAIN;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

public class SiteMapParser {
    public static final Logger LOG = LoggerFactory.getLogger(SiteMapParser.class);

    /** According to the specs, 50K URLs per Sitemap is the max */
    private static final int MAX_URLS = 50000;

    /** Sitemap docs must be limited to 10MB (10,485,760 bytes) */
    public static int MAX_BYTES_ALLOWED = 10485760;

    /* Tika's MediaType components */
    private final static Tika TIKA = new Tika();
    private final static MediaTypeRegistry MEDIA_TYPE_REGISTRY = MediaTypeRegistry.getDefaultRegistry();

    private final static List<MediaType> XML_MEDIA_TYPES = new ArrayList<MediaType>();
    private final static List<MediaType> TEXT_MEDIA_TYPES = new ArrayList<MediaType>();
    private final static List<MediaType> GZ_MEDIA_TYPES = new ArrayList<MediaType>();
    
    static {
        initMediaTypes();
    }

    /** True (by default) if invalid URLs should be rejected */
    private boolean strict;
    /** False (by default) if partial/incomplete documents should still be processed */
    private boolean allowPartial;

    public SiteMapParser() {
        this(true, false);
    }


	public SiteMapParser(boolean strict) {
        this(strict, false);
    }

	public SiteMapParser(boolean strict, boolean allowPartial) {
		this.strict = strict;
		this.allowPartial = allowPartial;
	}
	
    /**
     * @return whether invalid URLs will be rejected
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Returns a SiteMap or SiteMapIndex given an online sitemap URL<br/>
     * Please note that this method is a static method which goes online and
     * fetches the sitemap then parses it<br/>
     * <br/>
     * This method is a convenience method for a user who has a sitemap URL and
     * wants a "Keep it simple" way to parse it.
     * 
     * @param onlineSitemapUrl
     *            URL of the online sitemap
     * @return AbstractSiteMap object or null if the onlineSitemap is null
     **/
    public AbstractSiteMap parseSiteMap(URL onlineSitemapUrl) throws UnknownFormatException, IOException {
        if (onlineSitemapUrl == null) {
            return null;
        }
        byte[] bytes = IOUtils.toByteArray(onlineSitemapUrl);
        return parseSiteMap(bytes, onlineSitemapUrl);
    }

    /**
     * Returns a processed copy of an unprocessed sitemap object, i.e. transfer
     * the value of getLastModified Please note that the sitemap input stays
     * unchanged
     **/
    public AbstractSiteMap parseSiteMap(String contentType, byte[] content, final AbstractSiteMap sitemap) throws UnknownFormatException, IOException {
        AbstractSiteMap asmCopy = parseSiteMap(contentType, content, sitemap.getUrl());
        asmCopy.setLastModified(sitemap.getLastModified());
        return asmCopy;
    }

    /**
     * @return SiteMap/SiteMapIndex by guessing the content type from the binary
     *         content and URL
     **/
    public AbstractSiteMap parseSiteMap(byte[] content, URL url) throws UnknownFormatException, IOException {
        if (url == null) {
            return null;
        }
        String filename = FilenameUtils.getName(url.getPath());
        String contentType = TIKA.detect(content, filename);
        return parseSiteMap(contentType, content, url);
    }

    /**
     * @return SiteMap/SiteMapIndex given a content type, byte content and the
     *         URL of a sitemap
     **/
    public AbstractSiteMap parseSiteMap(String contentType, byte[] content, URL url) throws UnknownFormatException, IOException {
        MediaType mediaType = MediaType.parse(contentType);

        while (mediaType != null && !mediaType.equals(MediaType.OCTET_STREAM)) { // Octet-stream
                                                                                 // is
                                                                                 // the
                                                                                 // father
                                                                                 // of
                                                                                 // all
                                                                                 // binary
                                                                                 // types
            if (XML_MEDIA_TYPES.contains(mediaType)) {
                return processXml(url, content);
            } else if (TEXT_MEDIA_TYPES.contains(mediaType)) {
                return (AbstractSiteMap) processText(url.toString(), content);
            } else if (GZ_MEDIA_TYPES.contains(mediaType)) {
                return processGzip(url, content);
            } else {
                mediaType = MEDIA_TYPE_REGISTRY.getSupertype(mediaType); // Check
                                                                         // parent
                return parseSiteMap(mediaType.toString(), content, url);
            }
        }

        throw new UnknownFormatException("Can't parse sitemap with MediaType of: " + contentType + " (at: " + url + ")");
    }

    /**
     * Parse the given XML content.
     * 
     * @param sitemapUrl
     * @param xmlContent
     * @return
     * @throws UnknownFormatException
     */
    private AbstractSiteMap processXml(URL sitemapUrl, byte[] xmlContent) throws UnknownFormatException {

        BOMInputStream bomIs = new BOMInputStream(new ByteArrayInputStream(xmlContent));
        InputSource is = new InputSource();
        try {
            is.setCharacterStream(new BufferedReader(new InputStreamReader(bomIs, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            IOUtils.closeQuietly(bomIs);
            throw new RuntimeException("Impossible exception", e);
        }

        return processXml(sitemapUrl, is);
    }
    

	/**
     * Process a text-based Sitemap. Text sitemaps only list URLs but no
     * priorities, last mods, etc.
     * 
     * @param content
     * @throws IOException
     */
    private SiteMap processText(String sitemapUrl, byte[] content) throws IOException {
        LOG.debug("Processing textual Sitemap");

        SiteMap textSiteMap = new SiteMap(sitemapUrl);
        textSiteMap.setType(SitemapType.TEXT);

        BOMInputStream bomIs = new BOMInputStream(new ByteArrayInputStream(content));
        @SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(new InputStreamReader(bomIs, "UTF-8"));

        String line;
        int i = 1;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0 && i <= MAX_URLS) {
                try {
                    URL url = new URL(line);
                    boolean valid = urlIsLegal(textSiteMap.getBaseUrl(), url.toString());

                    if (valid || !strict) {
                        LOG.debug("  {}. {}", i++, url);

                        SiteMapURL surl = new SiteMapURL(url, valid);
                        textSiteMap.addSiteMapUrl(surl);
                    }
                } catch (MalformedURLException e) {
                    LOG.warn("Bad URL [{}]. From Sitemap: [{}]", line, sitemapUrl);
                }
            }
        }
        textSiteMap.setProcessed(true);

        return textSiteMap;
    }

    /**
     * Decompress the gzipped content and process the resulting XML Sitemap.
     * 
     * @param url
     *            - URL of the gzipped content
     * @param response
     *            - Gzipped content
     * @throws MalformedURLException
     * @throws IOException
     * @throws UnknownFormatException
     */
    private AbstractSiteMap processGzip(URL url, byte[] response) throws MalformedURLException, IOException, UnknownFormatException {

        LOG.debug("Processing gzip");

        AbstractSiteMap smi;
        InputStream is = new ByteArrayInputStream(response);

        // Remove .gz ending
        String xmlUrl = url.toString().replaceFirst("\\.gz$", "");

        LOG.debug("XML url = {}", xmlUrl);

        BOMInputStream decompressed = new BOMInputStream(new GZIPInputStream(is));
        InputSource in = new InputSource(decompressed);
        in.setSystemId(xmlUrl);
        smi = processXml(url, in);
        decompressed.close();
        return smi;
    }

    /**
     * Parse the given XML content.
     * 
     * @param sitemapUrl
     * @param is
     * @throws UnknownFormatException
     */
    private AbstractSiteMap processXml(URL sitemapUrl, InputSource is) throws UnknownFormatException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        AbstractSiteMapSAXHandler handler = new AbstractSiteMapSAXHandler(sitemapUrl, strict);
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(is, handler);
            return handler.getSiteMap();
        } catch (IOException e) {
            UnknownFormatException ufe = new UnknownFormatException("Failed to parse " + sitemapUrl);
            ufe.initCause(e);
            throw ufe;
        } catch (SAXException e) {
            if (allowPartial) {
                LOG.warn("Processed broken/partial sitemap for '" + sitemapUrl + "'");
                AbstractSiteMap sitemap = handler.getSiteMap();
                sitemap.setProcessed(true);
                return sitemap;
            } else {
                UnknownFormatException ufe = new UnknownFormatException("Failed to parse " + sitemapUrl);
                ufe.initCause(e);
                throw ufe;
            }
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    
    /**
     * See if testUrl is under sitemapBaseUrl. Only URLs under sitemapBaseUrl
     * are legal. Both URLs are first converted to lowercase before the
     * comparison is made (this could be an issue on web servers that are case
     * sensitive).
     * 
     * @param sitemapBaseUrl
     * @param testUrl
     * @return true if testUrl is under sitemapBaseUrl, false otherwise
     */
    static boolean urlIsLegal(String sitemapBaseUrl, String testUrl) {

        boolean ret = false;

        // Don't try a comparison if the URL is too short to match
        if (sitemapBaseUrl != null && sitemapBaseUrl.length() <= testUrl.length()) {
            String u = testUrl.substring(0, sitemapBaseUrl.length()).toLowerCase();
            ret = sitemapBaseUrl.equals(u);
        }
        LOG.trace("urlIsLegal: {}  <= {}  ? {}", sitemapBaseUrl, testUrl, ret);

        return ret;
    }

    /**
     * Performs a one time intialization of Tika's Media-Type components and
     * media type collection constants <br/>
     * Please note that this is a private static method which is called once per
     * CLASS (not per instance / object)
     */
    private static void initMediaTypes() {
        /* XML media types (and all aliases) */
        XML_MEDIA_TYPES.add(APPLICATION_XML);
        XML_MEDIA_TYPES.addAll(MEDIA_TYPE_REGISTRY.getAliases(APPLICATION_XML));

        /* TEXT media types (and all aliases) */
        TEXT_MEDIA_TYPES.add(TEXT_PLAIN);
        TEXT_MEDIA_TYPES.addAll(MEDIA_TYPE_REGISTRY.getAliases(TEXT_PLAIN));

        /* GZIP media types (and all aliases) */
        MediaType gzipMediaType = MediaType.parse("application/gzip");
        GZ_MEDIA_TYPES.add(gzipMediaType);
        GZ_MEDIA_TYPES.addAll(MEDIA_TYPE_REGISTRY.getAliases(gzipMediaType));
    }
}
