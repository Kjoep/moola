package be.echostyle.dbQueries;

import be.echostyle.moola.RepositoryException;
import be.echostyle.moola.util.Holder;
import be.echostyle.sql.Sql;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public abstract class JdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcRepository.class);

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private Sql dialect;

    public JdbcRepository(){}

    public JdbcRepository(JdbcRepository other) {
        dataSource = other.dataSource;
        jdbcTemplate = other.jdbcTemplate;
        dialect = other.dialect;
    }

    public QueryBuilder from(String table){
        return new QueryBuilder() {
            private String from = table;
            private String root = table;
            private List<String> where = new ArrayList<>();
            private List<String> order = new ArrayList<>();
            private List<String> group = new ArrayList<>();
            private List<String> distinctOn = new ArrayList<>();
            private List<Object> parameters = new ArrayList<>();
            private Integer limit;
            private Integer fromRange;

            @Override
            public QueryBuilder from(String table) {
                from = table;
                root = table;
                return this;
            }

            @Override
            public QueryBuilder join(String table, String localColumn, String remoteColmn) {
                from = from+" join "+table+" on "+root+"."+localColumn+" = "+table+"."+remoteColmn;
                return this;
            }

            @Override
            public QueryBuilder where(String criteria, Object... parameters) {
                where.add(criteria);
                this.parameters.addAll(Arrays.asList(parameters));
                return this;
            }

            @Override
            public QueryBuilder orderAsc(String column) {
                order.add(column + " ASC");
                return this;
            }

            @Override
            public QueryBuilder orderDesc(String column) {
                order.add(column + " DESC");
                return this;
            }

            @Override
            public QueryBuilder limit(int count, int from) {
                limit = count;
                fromRange = from;
                return this;
            }

            @Override
            public <T> List<T> list(Mapper<T> mapper, String... select) {
                String sql = buildQuery(select);
                log.debug("Executing {} with {}", sql, parameters);
                return query(sql, mapper, parameters.toArray(new Object[0]));
            }

            @Override
            public int count(String column) {
                if (!group.isEmpty() || !distinctOn.isEmpty()) {
                    List<String> allGroup = concat(group.stream(), distinctOn.stream()).collect(toList());
                    String sql = "select count(distinct("+String.join(", ", allGroup)+")) as cnt from " + table;
                    if (!where.isEmpty())
                        sql += " where " + String.join(" and ", where);
                    log.debug("Executing {} with {}", sql, parameters);
                    return querySingle(sql, r -> r.integer("cnt"), parameters.toArray(new Object[0]));
                }
                else {
                    String sql = "select count(" + column + ") as cnt from " + table;
                    if (!where.isEmpty())
                        sql += " where " + String.join(" and ", where);
                    log.debug("Executing {} with {}", sql, parameters);
                    return querySingle(sql, r -> r.integer("cnt"), parameters.toArray(new Object[0]));
                }
            }

            public <T> Stream<T> stream(Mapper<T> mapper, String... select) {
                String sql = buildQuery(select);
                log.debug("Executing {} with {}", sql, parameters);
                ArgumentPreparedStatementSetter argSetter = new ArgumentPreparedStatementSetter(dialect.mapTypes(parameters.toArray(new Object[0])));
                Holder<Connection> connectionHolder = new Holder<>();
                try {
                    connectionHolder.set(dataSource.getConnection());
                    PreparedStatement ps = connectionHolder.get().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    argSetter.setValues(ps);
                    ResultSet rs = ps.executeQuery();
                    return asStream(rs, mapper).onClose(()->{
                        closeQuietly(connectionHolder.get());
                    });
                } catch (SQLException e){
                    closeQuietly(connectionHolder.get());
                    throw new RepositoryException("Could not communicate with database", e);
                }
            }

            @Override
            public QueryBuilder groupedBy(String expression) {
                group.add(expression);
                return this;
            }

            @Override
            public QueryBuilder distinctOnAsc(String expression) {
                distinctOn.add(expression);
                return orderAsc(expression);
            }

            @Override
            public QueryBuilder distinctOnDesc(String expression) {
                distinctOn.add(expression);
                return orderDesc(expression);
            }

            private String buildQuery(String[] select) {
                String sql = "select ";
                if (!distinctOn.isEmpty())
                    sql+= "distinct on ("+String.join(", ", distinctOn)+") ";
                sql+= String.join(", ", select)+" from "+from;
                if (!where.isEmpty())
                    sql+=" where "+String.join(" and ", where);
                if (!group.isEmpty())
                    sql+=" group by ("+String.join(", ", group)+")";
                if (!order.isEmpty())
                    sql += " order by "+String.join(", ", order);
                if (limit!=null && fromRange!=null){
                    sql += " limit "+limit+" offset "+fromRange;
                }
                return sql;
            }

            @Override
            public void delete() {
                if (where.isEmpty()) throw new IllegalArgumentException("Delete without where");
                String sql = "delete from "+table;
                if (!where.isEmpty())
                    sql+=" where "+String.join(" and ", where);
                log.debug("Executing {} with {}", sql, parameters);
                jdbcTemplate.update(sql, dialect.mapTypes(parameters.toArray(new Object[0])));
            }

            @Override
            public int insertInto(String table, Value... mapping) {
                String[] select = map(mapping, Value::describe);
                String selectValues = buildQuery(select);
                String sql = "insert into "+table+" ("+selectValues+")";
                return jdbcTemplate.update(sql, parameters.toArray(new Object[0]));
            }

            @Override
            public int insertInto(String table, ConflictHandling conflictHandling, Value... mapping) {
                String[] select = map(mapping, Value::describe);
                String selectValues = buildQuery(select);
                String sql = "insert into "+table+" ("+selectValues+")";
                if (conflictHandling == ConflictHandling.IGNORE && dialect.supportsIgnoreConflict())
                    sql += " on conflict do nothing";
                return jdbcTemplate.update(sql, parameters.toArray(new Object[0]));
            }
        };
    }

    public InsertBuilder insert(String table, String... columns){
        return new InsertBuilder() {
            @Override
            public void values(Object... values) {
                String sql = "insert into "+table+
                        "("+String.join(",",columns)+") values "+
                        "("+ Stream.of(columns).map(v -> "?").collect(Collectors.joining(","))+")";
                log.debug("Inserting: {} with values {}", sql, Arrays.asList(values));
                jdbcTemplate.update(sql, dialect.mapTypes(values));
            }
        };
    }

    public MergeBuilder merge(String table, String... keyColumns){
        return new MergeBuilder(keyColumns) {
            public void perform() {
                dialect.merge(jdbcTemplate, table, this.keyColumns, this.columns, this.keyValues, this.values);
            }
        };
    }

    public UpdateBuilder update(String table, String keyColumn, Object keyValue) {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        return new UpdateBuilder() {
            @Override
            public UpdateBuilder set(String column, Object value) {
                columns.add(column);
                values.add(value);
                return this;
            }

            @Override
            public void perform() {
                if (columns.isEmpty()) return;
                String sql = "update "+table
                        +" set " + columns.stream().map(col -> col+" = ? ").collect(Collectors.joining(","))
                        +" where "+keyColumn+"=?";
                values.add(keyValue);
                log.debug("Executing {} with {}", sql, values);
                jdbcTemplate.update(sql, dialect.mapTypes(values.toArray(new Object[0])));
            }
        };
    }

    public <T> List<T> query (String sql, Mapper<T> mapper, Object... parameters){
        parameters = dialect.mapTypes(parameters);
        return jdbcTemplate.query(sql, parameters, (resultSet, i) -> mapper.map(adapter(resultSet)));
    }

    public <T> T querySingle (String sql, Mapper<T> mapper, Object... parameters){
        parameters = dialect.mapTypes(parameters);
        List<T> r = jdbcTemplate.query(sql, parameters, (resultSet, i) -> mapper.map(adapter(resultSet)));
        return r.isEmpty() ? null: r.iterator().next();
    }



    private <T> Stream<T> asStream(ResultSet rowSet, Mapper<T> mapper){

        try {
            if (!rowSet.next()) return StreamSupport.stream(Spliterators.emptySpliterator(), false);
        } catch (SQLException e){
            throw new RepositoryException("Could not communicate with database", e);
        }

        Iterator<ResultSet> iterator = new Iterator<ResultSet>() {
            boolean first = true;

            @Override
            public boolean hasNext() {
                try {
                    return !rowSet.isLast();
                } catch (SQLException e) {
                    throw new RepositoryException("Could not communicate with database", e);
                }
            }

            @Override
            public ResultSet next() {
                try {
                    if (!first && !rowSet.next()) throw new NoSuchElementException();
                    first = false;
                    return rowSet;
                } catch (SQLException e) {
                    throw new RepositoryException("Could not communicate with database", e);
                }
            }
        };

        Spliterator<ResultSet> sqlSplit = Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE);

        return StreamSupport
                .stream(sqlSplit, false)
                .map(rowset -> mapper.map(adapter(rowset)))
                .onClose(() -> {
                    try {
                        rowSet.close();
                    } catch (SQLException e) {
                        throw new RepositoryException("Could not communicate with database", e);
                    }
                });
    }

    protected Flyway flyway(){
        Flyway r = new Flyway();
        try (Connection connection = dataSource.getConnection()){
            String dialect = connection.getMetaData().getDatabaseProductName();
            r.setLocations("db/migration/common", "db/migration/" + dialect.toLowerCase());
            r.setDataSource(dataSource);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot read database type", e);
        }
        return r;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dialect = Sql.forDatasource(dataSource);
    }


    protected static QueryBuilder.Value literal(Object literal){
        return () -> {
            if (literal instanceof String) return "'"+literal+"'";
            else return ""+literal;
        };
    }

    protected static QueryBuilder.Value reference(String reference){
        return ()->reference;
    }

    private static <S> String[] map(S[] src, Function<S, String> mapper){
        String[] r = new String[src.length];
        for (int i = 0; i < src.length; i++) {
            r[i] = mapper.apply(src[i]);
        }
        return r;
    }

    public void init() {
        Flyway flyway = flyway();
        flyway.migrate();
    }

    public void drop() {
        Flyway flyway = flyway();
        flyway.clean();
    }

    private RowAdapter adapter(final ResultSet resultSet) {
        return new RowAdapter() {
            @Override
            public String string(String columnName) {
                try {
                    return resultSet.getString(columnName);
                } catch (SQLException e) {
                    throw new RepositoryException("Could not extract column: "+columnName, e);
                }
            }

            @Override
            public int integer(String columnName) {
                try {
                    return resultSet.getInt(columnName);
                } catch (SQLException e) {
                    throw new RepositoryException("Could not extract column: "+columnName, e);
                }
            }

            @Override
            public long longInt(String columnName) {
                try {
                    return resultSet.getLong(columnName);
                } catch (SQLException e) {
                    throw new RepositoryException("Could not extract column: "+columnName, e);
                }
            }

            @Override
            public LocalDateTime dateTime(String columnName) {
                try {
                    java.sql.Timestamp date = resultSet.getTimestamp(columnName);
                    if (date==null) return null;
                    return date.toLocalDateTime();
                } catch (SQLException e){
                    throw new RepositoryException("Could not extract column: "+columnName, e);
                }
            }

            @Override
            public List<Object> all() {
                try {
                    List<Object> r = new ArrayList<>();
                    int count = resultSet.getMetaData().getColumnCount();
                    for (int i = 0; i < count; i++) {
                        r.add(resultSet.getObject(i+1));
                    }
                    return r;
                } catch (SQLException e){
                    throw new RepositoryException("Could not extract columns", e);
                }
            }
        };
    }
    private static void closeQuietly(Connection conn) {
        if (conn==null) return;
        try {
            conn.close();
        } catch (Exception closeE){
            log.error("Error during close", closeE);
        }
    }


    public boolean supportsDistinctOn() {
        return dialect.supportsDistinctOn();
    }
}
