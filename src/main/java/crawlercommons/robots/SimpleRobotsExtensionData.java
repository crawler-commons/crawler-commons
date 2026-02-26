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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link RobotsExtensionData} that stores raw string
 * values as they appear in the robots.txt file.
 *
 * <p>
 * Each occurrence of an extension directive in the robots.txt file adds one
 * value to the list. This is compact: a single {@link ArrayList} per extension
 * per robots.txt.
 * </p>
 */
@SuppressWarnings("serial")
public class SimpleRobotsExtensionData implements RobotsExtensionData {

    private final RobotsExtension _extension;
    private final List<String> _values;

    public SimpleRobotsExtensionData(RobotsExtension extension) {
        _extension = extension;
        _values = new ArrayList<>();
    }

    @Override
    public RobotsExtension getExtension() {
        return _extension;
    }

    @Override
    public List<String> getValues() {
        return Collections.unmodifiableList(_values);
    }

    /**
     * Add a value collected from a robots.txt directive line.
     *
     * @param value
     *            the directive value (text after the colon)
     */
    public void addValue(String value) {
        _values.add(value);
    }

    /** Clear all stored values. */
    public void clearValues() {
        _values.clear();
    }

    @Override
    public Map<String, String[]> asMap() {
        return Map.of(_extension.getDirectiveName(), _values.toArray(new String[0]));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _extension.hashCode();
        result = prime * result + _values.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleRobotsExtensionData other = (SimpleRobotsExtensionData) obj;
        if (_extension != other._extension)
            return false;
        return _values.equals(other._values);
    }

    @Override
    public String toString() {
        return _extension.getDirectiveName() + ": " + _values;
    }
}
