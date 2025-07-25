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

import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Data model for Google extension to the sitemap protocol regarding images
 * indexing, as per http://www.google.com/schemas/sitemap-video/1.1
 */
@SuppressWarnings("serial")
public class VideoAttributes extends ExtensionMetadata {

    public static final String THUMBNAIL_LOC = "thumbnail_loc";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CONTENT_LOC = "content_loc";
    public static final String PLAYER_LOC = "player_loc";
    public static final String EXPIRATION_DATE = "expiration_date";
    public static final String RATING = "rating";
    public static final String VIEW_COUNT = "view_count";
    public static final String PUBLICATION_DATE = "publication_date";
    public static final String FAMILY_FRIENDLY = "family_friendly";
    public static final String TAGS = "tags";
    public static final String CATEGORY = "category";
    public static final String RESTRICTED_COUNTRIES = "restricted_countries";
    public static final String ALLOWED_COUNTRIES = "allowed_countries";
    public static final String GALLERY_LOC = "gallery_loc";
    public static final String GALLERY_TITLE = "gallery_title";
    public static final String PRICES = "prices";
    public static final String REQUIRES_SUBSCRIPTION = "requires_subscription";
    public static final String UPLOADER = "uploader";
    public static final String UPLOADER_INFO = "uploader_info";
    public static final String ALLOWED_PLATFORMS = "allowed_platforms";
    public static final String RESTRICTED_PLATFORMS = "restricted_platforms";
    public static final String IS_LIVE = "is_live";
    public static final String DURATION = "duration";

    /**
     * Video thumbnail URL found under video/thumbnail_loc (required)
     */
    private URL thumbnailLoc;

    /**
     * Video title found under video/title (required)
     */
    private String title;

    /**
     * Video description found under video/description (required)
     */
    private String description;

    /**
     * Video content location found under video/content_loc (depends) if not
     * specified, player location must be specified
     */
    private URL contentLoc;

    /**
     * Video player location found under video/player_loc (depends) if not
     * specified, content location must be specified
     */
    private URL playerLoc;

    /**
     * Video duration in seconds found under video/duration (recommended) Must
     * be integer between 0 and 28800 (8 hours)
     */
    private Integer duration;

    /**
     * Video expiration date found under video/expiration_date (recommended if
     * applicable)
     */
    private ZonedDateTime expirationDate;

    /**
     * Video rating found under video/rating (optional) Must be float value
     * between 0.0 and 5.0
     */
    private Float rating;

    /**
     * Video view count found under video/view_count (optional)
     */
    private Integer viewCount;

    /**
     * Video publication date found under video/publication_date (optional)
     */
    private ZonedDateTime publicationDate;

    /**
     * Video family friendly attribute found under video/family_friendly
     * (optional)
     */
    private Boolean familyFriendly;

    /**
     * Video tags found under video/tag (optional) Up to 32 tags can be
     * specified
     */
    private String[] tags;

    /**
     * Video category found under video/category (optional)
     */
    private String category;

    /**
     * Video restricted countries found under video/restriction (optional)
     * blacklist of countries filled if video/restriction node has an attribute
     * named relationship with a value of deny.
     */
    private String[] restrictedCountries;

    /**
     * Video allowed countries found under video/restriction (optional)
     * whitelist of countries filled if video/restriction node has an attribute
     * named relationship with a value of allow.
     */
    private String[] allowedCountries;

    /**
     * Video gallery location found under video/gallery_loc (optional)
     */
    private URL galleryLoc;

    /**
     * Video gallery title found under video/gallery_loc[@title] (optional)
     */
    private String galleryTitle;

    /**
     * Video prices found under video/price (optional)
     */
    private VideoPrice[] prices;

    /**
     * Video requires subscription (free or paid) found under
     * video/requires_subscription (optional)
     */
    private Boolean requiresSubscription;

    /**
     * Video uploader found under video/uploader (optional)
     */
    private String uploader;

