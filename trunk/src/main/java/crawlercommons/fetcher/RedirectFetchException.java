package crawlercommons.fetcher;


@SuppressWarnings({ "serial" })
public class RedirectFetchException extends BaseFetchException {
    
    // Possible redirect exception types.
    
    public enum RedirectExceptionReason {
        TOO_MANY_REDIRECTS,         // Request for original URL tried too many hops.
        PERM_REDIRECT_DISALLOWED,   // RedirectMode disallows a permanent redirect.
        TEMP_REDIRECT_DISALLOWED    // RedirectMode disallows a temp redirect.
    }

    private String _redirectedUrl;
    private RedirectExceptionReason _reason;
    
    public RedirectFetchException() {
        super();
    }
    
    public RedirectFetchException(String url, String redirectedUrl, RedirectExceptionReason reason) {
        super(url, "Too many redirects");
        _redirectedUrl = redirectedUrl;
        _reason = reason;
    }
    
    public String getRedirectedUrl() {
        return _redirectedUrl;
    }
    
    public RedirectExceptionReason getReason() {
        return _reason;
    }
    

}
