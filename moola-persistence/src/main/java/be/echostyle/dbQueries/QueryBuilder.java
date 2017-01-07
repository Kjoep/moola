package be.echostyle.dbQueries;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface QueryBuilder {
    QueryBuilder from(String table);
    QueryBuilder join(String table, String localColumn, String remoteColmn);
    QueryBuilder where(String criteria, Object... parameters);
    default QueryBuilder whereIn(String column, Set<String> values){
        values = new HashSet<>(values);
        if (values.remove(null))
            return where("("+column+" is null or "+column + " in ("+values.stream().map(s->"?").collect(Collectors.joining(","))+"))", values.toArray(new Object[0]));
        else
            return where(column + " in ("+values.stream().map(s->"?").collect(Collectors.joining(","))+")", values.toArray(new Object[0]));
    }
    QueryBuilder limit(int count, int from);
    default QueryBuilder limit(int count){
        return limit(count, 0);
    }
    QueryBuilder orderAsc(String column);
    QueryBuilder orderDesc(String column);

    default QueryBuilder whereBetween(String column, Object from, Object to){
        return where(column+" >= ?", from).where(column+" < ?",to);
    }

    QueryBuilder groupedBy(String expression);

    default <T> Optional<T> one(Mapper<T> mapper, String... columns){
        List<T> r = list(mapper, columns);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.iterator().next());
    }
    default <T> Set<T> set(Mapper<T> mapper, String... columns){
        return new HashSet<T>(list(mapper, columns));
    };
    <T> List<T> list(Mapper<T> mapper, String... columns);
    default <T> List<T> list(Mapper<T> mapper, List<String> columns){
        return list(mapper, columns.toArray(new String[0]));
    };
    <T> Stream<T> stream(Mapper<T> mapper, String... columns);
    int insertInto(String table, Value... mapping);
    int count(String column);
    void delete();


    interface Value {
        String describe();
    }
}
