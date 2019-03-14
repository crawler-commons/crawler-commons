/**
 * Copyright 2019 Crawler-Commons
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;

/**
 * Wraps a stream and skips over leading whitespace (at beginning of file) in
 * the wrapped stream. The wrapped stream must support to mark the current
 * position (see {@link InputStream#mark(int)}.
 * 
 * Only ASCII whitespace (space/U+0020, <code>\t</code>/U+0009, <code>\n</code>
 * /U+000A, vertical tab/U+000b, <code>\r</code>/U+000D) is skipped.
 */
public class SkipLeadingWhiteSpaceInputStream extends ProxyInputStream {

    private boolean inLeadingWhiteSpace = true;

    public SkipLeadingWhiteSpaceInputStream(InputStream proxy) {
        super(proxy);
        inLeadingWhiteSpace = proxy.markSupported();
    }

    protected void beforeRead(final int n) throws IOException {
        while (inLeadingWhiteSpace) {
            in.mark(1);
            int c = in.read();
            if (c == ' ' || c == '\t' || c == '\n' || c == 0x0b || c == '\r') {
                continue;
            } else {
                in.reset();
                inLeadingWhiteSpace = false;
            }
        }
    }
}