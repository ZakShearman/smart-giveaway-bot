package pink.zak.test.giveawaybot.discord.service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pink.zak.giveawaybot.discord.service.config.Config;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConfigTests {
    private Config config;

    @Test
    @BeforeEach
    void testCreateConfig() {
        try {
            URL resourceLocation = ClassLoader.getSystemResource("config-tests.yml");
            File file = new File(resourceLocation.toURI());
            this.config = new Config(file, true);
            System.out.println(this.config.getValueMap());
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    void testGeneralGetters() {
        assertNotNull(this.config.getConfiguration());
        assertTrue(this.config.isReloadable());
    }

    @Test
    void testString() {
        assertEquals("test-value", this.config.string("test-string"));
    }

    @Test
    void testBool() {
        assertTrue(this.config.bool("test-true-boolean"));
        assertFalse(this.config.bool("test-false-boolean"));
        assertFalse(this.config.bool("not-present-boolean"));
    }

    @Test
    void testInteger() {
        assertEquals(12345, this.config.integer("test-integer"));
        assertEquals(-1, this.config.integer("not-present-integer"));
    }

    @Test
    void testDoubl() {
        assertEquals(12.345, this.config.doubl("test-double"));
        assertEquals(-1, this.config.doubl("not-present-double"));
    }

    @Test
    void testStringList() {
        List<String> stringList = this.config.stringList("test-string-list");
        assertEquals(5, stringList.size());
        assertEquals("idk", stringList.get(2));
        assertTrue(this.config.stringList("not-present-string-list").isEmpty());
    }

    @Test
    void testList() {
        List<Integer> intList = this.config.list("test-integer-list");
        assertEquals(2, intList.size());
        assertEquals(1, intList.get(0));
        assertTrue(this.config.list("not-present-integer-list").isEmpty());
    }

    @Test
    void testHas() {
        assertTrue(this.config.has("test-string"));
        assertTrue(this.config.has("test-true-boolean"));
        assertTrue(this.config.has("some.thing.with.a.value"));
    }
}
