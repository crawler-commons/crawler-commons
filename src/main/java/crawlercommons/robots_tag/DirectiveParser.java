package crawlercommons.robots_tag;

public interface DirectiveParser<T> {
    /**
     * Parses the first directive (and its value, if applicable) from a {@link PreprocessedString}.
     * <p>
     * This method may throw a {@link RuntimeException} if any exception is thrown while parsing.
     *
     * @param input the {@link PreprocessedString} to process
     * @return the parsed directive and the input string without the parsed directive and its value
     */
    ParserResult<Directive<T>> parse(PreprocessedString input);
}
