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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import crawlercommons.filters.URLFilter;
import crawlercommons.mimetypes.MimeTypeDetector;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;
import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.sax.DelegatorHandler;
import crawlercommons.sitemaps.sax.UrlLimitExceededException;

public class SiteMapParser {
    public static final Logger LOG = LoggerFactory.getLogger(SiteMapParser.class);

    /**
     * According to the specs, 50K URLs per Sitemap is the max
     */
    public static final int MAX_URLS = 50000;

    /**
     * Sitemaps (including sitemap index files) &quot;must be no larger than
     * 50MB (52,428,800 bytes)&quot; as specified in the
     * <a href="https://www.sitemaps.org/protocol.html#index">Sitemaps XML
     * format</a> (before Nov. 2016 the limit has been 10MB).
     */
    public static final int MAX_BYTES_ALLOWED = 52428800;

    /**
     * True (by default) meaning that invalid URLs should be rejected, as the
     * official docs allow the siteMapURLs to be only under the base URL:
     * https://www.sitemaps.org/protocol.html#location
     */
    protected boolean strict = true;

    /**
     * If true: allow URLs from sitemaps only partially parsed because of format
     * errors or truncated (incompletely fetched) content. If false any parser
     * error will cause an {@link UnknownFormatException}.
     */
    private boolean allowPartial = false;

    /**
     * Indicates whether the parser should work with the namespace from the
     * specifications or any namespace. Defaults to false.
     **/
    protected boolean strictNamespace = false;

    /** Set of namespaces (if {@link #strictNamespace}) accepted by the parser. URLs from other namespaces are ignored. */
    protected Set<String> acceptedNamespaces = new HashSet<>();

    /**
     * Map of sitemap extension namespaces required to find the right extension
     * handler.
     */
    protected Map<String, Extension> extensionNamespaces = new HashMap<>();

    private MimeTypeDetector mimeTypeDetector;

    /**
     * Option to allow DTDs in sitemaps.
     */
    private boolean allowDocTypeDefinitions = false;

    /* Function to normalize or filter URLs. Does nothing by default. */
    private Function<String, String> urlFilter = (String url) -> url;

    /** Maximum number of URLs accepted from a single sitemap. */
    private int maxUrls = MAX_URLS;

    /** Maximum size of a sitemap (uncompressed) in bytes. */
    private long maxBytes = MAX_BYTES_ALLOWED;

    /**
     * SiteMapParser with strict location validation ({@link #isStrict()}) and not
     * allowing partially parsed content.
     */
    public SiteMapParser() {
        this(true, false);
    }

    /**
     * SiteMapParser with configurable location validation, not allowing
     * partially parsed content.
     * 
     * @param strict
     *            see {@link #isStrict()}
     */
    public SiteMapParser(boolean strict) {
        this(strict, false);
    }

    /**
     * @param strict
     *            see {@link #isStrict()}
     * @param allowPartial
     *            if true: allow URLs from sitemaps only partially parsed
     *            because of format errors or truncated (incompletely fetched)
     *            content. If false any parser error will cause an
     *            {@link UnknownFormatException}.
     */
    public SiteMapParser(boolean strict, boolean allowPartial) {
        this.strict = strict;
        this.allowPartial = allowPartial;

        this.mimeTypeDetector = new MimeTypeDetector();
    }

    /**
     * Sets if the parser allows a DTD in sitemaps or feeds.
     *
     * @param allowDocTypeDefinitions
     *            true if allowed. Default is false.
     */
    public void setAllowDocTypeDefinitions(boolean allowDocTypeDefinitions) {
        this.allowDocTypeDefinitions = allowDocTypeDefinitions;
    }

    /**
     * @return whether invalid URLs will be rejected (where invalid means that
     *         the URL is not under the base URL, see <a href=
     *         "https://www.sitemaps.org/protocol.html#location">sitemap file
     *         location</a>). See also {@link #urlIsValid(String, String)}.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * @return whether the parser allows any namespace or just the one from the
     *         specification (or any namespace accepted,
     *         {@link #addAcceptedNamespace(String)})
     */
    public boolean isStrictNamespace() {
        return strictNamespace;
    }

