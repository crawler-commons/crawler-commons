/**
 * Copyright 2026 Crawler-Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.utils.idn;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for {@link IdnConverter} instances.
 *
 * <p>
 * The default converter is selected once, on first use:
 * </p>
 * <ol>
 * <li>if the system property {@value #IMPL_PROPERTY} is set to
 * <code>jdk</code> or <code>icu4j</code>, that implementation is used</li>
 * <li>otherwise, if ICU4J is found on the classpath, the IDNA2008-compliant
 * {@link Icu4jIdnConverter} is used</li>
 * <li>otherwise, the IDNA2003-based {@link JdkIdnConverter} is used</li>
 * </ol>
 */
public final class IdnConverters {

    private static final Logger LOG = LoggerFactory.getLogger(IdnConverters.class);

    /** System property to force an implementation: "jdk" or "icu4j". */
    public static final String IMPL_PROPERTY = "crawlercommons.idn.impl";

    private static final String ICU4J_PROBE_CLASS = "com.ibm.icu.text.IDNA";

    private IdnConverters() {
    }

    /**
     * Lazy holder: the default is only resolved when first requested.
     */
    private static final class Holder {
        static final IdnConverter DEFAULT = resolveDefault();
    }

    /**
     * @return the default converter, see class documentation
     */
    public static IdnConverter getDefault() {
        return Holder.DEFAULT;
    }

    /**
     * @return the IDNA2003 converter based on {@link java.net.IDN}, always
     *         available
     */
    public static IdnConverter jdk() {
        return JdkIdnConverter.INSTANCE;
    }

    /**
     * @return whether ICU4J is available on the classpath
     */
    public static boolean isIcu4jAvailable() {
        try {
            Class.forName(ICU4J_PROBE_CLASS, false, IdnConverters.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    /**
     * @return the IDNA2008 converter based on ICU4J
     * @throws IllegalStateException
     *             if ICU4J is not available on the classpath
     */
    public static IdnConverter icu4j() {
        if (!isIcu4jAvailable()) {
            throw new IllegalStateException("ICU4J (com.ibm.icu:icu4j) not found on the classpath");
        }
        // Icu4jIdnConverter is only referenced (and thus loaded) here, after
        // the availability check
        return new Icu4jIdnConverter();
    }

    private static IdnConverter resolveDefault() {
        String impl = System.getProperty(IMPL_PROPERTY);
        IdnConverter converter;
        if (impl != null && impl.toLowerCase(Locale.ROOT).equals("jdk")) {
            converter = jdk();
        } else if (impl != null && impl.toLowerCase(Locale.ROOT).equals("icu4j")) {
            converter = icu4j();
        } else {
            if (impl != null) {
                LOG.warn("Unknown value for {}: {}, falling back to auto-detection", IMPL_PROPERTY, impl);
            }
            converter = isIcu4jAvailable() ? icu4j() : jdk();
        }
        LOG.debug("Using IDN converter: {}", converter.getName());
        return converter;
    }
}
