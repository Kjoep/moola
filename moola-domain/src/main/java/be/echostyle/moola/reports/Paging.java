package be.echostyle.moola.reports;

import java.util.Collections;
import java.util.List;

public interface Paging<T> {

    int totalPages(int perPage);
    List<T> page(int perPage, int page);

    static <T> Paging<T> empty() {
        return new Paging<T>() {
            public int totalPages(int perPage) {
                return 0;
            }
            public List<T> page(int perPage, int page) {
                return Collections.emptyList();
            }
        };
    }
}
