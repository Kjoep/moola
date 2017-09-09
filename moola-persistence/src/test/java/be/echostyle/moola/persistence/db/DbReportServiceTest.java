package be.echostyle.moola.persistence.db;

import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.category.Direction;
import be.echostyle.moola.peer.PeerRepository;
import be.echostyle.moola.reporting.Bucket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

@RunWith(JUnit4.class)
public class DbReportServiceTest extends DbTest {

    private DbReportService reportService;
    private DbAccountRepository repo;
    private CategoryRepository categories;
    private PeerRepository peers;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        reportService = new DbReportService();
        repo = new DbAccountRepository();
        peers = Mockito.mock(PeerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        repo.setDataSource(ds);
        repo.init();
        repo.setCategoryRepository(categories);
        repo.setPeerRepository(peers);
        reportService.setRepository(repo);
        reportService.setCategories(categories);
        reportService.setPeers(peers);
        addTestData();
    }

    @After
    public void tearDown() throws Exception {
        repo.drop();
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
        assertEquals(new BigDecimal("-6.4"), result.get(0).getTotal());
        assertEquals(new BigDecimal("11.2"), result.get(1).getTotal());
    }

    @Test
    public void canGroupByCategory(){
        Mockito.when(categories.getCategory(anyString())).then(invocation -> {
                        System.out.println("Mock call");
                    return mockCategory(invocation.getArgumentAt(0, String.class));
                });

        int count = reportService.report("test")
                .aggregate().byCategory().count();

        assertEquals(3, count);

        List<Bucket> result = reportService.report("johnny")
                .aggregate().byCategory().range(0, 2);

        assertEquals("groceries", result.get(0).getCategory().getId());
        assertEquals("salary", result.get(1).getCategory().getId());
        assertEquals(new BigDecimal("-48"), result.get(0).getTotal());
        assertEquals(new BigDecimal("27"), result.get(1).getTotal());
    }

    private Category mockCategory(String id) {
        return new Category() {
            public Direction getDirection() {
                return null;
            }
            public String getName() {
                return id;
            }
            public String getId() {
                return id;
            }
            public String getFgColor() {
                return null;
            }
            public String getBgColor() {
                return null;
            }
            public void setName(String name) {

            }
            public void setColor(String fgColor, String bgColor) {

            }
        };
    }

    @Test
    public void canGroupByDayAndType(){
        int count = reportService.report("test")
                .aggregate().byDay().byType().count();

        assertEquals(3, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byDay().range(0, 30);

        assertEquals(new BigDecimal("-6.2"), result.get(0).getTotal());
        assertEquals(new BigDecimal("11.2"), result.get(1).getTotal());
        assertEquals(new BigDecimal("-.2"), result.get(2).getTotal());

    }


    @Test
    public void canGroupByDay(){
        int count = reportService.report("test")
                .aggregate().byDay().count();

        assertEquals(3, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byDay().range(0, 30);

        assertEquals(new BigDecimal("-6.2"), result.get(0).getTotal());
        assertEquals(new BigDecimal("11.2"), result.get(1).getTotal());
        assertEquals(new BigDecimal("-.2"), result.get(2).getTotal());

    }

    @Test
    public void canGroupByMonth(){
        int count = reportService.report("test")
                .aggregate().byMonth().count();

        assertEquals(2, count);

        List<Bucket> result = reportService.report("test")
                .aggregate().byMonth().range(0, 2);

        assertEquals(new BigDecimal("-6.2"), result.get(0).getTotal());
        assertEquals(new BigDecimal("11"), result.get(1).getTotal());

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
        String week = repo.querySingle("select TS_WEEK(transaction_ts) as month from accTransaction where id = 'abc1'", row -> row.string("month"));
        assertEquals("2016/6", week);
    }


    @Override
    protected void addTestData() {
        JdbcTemplate template = new JdbcTemplate(ds);
        new MarkdownLoader(template).loadTestData(this);
    }

}