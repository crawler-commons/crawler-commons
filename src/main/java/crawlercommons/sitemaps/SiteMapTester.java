package crawlercommons.sitemaps;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

/** Sitemap Tool for recursively fetching all URL's from a sitemap (and all of it's children) **/
public class SiteMapTester {
    private static SiteMapParser parser = new SiteMapParser(false);

    public static void main(String[] args) throws IOException, UnknownFormatException {
        if (args.length < 1) {
            System.err.println("Usage: SiteMapTester <URL_TO_TEST> [MIME_TYPE]");
        } else {
            URL url = new URL(args[0]);
            String mt = (args.length > 1) ? args[1] : null;

            parse(url, mt);
        }
    }

    /** Parses a Sitemap recursively meaning that if the sitemap is a sitemapIndex then it parses all of the internal sitemaps */
    private static void parse(URL url, String mt) throws IOException, UnknownFormatException {
        byte[] content = IOUtils.toByteArray(url);

        AbstractSiteMap sm = null;
        // guesses the mimetype
        if (mt == null || mt.equals("")){
        	  sm = parser.parseSiteMap(content, url);
        } else {
            sm = parser.parseSiteMap(mt, content, url);
        }

        if (sm.isIndex()) {
            Collection<AbstractSiteMap> links = ((SiteMapIndex) sm).getSitemaps();
            for (AbstractSiteMap asm : links) {
                parse(asm.getUrl(), mt); // Recursive call
            }
        } else {
            Collection<SiteMapURL> links = ((SiteMap) sm).getSiteMapUrls();
            for (SiteMapURL smu : links) {
                System.out.println(smu.getUrl());
            }
        }
    }
}