package crawlercommons.fetcher;


@SuppressWarnings({ "serial" })
public class UrlFetchException extends BaseFetchException {
    
    public UrlFetchException() {
        super();
    }
    
    public UrlFetchException(String url, String msg) {
        super(url, msg);
    }

}
