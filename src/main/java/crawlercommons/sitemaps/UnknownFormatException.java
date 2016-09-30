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

@SuppressWarnings("serial")
public class UnknownFormatException extends Exception {

    private final String error;

    /** Default constructor - initializes instance variable to unknown */
    public UnknownFormatException() {
        super();
        error = "unknown";
    }

    /**
     * Constructor receives some kind of message that is saved in an instance
     * variable.
     * 
     * @param err
     *            a String object to use within the Execption
     */
    public UnknownFormatException(String err) {
        super(err);
        error = err;
    }

    /**
     * public method, callable by exception catcher. It returns the error
     * message.
     * 
     * @return a populated Exception as a String
     */
    public String getError() {
        return error;
    }
}