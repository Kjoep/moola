package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryFactory;
import be.echostyle.moola.category.CategoryRepository;

import java.util.List;

public class DbCategoryRepository extends JdbcRepository implements CategoryRepository, CategoryFactory {

    public DbCategoryRepository(){}

    public DbCategoryRepository(JdbcRepository other) {
        super(other);
    }

    @Override
    public Category getCategory(String id) {
        if (id==null) return null;
        if (id.equals("?")) return Category.UNKNOWN;
        return from(DbCategory.TABLE)
                .where(DbCategory.COL_ID + "=?", id)
                .one(this::mapCategory, DbCategory.ALL_COLS)
                .orElse(null);
    }

    @Override
    public List<Category> findCategories(String q) {
        return from(DbCategory.TABLE)
                .where(DbCategory.COL_NAME+ " like ?", "%"+q+"%")
                .list(this::mapCategory, DbCategory.ALL_COLS);
    }

    private DbCategory mapCategory(RowAdapter rowAdapter) {
        return new DbCategory(this, rowAdapter.string(DbCategory.COL_ID), rowAdapter.string(DbCategory.COL_NAME), rowAdapter.string(DbCategory.COL_COLOR));
    }

    @Override
    public Category createCategory(String id, String name) {
        String color = DbCategory.DEFAULT_COLOR;
        insert(DbCategory.TABLE, DbCategory.COL_ID, DbCategory.COL_NAME, DbCategory.COL_COLOR).values(id, name, color);
        return new DbCategory(this, id, name, color);
    }
}
