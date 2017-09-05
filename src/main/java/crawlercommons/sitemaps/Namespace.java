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

/**
 * supported sitemap formats:
 * https://www.sitemaps.org/protocol.html#otherformats
 */
public class Namespace {
    
	public static final String SITEMAP = "http://www.sitemaps.org/schemas/sitemap/0.9";
    
    /** 
     * RSS and Atom sitemap formats do not have strict definition.
     * But if we do not parse as namespace aware, then RSS/Atom files that choose to 
     * use namespaces will break.
     * The relaxed compromise for RSS/Atom is to always parse as "namespace aware", 
     * but we will only match elements by the localName, accepting any element namespace.  
    */
    public static final String RSS_2_0 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ATOM_0_3 = "http://purl.org/atom/ns#";
    public static final String ATOM_1_0 = "http://www.w3.org/2005/Atom";

}

