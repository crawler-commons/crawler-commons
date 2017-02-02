/**
 * Copyright 2016 Crawler-Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

import static org.apache.tika.mime.MediaType.APPLICATION_XML;
import static org.apache.tika.mime.MediaType.TEXT_PLAIN;

public class SiteMapParser {
    public static final Logger LOG = LoggerFactory.getLogger(SiteMapParser.class);

    /**
     * According to the specs, 50K URLs per Sitemap is the max
     */
    private static final int MAX_URLS = 50000;

    /**
     * Sitemaps (including sitemap index files) &quot;must be no larger than
     * 50MB (52,428,800 bytes)&quot; as specified in the
     * <a href="https://www.sitemaps.org/protocol.html#index">Sitemaps XML
     * format</a> (before Nov. 2016 the limit has been 10MB).
     */
    public static final int MAX_BYTES_ALLOWED = 52428800;

    /* Tika's MediaType components */
    private static final Tika TIKA = new Tika();
    private static final MediaTypeRegistry MEDIA_TYPE_REGISTRY = MediaTypeRegistry.getDefaultRegistry();

    private static final List<MediaType> XML_MEDIA_TYPES = new ArrayList<>();
    private static final List<MediaType> TEXT_MEDIA_TYPES = new ArrayList<>();
    private static final List<MediaType> GZ_MEDIA_TYPES = new ArrayList<>();

    static {
        initMediaTypes();
    }

    /**
     * True (by default) meaning that invalid URLs should be rejected, as the
     * official docs allow the siteMapURLs to be only under the base url:
     * http://www.sitemaps.org/protocol.html#location
     */
    protected boolean strict = true;

    public SiteMapParser() {
      //default constructor
    }

    public SiteMapParser(boolean strict) {
        this.strict = strict;
    }

    /**
     * @return whether invalid URLs will be rejected (where invalid means that
     *         the url is not under the base url)
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * <p>Returns a SiteMap or SiteMapIndex given an online sitemap URL</p>
     * <p>Please note that this method is a static method which goes online and
     * fetches the sitemap then parses it</p>
     * This method is a convenience method for a user who has a sitemap URL and
     * wants a "Keep it simple" way to parse it.
     * 
     * @param onlineSitemapUrl
     *            URL of the online sitemap
     * @return Extracted SiteMap/SiteMapIndex or null if the onlineSitemapUrl is
     *         null
     * @throws UnknownFormatException if there is an error parsing the sitemap
     * @throws IOException if there is an error reading in the site map {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(URL onlineSitemapUrl) throws UnknownFormatException, IOException {
        if (onlineSitemapUrl == null) {
            return null;
        }
        byte[] bytes = IOUtils.toByteArray(onlineSitemapUrl);
        return parseSiteMap(bytes, onlineSitemapUrl);
    }

    /**
     * Returns a processed copy of an unprocessed sitemap object, i.e. transfer
     * the value of getLastModified(). Please note that the sitemap input stays
     * unchanged. Note that contentType is assumed to be correct; in general it
     * is more robust to use the method that doesn't take a contentType, but
     * instead detects this using Tika.
     * 
     * @param contentType
     *            MIME type of content
     * @param content
     *            raw bytes of sitemap file
     * @param sitemap
     *            an {@link crawlercommons.sitemaps.AbstractSiteMap} implementation
     * @return Extracted SiteMap/SiteMapIndex
     * @throws UnknownFormatException if there is an error parsing the sitemap
     * @throws IOException if there is an error reading in the site map {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(String contentType, byte[] content, final AbstractSiteMap sitemap) throws UnknownFormatException, IOException {
        AbstractSiteMap asmCopy = parseSiteMap(contentType, content, sitemap.getUrl());
        asmCopy.setLastModified(sitemap.getLastModified());
        return asmCopy;
    }

    /**
     * Parse a sitemap, given the content bytes and the URL.
     * 
     * @param content
     *            raw bytes of sitemap file
     * @param url
     *            URL to sitemap file
     * @return Extracted SiteMap/SiteMapIndex
     * @throws UnknownFormatException if there is an error parsing the sitemap
     * @throws IOException if there is an error reading in the site map {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(byte[] content, URL url) throws UnknownFormatException, IOException {
        if (url == null) {
            return null;
        }
        String filename = FilenameUtils.getName(url.getPath());
        String contentType = TIKA.detect(content, filename);
        return parseSiteMap(contentType, content, url);
    }

    /**
     * Parse a sitemap, given the MIME type, the content bytes, and the URL.
     * Note that contentType is assumed to be correct; in general it is more
     * robust to use the method that doesn't take a contentType, but instead
     * detects this using Tika.
     * 
     * @param contentType
     *            MIME type of content
     * @param content
     *            raw bytes of sitemap file
     * @param url
     *            URL to sitemap file
     * @return Extracted SiteMap/SiteMapIndex
     * @throws UnknownFormatException if there is an error parsing the sitemap
     * @throws IOException if there is an error reading in the site map {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(String contentType, byte[] content, URL url) throws UnknownFormatException, IOException {
        MediaType mediaType = MediaType.parse(contentType);

        // Octet-stream is the father of all binary types
        while (mediaType != null && !mediaType.equals(MediaType.OCTET_STREAM)) {
            if (XML_MEDIA_TYPES.contains(mediaType)) {
                return processXml(url, content);
            } else if (TEXT_MEDIA_TYPES.contains(mediaType)) {
                return processText(url.toString(), content);
            } else if (GZ_MEDIA_TYPES.contains(mediaType)) {
                return processGzip(url, content);
            } else {
                mediaType = MEDIA_TYPE_REGISTRY.getSupertype(mediaType); // Check parent
                return parseSiteMap(mediaType.toString(), content, url);
            }
        }

        throw new UnknownFormatException("Can't parse a sitemap with the MediaType of: " + contentType + " (at: " + url + ")");
    }

    /**
     * Parse the given XML content.
     * 
     * @param sitemapUrl URL to sitemap file
     * @param xmlContent the byte[] backing the sitemapUrl
     * @return The site map
     * @throws UnknownFormatException if there is an error parsing the sitemap
     */
    protected AbstractSiteMap processXml(URL sitemapUrl, byte[] xmlContent) throws UnknownFormatException {

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
     * @param sitemapUrl a string sitemap URL
     * @param sitemapUrl URL to sitemap file
     * @param content the byte[] backing the sitemapUrl
     * @return The site map
     * @throws IOException if there is an error reading in the site map String
     */
    protected SiteMap processText(String sitemapUrl, byte[] content) throws IOException {
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
                addUrlIntoSitemap(line, textSiteMap, null, null, null, i++);
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
     * @return the site map
     * @throws UnknownFormatException if there is an error parsing the gzip
     * @throws IOException if there is an error reading in the gzip {@link java.net.URL}
     */
    protected AbstractSiteMap processGzip(URL url, byte[] response) throws IOException, UnknownFormatException {

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
     * @param sitemapUrl a sitemap {@link java.net.URL}
     * @param is an {@link org.xml.sax.InputSource} backing the sitemap
     * @return the site map
     * @throws UnknownFormatException if there is an error parsing the {@link org.xml.sax.InputSource}
     */
    protected AbstractSiteMap processXml(URL sitemapUrl, InputSource is) throws UnknownFormatException {

        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            doc = dbf.newDocumentBuilder().parse(is);
        } catch (Exception e) {
            LOG.debug(e.toString(), e);
            throw new UnknownFormatException("Error parsing XML for: " + sitemapUrl);
        }

        // See if this is a sitemap index
        NodeList nodeList = doc.getElementsByTagName("sitemapindex");
        if (nodeList.getLength() > 0) {
            nodeList = doc.getElementsByTagName("sitemap");
            return parseSitemapIndex(sitemapUrl, nodeList);
        } else if (doc.getElementsByTagName("urlset").getLength() > 0) {
            // This is a regular Sitemap
            return parseXmlSitemap(sitemapUrl, doc);
        } else if (doc.getElementsByTagName("link").getLength() > 0) {
            // Could be RSS or Atom
            return parseSyndicationFormat(sitemapUrl, doc);
        }

        throw new UnknownFormatException("Unknown XML format for: " + sitemapUrl);
    }

    /**
     * Parse XML that contains a valid Sitemap. Example of a Sitemap: 
     * <pre>
     * {@code 
     * <?xml version="1.0" encoding="UTF-8"?> 
     *   <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"> 
     *     <url>
     *       <loc>http://www.example.com/</loc> 
     *       <lastmod>lastmod>2005-01-01</lastmod>
     *       <changefreq>monthly</changefreq>
     *       <priority>0.8</priority>
     *     </url> 
     *     <url>
     *       <loc>http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii</loc>
     *       <changefreq>weekly</changefreq>
     *     </url>
     *   </urlset>
     * }
     * </pre>
     * @param sitemapUrl a sitemap {@link java.net.URL}
     * @param doc a {@link org.w3c.dom.Document} sitemap snippet
     * @return The sitemap
     */
    protected SiteMap parseXmlSitemap(URL sitemapUrl, Document doc) {

        SiteMap sitemap = new SiteMap(sitemapUrl);
        sitemap.setType(SitemapType.XML);

        NodeList list = doc.getElementsByTagName("url");

        // Loop through the <url>s
        for (int i = 0; i < list.getLength(); i++) {

            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) n;
                String lastMod = getElementValue(elem, "lastmod");
                String changeFreq = getElementValue(elem, "changefreq");
                String priority = getElementValue(elem, "priority");
                String loc = getElementValue(elem, "loc");

                addUrlIntoSitemap(loc, sitemap, lastMod, changeFreq, priority, i);
            }
        }

        sitemap.setProcessed(true);
        return sitemap;
    }

    /**
     * <p>Parse XML that contains a Sitemap Index. Example Sitemap Index:</p>
     * <pre>
     * {@code
     * <?xml version="1.0" encoding="UTF-8"?> 
     *   <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
     *     <sitemap>
     *       <loc>http://www.example.com/sitemap1.xml.gz</loc>
     *       <lastmod>2004-10-01T18:23:17+00:00</lastmod>
     *     </sitemap>
     *     <sitemap>
     *       <loc>http://www.example.com/sitemap2.xml.gz</loc>
     *       <lastmod>2005-01-01</lastmod>
     *     </sitemap>
     *   </sitemapindex>
     * }
     * </pre>
     * @param url
     *            - URL of Sitemap Index
     * @param nodeList a {@link org.w3c.dom.NodeList} backing the sitemap
     * @return The site map index
     */
    protected SiteMapIndex parseSitemapIndex(URL url, NodeList nodeList) {

        LOG.debug("Parsing Sitemap Index");

        SiteMapIndex sitemapIndex = new SiteMapIndex(url);
        sitemapIndex.setType(SitemapType.INDEX);

        // Loop through the <sitemap>s
        for (int i = 0; i < nodeList.getLength() && i < MAX_URLS; i++) {

            Node firstNode = nodeList.item(i);

            if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) firstNode;
                String loc = getElementValue(elem, "loc");

                // try the text content when no loc element
                // has been specified
                if (loc == null) {
                    loc = elem.getTextContent().trim();
                }

                try {
                    URL sitemapUrl = new URL(loc);
                    String lastmod = getElementValue(elem, "lastmod");
                    Date lastModified = SiteMap.convertToDate(lastmod);

                    // Right now we are not worried about sitemapUrls that point
                    // to different websites.

                    SiteMap s = new SiteMap(sitemapUrl, lastModified);
                    sitemapIndex.addSitemap(s);
                    LOG.debug("  {}. {}", (i + 1), s);
                } catch (MalformedURLException e) {
                    LOG.trace("Don't create an entry with a bad URL", e);
                    LOG.debug("Bad url: [{}]", loc);
                }
            }
        }
        sitemapIndex.setProcessed(true);
        return sitemapIndex;
    }

    /**
     * Parse the XML document, looking for a <b>feed</b> element to determine if
     * it's an <b>Atom doc</b> <b>rss</b> to determine if it's an <b>RSS
     * doc</b>.
     * 
     * @param sitemapUrl the URL location of the Sitemap
     * @param doc
     *            - XML document to parse
     * @return The sitemap
     * @throws UnknownFormatException
     *             if XML does not appear to be Atom or RSS
     */
    protected SiteMap parseSyndicationFormat(URL sitemapUrl, Document doc) throws UnknownFormatException {

        SiteMap sitemap = new SiteMap(sitemapUrl);

        // See if this is an Atom feed by looking for "feed" element
        NodeList list = doc.getElementsByTagName("feed");
        if (list.getLength() > 0) {
            parseAtom(sitemap, (Element) list.item(0), doc);
            sitemap.setProcessed(true);
            return sitemap;
        } else {
            // See if it is a RSS feed by looking for a "channel" element. This
            // avoids the issue
            // of having the outer tag named <rdf:RDF> that was causing this
            // code to fail. Inside of
            // the <rss> or <rdf> tag is a <channel> tag, so we can use that.
            // See https://github.com/crawler-commons/crawler-commons/issues/87
            // and also RSS 1.0 specification
            // http://web.resource.org/rss/1.0/spec
            list = doc.getElementsByTagName("channel");
            if (list.getLength() > 0) {
                parseRSS(sitemap, doc);
                sitemap.setProcessed(true);
                return sitemap;
            } else {
                throw new UnknownFormatException("Unknown syndication format at " + sitemapUrl);
            }
        }
    }

    /**
     * <p>Parse the XML document which is assumed to be in Atom format. Atom 1.0
     * example:
     * </p>
     * <pre>
     * {@code
     * <?xml version="1.0" encoding="utf-8"?>
     *   <feed xmlns="http://www.w3.org/2005/Atom">
     *     <title>Example Feed</title>
     *     <subtitle>A subtitle.</subtitle>
     *     <link href="http://example.org/feed/" rel="self"/>
     *     <link href="http://example.org/"/>
     *     <modified>2003-12-13T18:30:02Z</modified>
     *     <author>
     *       <name>John Doe</name>
     *       <email>johndoe@example.com</email>
     *     </author>
     *     <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>
     *     <entry>
     *       <title>Atom-Powered Robots Run Amok</title>
     *       <link href="http://example.org/2003/12/13/atom03"/>
     *       <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
     *       <updated>2003-12-13T18:30:02Z</updated>
     *       <summary>Some text.</summary>
     *     </entry>
     *     ...
     *   </feed>
     * }
     * </pre>
     * @param sitemap a {@link crawlercommons.sitemaps.SiteMap} backing the Atom feed
     * @param elem {@link org.w3c.dom.Element}'s to populate from the Sitemap
     * @param doc {@link org.w3c.dom.Document} to populate with the parse output
     */
    protected void parseAtom(SiteMap sitemap, Element elem, Document doc) {

        // Grab items from <feed><entry><link href="URL" /></entry></feed>
        // Use lastmod date from <feed><modified>DATE</modified></feed>

        LOG.debug("Parsing Atom XML");

        sitemap.setType(SitemapType.ATOM);

        String lastMod = getElementValue(elem, "modified");
        LOG.debug("lastMod = {}", lastMod);

        NodeList list = doc.getElementsByTagName("entry");

        // Loop through the <entry>s
        for (int i = 0; i < list.getLength() && i < MAX_URLS; i++) {

            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) n;
                String href = getElementAttributeValue(elem, "link", "href");

                addUrlIntoSitemap(href, sitemap, lastMod, null, null, i);
            }
        }
    }

    /**
     * <p>Parse XML document which is assumed to be in RSS format. RSS 2.0 example:
     * </p>
     * <pre>
     * {@code
     * <?xml version="1.0"?>
     *   <rss version="2.0">
     *     <channel> 
     *       <title>Lift Off News</title>
     *       <link>http://liftoff.msfc.nasa.gov/</link>
     *       <description>Liftoff to Space Exploration.</description>
     *       <language>en-us</language>
     *       <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>
     *       <lastBuildDate>Tue, 10 Jun 2003 09:41:01 GMT</lastBuildDate>
     *       <docs>http://blogs.law.harvard.edu/tech/rss</docs>
     *       <generator>Weblog Editor 2.0</generator>
     *       <managingEditor>editor@example.com</managingEditor>
     *       <webMaster>webmaster@example.com</webMaster>
     *       <ttl>5</ttl>
     *       <item>
     *         <title>Star City</title>
     *         <link>http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp</link>
     *         <description>How do Americans get ready to work with Russians aboard the
     *         International Space Station? They take a crash course in culture,
     *         language and protocol at Russia's Star City.
     *         </description>
     *         <pubDate>Tue, 03 Jun 2003 09:39:21 GMT</pubDate>
     *         <guid>http://liftoff.msfc.nasa.gov/2003/06/03.html#item573</guid>
     *       </item>
     *       <item>
     *         <title>Space Exploration</title>
     *         <link>http://liftoff.msfc.nasa.gov/</link>
     *         <description>Sky watchers in Europe, Asia, and parts of Alaska and Canada 
     *         will experience a partial eclipse of the Sun on Saturday, May 31.
     *         </description>
     *         <pubDate>Fri, 30 May 2003 11:06:42 GMT</pubDate>
     *         <guid>http://liftoff.msfc.nasa.gov/2003/05/30.html#item572</guid>
     *       </item>
     *     </channel>
     *   </rss>
     * }
     * </pre>
     * @param sitemap a {@link crawlercommons.sitemaps.SiteMap} object to populate with the RCC content
     * @param doc {@link org.w3c.dom.Document} to populate with the parse output
     */
    protected void parseRSS(SiteMap sitemap, Document doc) {

        // Grab items from <item><link>URL</link></item>
        // and last modified date from <pubDate>DATE</pubDate>

        LOG.debug("Parsing RSS doc");
        sitemap.setType(SitemapType.RSS);
        NodeList list = doc.getElementsByTagName("channel");
        Element elem = (Element) list.item(0);

        // Treat publication date as last mod (Tue, 10 Jun 2003 04:00:00 GMT)
        String lastMod = getElementValue(elem, "pubDate");
        LOG.debug("lastMod = ", lastMod);

        list = doc.getElementsByTagName("item");
        // Loop through the <item>s
        for (int i = 0; i < list.getLength() && i < MAX_URLS; i++) {

            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) n;
                String link = getElementValue(elem, "link");

                addUrlIntoSitemap(link, sitemap, lastMod, null, null, i);
            }
        }
    }

    /**
     * Get the element's textual content.
     * 
     * @param elem
     * @param elementName
     * @return The element value
     */
    protected String getElementValue(Element elem, String elementName) {

        NodeList list = elem.getElementsByTagName(elementName);
        if (list == null)
            return null;
        Element e = (Element) list.item(0);
        if (e != null) {
            return e.getTextContent();
        }
        return null;
    }

    /**
     * Get the element's attribute value.
     * 
     * @param elem
     * @param elementName
     * @param attributeName
     * @return The element attribute value
     */
    protected String getElementAttributeValue(Element elem, String elementName, String attributeName) {

        NodeList list = elem.getElementsByTagName(elementName);
        Element e = (Element) list.item(0);
        if (e != null) {
            return e.getAttribute(attributeName);
        }

        return null;
    }

    /**
     * Adds the given URL to the given sitemap while showing the relevant logs
     * @param urlStr an URL string to add to the {@link crawlercommons.sitemaps.SiteMap}
     * @param siteMap the sitemap to add URL(s) to
     * @param lastMod last time the {@link crawlercommons.sitemaps.SiteMapURL} was modified
     * @param changeFreq the {@link crawlercommons.sitemaps.SiteMapURL} change frquency
     * @param priority priority of this {@link crawlercommons.sitemaps.SiteMapURL}
     * @param urlIndex index position to which this entry has been added 
     */
    protected void addUrlIntoSitemap(String urlStr, SiteMap siteMap, String lastMod, String changeFreq, String priority, int urlIndex) {
        try {
            URL url = new URL(urlStr); // Checking the URL
            boolean valid = urlIsValid(siteMap.getBaseUrl(), url.toString());

            if (valid || !strict) {
                SiteMapURL sUrl = new SiteMapURL(url.toString(), lastMod, changeFreq, priority, valid);
                siteMap.addSiteMapUrl(sUrl);
                LOG.debug("  {}. {}", urlIndex + 1, sUrl);
            } else {
                LOG.warn("URL: {} is excluded from the sitemap as it is not a valid url = not under the base url: {}", url.toExternalForm(), siteMap.getBaseUrl());
            }
        } catch (MalformedURLException e) {
            LOG.warn("Bad url: [{}]", urlStr);
            LOG.trace("Can't create a sitemap entry with a bad URL", e);
        }
    }

    /**
     * See if testUrl is under sitemapBaseUrl. Only URLs under sitemapBaseUrl
     * are valid.
     * 
     * @param sitemapBaseUrl
     * @param testUrl
     * @return true if testUrl is under sitemapBaseUrl, false otherwise
     */
    protected boolean urlIsValid(String sitemapBaseUrl, String testUrl) {
        boolean ret = false;

        // Don't try a comparison if the URL is too short to match
        if (sitemapBaseUrl != null && sitemapBaseUrl.length() <= testUrl.length()) {
            String u = testUrl.substring(0, sitemapBaseUrl.length());
            ret = sitemapBaseUrl.equals(u);
        }

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