    /**
     * Sets the parser to allow any XML namespace or just the one from the
     * specification, or any accepted namespace (see
     * {@link #addAcceptedNamespace(String)}). Note enabling strict namespace
     * checking always adds the namespace defined by the current sitemap
     * specification ({@link Namespace#SITEMAP}) to the list of accepted
     * namespaces.
     * 
     * @param s
     *            if true enable strict namespace-checking, disable if false
     */
    public void setStrictNamespace(boolean s) {
        strictNamespace = s;
        if (strictNamespace) {
            addAcceptedNamespace(Namespace.SITEMAP);
        }
    }

    /**
     * Add namespace URI to set of accepted namespaces.
     * 
     * @param namespaceUri
     *            URI of the accepted XML namespace
     */
    public void addAcceptedNamespace(String namespaceUri) {
        acceptedNamespaces.add(namespaceUri);
    }

    /**
     * Add namespace URIs to set of accepted namespaces.
     * 
     * @param namespaceUris
     *            array of accepted XML namespace URIs
     */
    public void addAcceptedNamespace(String[] namespaceUris) {
        for (String namespaceUri : namespaceUris) {
            acceptedNamespaces.add(namespaceUri);
        }
    }

    /**
     * Enable a support for a sitemap extension in the parser.
     * 
     * @param extension
     *            sitemap extension (news, images, videos, etc.)
     */
    public void enableExtension(Extension extension) {
        for (String namespaceUri : Namespace.SITEMAP_EXTENSION_NAMESPACES.get(extension)) {
            extensionNamespaces.put(namespaceUri, extension);
        }
    }

    /**
     * Enable all supported sitemap extensions in the parser.
     */
    public void enableExtensions() {
        for (Extension extension : Extension.values()) {
            for (String namespaceUri : Namespace.SITEMAP_EXTENSION_NAMESPACES.get(extension)) {
                extensionNamespaces.put(namespaceUri, extension);
            }
        }
    }

    /**
     * Set URL filter function to normalize URLs found in sitemaps or filter
     * URLs away if the function returns null.
     */
    public void setURLFilter(Function<String, String> filter) {
        urlFilter = filter;
    }

    /**
     * Use {@link URLFilter} to filter URLs, eg. to configure that URLs found in
     * sitemaps are normalized by
     * {@link crawlercommons.filters.basic.BasicURLNormalizer}:
     * 
     * <pre>
     * sitemapParser.setURLFilter(new BasicURLNormalizer());
     * </pre>
     */
    public void setURLFilter(URLFilter filter) {
        urlFilter = filter::filter;
    }

    /**
     * @return the maximum number of URLs accepted from a single sitemap, see
     *         {@link #setMaxUrls(int)}
     */
    public int getMaxUrls() {
        return maxUrls;
    }

    /**
     * Set the maximum number of URLs accepted from a single sitemap, sitemap
     * index or feed. If a sitemap contains more URLs, parsing is stopped and
     * the sitemap is returned holding only the first <code>maxUrls</code>
     * URLs. URLs skipped because they are invalid or rejected by the URL
     * filter are not counted.
     * 
     * <p>
     * Note that the limit is applied per sitemap: a sitemap index may list up
     * to <code>maxUrls</code> sitemaps each containing up to
     * <code>maxUrls</code> URLs. With the default limit, recursively
     * processing a single sitemap index may result in up to 2.5 billion URLs.
     * </p>
     * 
     * @param maxUrls
     *            maximum number of URLs, default is {@link #MAX_URLS}
     * @throws IllegalArgumentException
     *             if maxUrls is not a positive number
     */
    public void setMaxUrls(int maxUrls) {
        if (maxUrls <= 0) {
            throw new IllegalArgumentException("Max. number of URLs must be positive: " + maxUrls);
        }
        this.maxUrls = maxUrls;
    }

