/**
 * Copyright 2023 Crawler-Commons
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
package crawlercommons.sitemaps.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Data model for the <a href=
 * "https://support.google.com/programmable-search/answer/1628213">PageMaps</a>
 * extension to the sitemap protocol used for Google's Programmable Search
 * Engine.
 * 
 * A PageMap holds a list of {@link PageMapDataObject}s, each PageMapDataObject
 * a map of attributes (pairs of name and value).
 */
@SuppressWarnings("serial")
public class PageMap extends ExtensionMetadata {

    private List<PageMapDataObject> dataObjects = new ArrayList<>();

    public List<PageMapDataObject> getPageMapDataObjects() {
        return dataObjects;
    }

    public void addDataObject(PageMapDataObject d) {
        dataObjects.add(d);
    }

    @Override
    public Map<String, String[]> asMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        for (PageMapDataObject dobj : dataObjects) {
            for (Entry<String, String[]> e : dobj.asMap().entrySet()) {
                map.put(e.getKey(), e.getValue());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PageMap: [");
        if (!dataObjects.isEmpty()) {
            sb.append('\n');
        }
        for (PageMapDataObject dobj : dataObjects) {
            sb.append(dobj.toString()).append(",\n");
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataObjects);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PageMap other = (PageMap) obj;
        return Objects.equals(dataObjects, other.dataObjects);
    }

}
