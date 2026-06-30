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

package crawlercommons.robots;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Container for data collected from a robots.txt extension directive.
 *
 * <p>
 * The default implementation ({@link SimpleRobotsExtensionData}) stores raw
 * string values as they appear in the robots.txt file. Consumers that need
 * richer typed data for specific extensions (e.g., parsing structured
 * Content-Signals fields) can provide their own implementations.
 * </p>
 *
 * @see RobotsExtension
 * @see BaseRobotRules#getExtensionData(RobotsExtension)
 */
public interface RobotsExtensionData extends Serializable {

    /**
     * @return the extension this data belongs to
     */
    RobotsExtension getExtension();

    /**
     * @return the raw string values collected for this extension directive,
     *         one entry per occurrence in the robots.txt file
     */
    List<String> getValues();

    /**
     * @return a map representation of this extension data, suitable for generic
     *         serialization. The map key is the directive name, and the value is
     *         an array of the raw string values.
     */
    Map<String, String[]> asMap();
}
