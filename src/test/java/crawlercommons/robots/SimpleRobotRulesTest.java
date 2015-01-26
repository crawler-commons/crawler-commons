package crawlercommons.robots;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

public class SimpleRobotRulesTest {

    @Test
    public void testSerialization() throws Exception {
        SimpleRobotRules expectedRules = new SimpleRobotRules();
        expectedRules.addRule("/images/", true);
        expectedRules.addSitemap("sitemap.xml");

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytes);
        oos.writeObject(expectedRules);
        oos.close();

        final ObjectInputStream iis = new ObjectInputStream(
                        new ByteArrayInputStream(bytes.toByteArray()));
        SimpleRobotRules actualRules = (SimpleRobotRules)iis.readObject();

        assertTrue(expectedRules.equals(actualRules));
    }


}
