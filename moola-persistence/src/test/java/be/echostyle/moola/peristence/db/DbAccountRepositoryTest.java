package be.echostyle.moola.peristence.db;

import be.echostyle.moola.*;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.security.acl.Group;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class DbAccountRepositoryTest extends DbTest {

    private DbAccountRepository repo;
    private CategoryRepository categories;
    private PeerRepository peers;

    @Before
    public void setUp() throws Exception {
        repo = new DbAccountRepository();
        peers = Mockito.mock(PeerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        ds = new SingleConnectionDataSource("jdbc:derby:memory:db;create=true", true);
        repo.setDataSource(ds);
        repo.init();
        repo.setCategoryRepository(categories);
        repo.setPeerRepository(peers);
        addTestData();
    }

    @Test
    public void listsAccounts(){
        SortedSet<Account> all = repo.getAccounts();

        assertEquals(4, all.size());
        Account first = all.iterator().next();
        assertEquals("Groupie", first.getName());
        assertEquals("groupie", first.getId());
        assertEquals(AccountType.GROUPED, first.getType());
    }

    @Test
    public void getsAccountById(){
        Account johnny = repo.getAccount("johnny");
        assertNotNull(johnny);
        assertEquals("JohnDoe", johnny.getName());
        assertEquals("johnny", johnny.getId());
        assertEquals(AccountType.SAVINGS, johnny.getType());
    }

    @Test
    public void unknownAccountIsNull(){
        Account georgy = repo.getAccount("georgy");
        assertNull(georgy);
    }

    @Test
    public void createsNewAccounts(){
        Account acc = repo.create("checky", "Checkings", AccountType.CHECKING);
        GroupedAccount gr = repo.createGrouped("groupish", "GroupIsh");

        verifyDb("select count(id) from Account where id = 'checky' and name = 'Checkings' and type = 'CHECKING'", 1);
        verifyDb("select count(id) from Account where id = 'groupish' and name = 'GroupIsh' and type = 'GROUPED'", 1);

        assertEquals("checky", acc.getId());
        assertEquals(AccountType.CHECKING, acc.getType());
        assertEquals("Checkings", acc.getName());

        assertEquals("groupish", gr.getId());
        assertEquals(AccountType.GROUPED, gr.getType());
        assertEquals("GroupIsh", gr.getName());
        assertEquals(0, gr.getMembers().size());

        gr.addMember(acc);
        assertEquals(1, gr.getMembers().size());
        assertEquals(acc, gr.getMembers().iterator().next());

        gr = (GroupedAccount) repo.getAccount("groupish");
        assertEquals(1, gr.getMembers().size());
        assertEquals(acc, gr.getMembers().iterator().next());
    }

    @Test
    public void getsTransactionsOnDay(){
        Account johnny = repo.getAccount("johnny");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        System.out.println(transactions.stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertEquals(2, transactions.size());
        assertEquals("I owe you some beers", transactions.get(0).getComment());
        assertEquals("Pay", transactions.get(1).getComment());
    }

    @Test
    public void getsTransactionsForBatch(){
        Account johnny = repo.getAccount("johnny");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions("abc123");
        System.out.println(transactions.stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertEquals(1, transactions.size());
        assertEquals("I owe you some beers", transactions.get(0).getComment());
    }

    @Test
    public void getsTransactionsWithLimit(){
        Account johnny = repo.getAccount("johnny");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions(day.plusDays(1).atStartOfDay(), 2);
        System.out.println(transactions.stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertEquals(2, transactions.size());
        assertEquals("I owe you some beers", transactions.get(0).getComment());
    }

    @Test
    public void getsTransactionsWithFilter(){
        Account johnny = repo.getAccount("johnny");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions(day.plusDays(1).atStartOfDay(), t -> t.getComment().contains("beer"), 2);
        System.out.println(transactions.stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertEquals(1, transactions.size());
        assertEquals("I owe you some beers", transactions.get(0).getComment());
    }

    @Test
    public void deletesSimpleAccount(){
        verifyDb("select count(id) from Account where id = 'johnny'", 1);
        verifyDb("select count(id) from AccTransaction where account_id = 'johnny'", 3);


        Account johnny = repo.getAccount("johnny");
        repo.removeAccount(johnny);

        verifyDb("select count(id) from Account where id = 'johnny'", 0);
        verifyDb("select count(id) from AccTransaction where account_id = 'johnny'", 0);
    }

    @Test
    public void deletesGroupAccount(){
        verifyDb("select count(id) from Account where id = 'groupie'", 1);
        verifyDb("select count(memberId) from AccGroupMembers where groupId = 'groupie'", 2);


        Account groupie = repo.getAccount("groupie");
        repo.removeAccount(groupie);

        verifyDb("select count(id) from Account where id = 'groupie'", 0);
        verifyDb("select count(memberId) from AccGroupMembers where groupId = 'groupie'", 0);
    }

    @Test
    public void deletesGroupMember(){
        verifyDb("select count(memberId) from AccGroupMembers where groupId = 'groupie'", 2);


        Account member = repo.getAccount("johnny");
        repo.removeAccount(member);

        verifyDb("select count(memberId) from AccGroupMembers where groupId = 'groupie'", 1);
    }

    @Test
    public void setsGroupMembers(){
        verifyDb("select memberId from AccGroupMembers where groupId = 'groupie'", set("test", "johnny"));

        GroupedAccount groupie = (GroupedAccount) repo.getAccount("groupie");
        Account johnny = repo.getAccount("johnny");
        Account jeanie = repo.getAccount("jeanie");

        groupie.setMembers(set(johnny, jeanie));

        verifyDb("select memberId from AccGroupMembers where groupId = 'groupie'", set("jeanie", "johnny"));
    }

    @Test
    public void editsTransactionDescription(){
        AccountEntry transaction = getTransactionAbc1();
        assertEquals("I owe you some beers", transaction.getDescription());
        transaction.setDescription("Bought you some beers");

        verifyDb("select count(id) from AccTransaction where id = 'abc1' and description='Bought you some beers'", 1);
    }

    @Test
    public void editsTransactionPeer(){
        Peer sandra = mock(Peer.class);
        when(sandra.getId()).thenReturn("sandraId");
        AccountEntry transaction = getTransactionAbc1();
        assertNull(transaction.getPeer());
        transaction.setPeer(sandra);
        verifyDb("select count(id) from AccTransaction where id = 'abc1' and peer_id='sandraId'", 1);
    }


    @Test
    public void getsTransactionPeer(){
        updateDb("update AccTransaction set peer_id = 'sandraId' where id = 'abc1'");
        Peer sandra = mock(Peer.class);
        when(sandra.getId()).thenReturn("sandraId");
        when(peers.getPeer("sandraId")).thenReturn(sandra);

        AccountEntry transaction = getTransactionAbc1();
        assertEquals(sandra, transaction.getPeer());
    }

    @Test
    public void getsTransactionCategory(){
        updateDb("update AccTransaction set category_id = 'groceriesId' where id = 'abc1'");
        Category groceries = mock(Category.class);
        when(groceries.getId()).thenReturn("groceriesId");
        when(categories.getCategory("groceriesId")).thenReturn(groceries);

        AccountEntry transaction = getTransactionAbc1();
        assertEquals(groceries, transaction.getCategory());
    }

    private AccountEntry getTransactionAbc1() {
        Account johnny = repo.getAccount("johnny");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions(day.plusDays(1).atStartOfDay(), 2);
        return transactions.get(0);
    }

    @Test
    public void editsTransactionCategory(){
        Account johnny = repo.getAccount("johnny");
        Peer sandra = mock(Peer.class);
        when(sandra.getId()).thenReturn("sandraId");
        LocalDate day = LocalDate.of(2016, 2, 12);
        List<AccountEntry> transactions = johnny.getTransactions(day.plusDays(1).atStartOfDay(), 2);
        AccountEntry transaction = transactions.get(0);

        assertNull(transaction.getPeer());

        transaction.setPeer(sandra);

        verifyDb("select count(id) from AccTransaction where id = 'abc1' and peer_id='sandraId'", 1);
    }

    protected void addTestData() {
        JdbcTemplate template = new JdbcTemplate(ds);

        template.execute("insert into account values('test', 'Test', 'CHECKING', '')");
        template.execute("insert into account values('johnny', 'JohnDoe', 'SAVINGS', '')");
        template.execute("insert into account values('jeanie', 'Jeanie', 'INVESTMENT', '')");
        template.execute("insert into account values('groupie', 'Groupie', 'GROUPED', '')");
        template.execute("insert into accTransaction values('abc1', 'abc123', 'johnny', TIMESTAMP ('2016-2-12', '20.00.00'), 'ringo', 'groceries', -2700, 200, 'I owe you some beers', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('abc3', '', 'johnny', TIMESTAMP ('2016-2-11', '20.00.00'), 'target', 'groceries', -2100, 200, 'I owe you some nuts', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('abc5', '', 'johnny', TIMESTAMP ('2016-2-12', '19.00.00'), 'ringo', 'salary', 2700, 200, 'Pay', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def2', '', 'test', TIMESTAMP ('2016-2-12', '20.00.00'), 'ringo', 'holiday', -620, 200, 'Vegas 17', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def4', '', 'test', TIMESTAMP ('2016-2-11', '20.00.00'), 'ringo', 'groceries', -20, 200, 'Bought some condoms', NULL, 'cardPayment', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accTransaction values('def6', '', 'test', TIMESTAMP ('2016-2-12', '20.00.00'), 'ringo', 'salary', 1120, 200, 'Pay', NULL, 'transfer', NULL, NULL, NULL, NULL, NULL)");
        template.execute("insert into accGroupMembers values('groupie', 'test')");
        template.execute("insert into accGroupMembers values('groupie', 'johnny')");
    }

    @After
    public void tearDown() throws Exception {
        repo.drop();
    }

    private static <T> Set<T> set(T... values){
        return new HashSet<>(Arrays.asList(values));
    }
}