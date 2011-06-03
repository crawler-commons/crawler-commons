package crawlercommons.fetcher;


@SuppressWarnings({ "serial" })
public class AbortedFetchException extends BaseFetchException {
    private AbortedFetchReason _abortReason;
    
    public AbortedFetchException() {
        super();
    }
    
    public AbortedFetchException(String url, AbortedFetchReason abortReason) {
        super(url, "Aborted due to " + abortReason);
        
        _abortReason = abortReason;
    }
    
    public AbortedFetchException(String url, String msg, AbortedFetchReason abortReason) {
        super(url, msg);
        
        _abortReason = abortReason;
    }
    
    public AbortedFetchReason getAbortReason() {
        return _abortReason;
    }
}