    /**
     * @return the maximum size of a sitemap in bytes, see
     *         {@link #setMaxBytes(long)}
     */
    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * Set the maximum size of a sitemap in bytes. The limit is applied to the
     * content passed to the parser and also to the uncompressed content of
     * gzipped sitemaps. If a sitemap is larger, an
     * {@link UnknownFormatException} is thrown unless partially parsed
     * sitemaps are allowed. In the latter case, the content is truncated and
     * the URLs found within the size limit are returned.
     * 
     * @param maxBytes
     *            maximum size in bytes, default is {@link #MAX_BYTES_ALLOWED}
     * @throws IllegalArgumentException
     *             if maxBytes is not a positive number
     */
    public void setMaxBytes(long maxBytes) {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("Max. number of bytes must be positive: " + maxBytes);
        }
        this.maxBytes = maxBytes;
    }

    /**
     * Returns a SiteMap or SiteMapIndex given an online sitemap URL
     *
     * Please note that this method is a static method which goes online and
     * fetches the sitemap then parses it
     *
     * This method is a convenience method for a user who has a sitemap URL and
     * wants a "Keep it simple" way to parse it.
     * 
     * @param onlineSitemapUrl
     *            URL of the online sitemap
     * @return Extracted SiteMap/SiteMapIndex or null if the onlineSitemapUrl is
     *         null
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error reading in the site map
     *             {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(URL onlineSitemapUrl) throws UnknownFormatException, IOException {
        if (onlineSitemapUrl == null) {
            return null;
        }
        byte[] bytes;
        try (InputStream in = limitSize(onlineSitemapUrl.openStream())) {
            bytes = IOUtils.toByteArray(in);
        }
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
     *            an {@link crawlercommons.sitemaps.AbstractSiteMap}
     *            implementation
     * @return Extracted SiteMap/SiteMapIndex
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error reading in the site map
     *             {@link java.net.URL}
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
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error reading in the site map
     *             {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(byte[] content, URL url) throws UnknownFormatException, IOException {
        if (url == null) {
            return null;
        }

        String contentType = mimeTypeDetector.detect(content);
        if (contentType == null) {
            String msg = String.format(Locale.ROOT, "Failed to detect MediaType of sitemap '%s'", url);
            throw new UnknownFormatException(msg);
        }
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
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error reading in the site map
     *             {@link java.net.URL}
     */
    public AbstractSiteMap parseSiteMap(String contentType, byte[] content, URL url) throws UnknownFormatException, IOException {
        String mimeType = mimeTypeDetector.normalize(contentType, content);

        checkSize(url, content.length);

        String msg;
        if (mimeTypeDetector.isXml(mimeType)) {
            return processXml(url, content);
        } else if (mimeTypeDetector.isText(mimeType)) {
            return processText(url, content);
        } else if (mimeTypeDetector.isGzip(mimeType)) {
            try (BoundedInputStream bounded = limitSize(new GZIPInputStream(new ByteArrayInputStream(content))); InputStream decompressed = new BufferedInputStream(bounded)) {
                String compressedType = mimeTypeDetector.detect(decompressed);
                if (mimeTypeDetector.isXml(compressedType)) {
                    return processGzippedXML(url, content);
                } else if (mimeTypeDetector.isText(compressedType)) {
                    SiteMap sitemap = processText(url, decompressed);
                    checkSize(url, bounded.getCount());
                    return sitemap;
                } else if (compressedType == null) {
                    msg = String.format(Locale.ROOT, "Failed to detect embedded MediaType of gzipped sitemap '%s'", url);
                } else {
                    msg = String.format(Locale.ROOT, "Can't parse a sitemap with MediaType '%s' (embedded in %s) from '%s'", compressedType, contentType, url);
                }
            } catch (UnknownFormatException e) {
                throw e;
            } catch (Exception e) {
                msg = String.format(Locale.ROOT, "Failed to detect embedded MediaType of gzipped sitemap '%s'", url);
                throw new UnknownFormatException(msg, e);
            }
        } else {
            msg = String.format(Locale.ROOT, "Can't parse a sitemap with MediaType '%s' from '%s'", contentType, url);
        }

        throw new UnknownFormatException(msg);
    }

