package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DirectiveCollection")
public class DirectiveCollectionTest {
    static final Directive<Void> FOO = new Directive<>("foo");
    static final Directive<Void> BAR = new Directive<>("bar");
    static final Directive<Void> BAZ = new Directive<>("baz");

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
}
