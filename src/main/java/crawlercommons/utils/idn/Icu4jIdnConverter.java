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

import java.util.EnumSet;
import java.util.Set;

import com.ibm.icu.text.IDNA;

/**
 * {@link IdnConverter} based on ICU4J's implementation of <a
 * href="https://www.unicode.org/reports/tr46/">UTS #46</a> in non-transitional
 * mode, i.e. IDNA2008 (<a href="https://www.rfc-editor.org/rfc/rfc5890">RFC
 * 5890</a>) plus the UTS #46 mapping step (case folding, normalization). This
 * is also what the <a href="https://url.spec.whatwg.org/#idna">WHATWG URL
 * standard</a> and hence browsers use.
 *
 * <p>
 * Requires <code>com.ibm.icu:icu4j</code> on the classpath. The dependency is
 * optional: this class must only be loaded after checking the availability of
 * ICU4J, see {@link IdnConverters}.
 * </p>
 */
public class Icu4jIdnConverter implements IdnConverter {

    /**
     * Errors which are ignored to stay compatible with {@link JdkIdnConverter}
     * and with the WHATWG URL standard (CheckHyphens=false,
     * VerifyDnsLength=false). The label length (max. 63 characters) is still
     * verified because {@link crawlercommons.domains.EffectiveTldFinder}
     * relies on it.
     */
    private static final Set<IDNA.Error> IGNORED_ERRORS = EnumSet.of( //
                    IDNA.Error.EMPTY_LABEL, //
                    IDNA.Error.DOMAIN_NAME_TOO_LONG, //
                    IDNA.Error.LEADING_HYPHEN, //
                    IDNA.Error.TRAILING_HYPHEN, //
                    IDNA.Error.HYPHEN_3_4);

    private final IDNA idna;

    public Icu4jIdnConverter() {
        this.idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII | IDNA.NONTRANSITIONAL_TO_UNICODE | IDNA.CHECK_BIDI | IDNA.CHECK_CONTEXTJ);
    }

    @Override
    public String toASCII(String name) {
        IDNA.Info info = new IDNA.Info();
        String result = idna.nameToASCII(name, new StringBuilder(name.length() + 8), info).toString();
        checkErrors(name, info);
        if (result.isEmpty() && !name.isEmpty()) {
            // e.g. a host consisting only of ignored code points (U+00AD soft
            // hyphen), cf. WHATWG URL "domain to ASCII"
            throw new IllegalArgumentException("IDN maps to empty string: " + name);
        }
        return result;
    }

    @Override
    public String toUnicode(String name) {
        IDNA.Info info = new IDNA.Info();
        String result = idna.nameToUnicode(name, new StringBuilder(name.length()), info).toString();
        checkErrors(name, info);
        return result;
    }

    @Override
    public String getName() {
        return "icu4j";
    }

    private static void checkErrors(String name, IDNA.Info info) {
        if (!info.hasErrors()) {
            return;
        }
        Set<IDNA.Error> errors = EnumSet.copyOf(info.getErrors());
        errors.removeAll(IGNORED_ERRORS);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid IDN " + name + ": " + errors);
        }
    }
}
