package be.echostyle.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresSql extends Sql {

    private static final Logger log = LoggerFactory.getLogger(PostgresSql.class);

    @Override
    public Type typeForString(String typeString) {
        switch (typeString) {
            case "character varying":
                return Type.VARCHAR;
            case "timestamp without time zone":
                return Type.TIMESTAMP;
            case "integer":
                return Type.INT;
            default:
                throw new IllegalArgumentException("Unknown H2 type: " + typeString);
        }
    }

    @Override
    public void merge(JdbcTemplate jdbc, String table, String[] keyColumns, String[] columns, Object[] keyValues, Object[] values) {
        List<Object> allValues = Stream.concat(Stream.of(keyValues), Stream.of(values)).collect(Collectors.toList());
        if (columns.length > 0){
            throw new UnsupportedOperationException("ON CONFLICT UPDATE is not yet implemented");
        }
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT DO NOTHING",
                table,
                Stream.concat(Stream.of(keyColumns), Stream.of(columns)).collect(Collectors.joining(", ")),
                allValues.stream().map(v -> "?").collect(Collectors.joining(", "))
        );

        log.debug("Merging: {} with keys {}, values {}", sql,
                Arrays.asList(keyValues), Arrays.asList(values));

        jdbc.update(sql, mapTypes(allValues));
    }
}
