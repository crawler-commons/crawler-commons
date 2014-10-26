package crawlercommons.sitemaps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created on 13/10/2014.
 *
 */
@RunWith(JUnit4.class)
public class SiteMapURLTest {
  SiteMapURL siteMapURL;

  @Before
  public void setUp() throws Exception {
      siteMapURL = new SiteMapURL("http://example.com", true);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetPriority() {
      assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);
      siteMapURL.setPriority(0.6);
      assertEquals(0.6, siteMapURL.getPriority(), 0);

      siteMapURL.setPriority(1.1);
      assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);

      siteMapURL.setPriority("0.6");
      assertEquals(0.6, siteMapURL.getPriority(), 0);

      siteMapURL.setPriority("BAD VALUE");
      assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);

      siteMapURL.setPriority("0.6");
      assertEquals(0.6, siteMapURL.getPriority(), 0);
      siteMapURL.setPriority("1.1");
      assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);
  }
}
