package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DirectiveCollection")
public class DirectiveCollectionTest {
    static final Directive<Void> FOO = new Directive<>("foo");
    static final Directive<Void> BAR = new Directive<>("bar");
    static final Directive<Void> BAZ = new Directive<>("baz");
    static final Directive<String> MAX_IMAGE_PREVIEW_1 = new Directive<>("max-image-preview", "none");
    static final Directive<String> MAX_IMAGE_PREVIEW_2 = new Directive<>("max-image-preview", "large");
    static final Directive<LocalDate> UNAVAILABLE_AFTER_1 = new Directive<>("unavailable_after", LocalDate.of(2025, 12, 31));
    static final Directive<LocalDate> UNAVAILABLE_AFTER_2 = new Directive<>("unavailable_after", LocalDate.of(2026, 1, 1));

    @Test
    @DisplayName("should initialize and clear properly")
    void initializeAndClear() {
        var directiveCollection = new DirectiveCollection();
        assertTrue(directiveCollection.isEmpty());
        assertTrue(directiveCollection.withoutProductToken().isEmpty());
        assertTrue(directiveCollection.withProductToken().isEmpty());

        directiveCollection.addDirective(FOO);
        directiveCollection.addDirective("MyBot", BAR);
        assertFalse(directiveCollection.isEmpty());
        assertFalse(directiveCollection.withoutProductToken().isEmpty());
        assertFalse(directiveCollection.withProductToken().isEmpty());

        directiveCollection.clear();
        assertTrue(directiveCollection.isEmpty());
        assertTrue(directiveCollection.withoutProductToken().isEmpty());
        assertTrue(directiveCollection.withProductToken().isEmpty());
    }

    @Test
    @DisplayName("should eliminate duplicates")
    void eliminateDuplicates() {
        var directiveCollection = new DirectiveCollection();

        //Without product token:
        directiveCollection.addDirective(FOO);
        directiveCollection.addDirective(BAR);
        directiveCollection.addDirective(FOO);
        assertEquals(Set.of(FOO, BAR), directiveCollection.toSet());
        assertEquals(Set.of(FOO, BAR), directiveCollection.withoutProductToken());

        //With product token:
        directiveCollection.addDirective("MyBot-1", BAZ);
        directiveCollection.addDirective("MyBot-1", BAR);
        directiveCollection.addDirective("MyBot-1", BAZ);
        assertEquals(Set.of(FOO, BAR, BAZ), directiveCollection.toSet());
        assertEquals(Set.of(BAR, BAZ), directiveCollection.withProductToken());
        assertEquals(Set.of(BAR, BAZ), directiveCollection.withProductToken("MyBot-1"));

        directiveCollection.addDirective("MyBot-2", BAZ);
        assertEquals(Set.of(FOO, BAR, BAZ), directiveCollection.toSet());
        assertEquals(Set.of(BAR, BAZ), directiveCollection.withProductToken());
        assertEquals(Set.of(BAZ), directiveCollection.withProductToken("MyBot-2"));
    }

    @Test
    @DisplayName("should provide getters that return immutable snapshots")
    void snapshotSemantics() {
        var directiveCollection = new DirectiveCollection();
        directiveCollection.addDirective(FOO);
        directiveCollection.addDirective("MyBot", FOO);

        //Obtain snapshots of the current state:
        var map = directiveCollection.toMap(); //Note: The toMap() method is expected to adhere to snapshot semantics, but the getMap() method is not expected to do so.
        var set = directiveCollection.toSet();
        var withoutProductToken = directiveCollection.withoutProductToken();
        var withProductToken = directiveCollection.withProductToken();
        var withMyBot = directiveCollection.withProductToken("MyBot");

        directiveCollection.addDirective(BAR);
        directiveCollection.addDirective("MyBot", BAR);

        //The snapshots should not contain the "bar" directive:
        assertFalse(map.values().stream().anyMatch(directives -> directives.contains(BAR)));
        assertFalse(set.contains(BAR));
        assertFalse(withoutProductToken.contains(BAR));
        assertFalse(withProductToken.contains(BAR));
        assertFalse(withMyBot.contains(BAR));

        directiveCollection.clear();

        //The snapshots should still be non-empty:
        assertFalse(map.isEmpty());
        assertFalse(set.isEmpty());
        assertFalse(withoutProductToken.isEmpty());
        assertFalse(withProductToken.isEmpty());
        assertFalse(withMyBot.isEmpty());
    }

    @Test
    @DisplayName("should reflect changes made directly to the underlying map")
    void modifyUnderlyingMap() {
        var directiveCollection = new DirectiveCollection();
        var map = directiveCollection.getMap();

        directiveCollection.addDirective(FOO);
        directiveCollection.addDirective("MyBot-1", FOO);

        map.get(DirectiveCollection.DIRECTIVES_WITHOUT_PRODUCT_TOKEN_KEY).add(BAR);
        map.get("MyBot-1").add(BAZ);
        map.put("MyBot-2", new HashSet<>(Set.of(FOO, BAR, BAZ))); //Use a HashSet because Set.of() returns an unmodifiable set while the DirectiveCollection class expects a modifiable set.

        assertEquals(directiveCollection.withoutProductToken(), Set.of(FOO, BAR));
        assertEquals(directiveCollection.withProductToken("MyBot-1"), Set.of(FOO, BAZ));
        assertEquals(directiveCollection.withProductToken("MyBot-2"), Set.of(FOO, BAR, BAZ));

        map.clear();
        assertTrue(directiveCollection.isEmpty());
        assertTrue(directiveCollection.withoutProductToken().isEmpty());
        assertTrue(directiveCollection.withProductToken().isEmpty());
    }

