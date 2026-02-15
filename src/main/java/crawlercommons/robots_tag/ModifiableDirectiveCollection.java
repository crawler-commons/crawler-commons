package crawlercommons.robots_tag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All user-facing methods are defined by the {@link DirectiveCollection} interface. Users can only query the collection, they can not to modify it.
 */
public final class ModifiableDirectiveCollection implements DirectiveCollection {
    private final All allDirectives = new All();
    private final WithProductToken directivesWithProductToken = new WithProductToken();
    private final WithoutProductToken directivesWithoutProductToken = new WithoutProductToken();

    /**
     * Adds a directive that applies to all robots.
     */
    void addDirective(Directive<?> directive) {
        allDirectives.directives.add(directive);
        directivesWithoutProductToken.directives.add(directive);
    }

    /**
     * Adds a directive that only applies to robots with a specific product token.
     * <p>
     * The product token must be trimmed and lowercased.
     */
    void addDirective(String productToken, Directive<?> directive) {
        allDirectives.directives.add(directive);

        directivesWithProductToken.directivesByProductToken.compute(productToken, (key, directiveSet) -> {
            if (directiveSet == null) {
                directiveSet = new HashSet<>();
            }

            directiveSet.add(directive);
            return directiveSet;
        });
    }

    void clear() {
        allDirectives.directives.clear();
        directivesWithProductToken.directivesByProductToken.clear();
        directivesWithoutProductToken.directives.clear();
    }

    @Override
    public AllDirectives all() {
        return allDirectives;
    }

    @Override
    public DirectivesWithProductToken withProductToken() {
        return directivesWithProductToken;
    }

    @Override
    public DirectivesWithoutProductToken withoutProductToken() {
        return directivesWithoutProductToken;
    }

    @Override
    public boolean isEmpty() {
        return allDirectives.directives.isEmpty();
    }

    private static class All implements AllDirectives {
        Set<Directive<?>> directives = new HashSet<>();

        @Override
        public Set<Directive<?>> toSet() {
            return Collections.unmodifiableSet(directives);
        }

        @Override
        public Stream<Directive<?>> stream() {
            return directives.stream();
        }
    }

    private static class WithProductToken implements DirectivesWithProductToken {
        Map<String, Set<Directive<?>>> directivesByProductToken = new HashMap<>();

        @Override
        public Set<Directive<?>> toSet() {
            return directivesByProductToken.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public Map<String, Set<Directive<?>>> toMap() {
            return directivesByProductToken.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Collections.unmodifiableSet(entry.getValue())));
        }
    }

    private static class WithoutProductToken implements DirectivesWithoutProductToken {
        Set<Directive<?>> directives = new HashSet<>();

        @Override
        public Set<Directive<?>> toSet() {
            return Collections.unmodifiableSet(directives);
        }

        @Override
        public Stream<Directive<?>> stream() {
            return directives.stream();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ModifiableDirectiveCollection)) return false;
        ModifiableDirectiveCollection other = (ModifiableDirectiveCollection) object;

        return Objects.equals(allDirectives.directives, other.allDirectives.directives)
            && Objects.equals(directivesWithProductToken.directivesByProductToken, other.directivesWithProductToken.directivesByProductToken)
            && Objects.equals(directivesWithoutProductToken.directives, other.directivesWithoutProductToken.directives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDirectives.directives, directivesWithProductToken.directivesByProductToken, directivesWithoutProductToken.directives);
    }
}
