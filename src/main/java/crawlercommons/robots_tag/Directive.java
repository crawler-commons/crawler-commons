package crawlercommons.robots_tag;

import java.util.Objects;
import java.util.Optional;

public final class Directive<T> {
    private final String name;
    private final Optional<T> value;

    public Directive(String name, Optional<T> value) {
        this.name = name;
        this.value = value;
    }

    public Directive(String name, T value) {
        this.name = name;
        this.value = Optional.ofNullable(value);
    }

    public Directive(String name) {
        this.name = name;
        this.value = Optional.empty();
    }

    public boolean hasValue() {
        return value.isPresent();
    }

    public String getName() {
        return name;
    }

    public Optional<T> getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Directive))
            return false;
        Directive<?> other = (Directive<?>) object;
        return Objects.equals(name, other.name) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return value.map(value -> name + ": " + value).orElse(name);
    }
}
