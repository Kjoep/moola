package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.Mapper;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.*;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.filters.TransactionFilter;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;
import be.echostyle.moola.persistence.cache.CachedCategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.echostyle.moola.persistence.db.DbAccountEntry.*;

public class SimpleDbAccount extends DbAccount implements SimpleAccount {

    private final PeerRepository peerRepository;
    private final CategoryRepository categoryRepository;

    SimpleDbAccount(String id, String name, AccountType type, DbAccountRepository repository, PeerRepository peerRepository, CategoryRepository categoryRepository, IdGenerator idGenerator) {
        super(id, name, type, repository, idGenerator);
        this.peerRepository = peerRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public AccountEntry addEntry(String batchId, LocalDateTime timestamp, int orderNr, long amount, long balance, String comment, AccountEntryType type, PeerInfo peerInfo, TerminalInfo terminalInfo) {
        DbAccountEntry r = find(timestamp, amount, comment, type, balance);
        if (r==null) {
            String id = idGenerator.generate();
            repository
                    .insert(DbAccountEntry.TABLE,
                            COL_ID, COL_BATCH_ID, COL_ACCOUNT_ID,
                            COL_TIMESTAMP, COL_ORDERNR, COL_AMOUNT, COL_COMMENT, COL_BALANCE, COL_TYPE,
                            COL_PEER_ACCOUNTNR, COL_PEER_NAME,
                            COL_TERMINAL_NAME, COL_TERMINAL_LOCATION, COL_TERMINAL_CARD)
                    .values(id, batchId, this.id,
                            timestamp, orderNr, amount, comment, balance, type,
                            peerInfo == null ? null : peerInfo.getAccountNr(), peerInfo == null ? null : peerInfo.getName(),
                            terminalInfo == null ? null : terminalInfo.getName(), terminalInfo == null ? null : terminalInfo.getLocation(), terminalInfo == null ? null : terminalInfo.getCard());
            r = new DbAccountEntry(id, repository, timestamp, orderNr, amount, balance, comment, peerInfo, terminalInfo, type, null, null, null);
        }
        return r;
    }

    @Override
    public List<AccountEntry> getTransactions(LocalDateTime from, LocalDateTime to) {
        CacheMapper mapper = cacheMapper();
        return repository
                .from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID +" = ?", this.id)
                .whereBetween(COL_TIMESTAMP, from, to)
                .orderDesc(COL_TIMESTAMP)
                .list(mapper, ALL_COLS);
    }

    @Override
    public List<AccountEntry> getTransactions(LocalDateTime to, int count, int from) {
        CacheMapper mapper = cacheMapper();
        return repository
                .from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID +" = ?", this.id)
                .where(COL_TIMESTAMP +" < ?", to)
                .orderDesc(COL_TIMESTAMP)
                .limit(count, from)
                .list(mapper, ALL_COLS);
    }

    @Override
    public List<AccountEntry> getTransactions(LocalDateTime to, TransactionFilter filter, int count, int from) {
        CacheMapper mapper = cacheMapper();
        try (Stream<AccountEntry> stream = repository
                .from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID + " = ?", this.id)
                .where(COL_TIMESTAMP + " < ?", to)
                .orderDesc(COL_TIMESTAMP)
                .stream(mapper, ALL_COLS)){
            return stream.filter(filter::match)
                    .skip(from)
                    .limit(count)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<AccountEntry> getTransactions(String batchId) {
        CacheMapper mapper = cacheMapper();
        return repository
                .from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID +" = ?", this.id)
                .where(COL_BATCH_ID + " = ?", batchId)
                .orderDesc(COL_TIMESTAMP)
                .list(mapper, ALL_COLS);
    }

    @Override
    public AccountEntry getTransaction(String transactonId) {
        return repository
                .from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID +" = ?", this.id)
                .where(COL_ID + " = ?", transactonId)
                .one(cacheMapper(), ALL_COLS).orElse(null);
    }

    @Override
    void removeAll() {
        repository.from(DbAccountEntry.TABLE).where(COL_ACCOUNT_ID+"=?",id).delete();
        super.removeAll();
    }

    private DbAccountEntry find(LocalDateTime timestamp, long amount, String comment, AccountEntryType type, long balance) {
        return (DbAccountEntry) repository.from(DbAccountEntry.TABLE)
                .where(COL_ACCOUNT_ID +" = ?", this.id)
                .where(COL_TIMESTAMP +" = ?", timestamp)
                .where(COL_AMOUNT+" = ?", amount)
                .where(COL_TYPE+" = ?", type)
                .where(COL_BALANCE+" = ?", balance)
                .one(cacheMapper(), ALL_COLS).orElse(null);
    }


    @Override
    public boolean contains(AccountEntry entry) {
        return false;
    }

    private CacheMapper cacheMapper() {
        return new CacheMapper(repository, categoryRepository, peerRepository);
    }

    static class CacheMapper implements Mapper<AccountEntry> {
        private JdbcRepository repository;
        private CategoryRepository categoryRepository;
        private PeerRepository peerRepository;

        public CacheMapper(JdbcRepository repository, CategoryRepository categoryRepository, PeerRepository peerRepository) {
            this.repository = repository;
            this.categoryRepository = categoryRepository;
            this.peerRepository = peerRepository;
        }

        public AccountEntry map(RowAdapter row) {
            CachedCategoryRepository cachedCategories = new CachedCategoryRepository(categoryRepository);
            PeerInfo peerInfo = PeerInfo.of(row.string(COL_PEER_ACCOUNTNR), row.string(COL_PEER_NAME));
            TerminalInfo terminalInfo = TerminalInfo.of(row.string(COL_TERMINAL_NAME), row.string(COL_TERMINAL_LOCATION), row.string(COL_TERMINAL_CARD));
            AccountEntryType type = row.value(COL_TYPE, AccountEntryType.class);
            Peer peer = row.reference(COL_PEER_ID, peerRepository::getPeer);
            Category category = row.reference(COL_CATEGORY_ID, cachedCategories::getCategory);
            DbAccountEntry r = new DbAccountEntry(row.string(COL_ID), repository, row.dateTime(COL_TIMESTAMP), row.integer(COL_ORDERNR), row.longInt(COL_AMOUNT), row.longInt(COL_BALANCE), row.string(COL_COMMENT), peerInfo, terminalInfo, type, row.string(COL_DESCRIPTION), peer, category);
            return r;
        }
    }
}
