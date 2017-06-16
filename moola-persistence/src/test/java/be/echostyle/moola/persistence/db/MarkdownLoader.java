package be.echostyle.moola.persistence.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Loads test data based on a markdown file
 */
public class MarkdownLoader {

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^\\s*#\\s*([a-zA-Z_]*)\\s*$");
    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile("^\\s*\\|((?:[^|]+\\|)*[^|]+)\\|\\s*$");

    private static final Logger log = LoggerFactory.getLogger(MarkdownLoader.class);

    private JdbcTemplate target;

    public MarkdownLoader(JdbcTemplate target) {
        this.target = target;
    }

    /**
     * Load the data files that is coupled with the class.
     * This file is presumed to be in the same package, have the same name, ending in
     * testdata.md
     */
    public void loadTestData(Object test){
        Class<?> testClass = test.getClass();
        String filename = testClass.getSimpleName() + ".testdata.md";
        try (InputStream dataStream = testClass.getResourceAsStream(filename)){
            if (dataStream == null)
                throw new TestConfigurationException("No test data file found: "+filename);

            loadTestData(dataStream);
        } catch (IOException e) {
            throw new TestConfigurationException("Could not load from "+filename, e);
        }
    }

    /**
     * Load test data from an input stream.
     */
    public void loadTestData(InputStream input){
       Scanner lineScanner = new Scanner(input);
       String line = lineScanner.nextLine();

       while (line!=null){
           Matcher tableNameMatcher = TABLE_NAME_PATTERN.matcher(line);
           if (tableNameMatcher.find())
               line = loadTable(tableNameMatcher.group(1), lineScanner);
           else
               line = lineScanner.hasNextLine() ? lineScanner.nextLine() : null;
       }
    }

    private String loadTable(String tableName, Scanner lineScanner) {
        log.info("Loading table {}", tableName);
        String line = lineScanner.nextLine();
        String[] headerRow = null;
        while (line != null && headerRow == null) {
            headerRow = parseRow(line);
            line = lineScanner.hasNextLine() ? lineScanner.nextLine() : null;
        }

        List<TypeHandler> fields = loadFields(tableName, headerRow);

        line = lineScanner.nextLine(); // separator row

        String[] row = parseRow(line);
        line = lineScanner.hasNextLine() ? lineScanner.nextLine() : null;
        while (line != null && row != null){
            loadRow(tableName, fields, row);
            row = parseRow(line);
            line = lineScanner.hasNextLine() ? lineScanner.nextLine() : null;
        }

        return line;
    }

    private void loadRow(String tableName, List<TypeHandler> fields, String[] row) {
        if (row.length>fields.size())
            throw new RuntimeException("Rows and headers not same size: " + fields + Arrays.asList(row));
        Iterator<TypeHandler> typeIterator = fields.iterator();
        Iterator<TypeHandler> fieldIterator = fields.iterator();
        String sql = String.format("insert into %s (%s) values (%s)",
                tableName,
                Stream.of(row).map(value -> fieldIterator.next().name()).collect(joining(", ")),
                Stream.of(row).map(value -> typeIterator.next().format(value)).collect(joining(", "))
                );
        log.debug("Executing {}", sql);
        target.execute(sql);
    }

    private List<TypeHandler> loadFields(String tableName, String[] headers) {
        List<String> headerList = Arrays.asList(headers);
        List<TypeHandler> r = target.query("SHOW COLUMNS FROM " + tableName, (rs, rowNum) -> {
            String name = rs.getString("field");
            String type = rs.getString("type");
            switch (type.split("\\(")[0]){
                case "VARCHAR":
                    return TypeHandler.string(name.toLowerCase());
                case "TIMESTAMP":
                    return TypeHandler.timestamp(name.toLowerCase());
                case "INTEGER":
                    return TypeHandler.integer(name.toLowerCase());
                default:
                    throw new RuntimeException("Unsupported type: "+type);
            }
        });
        List<TypeHandler> result = r.stream()
                .filter(field -> headerList.contains(field.name()))
                .sorted(new Comparator<TypeHandler>() {
                    @Override
                    public int compare(TypeHandler o1, TypeHandler o2) {
                        return headerList.indexOf(o1.name()) - headerList.indexOf(o2.name());
                    }
                })
                .collect(Collectors.toList());
        if (result.size()<headers.length)
            throw new RuntimeException("Some fields could not be mapped: " +
                    Stream.of(headers)
                            .filter(header ->
                                    result.stream()
                                            .noneMatch(th -> th.name().equals(header))
                            ).collect(Collectors.joining(",")));

        return result;
    }

    private String[] parseRow(String line) {
        if (line==null) return null;
        Matcher matcher = TABLE_ROW_PATTERN.matcher(line);
        if (!matcher.find()) return null;
        return matcher.group(1).trim().split("\\s*\\|\\s*");
    }

    private abstract static class TypeHandler {
        private String name;

        public TypeHandler(String name) {
            this.name = name;
        }

        public static TypeHandler integer(String fieldName) {
            return new TypeHandler(fieldName) {};
        }

        public static TypeHandler string(String fieldName) {
            return new TypeHandler(fieldName) {
                public String format(String value){
                    return "'"+value+"'";
                }
            };
        }

        public static TypeHandler timestamp(String fieldName) {
            return new TypeHandler(fieldName) {
                public String format(String value){
                    return "ts '"+value+"'";
                }
            };
        }

        public String name() {
            return name;
        }

        public String format(String value) {
            return value;
        }

        @Override
        public String toString() {
            return format(name);
        }
    }
}
