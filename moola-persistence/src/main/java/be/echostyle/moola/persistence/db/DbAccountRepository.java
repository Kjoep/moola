package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.*;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.PeerRepository;

import java.util.*;
import java.util.function.Function;

public class DbAccountRepository extends JdbcRepository implements AccountRepository, AccountFactory {

    private static final String TABLE = "Account";
    private static final String COL_ID = "Id";
    private static final String COL_NAME = "name";
    private static final String COL_TYPE = "type";

    private IdGenerator idGenerator = ()->UUID.randomUUID().toString();
    private PeerRepository peerRepository;
    private CategoryRepository categoryRepository;

    @Override
    public SortedSet<Account> getAccounts() {
        List<Account> results = query("select id, name, type from Account", this::fromRow);
        return sorted(results, Account::getName);
    }

    @Override
    public Account getAccount(String accountId) {
        return querySingle("select id, name, type from Account where id =? ", this::fromRow, accountId);
    }

    @Override
    public Account create(String id, String name, AccountType type) {
        insert(TABLE, COL_ID, COL_NAME, COL_TYPE).values(id, name, type);
        return new SimpleDbAccount(id, name, type, this, peerRepository, categoryRepository, idGenerator);
    }

    @Override
    public GroupedAccount createGrouped(String id, String name) {
        insert(TABLE, COL_ID, COL_NAME, COL_TYPE).values(id, name, AccountType.GROUPED);
        return new GroupedDbAccount(id, name, this, idGenerator);
    }

    @Override
    public void removeAccount(Account account) {
        if (account instanceof DbAccount) ((DbAccount) account).removeAll();
        from(TABLE).where(COL_ID +"=?", account.getId()).delete();
    }

    private DbAccount fromRow(RowAdapter row){
        AccountType type = row.value("type", AccountType.class);
        if (type==AccountType.GROUPED)
            return new GroupedDbAccount(row.string("id"), row.string("name"), this, idGenerator);
        else
            return new SimpleDbAccount(row.string("id"), row.string("name"), type, this, peerRepository, categoryRepository, idGenerator);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setPeerRepository(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private static <T> SortedSet<T> sorted(Collection<T> result, Function<T, ? extends Comparable> keyExtractor) {
        TreeSet<T> r = new TreeSet<>(Comparator.comparing(keyExtractor));
        r.addAll(result);
        return r;
    }

}