    /**
     * Fetch a sitemap from the specified URL, recursively fetching and
     * traversing the content of any enclosed sitemap index, and performing the
     * specified action for each sitemap URL until all URLs have been processed
     * or the action throws an exception.
     * <p>
     * This method is a convenience method for a user who has a sitemap URL and
     * wants a simple way to traverse it.
     * <p>
     * Exceptions thrown by the action are relayed to the caller.
     *
     * @param onlineSitemapUrl
     *            URL of the online sitemap
     * @param action
     *            The action to be performed for each element
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error fetching the content of any
     *             {@link java.net.URL}
     */
    public void walkSiteMap(URL onlineSitemapUrl, Consumer<SiteMapURL> action) throws UnknownFormatException, IOException {
        if (onlineSitemapUrl == null || action == null) {
            LOG.info("Got null sitemap URL and/or action, stopping traversal");
            return;
        }
        walkSiteMap(parseSiteMap(onlineSitemapUrl), action);
    }

    /**
     * Traverse a sitemap, recursively fetching and traversing the content of
     * any enclosed sitemap index, and performing the specified action for each
     * sitemap URL until all URLs have been processed or the action throws an
     * exception.
     * <p>
     * This method is a convenience method for a user who has a sitemap and
     * wants a simple way to traverse it.
     * <p>
     * Note that the limits on the number of URLs ({@link #setMaxUrls(int)}) and
     * the size ({@link #setMaxBytes(long)}) are applied to every single
     * sitemap but not to the traversal as a whole: with the default limits a
     * recursively processed sitemap index may hold up to 2.5 billion URLs
     * (50,000 sitemaps with 50,000 URLs each).
     * <p>
     * Exceptions thrown by the action are relayed to the caller.
     *
     * @param sitemap
     *            The sitemap to traverse
     * @param action
     *            The action to be performed for each element
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     * @throws IOException
     *             if there is an error fetching the content of any
     *             {@link java.net.URL}
     */
    public void walkSiteMap(AbstractSiteMap sitemap, Consumer<SiteMapURL> action) throws UnknownFormatException, IOException {
        if (sitemap == null || action == null) {
            LOG.info("Got null sitemap and/or action, stopping traversal");
            return;
        }
        if (sitemap.isIndex()) {
            final Collection<AbstractSiteMap> links = ((SiteMapIndex) sitemap).getSitemaps();
            for (final AbstractSiteMap asm : links) {
                if (asm == null) {
                    continue;
                }
                walkSiteMap(asm.getUrl(), action);
            }
        } else {
            final Collection<SiteMapURL> links = ((SiteMap) sitemap).getSiteMapUrls();
            for (final SiteMapURL url : links) {
                if (url == null) {
                    continue;
                }
                action.accept(url);
            }
        }
    }

    /**
     * Parse the given XML content.
     * 
     * @param sitemapUrl
     *            URL to sitemap file
     * @param xmlContent
     *            the byte[] backing the sitemapUrl
     * @return The site map
     * @throws UnknownFormatException
     *             if there is an error parsing the sitemap
     */
    protected AbstractSiteMap processXml(URL sitemapUrl, byte[] xmlContent) throws UnknownFormatException {

        InputStream in;
        try {
            in = new SkipLeadingWhiteSpaceInputStream(new BOMInputStream(new ByteArrayInputStream(xmlContent, 0, limitLength(xmlContent))));
        } catch (IOException e) {
            /*
             * Since commons-io 2.22.0 the BOMInputStream constructor reads the
             * byte-order mark eagerly and declares IOException. Reading from
             * the in-memory byte[] cannot fail, so this is unreachable - the
             * signature of this method is kept unchanged.
             */
            throw new UncheckedIOException("Failed to read byte-order mark of sitemap " + sitemapUrl, e);
        }
        InputSource is = new InputSource();
        is.setCharacterStream(new BufferedReader(new InputStreamReader(in, UTF_8)));

        return processXml(sitemapUrl, is);
    }

    /**
     * Process a text-based Sitemap. Text sitemaps only list URLs but no
     * priorities, last mods, etc.
     * 
     * @param sitemapUrl
     *            URL to sitemap file
     * @param content
     *            the byte[] backing the sitemapUrl
     * @return The site map
     * @throws IOException
     *             if there is an error reading in the site map content
     */
    protected SiteMap processText(URL sitemapUrl, byte[] content) throws IOException {
        return processText(sitemapUrl, new ByteArrayInputStream(content, 0, limitLength(content)));
    }

