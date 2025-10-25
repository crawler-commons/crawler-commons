package crawlercommons.utils;

import java.net.MalformedURLException;
import java.net.URI;
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
            if (base == null) {
                return new URI(spec).toURL();
            }

            String resolvedSpec = spec;
            if (spec.startsWith(":")) {
                // Prepending "./" makes spec starting with colon a valid relative path reference.
                resolvedSpec = "./" + spec;
            } else if (spec.startsWith("?")) {
                // Handle pure query targets, as browsers resolve differently than RFC 3986.
                // Prepend the base path's file component to spec.
                String basePath = base.getPath();
                if (basePath != null && !basePath.isEmpty()) {
                    int lastSlash = basePath.lastIndexOf('/');
                    if (lastSlash > -1 && lastSlash < (basePath.length() - 1)) {
                        resolvedSpec = basePath.substring(lastSlash + 1) + spec;
                    }
                }
            }

            return base.toURI().resolve(resolvedSpec).normalize().toURL();
        } catch (Exception e) {
            throw (MalformedURLException) new MalformedURLException(e.getMessage()).initCause(e);
        }
    }
}
