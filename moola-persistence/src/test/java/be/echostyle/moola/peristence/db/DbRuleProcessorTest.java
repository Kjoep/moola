package be.echostyle.moola.peristence.db;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.filters.FilterRepository;
import be.echostyle.moola.filters.FilterRule;
import be.echostyle.moola.filters.TransactionFilterFactory;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class DbRuleProcessorTest extends DbTest{

    private DbRuleProcessor processor;
    private FilterRepository filters;
    private PeerRepository peers;
    private CategoryRepository categories;
    private TransactionFilterFactory factory;

    private Peer irs;
    private Category taxes;

    private FilterRule setPeerToIrs;
    private FilterRule setCategoryToTaxes;

    @Before
    public void setUp() throws Exception {
        peers = Mockito.mock(PeerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        factory = Mockito.mock(TransactionFilterFactory.class);
        filters = Mockito.mock(DbFilterRepository.class);
        ds = new DriverManagerDataSource("jdbc:h2:mem");
        DbRuleProcessor processor = new DbRuleProcessor();
        processor.setCategories(categories);
        processor.setPeers(peers);
        processor.setFilters(filters);
        processor.setDataSource(ds);
        processor.init();
        processor.start();
        this.processor = processor;
        when(factory.createFilter(any())).thenReturn(entry -> true);
        addTestData();
    }

    @Test
    public void scheduledRulesAreExecutedPeersFirst() throws InterruptedException {
        verifyDb("select count(id) from accTransaction where peer_id = 'irs' and category_id = 'taxes' and id = '25'", 0);
        AccountEntry entry = Mockito.mock(DbAccountEntry.class);
        when(entry.getId()).thenReturn("25");
        processor.scheduleAll(entry);
        Thread.sleep(3000);
        processor.stop();
        verifyDb("select count(id) from accTransaction where peer_id = 'irs' and category_id = 'taxes' and id = '25'", 1);
    }

    @Override
    protected void addTestData() {
        irs = Mockito.mock(Peer.class);
        taxes = Mockito.mock(Category.class);
        when(peers.getPeer("irs")).thenReturn(irs);
        when(categories.getCategory("taxes")).thenReturn(taxes);
        when(irs.getId()).thenReturn("irs");
        when(taxes.getId()).thenReturn("taxes");

        setPeerToIrs = Mockito.mock(FilterRule.class);
        setCategoryToTaxes = Mockito.mock(FilterRule.class);

        when(setPeerToIrs.getId()).thenReturn("irs");
        when(setCategoryToTaxes.getId()).thenReturn("taxes");
        when(setPeerToIrs.getPeerToSet()).thenReturn(irs);
        when(setCategoryToTaxes.getCategoryToSet()).thenReturn(taxes);
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(0, AccountEntry.class).setPeer(irs);
            return null;
        }).when(setPeerToIrs).apply(any());
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(0, AccountEntry.class).setCategory(taxes);
            return null;
        }).when(setCategoryToTaxes).apply(any());
        when(filters.getRule("irs")).thenReturn(setPeerToIrs);
        when(filters.getRule("taxes")).thenReturn(setCategoryToTaxes);
        when(filters.getAllRules()).thenReturn(Arrays.asList(setCategoryToTaxes, setPeerToIrs));

        JdbcTemplate template = new JdbcTemplate(ds);

        template.execute("insert into account values('test', 'Test', 'CHECKING', '')");
        template.execute("insert into accTransaction values('25', '', 'test', {ts '2016-2-12 20:00:00'}, null, null, -620, 200, 'Damn taxes', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");

    }

    @After
    public void tearDown() throws Exception {
        processor.drop();
        processor.stop();
    }


}