package crawlercommons.robots_tag;

import crawlercommons.robots_tag.parsers.SimpleDirectiveParser;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses the content of {@code X-Robots-Tag} HTTP response headers.
 * <p>
 * This parser is not thread-safe.
 *
 * @apiNote Creating a new {@link RobotsTagParser} instance incurs some overhead (normalizing product tokens, compiling regular expressions).
 *          It is therefore recommended to reuse existing parser instances (with {@link #reset()}) if possible.
 * @implNote There is no official standard or specification for {@code X-Robots-Tag} headers.
 *           In some cases, their syntax is ambiguous, which makes parsing difficult.
 *           Different vendors may define and support different directives.
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/X-Robots-Tag">MDN: X-Robots-Tag</a>
 * @see <a href="https://developers.google.com/search/docs/crawling-indexing/robots-meta-tag">Google: Robots Meta Tags Specifications</a>
 */
public final class RobotsTagParser {
    /**
     * The target product tokens configured by the user, normalized.
     */
    private final Set<String> normalizedTargetProductTokens;

    private final Map<String, DirectiveParser<?>> directiveParsersByName;

    /**
     * A regular expression that matches all directive names known to the parser.
     */
    private final Pattern knownDirectiveNamesRegex;

    /**
     * A regular expression that matches all product tokens known to the parser.
     */
    private final Pattern knownProductTokensRegex;

    /**
     * All directives that have been collected since the last reset.
     */
    private final ModifiableDirectiveCollection directiveCollection;

    private final Consumer<ParserException> exceptionHandler;

    /**
     * @param targetProductTokens    Directives that apply to all robots (e.g. {@code foo} in {@code X-Robots-Tag: foo}) are always collected by the parser, but directives that only apply to specific robots (e.g. {@code bar} in {@code X-Robots-Tag: SomeBot: bar}) are only collected if the product token (in this case {@code SomeBot}) is one of the target product tokens.<br>
     *                               The default value is {@link Collections#emptySet()}.
     * @param directiveParsersByName Trimmed and lowercased directive names mapped to {@link DirectiveParser}s that can parse the corresponding directives.
     *                               The parser uses these {@link DirectiveParser}s to parse directives (especially key-value directives).<br>
     *                               The default value is {@link KnownDirectiveParsers#PARSERS_BY_NAME}.
     * @param exceptionHandler       The parser invokes this function when it encounters a {@link ParserException} while parsing.
     *                               Use this function to ignore, throw, log, count or collect exceptions.
     *                               If this function throws the encountered exception, the parser stops parsing the current input.
     *                               If the exception is not thrown, the parser advances to the next known directive or product token and continues to parse the rest of the current input.<br>
     *                               The default value is {@link ExceptionHandlers#ignoring(RuntimeException)}.
     */
    public RobotsTagParser(Set<String> targetProductTokens, Map<String, DirectiveParser<?>> directiveParsersByName, Consumer<ParserException> exceptionHandler) {
        this.normalizedTargetProductTokens = targetProductTokens.stream()
            .map(ParserUtils::normalizeProductToken)
            .collect(Collectors.toSet());

        this.directiveParsersByName = Map.copyOf(directiveParsersByName); //Defensive copy.
        this.knownDirectiveNamesRegex = ParserUtils.regexForCollectionElements(directiveParsersByName.keySet());
        this.knownProductTokensRegex = ParserUtils.regexForCollectionElements(normalizedTargetProductTokens);
        this.directiveCollection = new ModifiableDirectiveCollection();
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * @see RobotsTagParser#RobotsTagParser(Set, Map, Consumer)
     */
    public RobotsTagParser(Set<String> targetProductTokens) {
        this(targetProductTokens, KnownDirectiveParsers.PARSERS_BY_NAME, ExceptionHandlers::ignoring);
    }

    /**
     * @see RobotsTagParser#RobotsTagParser(Set, Map, Consumer)
     */
    public RobotsTagParser() {
        this(Collections.emptySet());
    }

    public static ParserBuilder<RobotsTagParser> builder() {
        return new ParserBuilder<>(RobotsTagParser::new);
    }

    /**
     * Parses an {@code X-Robots-Tag} HTTP response header.
     * <p>
     * This method can handle empty strings.
     * <p>
     * Nothing is done to ensure that the input is an {@code X-Robots-Tag} header.
     * <p>
     * This method throws a {@link RuntimeException} if the {@link #exceptionHandler} throws an exception.
     *
     * @param robotsHeader a single {@code X-Robots-Tag} header (not prefixed with {@code X-Robots-Tag:})
     */
    public void parse(String robotsHeader) {
        if (robotsHeader.contains(":")) {
            parseAmbiguousString(robotsHeader);
        } else {
            UnambiguousStringParser.parse(robotsHeader).forEach(directiveCollection::addDirective);
        }
    }

    /**
     * Parses an ambiguous directive string.
     * <p>
     * A directive string is ambiguous if it contains at least one colon. In {@code X-Robots-Tag} headers, a colon indicates directives that only apply to a specific robot or a key-value directive. There are three problems:
     * <ol>
     *     <li>Colons that separate product tokens from directives are indistinguishable from colons that separate directive names from directive values.</li>
     *     <li>Some directive values contain unescaped commas, which are indistinguishable from commas that separate directives.</li>
     *     <li>Some directive values contain unescaped colons, which are indistinguishable from colons that separate directive names from directive values.</li>
     * </ol>
     * <p>
     * An ambiguous string can not be treated as a string of comma-separated directives. Instead, it has to be parsed token by token.
     * <p>
     * This method throws a {@link RuntimeException} if the {@link #exceptionHandler} throws an exception.
     */
    private void parseAmbiguousString(String robotsHeader) {
        String stringToParse = ParserUtils.removeUnnecessaryLeadingCharacters(robotsHeader);
        String currentProductToken = null;

        while (!stringToParse.isEmpty()) {
            PreprocessedString preprocessed = new PreprocessedString(stringToParse);
            boolean delimiterIsColon = preprocessed.getDelimiter().isPresent() && preprocessed.getDelimiter().get() == ':';

            if (delimiterIsColon && normalizedTargetProductTokens.contains(preprocessed.getFirstToken())) { //The first token is a target product token.
                currentProductToken = preprocessed.getFirstToken();

                //Remove the product token from the string:
                stringToParse = preprocessed.getTail()
                    .map(ParserUtils::removeUnnecessaryLeadingCharacters)
                    .orElse("");
            } else { //The first token is either a directive name or an unknown product token.
                //Find a suitable DirectiveParser:
                DirectiveParser<?> parser = delimiterIsColon
                    ? directiveParsersByName.get(preprocessed.getFirstToken()) //If the first delimiter is a colon, then the first token is either a key-value directive or an unknown product token.
                    : SimpleDirectiveParser.getSingleton(); //If the first delimiter is a comma or Optional.empty(), then the directive must be a simple directive without a value.

                if (parser != null) {
                    try {
                        //Parse and collect the first directive:
                        ParserResult<? extends Directive<?>> parserResult = parser.parse(preprocessed);

                        if (currentProductToken == null) {
                            directiveCollection.addDirective(parserResult.getValue());
                        } else {
                            directiveCollection.addDirective(currentProductToken, parserResult.getValue());
                        }

                        //Remove the parsed directive (including its value, if applicable) from the string:
                        stringToParse = ParserUtils.removeUnnecessaryLeadingCharacters(parserResult.getRemainder());
                    } catch (Exception exception) { //The first token is a directive name.
                        exceptionHandler.accept(new ParserException("Failed to parse the first directive in \"" + stringToParse + '"', exception));

                        stringToParse = preprocessed.getTail()
                            .map(tail -> {
                                int colonIndex = tail.indexOf(':');
                                Matcher matcher = knownDirectiveNamesRegex.matcher(tail);

                                if (matcher.find() && (matcher.start() < colonIndex || colonIndex == -1)) { //Side effect!
                                    return tail.substring(matcher.start()); //Skipping to the next known directive name is safe as long as no colons are skipped over.
                                } else { //Either there is no next known directive name, or the next known directive name comes after a colon. That colon could indicate a group of directives that only apply to a specific robot, so ...
                                    return ParserUtils.dropUntilFirstMatch(knownProductTokensRegex, preprocessed); //... skipping to the next known product token is the only safe option.
                                }
                            })
                            .orElse("");
                    }
                } else { //The first token is either an unknown key-value directive name or an unknown product token.
                    exceptionHandler.accept(new ParserException("Failed to parse unknown token \"" + preprocessed.getFirstToken() + "\". Is it a directive name or a product token?"));
                    stringToParse = ParserUtils.dropUntilFirstMatch(knownProductTokensRegex, preprocessed); //The first token could be an unknown product token, so skipping to the next known directive name risks collecting directives that only apply to an unknown robot. As such, skipping to the next known product token is the only safe option.
                }
            }
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
