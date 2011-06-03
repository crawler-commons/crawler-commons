package crawlercommons.fetcher;

import java.io.IOException;

@SuppressWarnings({ "serial" })
public class IOFetchException extends BaseFetchException {
    
    public IOFetchException() {
        super();
    }
    
    public IOFetchException(String url, IOException e) {
        super(url, e);
    }

}
