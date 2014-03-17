/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.fetcher.file;

import java.io.File;
import java.net.URI;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.junit.Test;

import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.HttpFetchException;


public class SimpleFileFetcherTest {

    @Test
    public void testSimpleFetching() throws Exception {
        URI uri = new File("src/test/resources/text-file.txt").toURI();
        String url = uri.toURL().toExternalForm();
        
        SimpleFileFetcher fetcher = new SimpleFileFetcher();
        FetchedResult result = fetcher.get(url);
        Assert.assertEquals(0, result.getNumRedirects());
        Assert.assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        
        String fetchedContent = new String(result.getContent(), "us-ascii");
        Assert.assertEquals("Now is the time for all good men to come to the aid of their country.", fetchedContent);
    }

    @Test
    public void testMissingFile() throws Exception {
        String url = new File("src/main/resources/bogus-name").toURI().toURL().toExternalForm();
        
        SimpleFileFetcher fetcher = new SimpleFileFetcher();
        
        try {
            fetcher.get(url);
            Assert.fail("No exception throw");
        } catch (HttpFetchException e) {
            // valid
        }
    }
    
    @Test
    public void testEmptyFile() throws Exception {
        URI uri = new File("src/test/resources/empty-file").toURI();
        String url = uri.toURL().toExternalForm();
        
        SimpleFileFetcher fetcher = new SimpleFileFetcher();
        FetchedResult result = fetcher.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        Assert.assertEquals(0, result.getContentLength());
        Assert.assertEquals(0, result.getResponseRate());
    }
}
