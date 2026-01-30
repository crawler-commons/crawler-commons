package crawlercommons.robots_tag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All user-facing methods are defined by the {@link DirectiveCollection} interface. Users can only query the collection, they can not to modify it.
 */
public final class ModifiableDirectiveCollection implements DirectiveCollection {
    private final All allDirectives = new All();
    private final WithUserAgent directivesWithUserAgent = new WithUserAgent();
    private final WithoutUserAgent directivesWithoutUserAgent = new WithoutUserAgent();

    /**
     * Adds a directive that applies to all user agents.
     */
    void addDirective(Directive<?> directive) {
        allDirectives.directives.add(directive);
        directivesWithoutUserAgent.directives.add(directive);
    }

    /**
     * Adds a directive that only applies to a specific user agent.
     * <p>
     * The user agent must be trimmed and lowercased.
     */
    void addDirective(String userAgent, Directive<?> directive) {
        allDirectives.directives.add(directive);

        directivesWithUserAgent.directivesByUserAgent.compute(userAgent, (key, directiveSet) -> {
            if (directiveSet == null) {
                directiveSet = new HashSet<>();
            }

            directiveSet.add(directive);
            return directiveSet;
        });
    }

    void clear() {
        allDirectives.directives.clear();
        directivesWithUserAgent.directivesByUserAgent.clear();
        directivesWithoutUserAgent.directives.clear();
    }

    @Override
    public AllDirectives all() {
        return allDirectives;
    }

    @Override
    public DirectivesWithUserAgent withUserAgent() {
        return directivesWithUserAgent;
    }

    @Override
    public DirectivesWithoutUserAgent withoutUserAgent() {
        return directivesWithoutUserAgent;
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

    private static class WithUserAgent implements DirectivesWithUserAgent {
        Map<String, Set<Directive<?>>> directivesByUserAgent = new HashMap<>();

        @Override
        public Set<Directive<?>> toSet() {
            return directivesByUserAgent.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public Map<String, Set<Directive<?>>> toMap() {
            return directivesByUserAgent.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Collections.unmodifiableSet(entry.getValue())));
        }
    }

    private static class WithoutUserAgent implements DirectivesWithoutUserAgent {
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
            && Objects.equals(directivesWithUserAgent.directivesByUserAgent, other.directivesWithUserAgent.directivesByUserAgent)
            && Objects.equals(directivesWithoutUserAgent.directives, other.directivesWithoutUserAgent.directives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDirectives.directives, directivesWithUserAgent.directivesByUserAgent, directivesWithoutUserAgent.directives);
    }
}
