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
     * Returns directives that only apply to specific user agents.
     * <p>
     * Directives that apply to all user agents (see {@link DirectiveCollection#withoutUserAgent()}) are not included.
     *
     * @apiNote If this {@link DirectiveCollection} was populated by a {@link RobotsMetaParser} or a {@link RobotsTagParser}, then every directive returned by this method applies to one of the target user agents of the parser.
     */
    DirectivesWithUserAgent withUserAgent();

    /**
     * Returns directives that apply to all user agents.
     */
    DirectivesWithoutUserAgent withoutUserAgent();

    boolean isEmpty();

    interface AllDirectives {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        Stream<Directive<?>> stream();
    }

    interface DirectivesWithUserAgent {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        /**
         * <strong>Key:</strong> The trimmed and lowercased user agent.<br>
         * <strong>Value:</strong> Directives that only apply to the user agent.
         *
         * @implSpec The returned map should be unmodifiable.
         */
        Map<String, Set<Directive<?>>> toMap();
    }

    interface DirectivesWithoutUserAgent {
        /**
         * @implSpec The returned set should be unmodifiable.
         */
        Set<Directive<?>> toSet();

        Stream<Directive<?>> stream();
    }
}
