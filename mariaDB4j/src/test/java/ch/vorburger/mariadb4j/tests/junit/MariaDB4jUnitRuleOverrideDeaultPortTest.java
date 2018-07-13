package ch.vorburger.mariadb4j.tests.junit;

import ch.vorburger.mariadb4j.junit.MariaDBRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MariaDB4jUnitRuleOverrideDeaultPortTest {

    @Rule
    public MariaDBRule dbRule = new MariaDBRule(3307);

    @Test
    public void validatePort() {
        assertEquals(3307, dbRule.getConfiguration().getPort());
    }
}
