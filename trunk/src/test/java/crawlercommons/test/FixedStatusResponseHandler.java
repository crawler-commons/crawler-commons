package crawlercommons.test;

import java.io.IOException;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

/**
 * Reponse handler that always returns a 404.
 *
 */
@SuppressWarnings("serial")
public class FixedStatusResponseHandler extends AbstractHttpHandler {
    private int _status;
    
    public FixedStatusResponseHandler(int status) {
        _status = status;
    }
    
    @Override
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        throw new HttpException(_status, "Pre-defined error fetching: " + pathInContext);
    }
}
