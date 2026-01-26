package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.DirectiveParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("StringDirectiveParser")
class StringDirectiveParserTest implements KeyValueDirectiveParserTest<String> {
    @Override
    public DirectiveParser<String> provideDirectiveParser() {
        return StringDirectiveParser.getSingleton();
    }

    @Override
    public Stream<Arguments> provideTestArguments() {
        return Stream.of(
            arguments("max-image-preview", "none", "none"),
            arguments("message", "Hello World!", "Hello World!") //The directive value should not be transformed to uppercase or lowercase.
        );
    }
}
