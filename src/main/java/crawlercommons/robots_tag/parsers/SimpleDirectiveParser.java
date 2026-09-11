package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.Directive;
import crawlercommons.robots_tag.DirectiveParser;
import crawlercommons.robots_tag.ParserResult;
import crawlercommons.robots_tag.PreprocessedString;

import java.util.Optional;

/**
 * Parses simple directives that have no value (e.g. {@code follow} or
 * {@code index}).
 */
public final class SimpleDirectiveParser implements DirectiveParser<Void> {
    private static final SimpleDirectiveParser SINGLETON = new SimpleDirectiveParser();

    @Override
    public ParserResult<Directive<Void>> parse(PreprocessedString input) {
        Directive<Void> directive = new Directive<>(input.getFirstToken(), Optional.empty());
        String remainder = input.getString().substring(input.getDelimiterIndex());
        return new ParserResult<>(directive, remainder);
    }

    public static SimpleDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
