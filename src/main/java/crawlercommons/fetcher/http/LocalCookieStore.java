/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package crawlercommons.fetcher.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;

/**
 * Default implementation of {@link CookieStore} Initially copied from
 * HttpComponents Changes: removed synchronization
 * 
 * @deprecated As of release 0.6. We recommend directly using Apache HttpClient, 
 * async-http-client, or any other robust, industrial-strength HTTP clients.
 */
@Deprecated
@NotThreadSafe
public class LocalCookieStore implements CookieStore, Serializable {

    private static final long serialVersionUID = -7581093305228232025L;

    private final TreeSet<Cookie> cookies;

    public LocalCookieStore() {
        super();
        this.cookies = new TreeSet<Cookie>(new CookieIdentityComparator());
    }

    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent
     * cookies. If the given ookie has already expired it will not be added, but
     * existing values will still be removed.
     * 
     * @param cookie
     *            the {@link Cookie cookie} to be added
     * 
     * @see #addCookies(Cookie[])
     * 
     */
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
            cookies.remove(cookie);
            if (!cookie.isExpired(new Date())) {
                cookies.add(cookie);
            }
        }
    }

    /**
     * Adds an array of {@link Cookie HTTP cookies}. Cookies are added
     * individually and in the given array order. If any of the given cookies
     * has already expired it will not be added, but existing values will still
     * be removed.
     * 
     * @param cookies
     *            the {@link Cookie cookies} to be added
     * 
     * @see #addCookie(Cookie)
     * 
     */
    public void addCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cooky : cookies) {
                this.addCookie(cooky);
            }
        }
    }

    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP state
     * currently contains.
     * 
     * @return an array of {@link Cookie cookies}.
     */
    public List<Cookie> getCookies() {
        // create defensive copy so it won't be concurrently modified
        return new ArrayList<Cookie>(cookies);
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state that have
     * expired by the specified {@link java.util.Date date}.
     * 
     * @return true if any cookies were purged.
     * 
     * @see Cookie#isExpired(Date)
     */
    public boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
            if (it.next().isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Clears all cookies.
     */
    public void clear() {
        cookies.clear();
    }

    @Override
    public String toString() {
        return cookies.toString();
    }

}