    @Test
    @DisplayName("findOne() should return only matching directives")
    void findOne() {
        var directiveCollection = new DirectiveCollection();
        directiveCollection.addDirective("MyBot-1", FOO);
        directiveCollection.addDirective("MyBot-1", MAX_IMAGE_PREVIEW_1);

        //Everything matches:
        assertEquals(FOO, directiveCollection.findOne("foo", Void.class).orElseThrow());
        assertEquals(FOO, directiveCollection.findOne("MyBot-1", "foo", Void.class).orElseThrow());
        assertEquals(MAX_IMAGE_PREVIEW_1, directiveCollection.findOne("max-image-preview", String.class).orElseThrow());
        assertEquals(MAX_IMAGE_PREVIEW_1, directiveCollection.findOne("MyBot-1", "max-image-preview", String.class).orElseThrow());

        //Wrong value type, everything else matches:
        assertTrue(directiveCollection.findOne("foo", Integer.class).isEmpty());
        assertTrue(directiveCollection.findOne("max-image-preview", Integer.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-1", "foo", Integer.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-1", "max-image-preview", Integer.class).isEmpty());

        //Wrong directive name, everything else matches:
        assertTrue(directiveCollection.findOne("bar", Void.class).isEmpty());
        assertTrue(directiveCollection.findOne("bar", String.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-1", "bar", Void.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-1", "bar", String.class).isEmpty());

        //Wrong product token, everything else matches:
        assertTrue(directiveCollection.findOne("MyBot-2", "foo", Void.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-2", "max-image-preview", String.class).isEmpty());

        //Nothing matches:
        assertTrue(directiveCollection.findOne("bar", Integer.class).isEmpty());
        assertTrue(directiveCollection.findOne("MyBot-2", "bar", Integer.class).isEmpty());
    }

    @Test
    @DisplayName("findAll() should return only matching directives")
    void findAll() {
        var directiveCollection = new DirectiveCollection();

        //MyBot-1 gets one "foo" and one "max-image-preview" directive:
        directiveCollection.addDirective("MyBot-1", FOO);
        directiveCollection.addDirective("MyBot-1", MAX_IMAGE_PREVIEW_1);

        //MyBot-2 gets one "max-image-preview" and two "unavailable_after" directives:
        directiveCollection.addDirective("MyBot-2", MAX_IMAGE_PREVIEW_2);
        directiveCollection.addDirective("MyBot-2", UNAVAILABLE_AFTER_1);
        directiveCollection.addDirective("MyBot-2", UNAVAILABLE_AFTER_2);

        //Everything matches:
        assertEquals(Set.of(FOO), directiveCollection.findAll("foo", Void.class));
        assertEquals(Set.of(FOO), directiveCollection.findAll("MyBot-1", "foo", Void.class));

        assertEquals(Set.of(MAX_IMAGE_PREVIEW_1, MAX_IMAGE_PREVIEW_2), directiveCollection.findAll("max-image-preview", String.class));
        assertEquals(Set.of(MAX_IMAGE_PREVIEW_1), directiveCollection.findAll("MyBot-1", "max-image-preview", String.class));
        assertEquals(Set.of(MAX_IMAGE_PREVIEW_2), directiveCollection.findAll("MyBot-2", "max-image-preview", String.class));

        assertEquals(Set.of(UNAVAILABLE_AFTER_1, UNAVAILABLE_AFTER_2), directiveCollection.findAll("unavailable_after", LocalDate.class));
        assertEquals(Set.of(UNAVAILABLE_AFTER_1, UNAVAILABLE_AFTER_2), directiveCollection.findAll("MyBot-2", "unavailable_after", LocalDate.class));

        //Wrong value type, everything else matches:
        assertTrue(directiveCollection.findAll("foo", Integer.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-1", "foo", Integer.class).isEmpty());

        assertTrue(directiveCollection.findAll("max-image-preview", Integer.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-1", "max-image-preview", Integer.class).isEmpty());

        assertTrue(directiveCollection.findAll("unavailable_after", Integer.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-2", "unavailable_after", Integer.class).isEmpty());

        //Wrong directive name, everything else matches:
        assertTrue(directiveCollection.findAll("bar", Void.class).isEmpty());
        assertTrue(directiveCollection.findAll("bar", String.class).isEmpty());
        assertTrue(directiveCollection.findAll("bar", LocalDate.class).isEmpty());

        assertTrue(directiveCollection.findAll("MyBot-1", "bar", Void.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-1", "bar", String.class).isEmpty());

        assertTrue(directiveCollection.findAll("MyBot-2", "bar", String.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-2", "bar", LocalDate.class).isEmpty());

        //Wrong product token, everything else matches:
        assertTrue(directiveCollection.findAll("MyBot-3", "foo", Void.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-3", "max-image-preview", String.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-3", "unavailable_after", LocalDate.class).isEmpty());

        //Nothing matches:
        assertTrue(directiveCollection.findAll("bar", Integer.class).isEmpty());
        assertTrue(directiveCollection.findAll("MyBot-3", "bar", Integer.class).isEmpty());
    }
}
