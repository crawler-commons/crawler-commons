/**
 * Copyright 2025 Crawler-Commons
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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiteMapCrossSubmitValidatorTest {

    protected static SiteMap getSiteMap(String url, String content) throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser(false);
        String contentType = "text/plain";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        AbstractSiteMap asm = parser.parseSiteMap(contentType, bytes, new URL(url));
        AbstractSiteMapTest.testSerializable(asm);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        return (SiteMap) asm;
    }

    @Test
    public void testValidateURLs() throws Exception {
        SiteMap sm = getSiteMap("https://blog.example.com/sitemap.txt", //
                        "https://blog.example.com/blog-1.html\n" //
                                        + "https://blog.example.com/blog-2.html");
        assertEquals(2, sm.getSiteMapUrls().size());
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm);
        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    public void testValidateCrossSubmitHost() throws Exception {
        SiteMap sm = getSiteMap("https://www.example.com/sitemap.txt", //
                        "https://blog.example.com/blog-1.html\n" //
                                        + "https://blog.example.com/blog-2.html");
        assertEquals(2, sm.getSiteMapUrls().size());
        // sitemap was announced in https://blog.example.com/robots.txt
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, "blog.example.com");
        assertEquals(2, sm.getSiteMapUrls().size());
        // but not in https://www.otherdomain.com/robots.txt
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, "www.otherdomain.com");
        assertEquals(0, sm.getSiteMapUrls().size());
    }

    @Test
    public void testValidateCrossSubmitSites() throws Exception {
        String url = "https://www.example.com/sitemap.txt";
        /*
         * Note: for simplicity, in this test we use sitemaps with URLs from
         * multiple hosts, although the sitemap protocol specifies that
         * "all URLs in a Sitemap must be from a single host"
         * (https://www.sitemaps.org/protocol.html)
         */
        String content = "https://blog.example.com/blog-1.html\n" //
                        + "https://blog.example.com/blog-2.html\n" //
                        + "https://docs.example.com/index.html";
        SiteMap sm = getSiteMap(url, content);
        assertEquals(3, sm.getSiteMapUrls().size());
        // sitemap was announced in https://blog.example.com/robots.txt
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("blog.example.com"));
        assertEquals(2, sm.getSiteMapUrls().size());
        // but not in https://www.otherdomain.com/robots.txt
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("www.otherdomain.com"));
        assertEquals(0, sm.getSiteMapUrls().size());
        // test with multiple hosts
        sm = getSiteMap(url, content);
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("www.example.com", "blog.example.com", "docs.example.com"));
        assertEquals(3, sm.getSiteMapUrls().size());
        // equivalent call
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("www.example.com", "blog.example.com", "docs.example.com"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.HOST);
        assertEquals(3, sm.getSiteMapUrls().size());
        // test with ICANN / private domain
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("example.com"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.PRIVATE_DOMAIN);
        assertEquals(3, sm.getSiteMapUrls().size());
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("example.com"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.ICANN_DOMAIN);
        assertEquals(3, sm.getSiteMapUrls().size());
        /*
         * a more realistic test for a domain name below a suffix in the private
         * section of the public suffix list
         */
        content = "https://this.github.io/this.html\n" //
                        + "https://that.github.io/that-1.html\n" //
                        + "https://that.github.io/that-2.html";
        sm = getSiteMap(url, content);
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("this.github.io"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.PRIVATE_DOMAIN);
        assertEquals(1, sm.getSiteMapUrls().size());
        sm = getSiteMap(url, content);
        SiteMapCrossSubmitValidator.validateSiteMapURLs(sm, Set.of("that.github.io"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.PRIVATE_DOMAIN);
        assertEquals(2, sm.getSiteMapUrls().size());
        /** validate sitemap index */
        sm = getSiteMap(url, content);
        SiteMapIndex smi = new SiteMapIndex();
        smi.addSitemap(sm);
        SiteMapCrossSubmitValidator.validateSiteMapURLs(smi, Set.of("this.github.io"), SiteMapCrossSubmitValidator.CrossSubmitValidationLevel.PRIVATE_DOMAIN);
        assertEquals(1, sm.getSiteMapUrls().size());
    }

}
