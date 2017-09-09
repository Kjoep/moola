package be.echostyle.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Sql {

    private static final Logger log = LoggerFactory.getLogger(Sql.class);

    public abstract Type typeForString(String typeString);

    public static Sql forDatasource(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()){
            String dbName = connection.getMetaData().getDatabaseProductName();
            switch (dbName){
                case "H2":
                    return new H2Sql();
                case "PostgreSQL":
                    return new PostgresSql();
                default:
                    throw new IllegalArgumentException("Unsupported database engine: "+dbName);
            }
        } catch (SQLException e) {
            log.error("Could not determine database type", e);
            throw new RuntimeException("Could not determine database type", e);
        }
    }

    public abstract void merge(JdbcTemplate jdbc, String table, String[] keyColumns, String[] columns, Object[] keyValues, Object[] values);

    public enum Type {
        VARCHAR,
        INT,
        TIMESTAMP
    }

    public Object[] mapTypes(Object[] parameters) {
        Object[] r = new Object[parameters.length];
        for (int i=0; i<parameters.length; i++)
            r[i] = mapType(parameters[i]);
        return r;
    }

    public List<Object> mapTypes(List<Object> parameters) {
        return parameters.stream().map(this::mapType).collect(Collectors.toList());
    }

    public Object mapType(Object parameter) {
        if (parameter==null) return null;
        if (parameter instanceof LocalDateTime) return Date.from(((LocalDateTime) parameter).toInstant(ZoneOffset.UTC));
        if (parameter instanceof Enum<?>) return ((Enum) parameter).name();
        return parameter;
    }
}
