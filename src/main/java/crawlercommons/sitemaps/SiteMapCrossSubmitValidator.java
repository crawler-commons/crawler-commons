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

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.domains.EffectiveTldFinder;

/**
 * Validator for sitemap <a href=
 * "https://www.sitemaps.org/protocol.html#sitemaps_cross_submits">cross
 * submits</a>.
 * 
 * The sitemap protocol defines strict requirements regarding the
 * <a href="https://www.sitemaps.org/protocol.html#location">location of a
 * sitemap</a>:
 * 
 * <blockquote>
 * <p>
 * The location of a Sitemap file determines the set of URLs that can be
 * included in that Sitemap. A Sitemap file located at
 * http://example.com/catalog/sitemap.xml can include any URLs starting with
 * http://example.com/catalog/ but can not include URLs starting with
 * http://example.com/images/.
 * </p>
 * <p>
 * If you have the permission to change http://example.org/path/sitemap.xml, it
 * is assumed that you also have permission to provide information for URLs with
 * the prefix http://example.org/path/.
 * </p>
 * </blockquote>
 * 
 * However, when the sitemap location (on host A) is specified in the robots.txt
 * file of host B, this
 * <q>proves the ownership</q> and the sitemap is allowed to
 * <q><a href= "https://www.sitemaps.org/protocol.html#sitemaps_cross_submits"
 * >cross-submit</a></q> URLs on host B.
 * 
 * Note: in order to use the validator, you need to create a sitemap parser
 * without strict validation, see {@link SiteMapParser#isStrict()},
 * {@link SiteMapParser#urlIsValid(String, String)} and the constructors of
 * {@link SiteMapParser}.
 */
public class SiteMapCrossSubmitValidator {

    public static final Logger LOG = LoggerFactory.getLogger(SiteMapCrossSubmitValidator.class);

    public enum CrossSubmitValidationLevel {
        /**
         * Domain name below a suffix in the ICANN section of the public suffix
         * list. Cf. {@link EffectiveTldFinder}
         */
        ICANN_DOMAIN,
        /**
         * Domain name below a public suffix, cf. {@link EffectiveTldFinder}
         */
        PRIVATE_DOMAIN,
        /** Host name resp. full domain name */
        HOST
    }

    private SiteMapCrossSubmitValidator() {
    }

    /**
     * Validate the URLs submitted in a sitemap whether they are valid, that is
     * below the same URL prefix as the location of the sitemap. Invalid URLs
     * are removed.
     * 
     * Calling this method on a sitemap has the same effect as using a sitemap
     * parser with <b>strict</b> validation, see
     * {@link SiteMapParser#isStrict()}.
     * 
     * @param sitemap
     *            sitemap holding the URLs and the base URL (the URL prefix)
     */
    public static void validateSiteMapURLs(SiteMap sitemap) {
        LOG.debug("Validating sitemap URLs for prefix {}", sitemap.getBaseUrl());
        validateSiteMapURLs(sitemap, (URL url) -> SiteMapParser.urlIsValid(sitemap.getBaseUrl(), url.toString()));
    }

    /**
     * Validate the URLs in a sitemap against a single cross-submit host.
     * Invalid URLs are removed.
     * 
     * This method implements the typical cross-submit check for a sitemap
     * announced in the robots.txt of one host while located on a different
     * host. Naturally, it also verifies that <q>all URLs in a Sitemap must be
     * from a single host</q>.
     * 
     * @param sitemap
     * 
     * @param host
     *            host name proved for cross-submits. Usually the host of the
     *            robots.txt file the sitemap was announced.
     */
    public static void validateSiteMapURLs(SiteMap sitemap, String host) {
        LOG.debug("Validating sitemap URLs for cross-submit host {}", host);
        validateSiteMapURLs(sitemap, (URL url) -> url.getHost().equals(host));
    }

