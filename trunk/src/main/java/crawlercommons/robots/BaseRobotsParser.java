package crawlercommons.robots;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseRobotsParser implements Serializable {

    /**
     * Parse the robots.txt file in <content>, and return rules appropriate for
     * processing paths by <userAgent>
     * 
     * @param url URL that content was fetched from (for reporting purposes)
     * @param content raw bytes from the site's robots.txt file
     * @param contentType HTTP response header (mime-type)
     * @param robotName name of crawler, to be used when processing file contents
     *        (just the name portion, w/o version or other details)
     * @return robot rules.
     */
    
    public abstract BaseRobotRules parseContent(String url, byte[] content, String contentType, String robotName);
    
    
    /**
     * The fetch of robots.txt failed, so return rules appropriate give the
     * HTTP status code.
     * 
     * @param httpStatusCode a failure status code (NOT 2xx)
     * @return robot rules
     */
    public abstract BaseRobotRules failedFetch(int httpStatusCode);
}
