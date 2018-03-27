package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.QueryBuilder;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.PeerInfo;
import be.echostyle.moola.TerminalInfo;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.filters.RuleProcessor;
import be.echostyle.moola.filters.FilterRepository;
import be.echostyle.moola.filters.FilterRule;
import be.echostyle.moola.peer.PeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbRuleProcessor extends JdbcRepository implements RuleProcessor {

    private static final Logger log = LoggerFactory.getLogger(DbRuleProcessor.class);

    private static final String WORK_TABLE = "rulesbacklog";
    private static final String COL_ENTRY_ID = "entry_id";
    private static final String COL_FILTER_ID = "filter_id";

    private FilterRepository filters;
    private CategoryRepository categories;
    private PeerRepository peers;
    private int batchSize = 20;

    @Override
    public int scheduleForAll(FilterRule ref) {
        log.debug("Scheduling filter {} for all entries", ref);
        int r = from(DbAccountEntry.TABLE).insertInto(WORK_TABLE, reference(DbAccountEntry.COL_ID), literal(ref.getId()));
        if (workerThread!=null) workerThread.interrupt();
        return r;
    }

    @Override
    public int scheduleForNoCategory(FilterRule ref) {
        log.debug("Scheduling filter {} for entries without category", ref);
        int r = from(DbAccountEntry.TABLE)
                .where(DbAccountEntry.COL_CATEGORY_ID+" is null")
                .insertInto(WORK_TABLE, QueryBuilder.ConflictHandling.IGNORE, reference(DbAccountEntry.COL_ID), literal(ref.getId()));
        if (workerThread!=null) workerThread.interrupt();
        return r;
    }

    @Override
    public int scheduleForNoPeer(FilterRule ref) {
        log.debug("Scheduling filter {} for entries without peer", ref);
        int r =  from(DbAccountEntry.TABLE)
                .where(DbAccountEntry.COL_PEER_ID+" is null")
                .insertInto(WORK_TABLE, QueryBuilder.ConflictHandling.IGNORE, reference(DbAccountEntry.COL_ID), literal(ref.getId()));
        if (workerThread!=null) workerThread.interrupt();
        return r;
    }

    @Override
    public int scheduleAll(AccountEntry entry) {
        log.debug("Scheduling all filters for {}", entry);

        int r = from(DbFilterRule.TABLE)
                .where(DbFilterRule.COL_PEER + " is not null")
                .insertInto(WORK_TABLE, QueryBuilder.ConflictHandling.IGNORE, literal(entry.getId()), reference(DbFilterRule.COL_ID));
        r += from(DbFilterRule.TABLE)
                .where(DbFilterRule.COL_CATEGORY + " is not null")
                .insertInto(WORK_TABLE, QueryBuilder.ConflictHandling.IGNORE, literal(entry.getId()), reference(DbFilterRule.COL_ID));

        if (workerThread!=null) workerThread.interrupt();

        return r;
    }

    @Override
    public void schedule(AccountEntry entry, FilterRule rule) {
        log.debug("Scheduling filter {} for entry {}", rule, entry);
        if (!(entry instanceof DbAccountEntry)) throw new IllegalArgumentException("This filterProcessor only supports Account Entries that exist in DB");
        merge(WORK_TABLE, COL_ENTRY_ID, COL_FILTER_ID).keyValues(entry.getId(), rule.getId()).perform();
    }

    private int peersFirst(FilterRule one, FilterRule two) {
        if ((one.getPeerToSet()==null) == (two.getPeerToSet()==null))
            return one.getId().compareTo(two.getId());
        return one.getPeerToSet() == null ? 1 : -1;
    }

    @Override
    public int getBacklog() {
        return from(WORK_TABLE).count("*");
    }

    private Thread workerThread;
    private volatile boolean running = false;

    //TODO: make this into a private class and use wait/notify with sychronized methods instead
    // this also has the advantage that you can add a stop method
    public void start(){
        if (running) return;
        workerThread = new Thread(()->{
            running = true;

            log.info("Workerthread started");

            while (running) {
                try {
                    List<WorkEntry> batch = findWork();
                    log.trace("Found {} entries to process", batch.size());

                    if (batch.isEmpty()) {
                        Thread.sleep(5_000);
                    }
                    else {
                        for (WorkEntry entry: batch){
                            entry.process();
                            entry.remove();
                        }
                        log.info("Processed {} rules", batch.size());
                    }
                } catch (InterruptedException e) {
                } catch (RuntimeException e){
                    log.error("Workerthread crashed", e);
                    running = false;
                }

            }
            log.info("Workerthread stopped");
        });

        workerThread.setName(getClass().getSimpleName()+"_worker");
        workerThread.start();
    }

    private List<WorkEntry> findWork() {
        return from(WORK_TABLE)
                                .join(DbAccountEntry.TABLE, COL_ENTRY_ID, DbAccountEntry.COL_ID)
                                .limit(batchSize)
                                .list(this::mapEntry, combine(
                                        qualify(DbAccountEntry.TABLE, DbAccountEntry.ALL_COLS),
                                        new String[]{COL_FILTER_ID}));
    }

    public WorkEntry mapEntry(RowAdapter row) {
        DbAccountEntry accountEntry = new DbAccountEntry(
                row.string(DbAccountEntry.COL_ID),
                this,
                row.dateTime(DbAccountEntry.COL_TIMESTAMP),
                row.integer(DbAccountEntry.COL_ORDERNR),
                row.longInt(DbAccountEntry.COL_AMOUNT),
                row.longInt(DbAccountEntry.COL_BALANCE),
                row.string(DbAccountEntry.COL_COMMENT),
                PeerInfo.of(
                        row.string(DbAccountEntry.COL_PEER_ACCOUNTNR),
                        row.string(DbAccountEntry.COL_PEER_NAME)
                ),
                TerminalInfo.of(
                        row.string(DbAccountEntry.COL_TERMINAL_NAME),
                        row.string(DbAccountEntry.COL_TERMINAL_LOCATION),
                        row.string(DbAccountEntry.COL_TERMINAL_CARD)
                ),
                row.value(DbAccountEntry.COL_TYPE, AccountEntryType.class),
                row.string(DbAccountEntry.COL_DESCRIPTION),
                peers.getPeer(row.string(DbAccountEntry.COL_PEER_ID)),
                categories.getCategory(row.string(DbAccountEntry.COL_CATEGORY_ID))
        );

        return new WorkEntry(accountEntry, filters.getRule(row.string(COL_FILTER_ID)));
    }


    public void stop(){
        log.info("Workerthread stopping...");
        running = false;
        if (workerThread!=null)
            workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException e) {}
    }

    private class WorkEntry {
        private final DbAccountEntry accountEntry;
        private final FilterRule filter;

        public WorkEntry(DbAccountEntry accountEntry, FilterRule filter) {
            this.accountEntry = accountEntry;
            this.filter = filter;
        }

        public void process() {
            filter.apply(accountEntry);
        }

        public void remove() {
            from(WORK_TABLE)
                    .where(COL_ENTRY_ID+" = ?", accountEntry.getId())
                    .where(COL_FILTER_ID+" = ?", filter.getId())
                    .delete();
        }
    }

    public void setFilters(FilterRepository filters) {
        this.filters = filters;
    }

    public void setCategories(CategoryRepository categories) {
        this.categories = categories;
    }

    public void setPeers(PeerRepository peers) {
        this.peers = peers;
    }

    private static String[] combine(String[]... arrays){
        ArrayList<String> r = new ArrayList<String>();
        for (String[] arr:arrays){
            r.addAll(Arrays.asList(arr));
        }
        return r.toArray((new String[0]));
    }

    private static String[] qualify(String qualifier, String[] columns){
        String[] r = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            r[i] = qualifier+"."+columns[i];
        }
        return r;
    }
}
