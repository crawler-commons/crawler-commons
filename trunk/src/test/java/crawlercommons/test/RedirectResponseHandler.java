package crawlercommons.test;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

/**
 * Reponse handler that always returns a 404.
 *
 */
@SuppressWarnings("serial")
public class RedirectResponseHandler extends AbstractHttpHandler {
    
    private String _originalPath;
    private String _redirectUrl;
    
    public RedirectResponseHandler(String originalPath, String redirectUrl) {
        _originalPath = originalPath;
        _redirectUrl = redirectUrl;
    }
    
    @Override
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        if (pathInContext.equalsIgnoreCase(_originalPath)) {
            response.sendRedirect(_redirectUrl);
        } else if (_redirectUrl.contains(pathInContext)) {
            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/plain");

            String content = "redirected content";
            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes());
        } else {
            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/plain");

            String content = "other content";
            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes());
        }
    }
}
