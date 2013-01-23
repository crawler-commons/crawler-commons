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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;

import crawlercommons.fetcher.BadProtocolFetchException;
import crawlercommons.fetcher.BaseFetchException;
import crawlercommons.fetcher.BaseFetcher;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.HttpFetchException;
import crawlercommons.fetcher.IOFetchException;
import crawlercommons.fetcher.Payload;
import crawlercommons.fetcher.UrlFetchException;

@SuppressWarnings("serial")
public class SimpleFileFetcher extends BaseFetcher {

    /* (non-Javadoc)
     * @see crawlercommons.fetcher.BaseFetcher#get(java.lang.String, crawlercommons.fetcher.Payload)
     */
    @Override
    public FetchedResult get(String url, Payload payload) throws BaseFetchException {
        String path = null;
        
            try {
                URL realUrl = new URL(url);
                if (!realUrl.getProtocol().equals("file")) {
                    throw new BadProtocolFetchException(url);
                }
                
                path = realUrl.getPath();
                if (path.length() == 0) {
                    path = "/";
                }
            } catch (MalformedURLException e) {
                throw new UrlFetchException(url, e.getMessage());
            }
            
        File f = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            long startTime = System.currentTimeMillis();
            
            // TODO - limit to be no more than maxContentSize. We should read in up to 16K,
            // then call Tika to detect the content type and use that to do mime-type based
            // max size.
            
            // TODO - see Nutch's File protocol for doing a better job of mapping file
            // errors (e.g. file not found) to HttpBaseException such as 404.
            
            // TODO - see Nutch's File protocol for handling directories - return as HTML with links
            
            // TODO - see Nutch's File protocol for handling symlinks as redirects. We'd want
            // to then enforce max redirects, which means moving the redirect support back into
            // the BaseFetcher class.
            
            byte[] content = IOUtils.toByteArray(fis);
            long stopTime = System.currentTimeMillis();
            long totalReadTime = Math.max(1, stopTime - startTime);
            long responseRate = (content.length * 1000L) / totalReadTime;
            String contentType = "application/octet-stream";
            return new FetchedResult(url, url, System.currentTimeMillis(), new Metadata(), content, contentType, 
                    (int)responseRate, payload, url, 0, "localhost", HttpStatus.SC_OK, null);
        } catch (FileNotFoundException e) {
            throw new HttpFetchException(url, "Error fetching " + url, HttpStatus.SC_NOT_FOUND, new Metadata());
        } catch (IOException e) {
            throw new IOFetchException(url, e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    @Override
    public void abort() {
        // Do nothing, as our requests are all synchronous
    }

}
