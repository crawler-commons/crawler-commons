package crawlercommons.robots;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.AbstractHttpHandler;

import crawlercommons.fetcher.BaseFetcher;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.UserAgent;
import crawlercommons.test.SimulationWebServer;
import crawlercommons.test.TestUtils;


public class RobotUtilsTest {

    @SuppressWarnings("serial")
    private static class CircularRedirectResponseHandler extends AbstractHttpHandler {
        
        @Override
        public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
            response.sendRedirect(pathInContext);
        }
    }

    @SuppressWarnings("serial")
    private static class RedirectToTopResponseHandler extends AbstractHttpHandler {
        
        @Override
        public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
            if (pathInContext.endsWith("robots.txt")) {
                response.sendRedirect("/");
            } else {
                byte[] bytes = "<html><body></body></html>".getBytes("UTF-8");
                response.setContentLength(bytes.length);
                response.setContentType("text/html; charset=UTF-8");
                response.setStatus(200);
                
                OutputStream os = response.getOutputStream();
                os.write(bytes);
            }
        }
    }

    /**
     * Verify that when the web server has a circular redirect bug for robots.txt, we
     * treat it like "no robots".
     * 
     * @throws Exception
     */
    @Test
    public void testCircularRedirect() throws Exception {
        BaseFetcher fetcher = RobotUtils.createFetcher(TestUtils.CC_TEST_AGENT, 1);
        BaseRobotsParser parser = new SimpleRobotRulesParser();
        
        SimulationWebServer webServer = new SimulationWebServer();
        HttpServer server = webServer.startServer(new CircularRedirectResponseHandler(), 8089);
        
        try {
            BaseRobotRules rules = RobotUtils.getRobotRules(fetcher, parser, new URL("http://localhost:8089/robots.txt"));
            Assert.assertTrue(rules.isAllowAll());
        } finally {
            server.stop();
        }
    }

    @Test
    public void testRedirectToHtml() throws Exception {
        BaseFetcher fetcher = RobotUtils.createFetcher(TestUtils.CC_TEST_AGENT, 1);
        BaseRobotsParser parser = new SimpleRobotRulesParser();
        
        SimulationWebServer webServer = new SimulationWebServer();
        HttpServer server = webServer.startServer(new RedirectToTopResponseHandler(), 8089);
        
        try {
            BaseRobotRules rules = RobotUtils.getRobotRules(fetcher, parser, new URL("http://localhost:8089/robots.txt"));
            Assert.assertTrue(rules.isAllowAll());
        } finally {
            server.stop();
        }
    }
    
    @Test
    public void testMatchAgainstEmailAddress() throws Exception {
        // The "crawler@domain.com" email address shouldn't trigger a match against the
        // "crawler" user agent name in the robots.txt file.
        final String simpleRobotsTxt = "User-agent: crawler" + "\r\n"
        + "Disallow: /";

        BaseFetcher fetcher = Mockito.mock(BaseFetcher.class);
        FetchedResult result = Mockito.mock(FetchedResult.class);
        Mockito.when(result.getContent()).thenReturn(simpleRobotsTxt.getBytes());
        Mockito.when(fetcher.get(Mockito.any(String.class))).thenReturn(result);
        UserAgent userAgent = new UserAgent("testAgent", "crawler@domain.com", "http://www.domain.com");
        Mockito.when(fetcher.getUserAgent()).thenReturn(userAgent);
        
        URL robotsUrl = new URL("http://www.domain.com/robots.txt");
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        BaseRobotRules rules = RobotUtils.getRobotRules(fetcher, parser, robotsUrl);
        
        Assert.assertTrue(rules.isAllowed("http://www.domain.com/anypage.html"));
    }
    

}
