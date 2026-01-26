package crawlercommons.robots_tag;

public interface DirectiveParser<T> {
    /**
     * Parses the first directive (and its value, if applicable) from a {@link PreprocessedString}.
     *
     * @param input the {@link PreprocessedString} to process
     * @return the parsed directive and the input string without the parsed directive and its value
     * @throws Exception if any exceptions are thrown while parsing
     */
    ParserResult<Directive<T>> parse(PreprocessedString input);
}
