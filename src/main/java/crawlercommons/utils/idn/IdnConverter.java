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

/**
 * Converts <a href=
 * "https://en.wikipedia.org/wiki/Internationalized_domain_name">internationalized
 * domain names (IDNs)</a> between their Unicode and ASCII ("Punycode") forms.
 *
 * <p>
 * Implementations differ in the IDNA specification they follow, see
 * {@link IdnConverters#getDefault()} for how an implementation is selected.
 * </p>
 */
public interface IdnConverter {

    /**
     * Converts a domain name (or a single label) to its ASCII form.
     *
     * @param name
     *            domain name or label, may contain non-ASCII characters
     * @return ASCII representation of the name
     * @throws IllegalArgumentException
     *             if the name cannot be converted, e.g. because it contains
     *             disallowed code points or a label exceeds 63 characters
     */
    String toASCII(String name) throws IllegalArgumentException;

    /**
     * Converts a domain name (or a single label) to its Unicode form.
     *
     * @param name
     *            domain name or label, possibly containing Punycode labels
     *            (<code>xn--...</code>)
     * @return Unicode representation of the name
     * @throws IllegalArgumentException
     *             if the name cannot be converted
     */
    String toUnicode(String name) throws IllegalArgumentException;

    /**
     * @return a short name identifying the implementation, e.g. for logging
     */
    String getName();
}
