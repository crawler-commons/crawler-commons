package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.DirectiveParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("IntDirectiveParser")
class IntDirectiveParserTest implements KeyValueDirectiveParserTest<Integer> {
    @Override
    public DirectiveParser<Integer> provideDirectiveParser() {
        return IntDirectiveParser.getSingleton();
    }

    @Override
    public Stream<Arguments> provideTestArguments() {
        return Stream.of(arguments("max-snippet", "123", 123), arguments("max-snippet", "+123", 123), arguments("max-snippet", "-123", -123));
    }
}
