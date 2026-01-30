package crawlercommons.robots_tag;

/**
 * Well-known simple directives without values.
 */
public final class KnownSimpleDirectives {
    /*
        Sources and references are listed in KnownDirectiveParsers.
    */

    public static final Directive<?> ALL = new Directive<>("all");
    public static final Directive<?> ARCHIVE = new Directive<>("archive");
    public static final Directive<?> FOLLOW = new Directive<>("follow");
    public static final Directive<?> INDEX = new Directive<>("index");
    public static final Directive<?> INDEX_IF_EMBEDDED = new Directive<>("indexifembedded");
    public static final Directive<?> NONE = new Directive<>("none");
    public static final Directive<?> NO_AI = new Directive<>("noai");
    public static final Directive<?> NO_ARCHIVE = new Directive<>("noarchive");
    public static final Directive<?> NO_CACHE = new Directive<>("nocache");
    public static final Directive<?> NO_FOLLOW = new Directive<>("nofollow");
    public static final Directive<?> NO_IMAGE_AI = new Directive<>("noimageai");
    public static final Directive<?> NO_IMAGE_INDEX = new Directive<>("noimageindex");
    public static final Directive<?> NO_INDEX = new Directive<>("noindex");
    public static final Directive<?> NO_SITELINKS_SEARCH_BOX = new Directive<>("nositelinkssearchbox");
    public static final Directive<?> NO_SNIPPET = new Directive<>("nosnippet");
    public static final Directive<?> NO_TRANSLATE = new Directive<>("notranslate");
}
