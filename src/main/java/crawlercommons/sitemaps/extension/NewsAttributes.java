/**
 * Copyright 2018 Crawler-Commons
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
package crawlercommons.sitemaps.extension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data model for Google's extension to the sitemap protocol regarding news
 * indexing, as per <a href=
 * "http://www.google.com/schemas/sitemap-news/0.9">http
 * ://www.google.com/schemas/sitemap-news/0.9</a>.
 */
@SuppressWarnings("serial")
public class NewsAttributes extends ExtensionMetadata {

    /** Name of the news publication in which the article appears. */
    public static final String NAME = "name";
    /** Language of the news publication in which the article appears. */
    public static final String LANGUAGE = "language";
    public static final String GENRES = "genres";
    public static final String PUBLICATION_DATE = "publication_date";
    /** Title of the news article. */
    public static final String TITLE = "title";
    /** Accessibility of the news article. */
    public static final String ACCESS = "access";
    public static final String KEYWORDS = "keywords";
    public static final String STOCK_TICKERS = "stock_tickers";

    public static enum NewsGenre {
        Blog, OpEd, Opinion, PressRelease, Satire, UserGenerated
    }

    public static enum AccessOption {
        Subscription, Registration
    }

    /**
     * News publication name found under news/publication/name (required)
     */
    private String name;

    /**
     * News publication language found under news/publication/language
     * (required)
     */
    private String language;

    /**
     * News genres found under news/genres (required if applicable)
     */
    private NewsGenre[] genres;

    /**
     * News publication date found under news/publication_date (required)
     */
    private ZonedDateTime publicationDate;

    /**
     * News title found under news/title (required)
     */
    private String title;

    /**
     * News keywords found under news/keywords (optional) See <a
     * href="https://support.google.com/news/publisher/answer/116037"
     * >https://support.google.com/news/publisher/answer/116037</a> for examples
     */
    private String[] keywords;

    /**
     * News stock tickers found under news/stock_tickers (optional)
     */
    private String[] stockTickers;

    /** Accessibility of the news article. */
    private AccessOption access;

    public NewsAttributes() {
    }

    public NewsAttributes(final String name, final String language, final ZonedDateTime publicationDate, final String title) {
        this.name = name;
        this.language = language;
        this.publicationDate = publicationDate;
        this.title = title;
    }

    /** @return name of the news publication */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return language of the news publication */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public NewsGenre[] getGenres() {
        return genres;
    }

    public void setGenres(NewsGenre[] genres) {
        this.genres = genres;
    }

    public Date getPublicationDate() {
        if (publicationDate != null) {
            return Date.from(publicationDate.toInstant());
        }
        return null;
    }

    public ZonedDateTime getPublicationDateTime() {
        return publicationDate;
    }

    public void setPublicationDate(ZonedDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String[] getStockTickers() {
        return stockTickers;
    }

    public void setStockTickers(String[] stockTickers) {
        this.stockTickers = stockTickers;
    }

    public AccessOption getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = AccessOption.valueOf(access);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof NewsAttributes)) {
            return false;
        }
        NewsAttributes that = (NewsAttributes) other;
        return Objects.equals(name, that.name) //
                        && Objects.equals(language, that.language) //
                        && Objects.equals(title, that.title) //
                        && Objects.equals(publicationDate, that.publicationDate) //
                        && Objects.equals(access, that.access) //
                        && Objects.deepEquals(keywords, that.keywords) //
                        && Objects.deepEquals(genres, that.genres) //
                        && Objects.deepEquals(stockTickers, that.stockTickers);
    }

    @Override
    public boolean isValid() {
        return name != null && language != null && publicationDate != null && title != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("News name: ").append(name);
        sb.append(", title: ").append(title);
        sb.append(", language: ").append(language);
        sb.append(", publication-date: ").append(publicationDate);
        if (keywords != null) {
            sb.append(", keywords: ").append(String.join(", ", keywords));
        }
        // Arrays.asList(genres).stream().map(NewsGenre::toString);
        if (genres != null) {
            sb.append(", genres: ");
            for (int i = 0; i < genres.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(genres[i].toString());
            }
        }
        if (stockTickers != null) {
            sb.append(", stock_tickers: ").append(String.join(", ", stockTickers));
        }
        if (access != null) {
            sb.append(", access: ").append(access);
        }
        return sb.toString();
    }

    @Override
    public Map<String, String[]> asMap() {
        Map<String, String[]> map = new HashMap<>();

        if (name != null) {
            map.put(NAME, new String[] { name });
        }

        if (title != null) {
            map.put(TITLE, new String[] { title });
        }

        if (language != null) {
            map.put(LANGUAGE, new String[] { language });
        }

        if (publicationDate != null) {
            map.put(PUBLICATION_DATE, new String[] { publicationDate.toString() });
        }

        if (keywords != null) {
            map.put(KEYWORDS, keywords);
        }

        if (genres != null) {
            String[] genresStrArr = Arrays.stream(genres)
                    .map(Enum::name)
                    .toArray(String[]::new);
            map.put(GENRES, genresStrArr);
        }

        if (stockTickers != null) {
            map.put(STOCK_TICKERS, stockTickers);
        }

        return map;
    }
}
