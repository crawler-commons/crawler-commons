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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import crawlercommons.filters.basic.BasicURLNormalizer;

public class IdnConverterTest {

    private final IdnConverter jdk = IdnConverters.jdk();
    private final IdnConverter icu = IdnConverters.icu4j();

    @Test
    void testIcu4jIsDefaultWhenOnClasspath() {
        assertTrue(IdnConverters.isIcu4jAvailable());
        assertEquals("icu4j", IdnConverters.getDefault().getName());
    }

    @ParameterizedTest
    @CsvSource({ //
                    "schöne.bücher.de, xn--schne-lua.xn--bcher-kva.de", //
                    "нэб.рф, xn--90ax2c.xn--p1ai", //
                    "SCHÖNE.de, xn--schne-lua.de", //
                    "example.com., example.com.", //
                    "foo_bar.example.com, foo_bar.example.com" })
    void testCommonMappings(String unicode, String ascii) {
        assertEquals(ascii, jdk.toASCII(unicode));
        assertEquals(ascii, icu.toASCII(unicode));
    }

    @ParameterizedTest
    @CsvSource({ //
                    // IDNA2003 maps deviation characters, IDNA2008 keeps them
                    "straße.de, strasse.de, xn--strae-oqa.de", //
                    "βόλος.com, xn--nxasmq6b.com, xn--nxasmm1c.com" })
    void testIdna2003VsIdna2008(String unicode, String asciiIdna2003, String asciiIdna2008) {
        assertEquals(asciiIdna2003, jdk.toASCII(unicode));
        assertEquals(asciiIdna2008, icu.toASCII(unicode));
        assertEquals(unicode, icu.toUnicode(asciiIdna2008));
    }

    @Test
    void testUnicodeNewerThan32() {
        // Balinese script, added in Unicode 5.0 (issue #551)
        assertEquals("xn--9tfky.id", icu.toASCII("ᬩᬮᬶ.id"));
    }

    @Test
    void testInvalid() {
        // label exceeds 63 characters
        String longLabel = "ä" + "a".repeat(63) + ".de";
        assertThrows(IllegalArgumentException.class, () -> jdk.toASCII(longLabel));
        assertThrows(IllegalArgumentException.class, () -> icu.toASCII(longLabel));
        // U+2665 BLACK HEART SUIT: disallowed by strict IDNA2008 (NV8), but
        // accepted by UTS #46 (and browsers)
        assertEquals("xn--g6h.com", jdk.toASCII("♥.com"));
        assertEquals("xn--g6h.com", icu.toASCII("♥.com"));
        // U+200D ZERO WIDTH JOINER not in a valid context (CONTEXTJ rule)
        assertThrows(IllegalArgumentException.class, () -> icu.toASCII("a\u200Db.com"));
    }

    @Test
    void testBasicURLNormalizer() {
        BasicURLNormalizer jdkNormalizer = BasicURLNormalizer.newBuilder().idnConverter(jdk).build();
        BasicURLNormalizer icuNormalizer = BasicURLNormalizer.newBuilder().idnConverter(icu).build();
        assertEquals("https://strasse.de/", jdkNormalizer.filter("https://straße.de/"));
        assertEquals("https://xn--strae-oqa.de/", icuNormalizer.filter("https://straße.de/"));
    }
}