    /**
     * Video uploader location (optional) Must be on the same domain as the
     * &lt;loc&gt; this property refers to
     */
    private URL uploaderInfo;

    /**
     * Video restricted platforms found under video/platform (optional)
     * blacklist of platform filled if video/platform node has an attribute
     * named relationship with a value of deny.
     */
    private String[] restrictedPlatforms;

    /**
     * Video allowed platforms found under video/platform (optional) whitelist
     * of platforms filled if video/platform node has an attribute named
     * relationship with a value of allow.
     */
    private String[] allowedPlatforms;

    /**
     * Video is a live stream found under video/live (optional)
     */
    private Boolean isLive;

    /** If video is published as a series of raw videos */
    private ContentSegment[] contentSegments;

    /** Information about a single TV video */
    private TVShow tvShow;

    public URL getThumbnailLoc() {
        return thumbnailLoc;
    }

    public void setThumbnailLoc(URL thumbnailLoc) {
        this.thumbnailLoc = thumbnailLoc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getContentLoc() {
        return contentLoc;
    }

    public void setContentLoc(URL contentLoc) {
        this.contentLoc = contentLoc;
    }

    public URL getPlayerLoc() {
        return playerLoc;
    }

    public void setPlayerLoc(URL playerLoc) {
        this.playerLoc = playerLoc;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getExpirationDate() {
        if (expirationDate != null) {
            return Date.from(expirationDate.toInstant());
        }
        return null;
    }

    public ZonedDateTime getExpirationDateTime() {
        return expirationDate;
    }

    public void setExpirationDate(ZonedDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

    public Boolean getFamilyFriendly() {
        return familyFriendly;
    }

    public void setFamilyFriendly(Boolean familyFriendly) {
        this.familyFriendly = familyFriendly;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (tag == null) {
            return;
        }
        String[] arr;
        if (tags == null) {
            arr = new String[1];
            arr[0] = tag;
        } else {
            arr = Arrays.copyOf(tags, tags.length + 1);
            arr[tags.length] = tag;
        }
        tags = arr;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getRestrictedCountries() {
        return restrictedCountries;
    }

    public void setRestrictedCountries(String[] restrictedCountries) {
        this.restrictedCountries = restrictedCountries;
    }

    public String[] getAllowedCountries() {
        return allowedCountries;
    }

    public void setAllowedCountries(String[] allowedCountries) {
        this.allowedCountries = allowedCountries;
    }

    public URL getGalleryLoc() {
        return galleryLoc;
    }

    public void setGalleryLoc(URL galleryLoc) {
        this.galleryLoc = galleryLoc;
    }

    public String getGalleryTitle() {
        return galleryTitle;
    }

    public void setGalleryTitle(String galleryTitle) {
        this.galleryTitle = galleryTitle;
    }

    public VideoPrice[] getPrices() {
        return prices;
    }

    public void setPrices(VideoPrice[] prices) {
        this.prices = prices;
    }

    public void addPrice(VideoPrice price) {
        if (price == null) {
            return;
        }
        VideoPrice[] arr;
        if (prices == null) {
            arr = new VideoPrice[1];
            arr[0] = price;
        } else {
            arr = Arrays.copyOf(prices, prices.length + 1);
            arr[prices.length] = price;
        }
        prices = arr;
    }

    public Boolean getRequiresSubscription() {
        return requiresSubscription;
    }

    public void setRequiresSubscription(Boolean requiresSubscription) {
        this.requiresSubscription = requiresSubscription;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public URL getUploaderInfo() {
        return uploaderInfo;
    }

    public void setUploaderInfo(URL uploaderInfo) {
        this.uploaderInfo = uploaderInfo;
    }

    public String[] getRestrictedPlatforms() {
        return restrictedPlatforms;
    }

    public void setRestrictedPlatforms(String[] restrictedPlatforms) {
        this.restrictedPlatforms = restrictedPlatforms;
    }

    public String[] getAllowedPlatforms() {
        return allowedPlatforms;
    }

    public void setAllowedPlatforms(String[] allowedPlatforms) {
        this.allowedPlatforms = allowedPlatforms;
    }

    public Boolean getLive() {
        return isLive;
    }

    public void setLive(Boolean live) {
        isLive = live;
    }

    public enum VideoPriceType {
        own, rent
    }

    public enum VideoPriceResolution {
        SD, HD
    }

    public static final class VideoPrice implements Serializable {
        /**
         * Video price currency found under video/price[@currency] (required)
         */
        private final String currency;

        /**
         * Video price type (rent vs own) found under video/price[@type]
         * (optional, defaults to own)
         */
        private final VideoPriceType type;

        /**
         * Video price resolution found under video/price[@resolution]
         */
        private final VideoPriceResolution resolution;

        /**
         * Video price
         */
        private Float price;

        public VideoPrice(String currency, Float price) {
            this(currency, price, VideoPriceType.own);
        }

        public VideoPrice(String currency, Float price, VideoPriceType type) {
            this(currency, price, type, null);
        }

        public VideoPrice(String currency, Float price, VideoPriceType type, VideoPriceResolution resolution) {
            this.currency = currency;
            this.price = price;
            this.type = type;
            this.resolution = resolution;
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "value: %.2f, currency: %s, type: %s, resolution: %s", price, currency, type, resolution);
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof VideoPrice)) {
                return false;
            }
            VideoPrice that = (VideoPrice) other;
            return Objects.equals(currency, that.currency) //
                            && Objects.equals(price, that.price) //
                            && type == that.type //
                            && Objects.equals(resolution, that.resolution);
        }

        public String getCurrency() {
            return currency;
        }

        public VideoPriceType getType() {
            return type;
        }

        public VideoPriceResolution getResolution() {
            return resolution;
        }

        public Float getPrice() {
            return price;
        }

        public void setPrice(Float price) {
            this.price = price;
        }

    }

    /**
     * <blockquote>
     * <p>
     * Use &lt;video:content_segment_loc&gt; only in conjunction with
     * &lt;video:player_loc&gt;.
     * </p>
     * 
     * <p>
     * If you publish your video as a series of raw videos (for example, if you
     * submit a full movie as a continuous series of shorter clips), you can use
     * the &lt;video:content_segment_loc&gt; to supply us with a series of URLs,
     * in the order in which they should be concatenated to recreate the video
     * in its entirety. Each URL should point to a .mpg, .mpeg, .mp4, .m4v,
     * .mov, .wmv, .asf, .avi, .ra, .ram, .rm, .flv, or other video file format.
     * It should not point to any Flash content.
     * </p>
     * </blockquote>
     * 
     * (Source: <a href=
     * "https://www.google.com/schemas/sitemap-video/1.1/sitemap-video.xsd"
     * >sitemap-video.xsd</a>)
     */
    public static final class ContentSegment implements Serializable {
        private Integer duration;
        private URL loc;

        public ContentSegment(Integer duration, URL loc) {
            this.duration = duration;
            this.loc = loc;
        }

        public Integer getDuration() {
            return duration;
        }

        public URL getLocation() {
            return loc;
        }

        @Override
        public String toString() {
            return "segment: " + loc.toString() + " (" + duration + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof ContentSegment)) {
                return false;
            }
            return urlEquals(loc, ((ContentSegment) other).loc) && Objects.equals(duration, ((ContentSegment) other).duration);
        }
    }

