package crawlercommons.test;

import java.io.IOException;
import java.io.OutputStream;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

@SuppressWarnings("serial")
public class StringResponseHandler extends AbstractHttpHandler {
    
    private String _contentType;
    private String _response;
    
    /**
     * Create an HTTP response handler that always sends back a fixed string
     * 
     */
    public StringResponseHandler(String contentType, String response) {
        _contentType = contentType;
        _response = response;
    }
    
    @Override
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        try {
            byte[] bytes = _response.getBytes("UTF-8");
            response.setContentLength(bytes.length);
            response.setContentType(_contentType);
            response.setStatus(200);
            
            OutputStream os = response.getOutputStream();
            os.write(bytes);
        } catch (Exception e) {
            throw new HttpException(500, e.getMessage());
        }
    }
}
