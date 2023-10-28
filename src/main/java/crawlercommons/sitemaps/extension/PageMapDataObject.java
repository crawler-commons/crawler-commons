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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class PageMapDataObject extends ExtensionMetadata {
    private String type;
    private String id;
    private Map<String, String> attributes;

    public PageMapDataObject(String type, String id) {
        this.type = type;
        this.id = id;
        attributes = new LinkedHashMap<>();
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public String addAttribute(String name, String value) {
        return attributes.put(name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PageMapDataObject other = (PageMapDataObject) obj;
        return Objects.equals(attributes, other.attributes) && Objects.equals(id, other.id) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{type = ").append(type);
        sb.append(", id = ").append(id);
        sb.append(", attributes = ").append(attributes);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Map<String, String[]> asMap() {
        String keyFormat = "%s::%s";
        String valueFormat = "%s: %s";
        String key = String.format(Locale.ROOT, keyFormat, (getType() == null ? "" : getType()), (getId() == null ? "" : getId()));
        String[] values = getAttributes().entrySet().stream().map((Entry<String, String> e) -> String.format(Locale.ROOT, valueFormat, e.getKey(), e.getValue())).toArray(String[]::new);
        return Map.of(key, values);
    }
}
