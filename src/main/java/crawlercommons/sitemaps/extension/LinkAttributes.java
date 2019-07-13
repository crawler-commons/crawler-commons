/**
 * Copyright 2018 Crawler-Commons
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

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Data model for Google extension to the sitemap protocol regarding alternate
 * links indexing. Cf. https://support.google.com/webmasters/answer/189077:
 * <blockquote>Each <code>&lt;url&gt;</code> element must have a child element:
 * <code>&lt;xhtml:link rel="alternate" hreflang="supported_language-code"&gt;</code>
 * that lists every alternate version of the page, including itself. The order
 * of these child <code>&lt;xhtml:link&gt;</code> elements doesn't matter,
 * though you might want to keep them in the same order to make them easier for
 * you to check for mistakes.</blockquote>
 */
public class LinkAttributes extends ExtensionMetadata {
    /**
     * Link's href attribute
     */
    private URL href;

    /**
     * Link's other attributes key and values
     */
    private Map<String, String> params;

    public LinkAttributes() {
    }

    public LinkAttributes(URL href) {
        this.href = href;
    }

    public URL getHref() {
        return href;
    }

    public void setHref(URL href) {
        this.href = href;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Links href: ").append(href);
        if (params != null && !params.isEmpty()) {
            sb.append(", params: ");
            boolean first = true;
            for (Entry<String, String> e : params.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(e.getKey()).append(':').append(e.getValue());
                first = false;
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof LinkAttributes)) {
            return false;
        }
        LinkAttributes that = (LinkAttributes) other;
        return Objects.equals(href, that.href) //
                        && Objects.equals(params, that.params);
    }

}