    public ContentSegment[] getContentSegmentLocs() {
        return contentSegments;
    }

    public void setContentSegmentLocs(ContentSegment[] segments) {
        contentSegments = segments;
    }

    public void addContentSegment(Integer duration, URL loc) {
        ContentSegment segment = new ContentSegment(duration, loc);
        if (contentSegments == null) {
            contentSegments = new ContentSegment[1];
            contentSegments[0] = segment;
        } else {
            ContentSegment[] arr = Arrays.copyOf(contentSegments, contentSegments.length + 1);
            arr[contentSegments.length] = segment;
            contentSegments = arr;
        }
    }

    public static final class TVShow implements Serializable {
        public enum VideoType {
            full, preview, clip, interview, news, other;
        }

        private String showTitle;
        private VideoType videoType;
        private String episodeTitle;
        private Integer seasonNumber;
        private Integer episodeNumber;
        private ZonedDateTime premierDate;

        public String getShowTitle() {
            return showTitle;
        }

        public void setShowTitle(String title) {
            showTitle = title;
        }

        public VideoType getVideoType() {
            return videoType;
        }

        public void setVideoType(VideoType videoType) {
            this.videoType = videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = VideoType.valueOf(videoType);
        }

        public String getEpisodeTitle() {
            return episodeTitle;
        }