    /**
     * Validate the URLs in a sitemap against a set of cross-submit hosts.
     * Invalid URLs are removed.
     * 
     * This method is useful, if the same sitemap location is found in the
     * robots.txt files of multiple hosts.
     * 
     * @param sitemap
     *            sitemap holding the URLs to be validated
     * @param hosts
     *            set of host names proved for cross-submits
     */
    public static void validateSiteMapURLs(SiteMap sitemap, Collection<String> hosts) {
        LOG.debug("Validating sitemap URLs against a set of cross-submit hosts {}", hosts);
        validateSiteMapURLs(sitemap, (URL url) -> hosts.contains(url.getHost()));
    }

    /**
     * Validate a single URL whether its host, ICANN or private domain is part
     * of a list of domain names.
     * 
     * @param url
     *            URL to validate
     * @param domains
     *            set of domain names proved for cross-submits
     * @param domainValidationLevel
     *            validation level for the domain names
     */
    protected static boolean validate(URL url, Collection<String> domains, CrossSubmitValidationLevel domainValidationLevel) {
        String domain;
        switch (domainValidationLevel) {
            case ICANN_DOMAIN:
            domain = EffectiveTldFinder.getAssignedDomain(url.getHost(), false, true);
            return domains.contains(domain);
            case PRIVATE_DOMAIN:
            domain = EffectiveTldFinder.getAssignedDomain(url.getHost(), false, false);
            return domains.contains(domain);
            default:
            return domains.contains(url.getHost());
        }
    }

    /**
     * Validate the URLs in a sitemap against a set of cross-submit domains.
     * Invalid URLs are removed.
     * 
     * This method is useful, if a site owner is not sufficiently precise
     * regarding sitemap location. It allows to <q>prove ownership</q> by
     * verifying that both the sitemap and cross-submit host share the same
     * domain holder. The public suffix list is used to determine the shared
     * part below the suffix, cf. {@link EffectiveTldFinder}.
     * 
     * @param sitemap
     *            sitemap holding the URLs to be validated
     * @param domains
     *            set of domain names proved for cross-submits
     * @param domainValidationLevel
     *            validation level for the domain names
     */
    public static void validateSiteMapURLs(SiteMap sitemap, Collection<String> domains, CrossSubmitValidationLevel domainValidationLevel) {
        LOG.debug("Validating sitemap URLs against a set of cross-submit sites {} ({})", domains, domainValidationLevel);
        validateSiteMapURLs(sitemap, (URL url) -> validate(url, domains, domainValidationLevel));
    }

    public static void validateSiteMapURLs(SiteMap sitemap, Predicate<URL> validator) {
        Iterator<SiteMapURL> iter = sitemap.getSiteMapUrls().iterator();
        int validated = 0, invalid = 0;
        while (iter.hasNext()) {
            SiteMapURL u = iter.next();
            validated++;
            if (!validator.test(u.getUrl())) {
                LOG.debug("Removing URL: {}", u.toString());
                iter.remove();
                invalid++;
            }
        }
        LOG.debug("Validated {} sitemap URLs: {} valid, {} invalid", validated, (validated - invalid), invalid);
    }

    /**
     * Validation of a sitemap or recursive validation of a sitemap index.
     * 
     * See
     * {@link #validateSiteMapURLs(SiteMap, Collection, CrossSubmitValidationLevel)}
     * .
     * 
     * @param sitemap
     *            sitemap or sitemap index, holding the URLs to be validated
     * @param domains
     *            set of domain names proved for cross-submits
     * @param domainValidationLevel
     *            validation level for the domain names
     */
    public static void validateSiteMapURLs(AbstractSiteMap sitemap, Collection<String> domains, CrossSubmitValidationLevel domainValidationLevel) {
        if (sitemap.isIndex()) {
            Collection<AbstractSiteMap> sitemaps = ((SiteMapIndex) sitemap).getSitemaps();
            for (AbstractSiteMap asm : sitemaps) {
                // recursive call
                validateSiteMapURLs(asm, domains, domainValidationLevel);
            }
        } else {
            validateSiteMapURLs((SiteMap) sitemap, domains, domainValidationLevel);
        }
    }
}
