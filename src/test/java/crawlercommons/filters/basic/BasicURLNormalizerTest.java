/**
 * Copyright 2016 Crawler-Commons
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

package crawlercommons.filters.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/** Unit tests for BasicURLNormalizer. */
public class BasicURLNormalizerTest {
    private static BasicURLNormalizer normalizer;

    @BeforeAll
    public static void setup() {
        normalizer = new BasicURLNormalizer();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/normalizer/weirdToNormalizedUrls.csv")
    void testBasicNormalizer(String weirdUrl, String expectedNormalizedUrl) {
        assertEquals(expectedNormalizedUrl, normalizer.filter(weirdUrl), "normalizing: " + weirdUrl);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/normalizer/invalidUrls.csv")
    void testBasicNormalizerExceptionCaught(String weirdUrl) {
        assertNull(normalizer.filter(weirdUrl), "normalizing: " + weirdUrl);
    }

    @Test
    public void testRemoveSessionQueryParameters() {
        List<String> invalidParameters = Arrays
            .asList("sid", "phpsessid", "sessionid", "jsessionid");
        normalizer = new BasicURLNormalizer(new TreeSet<>(invalidParameters));
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf&a=1", "http://foo.com/foo.php?a=1");
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf&a=1&b", "http://foo.com/foo.php?a=1&b");
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf", "http://foo.com/foo.php");
    }

    private void normalizeTest(String weird, String normal) {
        assertEquals(normal, normalizer.filter(weird), "normalizing: " + weird);
    }

}