        public void setEpisodeTitle(String title) {
            episodeTitle = title;
        }

        public Integer getSeasonNumber() {
            return seasonNumber;
        }

        public void setSeasonNumber(Integer num) {
            this.seasonNumber = num;
        }

        public Integer getEpisodeNumber() {
            return episodeNumber;
        }

        public void setEpisodeNumber(Integer num) {
            this.episodeNumber = num;
        }

        public ZonedDateTime getPremierDate() {
            return premierDate;
        }

        public void setPremierDate(ZonedDateTime premierDate) {
            this.premierDate = premierDate;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof TVShow)) {
                return false;
            }
            TVShow o = (TVShow) other;
            return showTitle.equals(o.showTitle) //
                            && videoType.equals(o.videoType) //
                            && episodeTitle.equals(o.episodeTitle) //
                            && seasonNumber.equals(o.seasonNumber) //
                            && episodeNumber.equals(o.episodeNumber) //
                            && premierDate.equals(o.premierDate);
        }
    }

    public TVShow getTVShow() {
        return tvShow;
    }

    public void setTVShow(TVShow show) {
        tvShow = show;
    }

    public VideoAttributes() {
    }

    public VideoAttributes(URL thumbnailLoc, String title, String description, URL contentLoc, URL playerLoc) {
        this.thumbnailLoc = thumbnailLoc;
        this.title = title;
        this.description = description;
        this.contentLoc = contentLoc;
        this.playerLoc = playerLoc;
    }

    @Override
    public String toString() {
        return new StringBuilder("Video title: ").append(title) //
                        .append(", description: ").append(description) //
                        .append(", thumbnail: ").append(thumbnailLoc) //
                        .append(", contentLoc: ").append(contentLoc) //
                        .append(", playerLoc: ").append(playerLoc) //
                        .append(", prices: ").append(Arrays.toString(prices)) //
                        .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof VideoAttributes)) {
            return false;
        }
        VideoAttributes that = (VideoAttributes) other;
        return urlEquals(thumbnailLoc, that.thumbnailLoc) //
                        && Objects.equals(title, that.title) //
                        && Objects.equals(description, that.description) //
                        && urlEquals(contentLoc, that.contentLoc) //
                        && urlEquals(playerLoc, that.playerLoc) //
                        && Objects.equals(duration, that.duration) //
                        && Objects.equals(expirationDate, that.expirationDate) //
                        && Objects.equals(rating, that.rating) //
                        && Objects.equals(viewCount, that.viewCount) //
                        && Objects.equals(publicationDate, that.publicationDate) //
                        && Objects.equals(familyFriendly, that.familyFriendly) //
                        && Objects.deepEquals(tags, that.tags) //
                        && Objects.equals(category, that.category) //
                        && Objects.deepEquals(restrictedCountries, that.restrictedCountries) //
                        && Objects.deepEquals(allowedCountries, that.allowedCountries) //
                        && urlEquals(galleryLoc, that.galleryLoc) //
                        && Objects.equals(galleryTitle, that.galleryTitle) //
                        && Objects.deepEquals(prices, that.prices) //
                        && Objects.equals(requiresSubscription, that.requiresSubscription) //
                        && Objects.equals(uploader, that.uploader) //
                        && Objects.equals(uploaderInfo, that.uploaderInfo) //
                        && Objects.deepEquals(allowedPlatforms, that.allowedPlatforms) //
                        && Objects.deepEquals(restrictedPlatforms, that.restrictedPlatforms) //
                        && Objects.equals(isLive, that.isLive) //
                        && Objects.deepEquals(contentSegments, that.contentSegments) //
                        && Objects.equals(tvShow, that.tvShow);
    }

    @Override
    public boolean isValid() {
        return thumbnailLoc != null && title != null && title.length() <= 100 && description != null && description.length() <= 2048 && (contentLoc != null || playerLoc != null);
    }

    @Override
    public Map<String, String[]> asMap() {
        Map<String, String[]> map = new HashMap<>();

        if (thumbnailLoc != null) {
            map.put(THUMBNAIL_LOC, new String[] { thumbnailLoc.toString() });
        }

        if (title != null) {
            map.put(TITLE, new String[] { title });
        }

        if (description != null) {
            map.put(DESCRIPTION, new String[] { description });
        }

        if (contentLoc != null) {
            map.put(CONTENT_LOC, new String[] { contentLoc.toString() });
        }

        if (playerLoc != null) {
            map.put(PLAYER_LOC, new String[] { playerLoc.toString() });
        }

        if (expirationDate != null) {
            map.put(EXPIRATION_DATE, new String[] { expirationDate.toString() });
        }

        if (rating != null) {
            map.put(RATING, new String[] { rating.toString() });
        }

        if (viewCount != null) {
            map.put(VIEW_COUNT, new String[] { viewCount.toString() });
        }

        if (publicationDate != null) {
            map.put(PUBLICATION_DATE, new String[] { publicationDate.toString() });
        }

        if (familyFriendly != null) {
            map.put(FAMILY_FRIENDLY, new String[] { familyFriendly.toString() });
        }

        if (tags != null) {
            map.put(TAGS, tags);
        }

        if (category != null) {
            map.put(CATEGORY, new String[] { category });
        }

        if (restrictedCountries != null) {
            map.put(RESTRICTED_COUNTRIES, restrictedCountries);
        }

        if (allowedCountries != null) {
            map.put(ALLOWED_COUNTRIES, allowedCountries);
        }

        if (galleryLoc != null) {
            map.put(GALLERY_LOC, new String[]{ galleryLoc.toString() });
        }

        if (galleryTitle != null) {
            map.put(GALLERY_TITLE, new String[]{ galleryTitle });
        }

        if (prices != null) {
            String[] videoPricesArr = Arrays.stream(prices)
                    .map(VideoPrice::toString)
                    .toArray(String[]::new);
            map.put(PRICES, videoPricesArr);
        }

        if (requiresSubscription != null) {
            map.put(REQUIRES_SUBSCRIPTION, new String[]{ requiresSubscription.toString() });
        }

        if (uploader != null) {
            map.put(UPLOADER, new String[]{ uploader });
        }

        if (uploaderInfo != null) {
            map.put(UPLOADER_INFO, new String[]{ uploaderInfo.toString() });
        }

        if (allowedPlatforms != null) {
            map.put(ALLOWED_PLATFORMS, allowedPlatforms);
        }

        if (restrictedPlatforms != null) {
            map.put(RESTRICTED_PLATFORMS, restrictedPlatforms);
        }

        if (isLive != null) {
            map.put(IS_LIVE, new String[]{ isLive.toString() });
        }

        if (duration != null) {
            map.put(DURATION, new String[]{ duration.toString() });
        }
        return Collections.unmodifiableMap(map);
    }
}
