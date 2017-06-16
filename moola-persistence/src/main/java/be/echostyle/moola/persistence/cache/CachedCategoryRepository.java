package be.echostyle.moola.persistence.cache;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;

import java.util.List;

public class CachedCategoryRepository implements CategoryRepository {

    private Cache<String, Category> cache = new MapCache<>();
    private CategoryRepository target;

    public CachedCategoryRepository(CategoryRepository target) {
        this.target = target;
    }

    @Override
    public Category getCategory(String id) {
        return cache.get(id, ()->target.getCategory(id));
    }

    @Override
    public List<Category> findCategories(String q) {
        List<Category> r = target.findCategories(q);
        for (Category cat:r){
            cache.put(cat.getId(), cat);
        }
        return r;
    }
}
