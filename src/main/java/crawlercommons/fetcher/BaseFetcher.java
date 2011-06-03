/*
 * Copyright (c) 2010 TransPac Software, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package crawlercommons.fetcher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tika.mime.MediaType;

@SuppressWarnings("serial")
public abstract class BaseFetcher implements Serializable {
    
    public enum RedirectMode {
        FOLLOW_ALL,         // Fetcher will try to follow all redirects
        FOLLOW_TEMP,        // Temp redirects are automatically followed, but not pemanent.
        FOLLOW_NONE         // No redirects are followed.
    }
    
    public static final int NO_MIN_RESPONSE_RATE = Integer.MIN_VALUE;
    public static final int NO_REDIRECTS = 0;
    
    public static final int DEFAULT_MIN_RESPONSE_RATE = NO_MIN_RESPONSE_RATE;
    public static final int DEFAULT_MAX_CONTENT_SIZE = 64 * 1024;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final int DEFAULT_MAX_REDIRECTS = 20;
    public static final String DEFAULT_ACCEPT_LANGUAGE = "en-us,en-gb,en;q=0.7,*;q=0.3";
    public static final RedirectMode DEFAULT_REDIRECT_MODE = RedirectMode.FOLLOW_ALL;

    protected int _maxThreads;
    protected UserAgent _userAgent;
    protected Map<String, Integer> _maxContentSizes = new HashMap<String, Integer>();
    protected int _defaultMaxContentSize = DEFAULT_MAX_CONTENT_SIZE;
    protected int _maxRedirects = DEFAULT_MAX_REDIRECTS;
    protected int _maxConnectionsPerHost = DEFAULT_MAX_CONNECTIONS_PER_HOST;
    protected int _minResponseRate = DEFAULT_MIN_RESPONSE_RATE;
    protected String _acceptLanguage = DEFAULT_ACCEPT_LANGUAGE;
    protected RedirectMode _redirectMode = DEFAULT_REDIRECT_MODE;
    protected Set<String> _validMimeTypes = new HashSet<String>();

    public BaseFetcher(int maxThreads, UserAgent userAgent) {
        _maxThreads = maxThreads;
        _userAgent = userAgent;
    }

    public int getMaxThreads() {
        return _maxThreads;
    }

    public UserAgent getUserAgent() {
        return _userAgent;
    }
    
    public void setDefaultMaxContentSize(int defaultMaxContentSize) {
        _defaultMaxContentSize = defaultMaxContentSize;
    }
    
    public int getDefaultMaxContentSize() {
        return _defaultMaxContentSize;
    }
    
    public void setMaxContentSize(String mimeType, int maxContentSize) {
        _maxContentSizes.put(mimeType, maxContentSize);
    }

    public int getMaxContentSize(String mimeType) {
        Integer result = _maxContentSizes.get(mimeType);
        if (result == null) {
            result = getDefaultMaxContentSize();
        }
        
        return result;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        _maxConnectionsPerHost = maxConnectionsPerHost;
    }
    
    public int getMaxConnectionsPerHost() {
        return _maxConnectionsPerHost;
    }
    
    public void setMinResponseRate(int minResponseRate) {
        _minResponseRate = minResponseRate;
    }

    /**
     * Return the minimum response rate. If the speed at which bytes are being returned
     * from the server drops below this, the fetch of that page will be aborted.
     * @return bytes/second
     */
    public int getMinResponseRate() {
        return _minResponseRate;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        _acceptLanguage = acceptLanguage;
    }
    
    public String getAcceptLanguage() {
        return _acceptLanguage;
    }
    
    public void setMaxRedirects(int maxRedirects) {
        _maxRedirects = maxRedirects;
    }
    
    public int getMaxRedirects() {
        return _maxRedirects;
    }
    
    public void setRedirectMode(RedirectMode mode) {
        _redirectMode = mode;
    }
    
    public RedirectMode getRedirectMode() {
        return _redirectMode;
    }
    
    public Set<String> getValidMimeTypes() {
        return _validMimeTypes;
    }
    
    public void setValidMimeTypes(Set<String> validMimeTypes) {
        _validMimeTypes = new HashSet<String>(validMimeTypes);
    }
    
    public void addValidMimeTypes(Set<String> validMimeTypes) {
        _validMimeTypes.addAll(validMimeTypes);
    }
    
    public void addValidMimeType(String validMimeType) {
        _validMimeTypes.add(validMimeType);
    }
    
    // Return results of HTTP GET request
    public abstract FetchedResult get(String url) throws BaseFetchException;
    
    public abstract void abort();
    
    protected static String getMimeTypeFromContentType(String contentType) {
        String result = "";
        MediaType mt = MediaType.parse(contentType);
        if (mt != null) {
            result = mt.getType() + "/" + mt.getSubtype();
        }
        
        return result;
    }
    

}
