package crawlercommons.robots_tag;

import crawlercommons.robots_tag.parsers.IntDirectiveParser;
import crawlercommons.robots_tag.parsers.SimpleDirectiveParser;
import crawlercommons.robots_tag.parsers.StringDirectiveParser;
import crawlercommons.robots_tag.parsers.TemporalDirectiveParser;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link DirectiveParser}s for well-known directives.
 */
public final class KnownDirectiveParsers {
    /*
        Different vendors support different directives. Vendors may also stop supporting directives that they previously supported.
        The ability to parse directives that are no longer supported by some vendor(s) is useful, because such directives can still be encountered while crawling.

        Sources and references:
            - Apple: https://support.apple.com/en-us/119829
            - Baidu: https://www.baidu.com/search/robots_english.html
            - Bing: https://www.bing.com/webmasters/help/which-robots-metatags-does-bing-support-5198d240
            - DeviantArt: https://www.deviantart.com/team/journal/UPDATE-All-Deviations-Are-Opted-Out-of-AI-Datasets-934500371
            - Google: https://developers.google.com/search/docs/crawling-indexing/robots-meta-tag
            - HTML 4: https://www.w3.org/TR/html4/appendix/notes.html#h-B.4.1.2 (first published in 1997)
            - Yandex: https://yandex.com/support/webmaster/en/controlling-robot/metatags
            - robotstxt.org: https://www.robotstxt.org/metabof.html (report from a 1996 workshop)
     */

    /**
     * Directive: {@code all}
     * <p>
     * Specified by: Apple, Google, HTML 4, Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> ALL = new NamedDirectiveParser<>("all", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code archive}
     * <p>
     * Specified by: Yandex
     */
    public static final NamedDirectiveParser<?> ARCHIVE = new NamedDirectiveParser<>("archive", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code follow}
     * <p>
     * Specified by: Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> FOLLOW = new NamedDirectiveParser<>("follow", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code index}
     * <p>
     * Specified by: HTML 4, Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> INDEX = new NamedDirectiveParser<>("index", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code indexifembedded}
     * <p>
     * Specified by: Google
     */
    public static final NamedDirectiveParser<?> INDEX_IF_EMBEDDED = new NamedDirectiveParser<>("indexifembedded", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code max-image-preview} (e.g. {@code max-image-preview: standard})
     * <p>
     * Specified by: Bing, Google
     */
    public static final NamedDirectiveParser<String> MAX_IMAGE_PREVIEW = new NamedDirectiveParser<>("max-image-preview", StringDirectiveParser.getSingleton());

    /**
     * Directive: {@code max-snippet} (e.g. {@code max-snippet: 90})
     * <p>
     * Specified by: Bing, Google
     */
    public static final NamedDirectiveParser<Integer> MAX_SNIPPET = new NamedDirectiveParser<>("max-snippet", IntDirectiveParser.getSingleton());

    /**
     * Directive: {@code max-video-preview} (e.g. {@code max-video-preview: 10})
     * <p>
     * Specified by: Bing, Google
     */
    public static final NamedDirectiveParser<Integer> MAX_VIDEO_PREVIEW = new NamedDirectiveParser<>("max-video-preview", IntDirectiveParser.getSingleton());

    /**
     * Directive: {@code none}
     * <p>
     * Specified by: Apple, Google, Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> NONE = new NamedDirectiveParser<>("none", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code noai}
     * <p>
     * Specified by: DeviantArt
     */
    public static final NamedDirectiveParser<?> NO_AI = new NamedDirectiveParser<>("noai", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code noarchive}
     * <p>
     * Specified by: Baidu, Bing, Google (deprecated), Yandex
     */
    public static final NamedDirectiveParser<?> NO_ARCHIVE = new NamedDirectiveParser<>("noarchive", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code nocache}
     * <p>
     * Specified by: Bing
     */
    public static final NamedDirectiveParser<?> NO_CACHE = new NamedDirectiveParser<>("nocache", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code nofollow}
     * <p>
     * Specified by: Apple, Baidu, Google, HTML 4, Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> NO_FOLLOW = new NamedDirectiveParser<>("nofollow", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code noimageai}
     * <p>
     * Specified by: DeviantArt
     */
    public static final NamedDirectiveParser<?> NO_IMAGE_AI = new NamedDirectiveParser<>("noimageai", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code noimageindex}
     * <p>
     * Specified by: Google
     */
    public static final NamedDirectiveParser<?> NO_IMAGE_INDEX = new NamedDirectiveParser<>("noimageindex", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code noindex}
     * <p>
     * Specified by: Apple, Bing, Google, HTML 4, Yandex, robotstxt.org
     */
    public static final NamedDirectiveParser<?> NO_INDEX = new NamedDirectiveParser<>("noindex", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code nositelinkssearchbox}
     * <p>
     * Specified by: Google (deprecated)
     */
    public static final NamedDirectiveParser<?> NO_SITELINKS_SEARCH_BOX = new NamedDirectiveParser<>("nositelinkssearchbox", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code nosnippet}
     * <p>
     * Specified by: Apple, Bing, Google
     */
    public static final NamedDirectiveParser<?> NO_SNIPPET = new NamedDirectiveParser<>("nosnippet", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code notranslate}
     * <p>
     * Specified by: Google
     */
    public static final NamedDirectiveParser<?> NO_TRANSLATE = new NamedDirectiveParser<>("notranslate", SimpleDirectiveParser.getSingleton());

    /**
     * Directive: {@code unavailable_after} (e.g. {@code unavailable_after: 2025-12-31})
     * <p>
     * Specified by: Google
     */
    public static final NamedDirectiveParser<Temporal> UNAVAILABLE_AFTER = new NamedDirectiveParser<>("unavailable_after", TemporalDirectiveParser.getSingleton());

    /**
     * <strong>Key:</strong> The name of the directive, all lowercase.<br>
     * <strong>Value:</strong> A {@link DirectiveParser} that can parse the directive.
     * <p>
     * This map is unmodifiable.
     */
    public static final Map<String, DirectiveParser<?>> PARSERS_BY_NAME = Stream.of(
        ALL,
        ARCHIVE,
        FOLLOW,
        INDEX,
        INDEX_IF_EMBEDDED,
        MAX_IMAGE_PREVIEW,
        MAX_SNIPPET,
        MAX_VIDEO_PREVIEW,
        NONE,
        NO_AI,
        NO_ARCHIVE,
        NO_CACHE,
        NO_FOLLOW,
        NO_IMAGE_AI,
        NO_IMAGE_INDEX,
        NO_INDEX,
        NO_SITELINKS_SEARCH_BOX,
        NO_SNIPPET,
        NO_TRANSLATE,
        UNAVAILABLE_AFTER
    ).collect(Collectors.toUnmodifiableMap(NamedDirectiveParser::getDirectiveName, NamedDirectiveParser::getDirectiveParser));
}