    /**
     * Process a text-based Sitemap. Text sitemaps only list URLs but no
     * priorities, last mods, etc.
     *
     * @param sitemapUrl
     *            URL to sitemap file
     * @param stream
     *            content stream
     * @return The site map
     * @throws IOException
     *             if there is an error reading in the site map content
     */
    protected SiteMap processText(URL sitemapUrl, InputStream stream) throws IOException {
        LOG.debug("Processing textual Sitemap");

        SiteMap textSiteMap = new SiteMap(sitemapUrl);
        textSiteMap.setType(SitemapType.TEXT);

        BOMInputStream bomIs = new BOMInputStream(stream);
        @SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(new InputStreamReader(bomIs, UTF_8));

        String line;
        int i = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String urlFiltered = urlFilter.apply(line);
            if (urlFiltered == null) {
                LOG.info("Filtered url: [{}]", line.substring(0, Math.min(1024, line.length())));
                continue;
            }
            try {
                URL url = new URI(urlFiltered).toURL();
                boolean valid = urlIsValid(textSiteMap.getBaseUrl(), url.toString());
                if (valid || !strict) {
                    if (i >= maxUrls) {
                        LOG.warn("Truncated sitemap {}: more than {} URLs", sitemapUrl, maxUrls);
                        break;
                    }
                    SiteMapURL sUrl = new SiteMapURL(url, valid);
                    textSiteMap.addSiteMapUrl(sUrl);
                    LOG.debug("  {}. {}", (++i), sUrl);
                } else {
                    LOG.info("URL: {} is excluded from the sitemap as it is not a valid url = not under the base url: {}", url.toExternalForm(), textSiteMap.getBaseUrl());
                }
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
                LOG.warn("Bad url: [{}]", line.substring(0, Math.min(1024, line.length())));
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
     * @throws UnknownFormatException
     *             if there is an error parsing the gzip
     * @throws IOException
     *             if there is an error reading in the gzip {@link java.net.URL}
     */
    protected AbstractSiteMap processGzippedXML(URL url, byte[] response) throws IOException, UnknownFormatException {

        LOG.debug("Processing gzipped XML");

        InputStream is = new ByteArrayInputStream(response);

        // Remove .gz ending
        String xmlUrl = url.toString().replaceFirst("\\.gz$", "");
        LOG.debug("XML url = {}", xmlUrl);

        BoundedInputStream bounded = limitSize(new GZIPInputStream(is));
        InputStream decompressed = new SkipLeadingWhiteSpaceInputStream(new BOMInputStream(bounded));
        InputSource in = new InputSource(decompressed);
        in.setSystemId(xmlUrl);
        AbstractSiteMap sitemap;
        try {
            sitemap = processXml(url, in);
        } catch (UnknownFormatException e) {
            if (bounded.getCount() > maxBytes) {
                // parsing failed because the content was truncated
                String msg = String.format(Locale.ROOT, "Sitemap '%s' exceeds the size limit of %d bytes", url, maxBytes);
                throw new UnknownFormatException(msg, e);
            }
            throw e;
        }
        checkSize(url, bounded.getCount());
        return sitemap;
    }

    /**
     * Verify that the size of a sitemap does not exceed the limit, see
     * {@link #setMaxBytes(long)}.
     * 
     * @param sitemapUrl
     *            URL of the sitemap
     * @param size
     *            size of the sitemap in bytes
     * @throws UnknownFormatException
     *             if the limit is exceeded and partially parsed sitemaps are
     *             not allowed
     */
    private void checkSize(URL sitemapUrl, long size) throws UnknownFormatException {
        if (size <= maxBytes) {
            return;
        }
        if (!allowPartial) {
            String msg = String.format(Locale.ROOT, "Sitemap '%s' exceeds the size limit of %d bytes", sitemapUrl, maxBytes);
            throw new UnknownFormatException(msg);
        }
        LOG.warn("Truncated sitemap {}: size limit of {} bytes exceeded", sitemapUrl, maxBytes);
    }

    /**
     * Wrap the stream so that one byte more than the size limit can be read.
     * The extra byte allows to distinguish content which exceeds the limit
     * from content with a size equal to the limit.
     */
    private BoundedInputStream limitSize(InputStream stream) throws IOException {
        long max = (maxBytes == Long.MAX_VALUE) ? maxBytes : maxBytes + 1;
        return BoundedInputStream.builder().setInputStream(stream).setMaxCount(max).get();
    }

    /** @return the number of bytes of the content within the size limit */
    private int limitLength(byte[] content) {
        return (int) Math.min(content.length, maxBytes);
    }

    /**
     * Parse the given XML content.
     * 
     * @param sitemapUrl
     *            a sitemap {@link java.net.URL}
     * @param is
     *            an {@link org.xml.sax.InputSource} backing the sitemap
     * @return the site map
     * @throws UnknownFormatException
     *             if there is an error parsing the
     *             {@link org.xml.sax.InputSource}
     */
    protected AbstractSiteMap processXml(URL sitemapUrl, InputSource is) throws UnknownFormatException {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        // disable validation and avoid that DTDs, schemas, XML snippets, etc.
        // are fetched from remote servers or the local file system
        factory.setValidating(false);
        factory.setXIncludeAware(false);

        // support the use of an explicit namespace.
        factory.setNamespaceAware(true);

        // Configure underlying parser features to reduce the risk of XXE attacks
        // See https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            if (!this.allowDocTypeDefinitions) {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure XML parser: " + e.toString());
        }

        DelegatorHandler handler = new DelegatorHandler(sitemapUrl, strict);
        handler.setStrictNamespace(isStrictNamespace());
        if (isStrictNamespace()) {
            handler.setAcceptedNamespaces(acceptedNamespaces);
        }
        handler.setExtensionNamespaces(extensionNamespaces);
        handler.setURLFilter(urlFilter);
        handler.setMaxUrls(maxUrls);

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setEntityResolver(new EntityResolver() {
                // noop entity resolver, does not fetch remote content
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    return new InputSource(new StringReader(""));
                }
            });
            saxParser.parse(is, handler);
            AbstractSiteMap sitemap = handler.getSiteMap();
            if (sitemap == null) {
                UnknownFormatException ex = handler.getException();
                if (ex != null) {
                    throw ex;
                }
                throw new UnknownFormatException("Unknown XML format for: " + sitemapUrl);
            }
            return sitemap;
        } catch (IOException e) {
            LOG.warn("Error parsing sitemap {}: {}", sitemapUrl, e.getMessage());
            UnknownFormatException ufe = new UnknownFormatException("Failed to parse " + sitemapUrl);
            ufe.initCause(e);
            throw ufe;
        } catch (UrlLimitExceededException e) {
            LOG.warn("Truncated sitemap {}: more than {} URLs", sitemapUrl, maxUrls);
            AbstractSiteMap sitemap = handler.getSiteMap();
            sitemap.setProcessed(true);
            return sitemap;
        } catch (SAXException e) {
            LOG.warn("Error parsing sitemap {}: {}", sitemapUrl, e.getMessage());
            AbstractSiteMap sitemap = handler.getSiteMap();
            if (allowPartial && sitemap != null) {
                LOG.warn("Processed broken/partial sitemap for '{}'", sitemapUrl);
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
     * Verify whether the <code>testUrl</code> is under the
     * <code>sitemapBaseUrl</code>. Only URLs under sitemapBaseUrl are valid,
     * see <a href="https://www.sitemaps.org/protocol.html#location">sitemap
     * file location</a>. This is method is used when the parser is configured
     * with <b>strict</b> checking, see {@link #isStrict()}.
     * 
     * Note that this method does not allow to check for <a href=
     * "https://www.sitemaps.org/protocol.html#sitemaps_cross_submits">cross
     * submits</a>: if a sitemap, located on host A, is referenced in the
     * robots.txt file of host B, then this is considered as
     * <q>prove of ownership</q> and the sitemap is allowed to list URLs on host
     * B. For cross-submit validation, see {@link SiteMapCrossSubmitValidator}.
     * 
     * @param sitemapBaseUrl
     *            the base URL of the sitemap
     * @param testUrl
     *            the URL to be tested
     * @return true if testUrl is under sitemapBaseUrl, false otherwise
     */
    public static boolean urlIsValid(String sitemapBaseUrl, String testUrl) {
        return testUrl.startsWith(sitemapBaseUrl);
    }
}
