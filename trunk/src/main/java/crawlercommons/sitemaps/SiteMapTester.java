package crawlercommons.sitemaps;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

/** Utility class for testing the Sitemap Parsing **/
public class SiteMapTester {

    SiteMapParser parser = new SiteMapParser();

    private void parse(URL url, boolean recursive) throws IOException, UnknownFormatException {
        byte[] content = IOUtils.toByteArray(url);

        String mt = "";
        AbstractSiteMap sm = parser.parseSiteMap(mt, content, url);

        System.out.println(sm.toString());

        if (recursive && sm.isIndex()) {
            Collection<AbstractSiteMap> links = ((SiteMapIndex) sm).getSitemaps();
            for (AbstractSiteMap asm : links) {
                parse(asm.getUrl(), recursive);
            }
        } else if (!sm.isIndex()) {
            Collection<SiteMapURL> links = ((SiteMap) sm).getSiteMapUrls();
            for (SiteMapURL smu : links) {
                System.out.println("\t" + smu.toString());
            }
        }
    }

    public static void main(String[] args) throws IOException, UnknownFormatException {
        if (args.length<1) {
            System.err.println("SiteMapTester URL_to_test");
        }
        
        URL url = new URL(args[0]);

        SiteMapTester tester = new SiteMapTester();
        tester.parse(url, true);
    }

}
