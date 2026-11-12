package crawlercommons.robots_tag;

import java.util.*;
import java.util.stream.Collectors;

public final class DirectiveCollection {
    /**
     * The key for directives that apply to all robots.
     */
    public static final String DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY = "*"; //Java's unmodifiable collections do not allow null values, so this string must not be null.

    /**
     * <strong>Key:</strong> The product token <i>p</i>.<br>
     * <strong>Value:</strong> Directives that only apply to robots with the product token <i>p</i>, or directives that apply to all robots if <i>p</i> is equal to {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     */
    private final Map<String, Set<Directive<?>>> directivesByProductToken = new HashMap<>();

    /**
     * Adds a directive that applies to all robots.
     */
    public void addDirective(Directive<?> directive) {
        addDirective(DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY, directive);
    }

    /**
     * Adds a directive that only applies to robots with a specific product token.
     * <p>
     * The product token is case-sensitive.
     *
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
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
    }

    public boolean isEmpty() {
        return directivesByProductToken.isEmpty();
    }

    /**
     * Returns a map of directives grouped by product token.
     * <p>
     * The key for directives that apply to all robots is {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     * <p>
     * This method returns a reference to the internal data structure of this {@link DirectiveCollection}. Any changes to this {@link DirectiveCollection} will be reflected in the returned map, and any changes to the returned map will be reflected in this {@link DirectiveCollection}.
     *
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
     */
    public Map<String, Set<Directive<?>>> getMap() {
        return directivesByProductToken;
    }

    /**
     * Returns a map of directives grouped by product token.
     * <p>
     * The key for directives that apply to all robots is {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY}.
     * <p>
     * The collection returned by this method is immutable.
     *
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
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
     * @param productToken the product token (case-sensitive)
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
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
        Set<Directive<?>> directives = directivesByProductToken.get(DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY);

        if (directives == null) {
            return Collections.emptySet();
        } else {
            return Set.copyOf(directives);
        }
    }

    /**
     * Finds a directive by product token, directive name, and directive value type.
     * <p>
     * If this {@link DirectiveCollection} contains multiple matching directives, any matching directive may be returned.
     * <p>
     * Example:
     * <pre>{@code
     * Optional<Directive<Void>> follow = directiveCollection.findOne("MyBot", "follow", Void.class);
     * Optional<Directive<Integer>> maxSnippet = directiveCollection.findOne("MyBot", "max-snippet", Integer.class);
     * }</pre>
     *
     * @param productToken  The product token (case-sensitive) to which the directive applies. Use {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY} to find a directive that applies to all robots.
     * @param directiveName the directive name (case-sensitive)
     * @param valueType     The type of the directive value. Use {@link Void} to find a directive without a value.
     * @return an {@link Optional} containing a matching directive from this {@link DirectiveCollection}, or an empty {@link Optional} if there is no matching directive
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<Directive<T>> findOne(String productToken, String directiveName, Class<T> valueType) {
        return (Optional<Directive<T>>) (Optional) directivesByProductToken.getOrDefault(productToken, Collections.emptySet()).stream()
                .filter(directive -> directive.hasValueType(valueType) && directive.getName().equals(directiveName)) //The type check comes first because it is probably faster than the string comparison.
                .findFirst();
    }

    /**
     * Finds a directive by directive name and directive value type.
     * <p>
     * If this {@link DirectiveCollection} contains multiple matching directives, any matching directive may be returned.
     * <p>
     * Example:
     * <pre>{@code
     * Optional<Directive<Void>> follow = directiveCollection.findOne("follow", Void.class);
     * Optional<Directive<Integer>> maxSnippet = directiveCollection.findOne("max-snippet", Integer.class);
     * }</pre>
     *
     * @param directiveName the directive name (case-sensitive)
     * @param valueType     The type of the directive value. Use {@link Void} to find a directive without a value.
     * @return an {@link Optional} containing a matching directive from this {@link DirectiveCollection}, or an empty {@link Optional} if there is no matching directive
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<Directive<T>> findOne(String directiveName, Class<T> valueType) {
        return (Optional<Directive<T>>) (Optional) directivesByProductToken.values().stream()
                .flatMap(Set::stream)
                .filter(directive -> directive.hasValueType(valueType) && directive.getName().equals(directiveName))
                .findFirst();
    }

    /**
     * Finds directives by product token, directive name, and directive value type.
     * <p>
     * The collection returned by this method is immutable.
     * <p>
     * Example:
     * <pre>{@code
     * Set<Directive<Void>> follow = directiveCollection.findAll("MyBot", "follow", Void.class);
     * Set<Directive<Integer>> maxSnippet = directiveCollection.findAll("MyBot", "max-snippet", Integer.class);
     * }</pre>
     *
     * @param productToken  The product token (case-sensitive) to which the directives apply. Use {@link #DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY} to find directives that apply to all robots.
     * @param directiveName the directive name (case-sensitive)
     * @param valueType     The type of the directive value. Use {@link Void} to find directives without a value.
     * @return a set of all matching directives from this {@link DirectiveCollection}
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Set<Directive<T>> findAll(String productToken, String directiveName, Class<T> valueType) {
        return (Set<Directive<T>>) (Set) directivesByProductToken.getOrDefault(productToken, Collections.emptySet()).stream()
                .filter(directive -> directive.hasValueType(valueType) && directive.getName().equals(directiveName))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds directives by directive name and directive value type.
     * <p>
     * The collection returned by this method is immutable.
     * <p>
     * Example:
     * <pre>{@code
     * Set<Directive<Void>> follow = directiveCollection.findAll("follow", Void.class);
     * Set<Directive<Integer>> maxSnippet = directiveCollection.findAll("max-snippet", Integer.class);
     * }</pre>
     *
     * @param directiveName the directive name (case-sensitive)
     * @param valueType     The type of the directive value. Use {@link Void} to find directives without a value.
     * @return a set of all matching directives from this {@link DirectiveCollection}
     * @apiNote The {@link RobotsMetaParser} and the {@link RobotsTagParser} always trim and lowercase product tokens and directive names.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Set<Directive<T>> findAll(String directiveName, Class<T> valueType) {
        return (Set<Directive<T>>) (Set) directivesByProductToken.values().stream()
                .flatMap(Set::stream)
                .filter(directive -> directive.hasValueType(valueType) && directive.getName().equals(directiveName))
                .collect(Collectors.toUnmodifiableSet());
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
