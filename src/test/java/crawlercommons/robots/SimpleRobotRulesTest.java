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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
}
