package be.echostyle.moola.peristence.db;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class DbTest {

    protected DataSource ds;

    protected abstract void addTestData();


    protected void updateDb(String sql) {
        JdbcTemplate template = new JdbcTemplate(ds);
        template.execute(sql);
    }

    protected void verifyDb(String sql, int expected) {
        JdbcTemplate template = new JdbcTemplate(ds);
        Integer r = template.queryForObject(sql, Integer.class);
        assertEquals(expected, r.intValue());
    }

    protected void verifyDb(String sql, Set<String> expected) {
        JdbcTemplate template = new JdbcTemplate(ds);
        List<String> r = template.queryForList(sql, String.class);
        assertEquals(expected, new HashSet<String>(r));
    }


}
