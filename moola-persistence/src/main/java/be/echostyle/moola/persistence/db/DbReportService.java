package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.Mapper;
import be.echostyle.dbQueries.QueryBuilder;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.*;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;
import be.echostyle.moola.persistence.cache.CachedCategoryRepository;
import be.echostyle.moola.persistence.cache.CachedPeerRepository;
import be.echostyle.moola.reporting.*;
import be.echostyle.moola.reports.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static be.echostyle.moola.persistence.db.DbAccountEntry.*;

public class DbReportService implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(DbReportService.class);

    private DbAccountRepository repository;
    private CategoryRepository categoryRepository;
    private PeerRepository peerRepository;

    @Override
    public EntryQuery report(String accountId) {
        Account account = repository.getAccount(accountId);
        if (account==null) {
            log.warn("No account found: {}", accountId);
            return new EmptyQuery();
        }
        return new EntryQuery() {

            QueryBuilder q = repository
                    .from(DbAccountEntry.TABLE)
                    .whereIn(DbAccountEntry.COL_ACCOUNT_ID, account.getSimpleIds());

            @Override
            public EntryQuery withPeer(Set<String> peerIds) {
                q = q.whereIn(DbAccountEntry.COL_PEER_ID, peerIds);
                return this;
            }

            @Override
            public EntryQuery withCategory(Set<String> categoryId) {
                q = q.whereIn(DbAccountEntry.COL_CATEGORY_ID, categoryId);
                return this;
            }

            @Override
            public EntryQuery withType(Set<AccountEntryType> type) {
                q = q.whereIn(COL_TYPE, type.stream().map(Enum::name).collect(Collectors.toSet()));
                return this;
            }

            @Override
            public EntryQuery withTimestamp(LocalDateTime from, LocalDateTime to) {
                q = q
                        .where(COL_TIMESTAMP+" >= ?", from)
                        .where(COL_TIMESTAMP+ " < ?", to);
                return this;
            }

            @Override
            public EntryQuery newestFirst() {
                q = q.orderDesc(COL_TIMESTAMP);
                return this;
            }

            @Override
            public int count() {
                return q.count(DbAccountEntry.COL_ID);
            }

            @Override
            public List<AccountEntry> range(int from, int limit) {
                return q
                        .limit(limit, from)
                        .list(cacheMapper(), DbAccountEntry.ALL_COLS);
            }

            @Override
            public AggregatedQuery aggregate() {
                return new AggregatedQueryAdapter(this) {

                    private List<Consumer<BucketMapper>> mapperProcessors = new ArrayList<>();
                    private Map<String, String> keys = new HashMap<>();

                    private <T> void aggregateDesc(String expression, String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
                        q = q.groupedBy(expression).orderDesc(expression);
                        mapperProcessors.add(mapper -> mapper.add(name, getter, setter));
                        keys.put(name, expression);
                    }

                    private <T> void aggregate(String expression, String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
                        q = q.groupedBy(expression).orderAsc(expression);
                        mapperProcessors.add(mapper -> mapper.add(name, getter, setter));
                        keys.put(name, expression);
                    }

                    @Override
                    public AggregatedQuery byDay() {
                        aggregateDesc(func("TS_DAY", COL_TIMESTAMP), "timeslice", RowAdapter::string, Bucket::setTimeSlice);
                        return this;
                    }

                    @Override
                    public AggregatedQuery byWeek() {
                        aggregateDesc(func("TS_WEEK", COL_TIMESTAMP), "timeslice", RowAdapter::string, Bucket::setTimeSlice);
                        return this;
                    }

                    @Override
                    public AggregatedQuery byMonth() {
                        aggregateDesc(func("TS_MONTH", COL_TIMESTAMP), "timeslice", RowAdapter::string, Bucket::setTimeSlice);
                        return this;
                    }

                    @Override
                    public AggregatedQuery byYear() {
                        aggregateDesc(func("TS_YEAR", COL_TIMESTAMP), "timeslice", RowAdapter::string, Bucket::setTimeSlice);
                        return this;
                    }

                    @Override
                    public AggregatedQuery byType() {
                        aggregate(COL_TYPE, "type", (row, key) -> row.value(key, AccountEntryType.class), Bucket::setType);
                        return this;
                    }

                    @Override
                    public AggregatedQuery byCategory() {
                        q = q.groupedBy(COL_CATEGORY_ID).orderAsc(COL_CATEGORY_ID);
                        mapperProcessors.add(mapper -> mapper.addCategory("category"));
                        keys.put("category", COL_CATEGORY_ID);
                        return this;
                    }

                    @Override
                    public int count() {
                        return q.count("*");
                    }

                    @Override
                    public List<Bucket> range(int from, int limit) {
                        List<String> select = new ArrayList<>();
                        select.add(func("COUNT", DbAccountEntry.COL_AMOUNT)+" as count");
                        select.add(func("SUM", DbAccountEntry.COL_AMOUNT)+" as total");
                        select.addAll(keys.entrySet().stream().map(e->e.getValue()+" as "+e.getKey()).collect(Collectors.toList()));
                        return q
                                .limit(limit, from)
                                .list(bucketCacheMapper(), select);
                    }

                    private BucketMapper bucketCacheMapper() {
                        BucketMapper r = new BucketMapper(repository,
                                new CachedCategoryRepository(categoryRepository),
                                new CachedPeerRepository(peerRepository));
                        for (Consumer<BucketMapper> processor: mapperProcessors)
                            processor.accept(r);
                        return r;
                    }
                };
            }
        };
    }

    private static String func(String function, String field) {
        return function+"("+field+")";
    }

    private SimpleDbAccount.CacheMapper cacheMapper() {
        return new SimpleDbAccount.CacheMapper(repository, new DbCategoryRepository(repository), new DbPeerRepository(repository));
    }

    private Paging<AccountEntry> asPages(final EntryQuery eq) {
        int count = eq.count();
        return new Paging<AccountEntry>() {
            @Override
            public int totalPages(int perPage) {
                return (int)Math.ceil(count /(double)perPage);
            }

            @Override
            public List<AccountEntry> page(int perPage, int page) {
                return  eq.range(perPage*page, perPage);
            }
        };
    }

    public void setRepository(DbAccountRepository repository) {
        this.repository = repository;
    }

    public void setCategories(CategoryRepository categories) {
        this.categoryRepository = categories;
    }

    public void setPeers(PeerRepository peers) {
        this.peerRepository = peers;
    }

    /**
     * Maps a data row into a bucket, using a bunch of BucketValueMappers
     */
    static class BucketMapper implements Mapper<Bucket> {
        private List<BucketValueMapper<?>> keys;
        private JdbcRepository repository;
        private CategoryRepository categoryRepository;
        private PeerRepository peerRepository;

        public BucketMapper(JdbcRepository repository, CategoryRepository categoryRepository, PeerRepository peerRepository) {
            this.keys = new ArrayList<>();
            this.repository = repository;
            this.categoryRepository = categoryRepository;
            this.peerRepository = peerRepository;
        }

        public Bucket map(RowAdapter row) {
            Bucket r = new Bucket();
            for (BucketValueMapper<?> key:keys)
                key.map(row, r);
            r.setCount(row.longInt("count"));
            r.setTotal(row.longInt("total"));
            return r;
        }

        /**
         * Add a value to be mapped into the bucket
         * @param name
         *  name of the key to map
         * @param getter
         *  method on the RowAdapter to use to retrieve the value (e.g. RowAdapter::getString)
         * @param setter
         *  method on the bucket to set the value to
         */
        public <T> void add(String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
            keys.add(new BucketValueMapper<>(name, getter, setter));
        }

        /**
         * Same as add, but specifically for the 'category' field. This will resolve the category in the repository.
         */
        public void addCategory(String name) {
            keys.add(new BucketValueMapper<>(name, (row, key)->row.reference(key, categoryRepository::getCategory), Bucket::setCategory));
        }

        /**
         * Same as add, but specifically for the 'peer' field. This will resolve the peer in the repository.
         */
        public void addPeer(String name) {
            keys.add(new BucketValueMapper<>(name, (row, key)->row.reference(key, peerRepository::getPeer), Bucket::setPeer));
        }

        private class BucketValueMapper<T> {
            private final String name;
            private final BiFunction<RowAdapter, String, T> getter;
            private final BiConsumer<Bucket, T> setter;

            public BucketValueMapper(String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
                this.name = name;
                this.getter = getter;
                this.setter = setter;
            }

            public void map(RowAdapter row, Bucket bucket) {
                T value = getter.apply(row, name);
                setter.accept(bucket, value);
            }
        }

    }

    private static abstract class AggregatedQueryAdapter implements AggregatedQuery {

        private EntryQuery base;

        public AggregatedQueryAdapter(EntryQuery base) {
            this.base = base;
        }

        @Override
        public int count() {
            return base.count();
        }

        @Override
        public AggregatedQuery withPeer(Set<String> peerId) {
            base = base.withPeer(peerId);
            return this;
        }

        @Override
        public AggregatedQuery withCategory(Set<String> categoryId) {
            base = base.withCategory(categoryId);
            return this;
        }

        @Override
        public AggregatedQuery withType(Set<AccountEntryType> type) {
            base = base.withType(type);
            return this;
        }

        @Override
        public AggregatedQuery withTimestamp(LocalDateTime from, LocalDateTime to) {
            base = base.withTimestamp(from, to);
            return this;
        }

        @Override
        public AggregatedQuery newestFirst() {
            base = base.newestFirst();
            return this;
        }

        @Override
        public AggregatedQuery aggregate() {
            return this;
        }
    }

}
