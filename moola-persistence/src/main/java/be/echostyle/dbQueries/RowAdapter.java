package be.echostyle.dbQueries;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public interface RowAdapter {
    String string(String columnName);
    int integer(String columnName);
    long longInt(String columnName);
    LocalDateTime dateTime(String columnName);
    List<Object> all();

    default <T> T reference(String columnName, Function<String, T> fetcher){
        String str = string(columnName);
        if (str==null || str.equals("")) return null;
        return fetcher.apply(str);
    }

    default <T extends Enum<T>> T value(String columnName, Class<T> enumClass) {
        return reference(columnName, v -> Enum.valueOf(enumClass, v));
    }

}
