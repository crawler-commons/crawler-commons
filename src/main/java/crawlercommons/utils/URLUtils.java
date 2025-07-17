package crawlercommons.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

    /**
     * Resolves a URL against a base URL.
     *
     * @param base the base URL
     * @param spec the URL specification to resolve
     * @return the resolved URL
     * @throws MalformedURLException if the URL cannot be resolved
     */
    public static URL resolve(URL base, String spec) throws MalformedURLException {
        try {
            return base.toURI().resolve(spec).toURL();
        } catch (Exception e) {
            throw (MalformedURLException) new MalformedURLException(e.getMessage()).initCause(e);
        }
    }
}
