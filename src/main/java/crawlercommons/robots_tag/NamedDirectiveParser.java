package crawlercommons.robots_tag;

/**
 * Combines a {@link DirectiveParser} with the name of a directive that it can parse.
 */
public final class NamedDirectiveParser<T> {
    private final String directiveName;
    private final DirectiveParser<T> directiveParser;

    public NamedDirectiveParser(String directiveName, DirectiveParser<T> directiveParser) {
        this.directiveName = directiveName;
        this.directiveParser = directiveParser;
    }

    public String getDirectiveName() {
        return directiveName;
    }

    public DirectiveParser<T> getDirectiveParser() {
        return directiveParser;
    }
}
