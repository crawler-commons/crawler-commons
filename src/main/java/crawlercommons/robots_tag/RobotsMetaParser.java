package crawlercommons.robots_tag;

import crawlercommons.robots_tag.parsers.SimpleDirectiveParser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses the content of {@code <meta name="robots" content="...">} HTML elements.
 * <p>
 * This parser is not thread-safe.
 *
 * @apiNote Creating a new {@link RobotsMetaParser} instance incurs some overhead (normalizing user agents, compiling regular expressions).
 *          It is therefore recommended to reuse existing parser instances (with {@link #reset()}) if possible.
 * @implNote There is no official standard or specification for {@code <meta name="robots">} elements.
 *           In some cases, their syntax is ambiguous, which makes parsing difficult.
 *           Different vendors may define and support different directives.
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/meta/name/robots">MDN: &lt;meta name=&quot;robots&quot;&gt;</a>
 * @see <a href="https://developers.google.com/search/docs/crawling-indexing/robots-meta-tag">Google: Robots Meta Tags Specifications</a>
 */
public final class RobotsMetaParser {
    private static final Function<String, Optional<String>> GET_NAME_FUNCTION = ParserUtils.createAttributeGetter("name");
    private static final Function<String, Optional<String>> GET_CONTENT_FUNCTION = ParserUtils.createAttributeGetter("content");

    /**
     * Extracts the value of the {@code name} attribute from an HTML element.
     *
     * @param htmlElement the HTML element
     * @return the value of the {@code name} attribute, or {@link Optional#empty()} if the {@code name} attribute is not present
     */
    private static Optional<String> getNameAttribute(String htmlElement) {
        return GET_NAME_FUNCTION.apply(htmlElement); //Because it compiles a regular expression, ParserUtils.createAttributeGetter() is expensive. Implementing getNameAttribute() like this ensures that ParserUtils.createAttributeGetter() is only invoked once.
    }

    /**
     * Extracts the value of the {@code content} attribute from an HTML element.
     *
     * @param htmlElement the HTML element
     * @return the value of the {@code content} attribute, or {@link Optional#empty()} if the {@code content} attribute is not present
     */
    private static Optional<String> getContentAttribute(String htmlElement) {
        return GET_CONTENT_FUNCTION.apply(htmlElement);
    }

    /**
     * The target user agents configured by the user, normalized.
     */
    private final Set<String> normalizedTargetUserAgents;

    private final Map<String, DirectiveParser<?>> directiveParsersByName;

    /**
     * A regular expression that matches all directive names known to the parser.
     */
    private final Pattern knownDirectiveNamesRegex;

    /**
     * All directives that have been collected since the last reset.
     */
    private final ModifiableDirectiveCollection directiveCollection;

    private final Consumer<ParserException> exceptionHandler;

    /**
     * @param targetUserAgents       Directives that apply to all user agents (e.g. {@code foo} in {@code <meta name="robots" content="foo">}) are always collected by the parser, but directives from user agent groups (e.g. {@code bar} in {@code <meta name="SomeBot" content="bar">}) are only collected if the user agent (in this case {@code SomeBot}) is one of the target user agents.<br>
     *                               The default value is {@link Collections#emptySet()}.
     * @param directiveParsersByName Trimmed and lowercased directive names mapped to {@link DirectiveParser}s that can parse the corresponding directives.
     *                               The parser uses these {@link DirectiveParser}s to parse directives (especially key-value directives).<br>
     *                               The default value is {@link KnownDirectiveParsers#PARSERS_BY_NAME}.
     * @param exceptionHandler       The parser invokes this function when it encounters a {@link ParserException} while parsing.
     *                               Use this function to ignore, throw, log, count or collect exceptions.
     *                               If this function throws the encountered exception, the parser stops parsing the current input.
     *                               If the exception is not thrown, the parser advances to the next known directive or user agent and continues to parse the rest of the current input.<br>
     *                               The default value is {@link ExceptionHandlers#ignoring(RuntimeException)}.
     */
    public RobotsMetaParser(Set<String> targetUserAgents, Map<String, DirectiveParser<?>> directiveParsersByName, Consumer<ParserException> exceptionHandler) {
        this.normalizedTargetUserAgents = targetUserAgents.stream()
            .map(ParserUtils::normalizeUserAgent)
            .collect(Collectors.toSet());

        this.directiveParsersByName = Map.copyOf(directiveParsersByName); //Defensive copy.
        this.knownDirectiveNamesRegex = ParserUtils.regexForCollectionElements(directiveParsersByName.keySet());
        this.directiveCollection = new ModifiableDirectiveCollection();
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * @see RobotsMetaParser#RobotsMetaParser(Set, Map, Consumer)
     */
    public RobotsMetaParser(Set<String> targetUserAgents) {
        this(targetUserAgents, KnownDirectiveParsers.PARSERS_BY_NAME, ExceptionHandlers::ignoring);
    }

    /**
     * @see RobotsMetaParser#RobotsMetaParser(Set, Map, Consumer)
     */
    public RobotsMetaParser() {
        this(Collections.emptySet());
    }

    public static ParserBuilder<RobotsMetaParser> builder() {
        return new ParserBuilder<>(RobotsMetaParser::new);
    }

    /**
     * Parses a {@code <meta name="robots" content="...">} HTML element.
     * <p>
     * If the element has a {@code name} and a {@code content} attribute and if the value of the {@code name} attribute is equal to "robots" or one of the target user agents, then the directives from the {@code content} attribute are parsed and collected.
     * <p>
     * This method can handle empty strings, empty {@code <meta>} elements, {@code <meta>} elements that lack the required attributes, and {@code <meta>} elements that have unrelated attributes.
     * <p>
     * Nothing is done to ensure that the input is a {@code <meta>} element.
     * <p>
     * This method may throw a {@link RuntimeException} if the {@link #exceptionHandler} throws an exception.
     *
     * @param metaElement a single {@code <meta>} element
     */
    public void parse(String metaElement) {
        Optional<String> nameOption = getNameAttribute(metaElement).map(ParserUtils::normalizeUserAgent);
        boolean shouldCollect = nameOption.stream().anyMatch(name -> name.equals("robots") || normalizedTargetUserAgents.contains(name));

        if (shouldCollect) {
            getContentAttribute(metaElement).ifPresent(content -> {
                String name = nameOption.get();

                if (content.contains(":")) {
                    parseAmbiguousString(name, content);
                } else {
                    UnambiguousStringParser.parse(content).forEach(directive -> collectDirective(name, directive));
                }
            });
        }
    }

    /**
     * Parses an ambiguous directive string.
     * <p>
     * A directive string is ambiguous if it contains at least one colon. In {@code <meta name="robots">} elements, a colon indicates a key-value directive. There are two problems:
     * <ol>
     *     <li>Some directive values contain unescaped commas, which are indistinguishable from commas that separate directives.</li>
     *     <li>Some directive values contain unescaped colons, which are indistinguishable from colons that separate directive names from directive values.</li>
     * </ol>
     * <p>
     * An ambiguous string can not be treated as a string of comma-separated directives. Instead, it has to be parsed directive by directive.
     * <p>
     * This method may throw a {@link RuntimeException} if the {@link #exceptionHandler} throws an exception.
     */
    private void parseAmbiguousString(String name, String content) {
        String stringToParse = ParserUtils.removeUnnecessaryLeadingCharacters(content);

        while (!stringToParse.isEmpty()) {
            PreprocessedString preprocessed = new PreprocessedString(stringToParse);
            boolean delimiterIsColon = preprocessed.getDelimiter().isPresent() && preprocessed.getDelimiter().get() == ':';

            //Find a suitable DirectiveParser:
            DirectiveParser<?> parser = delimiterIsColon
                ? directiveParsersByName.get(preprocessed.getFirstToken()) //If the first delimiter is a colon, then the directive must be a key-value directive.
                : SimpleDirectiveParser.getSingleton(); //If the first delimiter is a comma or Optional.empty(), then the directive must be a simple directive without a value.

            if (parser != null) {
                try {
                    //Parse and collect the first directive:
                    ParserResult<? extends Directive<?>> parserResult = parser.parse(preprocessed);
                    collectDirective(name, parserResult.getValue());

                    //Remove the parsed directive (including its value, if applicable) from the string:
                    stringToParse = ParserUtils.removeUnnecessaryLeadingCharacters(parserResult.getRemainder());
                } catch (Exception exception) { //The first token is a directive name.
                    exceptionHandler.accept(new ParserException("Failed to parse the first directive in \"" + stringToParse + '"', exception));
                    stringToParse = ParserUtils.dropUntilFirstMatch(knownDirectiveNamesRegex, preprocessed); //It is unclear where the first directive ends, so skipping to the next known directive name is the only safe option.
                }
            } else { //The first token is an unknown key-value directive name.
                exceptionHandler.accept(new ParserException("Failed to find a suitable DirectiveParser for \"" + preprocessed.getFirstToken() + '"'));
                stringToParse = ParserUtils.dropUntilFirstMatch(knownDirectiveNamesRegex, preprocessed); //It is unclear where the first directive ends, so skipping to the next known directive name is the only safe option.
            }
        }
    }

    private void collectDirective(String name, Directive<?> directive) {
        if (name.equals("robots")) {
            directiveCollection.addDirective(directive);
        } else {
            directiveCollection.addDirective(name, directive);
        }
    }

    /**
     * Returns all directives that have been collected since the last reset.
     */
    public DirectiveCollection getCollectedDirectives() {
        return directiveCollection;
    }

    /**
     * Clears the set of collected directives.
     */
    public void reset() {
        directiveCollection.clear();
    }
}
