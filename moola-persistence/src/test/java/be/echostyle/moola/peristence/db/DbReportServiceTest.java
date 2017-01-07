package be.echostyle.moola.peristence.db;

import be.echostyle.dbQueries.Mapper;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.PeerRepository;
import be.echostyle.moola.reporting.Bucket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DbReportServiceTest extends DbTest {

    private DbReportService reportService;
    private DbAccountRepository repo;
    private CategoryRepository categories;
    private PeerRepository peers;

    @Before
    public void setUp() throws Exception {
        reportService = new DbReportService();
        repo = new DbAccountRepository();
        peers = Mockito.mock(PeerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        ds = new SingleConnectionDataSource("jdbc:h2:mem:", true);
        repo.setDataSource(ds);
        repo.init();
        repo.setCategoryRepository(categories);
        repo.setPeerRepository(peers);
        reportService.setRepository(repo);
        addTestData();
    }

    @Test
    public void canGroupByType(){
        int count = reportService.report("test")
                .aggregate().byType().count();

        assertEquals(2, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byType().range(0, 2);

        assertEquals(AccountEntryType.cardPayment, result.get(0).getType());
        assertEquals(AccountEntryType.transfer, result.get(1).getType());
        assertEquals(-640, result.get(0).getTotal());
        assertEquals(1120, result.get(1).getTotal());
    }

    @Test
    public void canGroupByDayAndType(){
        int count = reportService.report("test")
                .aggregate().byDay().byType().count();

        assertEquals(3, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byDay().range(0, 30);

        assertEquals(-620, result.get(0).getTotal());
        assertEquals(1120, result.get(1).getTotal());
        assertEquals(-20, result.get(2).getTotal());

    }


    @Test
    public void canGroupByDay(){
        int count = reportService.report("test")
                .aggregate().byDay().count();

        assertEquals(3, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byDay().range(0, 30);

        assertEquals(-620, result.get(0).getTotal());
        assertEquals(1120, result.get(1).getTotal());
        assertEquals(-20, result.get(2).getTotal());

    }

    @Test
    public void canGroupByMonth(){
        int count = reportService.report("test")
                .aggregate().byMonth().count();

        assertEquals(2, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byMonth().range(0, 2);

        assertEquals(-620, result.get(0).getTotal());
        assertEquals(1100, result.get(1).getTotal());

    }

    @Test
    public void calculatesMonth(){
        String month = repo.querySingle("select TS_MONTH(transaction_ts) as month from accTransaction where id = 'abc1'", row -> row.string("month"));
        assertEquals("2016-02", month);
    }

    @Test
    public void calculatesDay(){
        String month = repo.querySingle("select TS_DAY(transaction_ts) as month from accTransaction where id = 'abc1'", row -> row.string("month"));
        assertEquals("2016-02-12", month);
    }

    @Test
    public void calculatesWeek(){
        String month = repo.querySingle("select TS_WEEK(transaction_ts) as month from accTransaction where id = 'abc1'", row -> row.string("month"));
        assertEquals("2016/7", month);
    }


    @Override
    protected void addTestData() {
        JdbcTemplate template = new JdbcTemplate(ds);
        template.execute("insert into account values('test', 'Test', 'CHECKING', '')");
        template.execute("insert into account values('johnny', 'JohnDoe', 'SAVINGS', '')");
        template.execute("insert into account values('jeanie', 'Jeanie', 'INVESTMENT', '')");
        template.execute("insert into account values('groupie', 'Groupie', 'GROUPED', '')");
        template.execute("insert into accTransaction values('abc1', 'abc123', 'johnny', {ts '2016-2-12 20:00:00'}, 'ringo', 'groceries', -2700, 200, 'I owe you some beers', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('abc3', '', 'johnny', {ts '2016-2-11 20:00:00'}, 'target', 'groceries', -2100, 200, 'I owe you some nuts', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('abc5', '', 'johnny', {ts '2016-2-12 19:00:00'}, 'ringo', 'salary', 2700, 200, 'Pay', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def2', '', 'test', {ts '2016-3-12 20:00:00'}, 'ringo', 'holiday', -620, 200, 'Vegas 17', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def4', '', 'test', {ts '2016-2-11 20:00:00'}, 'ringo', 'groceries', -20, 200, 'Bought some condoms', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def6', '', 'test', {ts '2016-2-12 20:00:00'}, 'ringo', 'salary', 1120, 200, 'Pay', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accGroupMembers values('groupie', 'test')");
        template.execute("insert into accGroupMembers values('groupie', 'johnny')");

    }

    @After
    public void tearDown() throws Exception {
        repo.drop();
    }

}