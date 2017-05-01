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
package crawlercommons.sitemaps;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * Data model for Google extension to the sitemap protocol regarding alternate links indexing.
 */
public class LinkAttributes {
    /**
     * Link's href attribute
     */
    private URI href;

    /**
     * Link's other attributes key and values
     */
    private Map<String, String> params;


    private LinkAttributes(){}

    public LinkAttributes(URI href) {
        this.href = href;
    }

    public URI getHref() {
        return href;
    }

    public void setHref(URI href) {
        this.href = href;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof LinkAttributes)) {
            return false;
        }
        LinkAttributes that = (LinkAttributes)other;
        if (!Objects.equals(href, that.href)) {
            return false;
        }
        if (!Objects.equals(params, that.params)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 37;

        result = 31*result + (href == null ? 0 : href.hashCode());
        result = 31*result + (params == null ? 0 : params.hashCode());

        return result;
    }

}
