package be.echostyle.moola.peristence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.Mapper;
import be.echostyle.dbQueries.QueryBuilder;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.*;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.PeerRepository;
import be.echostyle.moola.peristence.cache.CachedCategoryRepository;
import be.echostyle.moola.reporting.*;
import be.echostyle.moola.reports.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static be.echostyle.moola.peristence.db.DbAccountEntry.*;

public class DbReportService implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(DbReportService.class);
    private static final List<BucketMapper> BUCKET_AGGREGATIONS = Arrays.asList(
            new BucketMapper<>(func("SUM", DbAccountEntry.COL_AMOUNT),"total",RowAdapter::longInt, Bucket::setTotal),
            new BucketMapper<>(func("COUNT", DbAccountEntry.COL_AMOUNT),"count",RowAdapter::longInt, Bucket::setCount));

    private DbAccountRepository repository;

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

                    private List<BucketMapper<?>> bucketMappers = new ArrayList<>();

                    private <T> void aggregateDesc(String expression, String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
                        q = q.groupedBy(expression).orderDesc(expression);
                        bucketMappers.add(new BucketMapper<>(expression, name, getter, setter));
                    }

                    private <T> void aggregate(String expression, String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {
                        q = q.groupedBy(expression).orderAsc(expression);
                        bucketMappers.add(new BucketMapper<>(expression, name, getter, setter));
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
                    public int count() {
                        return q.count("*");
                    }

                    @Override
                    public List<Bucket> range(int from, int limit) {
                        List<String> select = new ArrayList<>();
                        select.addAll(bucketMappers.stream().map(kh -> kh.expression +" as "+kh.name).collect(Collectors.toList()));
                        select.addAll(BUCKET_AGGREGATIONS.stream().map(kh -> kh.expression +" as "+kh.name).collect(Collectors.toList()));
                        return q
                                .limit(limit, from)
                                .list(bucketCacheMapper(bucketMappers), select);
                    }
                };
            }
        };
    }

    private static String func(String function, String field) {
        return function+"("+field+")";
    }

    private Mapper<Bucket> bucketCacheMapper(List<BucketMapper<?>> keys) {
        return new BucketCacheMapper(keys, repository, new DbCategoryRepository(repository), new DbPeerRepository(repository));
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

    static class BucketCacheMapper implements Mapper<Bucket> {
        private List<BucketMapper<?>> keys;
        private JdbcRepository repository;
        private CategoryRepository categoryRepository;
        private PeerRepository peerRepository;

        public BucketCacheMapper(List<BucketMapper<?>> keys, JdbcRepository repository, CategoryRepository categoryRepository, PeerRepository peerRepository) {
            this.keys = keys;
            this.repository = repository;
            this.categoryRepository = categoryRepository;
            this.peerRepository = peerRepository;
        }

        public Bucket map(RowAdapter row) {
            CachedCategoryRepository cachedCategories = new CachedCategoryRepository(categoryRepository);
            Bucket r = new Bucket();
            for (BucketMapper<?> key:keys)
                key.map(row, r);
            for (BucketMapper<?> value:BUCKET_AGGREGATIONS)
                value.map(row, r);
            return r;
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

    private static class BucketMapper<T> {
        private final String expression;
        private final String name;
        private final BiFunction<RowAdapter, String, T> getter;
        private final BiConsumer<Bucket, T> setter;

        public BucketMapper(String expression, String name, BiFunction<RowAdapter, String, T> getter, BiConsumer<Bucket, T> setter) {

            this.expression = expression;
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
