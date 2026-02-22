package crawlercommons.robots_tag;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface DirectiveCollection {
    /**
     * Returns all directives within this collection.
     */
    AllDirectives all();

    /**
     * Returns directives that only apply to specific robots (such as directive
     * {@code foo} in {@code <meta name="SomeBot" content="foo">} or
     * {@code X-Robots-Tag: SomeBot: foo}).
     * <p>
     * Directives that apply to all robots (see
     * {@link DirectiveCollection#withoutProductToken()}) are not included.
     * 
     * @apiNote If this {@link DirectiveCollection} was populated by a
     *          {@link RobotsMetaParser} or a {@link RobotsTagParser}, then
     *          every directive returned by this method applies to one of the
     *          target product tokens of the parser.
     */
    DirectivesWithProductToken withProductToken();

    /**
     * Returns directives that apply to all robots (such as directive
     * {@code foo} in {@code <meta name="robots" content="foo">} or
     * {@code X-Robots-Tag: foo}).
     * <p>
     * Directives that only apply to specific robots (see
     * {@link DirectiveCollection#withProductToken()}) are not included.
     */
    DirectivesWithoutProductToken withoutProductToken();

    boolean isEmpty();

    interface AllDirectives {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        Stream<Directive<?>> stream();
    }

    interface DirectivesWithProductToken {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        /**
         * <strong>Key:</strong> The trimmed and lowercased product token
         * <i>p</i>.<br>
         * <strong>Value:</strong> Directives that only apply to robots with the
         * product token <i>p</i>.
         * 
         * @implSpec The returned map should be unmodifiable.
         */
        Map<String, Set<Directive<?>>> toMap();
    }

    interface DirectivesWithoutProductToken {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        Stream<Directive<?>> stream();
    }
}
