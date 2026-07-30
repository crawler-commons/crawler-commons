package crawlercommons.robots_tag;

import java.util.*;
import java.util.stream.Collectors;

public final class DirectiveCollection {
    /**
     * The key for directives that apply to all robots.
     */
    public static final String DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY = "*"; //Java's unmodifiable collections do not allow null values, so this string must not be null.

    /**
     * <strong>Key:</strong> The trimmed and lowercased product token <i>p</i>.<br>
     * <strong>Value:</strong> Directives that only apply to robots with the product token <i>p</i>, or directives that apply to all robots if <i>p</i> is equal to {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     */
    private final Map<String, Set<Directive<?>>> directivesByProductToken = new HashMap<>();

    /**
     * This field exists so that {@link #addDirective(Directive)} and {@link #withoutProductToken()} do not need to look up the mapping for {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY} in {@link #directivesByProductToken}.
     */
    private Set<Directive<?>> directivesWithoutProductToken = null;

    /**
     * Adds a directive that applies to all robots.
     */
    public void addDirective(Directive<?> directive) {
        if (directivesWithoutProductToken == null) {
            directivesWithoutProductToken = new HashSet<>();
            directivesByProductToken.put(DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY, directivesWithoutProductToken);
        }

        directivesWithoutProductToken.add(directive);
    }

    /**
     * Adds a directive that only applies to robots with a specific product token.
     * <p>
     * The product token must be trimmed and lowercased.
     */
    public void addDirective(String productToken, Directive<?> directive) {
        directivesByProductToken.compute(productToken, (key, directiveSet) -> {
            if (directiveSet == null) {
                return new HashSet<>();
            } else {
                return directiveSet;
            }
        }).add(directive);
    }

    /**
     * Removes all directives and product tokens from this collection.
     */
    public void clear() {
        directivesByProductToken.clear();
        directivesWithoutProductToken = null;
    }

    public boolean isEmpty() {
        return directivesByProductToken.isEmpty();
    }

    /**
     * Returns a map of directives grouped by product token.
     * <p>
     * The product tokens are trimmed and lowercased. The key for directives that apply to all robots is {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     * <p>
     * This method returns a reference to the internal data structure of this {@link DirectiveCollection}. Any changes to this {@link DirectiveCollection} will be reflected in the returned map, and any changes to the returned map will be reflected in this {@link DirectiveCollection}.
     */
    public Map<String, Set<Directive<?>>> getMap() {
        return directivesByProductToken;
    }

    /**
     * Returns a map of directives grouped by product token.
     * <p>
     * The product tokens are trimmed and lowercased. The key for directives that apply to all robots is {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     * <p>
     * The collection returned by this method is immutable.
     */
    public Map<String, Set<Directive<?>>> toMap() {
        return directivesByProductToken.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
    }

    /**
     * Returns all directives, regardless of which robots they apply to.
     * <p>
     * The collection returned by this method is immutable.
     */
    public Set<Directive<?>> toSet() {
        return directivesByProductToken.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns directives that only apply to specific robots (such as directive
     * {@code foo} in {@code <meta name="SomeBot" content="foo">} or
     * {@code X-Robots-Tag: SomeBot: foo}).
     * <p>
     * Directives that apply to all robots (see
     * {@link #withoutProductToken()}) are not included.
     * <p>
     * The collection returned by this method is immutable.
     *
     * @apiNote If this {@link DirectiveCollection} was populated by a
     *          {@link RobotsMetaParser} or a {@link RobotsTagParser}, then
     *          every directive returned by this method applies to one of the
     *          target product tokens of the parser.
     */
    public Set<Directive<?>> withProductToken() {
        return directivesByProductToken.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns directives that only apply to robots with a specific product token (such as directive
     * {@code foo} in {@code <meta name="SomeBot" content="foo">} or
     * {@code X-Robots-Tag: SomeBot: foo}).
     * <p>
     * Directives that apply to all robots (see
     * {@link #withoutProductToken()}) are not included.
     * <p>
     * The collection returned by this method is immutable.
     *
     * @param productToken the product token
     */
    public Set<Directive<?>> withProductToken(String productToken) {
        Set<Directive<?>> directives = directivesByProductToken.get(productToken);

        if (directives == null) {
            return Collections.emptySet();
        } else {
            return Set.copyOf(directives);
        }
    }

    /**
     * Returns directives that apply to all robots (such as directive
     * {@code foo} in {@code <meta name="robots" content="foo">} or
     * {@code X-Robots-Tag: foo}).
     * <p>
     * Directives that only apply to specific robots (see
     * {@link #withProductToken()} and {@link #withProductToken(String)}) are not included.
     * <p>
     * The collection returned by this method is immutable.
     */
    public Set<Directive<?>> withoutProductToken() {
        if (directivesWithoutProductToken == null) {
            return Collections.emptySet();
        } else {
            return Set.copyOf(directivesWithoutProductToken);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DirectiveCollection)) return false;
        DirectiveCollection other = (DirectiveCollection) object;
        return Objects.equals(directivesByProductToken, other.directivesByProductToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directivesByProductToken);
    }

    @Override
    public String toString() {
        return directivesByProductToken.toString();
    }
}
