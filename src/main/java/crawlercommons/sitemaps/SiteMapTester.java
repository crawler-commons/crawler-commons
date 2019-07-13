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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sitemap Tool for recursively fetching all URL's from a sitemap (and all of
 * it's children)
 **/
public class SiteMapTester {

    private static final Logger LOG = LoggerFactory.getLogger(SiteMapTester.class);
    private static SiteMapParser saxParser = new SiteMapParser(false, true);

    public static void main(String[] args) throws IOException, UnknownFormatException {
        if (args.length < 1) {
            LOG.error("Fetch and process a sitemap (recursively if a sitemap index)");
            LOG.error("Usage: SiteMapTester <URL_TO_TEST> [MIME_TYPE]");
            LOG.error("Options:");
            LOG.error("  URL_TO_TEST  URL of sitemap");
            LOG.error("  MIME_TYPE    force processing sitemap as MIME type,");
            LOG.error("               bypass automatic MIME type detection");
            LOG.error("Java properties:");
            LOG.error("  sitemap.strictNamespace");
            LOG.error("                  if true sitemaps are required to use the standard namespace URI");
            LOG.error("  sitemap.extensions");
            LOG.error("                  if true enable sitemap extension parsing");
        } else {
            URL url = new URL(args[0]);
            String mt = (args.length > 1) ? args[1] : null;

            parse(url, mt);
        }
    }

    /**
     * Parses a Sitemap recursively meaning that if the sitemap is a
     * sitemapIndex then it parses all of the internal sitemaps
     */
    private static void parse(URL url, String mt) throws IOException, UnknownFormatException {
        byte[] content = IOUtils.toByteArray(url);

        LOG.info("Parsing {} {}", url, ((mt != null && !mt.isEmpty()) ? "as MIME type " + mt : ""));

        boolean strictNamespace = new Boolean(System.getProperty("sitemap.strictNamespace"));
        saxParser.setStrictNamespace(strictNamespace);

        boolean enableExtensions = new Boolean(System.getProperty("sitemap.extensions"));
        if (enableExtensions) {
            saxParser.enableExtensions();
        }

        AbstractSiteMap sm = null;
        // guesses the mimetype
        if (mt == null || mt.equals("")) {
            sm = saxParser.parseSiteMap(content, url);
        } else {
            sm = saxParser.parseSiteMap(mt, content, url);
        }

        if (sm.isIndex()) {
            Collection<AbstractSiteMap> links = ((SiteMapIndex) sm).getSitemaps();
            for (AbstractSiteMap asm : links) {
                parse(asm.getUrl(), mt); // Recursive call
            }
        } else {
            Collection<SiteMapURL> links = ((SiteMap) sm).getSiteMapUrls();
            for (SiteMapURL smu : links) {
                if (enableExtensions) {
                    LOG.info(smu.toString());
                } else {
                    LOG.info(smu.getUrl().toString());
                }
            }
        }
    }
}