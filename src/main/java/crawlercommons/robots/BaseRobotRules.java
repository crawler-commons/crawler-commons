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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Result from parsing a single robots.txt file - which means we get a set of
 * rules, and a crawl-delay.
 */
@SuppressWarnings("serial")
public abstract class BaseRobotRules implements Serializable {

    public static final long UNSET_CRAWL_DELAY = Long.MIN_VALUE;

    public abstract boolean isAllowed(String url);

    public abstract boolean isAllowAll();

    public abstract boolean isAllowNone();

    private long _crawlDelay = UNSET_CRAWL_DELAY;
    private boolean _deferVisits = false;
    private LinkedHashSet<String> _sitemaps;

    public BaseRobotRules() {
        _sitemaps = new LinkedHashSet<>();
    }

    public long getCrawlDelay() {
        return _crawlDelay;
    }

    public void setCrawlDelay(long crawlDelay) {
        _crawlDelay = crawlDelay;
    }

    public boolean isDeferVisits() {
        return _deferVisits;
    }

    public void setDeferVisits(boolean deferVisits) {
        _deferVisits = deferVisits;
    }

    /** Add sitemap URL to rules if not a duplicate */
    public void addSitemap(String sitemap) {
        _sitemaps.add(sitemap);
    }

    /** Get URLs of sitemap links found in robots.txt */
    public List<String> getSitemaps() {
        return new ArrayList<>(_sitemaps);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (_crawlDelay ^ (_crawlDelay >>> 32));
        result = prime * result + (_deferVisits ? 1231 : 1237);
        result = prime * result + ((_sitemaps == null) ? 0 : _sitemaps.hashCode());
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
        BaseRobotRules other = (BaseRobotRules) obj;
        if (_crawlDelay != other._crawlDelay)
            return false;
        if (_deferVisits != other._deferVisits)
            return false;
        if (_sitemaps == null) {
            if (other._sitemaps != null)
                return false;
        } else if (!_sitemaps.equals(other._sitemaps))
            return false;
        return true;
    }

    /**
     * Returns a string with the crawl delay as well as a list of sitemaps if
     * they exist (and aren't more than 10)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass()).append(":\n");
        long delay = getCrawlDelay();
        if (delay == UNSET_CRAWL_DELAY) {
            sb.append(" - no crawl delay\n");
        } else {
            sb.append(" - crawl delay: ").append(delay).append('\n');
        }

        List<String> sitemaps = getSitemaps();
        int nSitemaps = sitemaps.size();
        if (nSitemaps == 0) {
            sb.append(" - no sitemap URLs\n");
        } else {
            sb.append(" - number of sitemap URLs: ").append(nSitemaps).append('\n');
            int numOfSitemapsToShow = Math.min(nSitemaps, 10);
            for (int i = 0; i < numOfSitemapsToShow; i++) {
                sb.append(sitemaps.get(i)).append("\n");
            }
        }

        return sb.toString();
    }
}
