package be.echostyle.moola.peristence.db;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
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
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class DbFilterRepositoryTest extends DbTest {

    private DbFilterRepository repo;
    private PeerRepository peers;
    private CategoryRepository categories;
    private TransactionFilterFactory factory;

    private Peer irs;
    private Category taxes;

    @Before
    public void setUp() throws Exception {
        DbFilterRepository repo = new DbFilterRepository();
        peers = Mockito.mock(PeerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        factory = Mockito.mock(TransactionFilterFactory.class);
        ds = new SingleConnectionDataSource("jdbc:h2:mem:", true);
        repo.setDataSource(ds);
        repo.setCategoryRepository(categories);
        repo.setPeerRepository(peers);
        repo.setFilterFactory(factory);
        repo.init();
        this.repo = repo;
        when(factory.createFilter(any())).thenReturn(entry -> true);
        addTestData();
    }

    @Test
    public void findsFilter(){
        FilterRule filter = repo.getRule("test1");
        assertEquals("I am the walrus", filter.getExpression());
        assertEquals(irs, filter.getPeerToSet());
        assertEquals(taxes, filter.getCategoryToSet());
    }

    @Test
    public void foundFiltersAreApplicable(){
        AccountEntry entry = Mockito.mock(AccountEntry.class);
        FilterRule filter = repo.getRule("test1");
        filter.apply(entry);

        verify(this.factory).createFilter("I am the walrus");
        verify(entry).setCategory(taxes);
        verify(entry).setPeer(irs);
    }

    @Test
    public void createdFiltersArePersisted(){
        verifyDb("select count(id) from rule where expression='trump == 0' and category_id='11'", 0);
        FilterRule filter = repo.createRule("trump == 0");
        filter.setCategoryToSet(taxes);
        verifyDb("select count(id) from rule where expression='trump == 0' and category_id='11'", 1);
    }

    @Test
    public void createdFiltersWork(){
        AccountEntry entry = Mockito.mock(AccountEntry.class);
        FilterRule filter = repo.createRule("trump == 'idiot'");
        filter.setCategoryToSet(taxes);
        filter.setPeerToSet(irs);
        filter.apply(entry);

        verify(this.factory).createFilter("trump == 'idiot'");
        verify(entry).setCategory(taxes);
        verify(entry).setPeer(irs);
    }

    @After
    public void tearDown() throws Exception {
        repo.drop();
    }


    @Override
    protected void addTestData() {
        irs = Mockito.mock(Peer.class);
        taxes = Mockito.mock(Category.class);
        when(peers.getPeer("irs")).thenReturn(irs);
        when(categories.getCategory("taxes")).thenReturn(taxes);
        doReturn("11").when(taxes).getId();

        JdbcTemplate template = new JdbcTemplate(ds);

        template.execute("insert into rule values('test1', 'I am the walrus', 'taxes', 'irs')");
    }

}