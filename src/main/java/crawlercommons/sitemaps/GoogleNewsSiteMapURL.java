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

import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * @author Michael Lavelle
 */
public class GoogleNewsSiteMapURL extends SiteMapURL {

  private String publicationName;
  private String publicationLanguage;
  private String title;
  private Date publicationDate;
  private List<String> keywords;
  private List<String> stockTickers;
  private List<String> genres;


  /**
   * @param url
   * @param publicationName 
   * @param publicationLanguage 
   * @param title 
   * @param publicationDate 
   * @param valid
   */
  public GoogleNewsSiteMapURL(String url, String publicationName, String publicationLanguage,
      String title, String publicationDate,
      boolean valid) {
    super(url, valid);
    this.publicationName = publicationName;
    this.publicationLanguage = publicationLanguage;
    this.title = title;
    this.publicationDate = SiteMap.convertToDate(publicationDate);
  }

  /**
   * @param url
   * @param publicationName 
   * @param publicationLanguage 
   * @param title 
   * @param publicationDate 
   * @param lastModified
   * @param changeFreq
   * @param priority
   * @param valid
   */
  public GoogleNewsSiteMapURL(String url, String publicationName, String publicationLanguage,
      String title, String publicationDate, String lastModified, String changeFreq, String priority,
      boolean valid) {
    super(url, lastModified, changeFreq, priority, valid);
    this.publicationName = publicationName;
    this.publicationLanguage = publicationLanguage;
    this.title = title;
    this.publicationDate = SiteMap.convertToDate(publicationDate);
  }

  /**
   * @param url
   * @param publicationName 
   * @param publicationLanguage 
   * @param title 
   * @param publicationDate 
   * @param valid
   */
  public GoogleNewsSiteMapURL(URL url, String publicationName, String publicationLanguage,
      String title, String publicationDate, boolean valid) {
    super(url, valid);
    this.publicationName = publicationName;
    this.publicationLanguage = publicationLanguage;
    this.title = title;
    this.publicationDate = SiteMap.convertToDate(publicationDate);
  }

  /**
   * @param url
   * @param publicationName 
   * @param publicationLanguage 
   * @param title 
   * @param publicationDate 
   * @param lastModified
   * @param changeFreq
   * @param priority
   * @param valid
   */
  public GoogleNewsSiteMapURL(URL url, String publicationName, String publicationLanguage,
      String title, String publicationDate, Date lastModified, ChangeFrequency changeFreq,
      double priority, boolean valid) {
    super(url, lastModified, changeFreq, priority, valid);
    this.publicationName = publicationName;
    this.publicationLanguage = publicationLanguage;
    this.title = title;
    this.publicationDate = SiteMap.convertToDate(publicationDate);
  }

  /**
   * @return the publicationName
   */
  public String getPublicationName() {
    return publicationName;
  }

  /**
   * @return the publicationLanguage
   */
  public String getPublicationLanguage() {
    return publicationLanguage;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the publicationDate
   */
  public Date getPublicationDate() {
    return publicationDate;
  }

  /**
   * @param keywords the keywords to set
   */
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  /**
   * @param stockTickers the stockTickers to set
   */
  public void setStockTickers(List<String> stockTickers) {
    this.stockTickers = stockTickers;
  }

  /**
   * @param genres the genres to set
   */
  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  /**
   * @return the keywords
   */
  public List<String> getKeywords() {
    return keywords;
  }

  /**
   * @return the stockTickers
   */
  public List<String> getStockTickers() {
    return stockTickers;
  }

  /**
   * @return the genres
   */
  public List<String> getGenres() {
    return genres;
  }

}
