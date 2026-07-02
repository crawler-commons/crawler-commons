package crawlercommons.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import crawlercommons.url.CrawlerURL;

public class URLUtils {

    /**
     * Resolves a URL against a base URL.
     * 
     * <p>
     * Context: this helper was introduced when migrating callers from using
     * {@code java.net.URL} constructors to {@code URI}. The raw
     * {@code URI.resolve} semantics differ in a few corner cases from
     * {@code URL}-based resolution did. To avoid scattering ad-hoc fixes across
     * the codebase, the normalization logic lives here so resolution behavior
     * is consistent and easier to maintain.
     * </p>
     * 
     * @param base
     *            the base URL
     * @param spec
     *            the URL specification to resolve
     * @return the resolved URL
     * @throws MalformedURLException
     *             if the URL cannot be resolved
     */
    public static URL resolve(URL base, String spec) throws MalformedURLException {
        try {
            if (base == null) {
                return new URI(spec).toURL();
            }

            String resolvedSpec = spec;
            if (spec.startsWith(":")) {
                // Prepending "./" makes spec starting with colon a valid
                // relative path reference.
                resolvedSpec = "./" + spec;
            } else if (spec.startsWith("?")) {
                // Handle pure query targets, as browsers resolve differently
                // than RFC 3986.
                // Prepend the base path's file component to spec.
                String basePath = base.getPath();
                if (basePath != null && !basePath.isEmpty()) {
                    int lastSlash = basePath.lastIndexOf('/');
                    if (lastSlash > -1 && lastSlash < (basePath.length() - 1)) {
                        resolvedSpec = basePath.substring(lastSlash + 1) + spec;
                    }
                }
            }

            return base.toURI().resolve(resolvedSpec).toURL();
        } catch (Exception e) {
            throw (MalformedURLException) new MalformedURLException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Resolves a URL against a base URL.
     *
     * <p>
     * Overload accepting a {@code java.net.URI} base, added for issue #556 to
     * let callers that already hold a {@code URI} avoid converting to
     * {@code java.net.URL} first. It delegates to
     * {@link #resolve(URL, String)} so resolution semantics stay identical.
     * </p>
     *
     * @param base
     *            the base URL, or {@code null}
     * @param spec
     *            the URL specification to resolve
     * @return the resolved URL
     * @throws MalformedURLException
     *             if the URL cannot be resolved
     */
    public static URL resolve(URI base, String spec) throws MalformedURLException {
        if (base == null) {
            return resolve((URL) null, spec);
        }
        try {
            return resolve(base.toURL(), spec);
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            throw (MalformedURLException) new MalformedURLException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Resolves a URL against a base URL.
     *
     * <p>
     * Overload accepting a {@link CrawlerURL} base, added for issue #556 to let
     * callers pass the lazily-converting wrapper directly. It delegates to
     * {@link #resolve(URL, String)} so resolution semantics stay identical.
     * </p>
     *
     * @param base
     *            the base URL, or {@code null}
     * @param spec
     *            the URL specification to resolve
     * @return the resolved URL
     * @throws MalformedURLException
     *             if the URL cannot be resolved
     */
    public static URL resolve(CrawlerURL base, String spec) throws MalformedURLException {
        if (base == null) {
            return resolve((URL) null, spec);
        }
        try {
            return resolve(base.toJavaURL(), spec);
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            throw (MalformedURLException) new MalformedURLException(e.getMessage()).initCause(e);
        }
    }
}
