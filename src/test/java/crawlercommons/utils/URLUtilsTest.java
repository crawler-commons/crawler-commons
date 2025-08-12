/**
 * Copyright 2025 Crawler-Commons
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

package crawlercommons.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class URLUtilsTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/url-utils/url-resolve.csv")
    void testResolveURLs(String spec, String base, String expected) throws MalformedURLException, URISyntaxException {
        URL baseUrl = null;
        URI baseUri = null;
        if (!base.isBlank()) {
            baseUrl = new URL(base);
            assertNotNull(baseUrl, "Failed to instantiate base URL");
            baseUri = new URI(base);
            assertNotNull(baseUri, "Failed to instantiate base URI");
        }
        // TODO: remove the following line (for comparison only)
        assertEquals(expected, new URL(baseUrl, spec).toString());
        URL resolvedUrl = URLUtils.resolve(baseUrl, spec);
        assertNotNull(resolvedUrl);
        assertEquals(expected, resolvedUrl.toString());
        // TODO: test URLUtils.resolve(baseUri, spec) ?
    }
}
