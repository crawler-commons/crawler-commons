package crawlercommons.robots_tag;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A builder for {@link RobotsMetaParser}s and {@link RobotsTagParser}s.
 *
 * @param <T> the type of parser built by this {@link ParserBuilder} (i.e. either {@link RobotsMetaParser} or {@link RobotsTagParser})
 * @implNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} can share the same builder class because both parsers use the same constructor parameters with the same default values.
 * @see RobotsMetaParser#RobotsMetaParser(Set, Map, Consumer)
 * @see RobotsTagParser#RobotsTagParser(Set, Map, Consumer)
 */
public final class ParserBuilder<T> {
    private final ParserConstructor<T> parserConstructor;

    private Set<String> targetUserAgents;
    private Map<String, DirectiveParser<?>> directiveParsersByName;
    private Consumer<ParserException> exceptionHandler;

    ParserBuilder(ParserConstructor<T> parserConstructor) {
        this.parserConstructor = parserConstructor;
        this.targetUserAgents = Collections.emptySet();
        this.directiveParsersByName = KnownDirectiveParsers.PARSERS_BY_NAME;
        this.exceptionHandler = ExceptionHandlers::ignoring;
    }

    public ParserBuilder<T> withTargetUserAgents(Set<String> targetUserAgents) {
        this.targetUserAgents = targetUserAgents;
        return this;
    }

    public ParserBuilder<T> withDirectiveParsersByName(Map<String, DirectiveParser<?>> directiveParsersByName) {
        this.directiveParsersByName = directiveParsersByName;
        return this;
    }

    public ParserBuilder<T> withExceptionHandler(Consumer<ParserException> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public T build() {
        return parserConstructor.apply(targetUserAgents, directiveParsersByName, exceptionHandler);
    }

    @FunctionalInterface
    interface ParserConstructor<T> {
        T apply(Set<String> targetUserAgents, Map<String, DirectiveParser<?>> directiveParsersByName, Consumer<ParserException> exceptionHandler);
    }
}
