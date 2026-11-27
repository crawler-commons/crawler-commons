/*
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

package crawlercommons.robots_tag;

import java.util.Objects;

/**
 * A parsed value and the remainder of the string it was parsed from (i.e. the
 * original string that the value was parsed from, but without the parsed
 * value).
 */
public final class ParserResult<T> {
    private final T value;
    private final String remainder;

    public ParserResult(T value, String remainder) {
        this.value = value;
        this.remainder = remainder;
    }

    public T getValue() {
        return value;
    }

    public String getRemainder() {
        return remainder;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParserResult))
            return false;
        ParserResult<?> other = (ParserResult<?>) object;
        return Objects.equals(value, other.value) && Objects.equals(remainder, other.remainder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, remainder);
    }

    @Override
    public String toString() {
        return "ParserResult(" + value + ", \"" + remainder + "\")";
    }
}
