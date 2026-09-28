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

import static java.net.IDN.ALLOW_UNASSIGNED;

import java.net.IDN;

/**
 * {@link IdnConverter} based on {@link java.net.IDN}, which implements IDNA2003
 * (<a href="https://www.rfc-editor.org/rfc/rfc3490">RFC 3490</a>).
 *
 * <p>
 * Always available, but deviates from IDNA2008 for some characters: e.g.
 * <code>straße.de</code> is mapped to <code>strasse.de</code> and not to
 * <code>xn--strae-oqa.de</code>. Code points not assigned in Unicode 3.2 are
 * accepted ({@link IDN#ALLOW_UNASSIGNED}) but not validated.
 * </p>
 */
public class JdkIdnConverter implements IdnConverter {

    static final JdkIdnConverter INSTANCE = new JdkIdnConverter();

    @Override
    public String toASCII(String name) {
        return IDN.toASCII(name, ALLOW_UNASSIGNED);
    }

    @Override
    public String toUnicode(String name) {
        return IDN.toUnicode(name, ALLOW_UNASSIGNED);
    }

    @Override
    public String getName() {
        return "jdk";
    }
}
