package be.echostyle.moola.persistence.db;

import be.echostyle.moola.peer.Peer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DbPeerRepositoryTest extends DbTest {

    private DbPeerRepository repo;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        repo = new DbPeerRepository();
        repo.setDataSource(ds);
        repo.init();
        addTestData();
    }

    @After
    public void tearDown() throws Exception {
        repo.drop();
    }

    @Test
    public void findsPeer(){
        Peer sandra = repo.getPeer("sandra");

        assertEquals("sandra", sandra.getId());
        assertEquals("Sandra", sandra.getName());
    }

    @Test
    public void setsPeerName(){
        Peer sandra = repo.getPeer("sandra");

        sandra.setName("sannyke");

        verifyDb("select count(id) from peer where id = 'sandra' and name = 'sannyke'", 1);
    }

    protected void addTestData() {
        JdbcTemplate template = new JdbcTemplate(ds);

        template.execute("insert into peer values('sandra', 'Sandra')");
        template.execute("insert into peer values('lisa', 'Lisa')");
        template.execute("insert into peer values('duud', 'The Dude')");
    }



}