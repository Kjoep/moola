package be.echostyle.moola.category;

import java.util.List;

public interface CategoryRepository {

    Category getCategory(String id);

    List<Category> findCategories(String q);
}
