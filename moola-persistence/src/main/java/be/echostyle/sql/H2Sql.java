package be.echostyle.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class H2Sql extends Sql {

    private static final Logger log = LoggerFactory.getLogger(H2Sql.class);

    @Override
    public Type typeForString(String typeString) {
        switch (typeString) {
            case "12":
                return Type.VARCHAR;
            case "93":
                return Type.TIMESTAMP;
            case "4":
                return Type.INT;
            default:
                throw new IllegalArgumentException("Unknown H2 type: " + typeString);
        }
    }

    @Override
    public void merge(JdbcTemplate jdbc, String table, String[] keyColumns, String[] columns, Object[] keyValues, Object[] values) {
        List<Object> allValues = Stream.concat(Stream.of(keyValues), Stream.of(values)).collect(Collectors.toList());
        String sql = String.format("MERGE INTO %s (%s) KEY (%s) VALUES (%s)",
                table,
                Stream.concat(Stream.of(keyColumns), Stream.of(columns)).collect(Collectors.joining(", ")),
                Stream.of(keyColumns).collect(Collectors.joining(", ")),
                allValues.stream().map(v -> "?").collect(Collectors.joining(", "))
            );

        log.debug("Merging: {} with keys {}, values {}", sql,
                Arrays.asList(keyValues), Arrays.asList(values));

        jdbc.update(sql, mapTypes(allValues).toArray());
    }

    @Override
    public boolean supportsDistinctOn() {
        return false;
    }

    @Override
    public boolean supportsIgnoreConflict() {
        return false;
    }
}
