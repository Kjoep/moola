package be.echostyle.moola.persistence.db;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class DbTest {

    protected DataSource ds;

    @Before
    public void setUp() throws Exception {
        ds = new SingleConnectionDataSource("jdbc:h2:mem:;MODE=PostgreSQL", true);
//        ds = new SingleConnectionDataSource("jdbc:postgresql:postgres", "postgres", "moola", true);
//        Connection connection = ds.getConnection();
//        connection.prepareStatement("drop database if exists moola").execute();
//        connection.prepareStatement("create database moola").execute();
//        connection.close();
//        ds = new SingleConnectionDataSource("jdbc:postgresql:moola", "postgres", "moola", true);
    }

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
        assertEquals(expected, new HashSet<>(r));
    }


}
