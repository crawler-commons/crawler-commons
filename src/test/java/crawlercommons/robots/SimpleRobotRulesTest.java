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

package crawlercommons.robots;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import crawlercommons.filters.basic.BasicURLNormalizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleRobotRulesTest {

    @Test
    public void testSerialization() throws Exception {
        SimpleRobotRules expectedRules = new SimpleRobotRules();
        expectedRules.addRule("/images/", true);
        expectedRules.addSitemap("sitemap.xml");

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytes);
        oos.writeObject(expectedRules);
        oos.close();

        final ObjectInputStream iis = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SimpleRobotRules actualRules = (SimpleRobotRules) iis.readObject();

        assertTrue(expectedRules.equals(actualRules));
    }

    @ParameterizedTest
    @CsvSource({ "https://www.example.com/foo/../disallowed/bar.html", //
                    "https://www.example.com////disallowed/bar.html" })
    public void testUrlsNotNormalized(String urlNotNormalized) throws MalformedURLException {
        SimpleRobotRules rules = new SimpleRobotRules();
        rules.addRule("/", true);
        rules.addRule("/disallowed/", false);
        // URL would be disallowed if normalized
        assertTrue(rules.isAllowed(urlNotNormalized));
        BasicURLNormalizer normalizer = new BasicURLNormalizer();
        String urlNormalized = normalizer.filter(urlNotNormalized);
        // URL disallowed if properly normalized
        assertFalse(rules.isAllowed(urlNormalized));
        // base URL (path = "/") is allowed
        String baseURL = new URL(new URL(urlNormalized), "/").toString();
        assertTrue(rules.isAllowed(baseURL));
    }
}
