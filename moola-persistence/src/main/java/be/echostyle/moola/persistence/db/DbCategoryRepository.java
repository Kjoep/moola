package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.category.*;

import java.util.List;
import java.util.Set;

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
        return new DbCategory(this, rowAdapter.string(DbCategory.COL_ID), rowAdapter.string(DbCategory.COL_NAME), rowAdapter.string(DbCategory.COL_COLOR), rowAdapter.value(DbCategory.COL_DIRECTION, Direction.class), rowAdapter.value(DbCategory.COL_RECURRENCE, Recurrence.class), rowAdapter.string(DbCategory.COL_PARENT));
    }

    @Override
    public Category createCategory(String id, String name) {
        String color = DbCategory.DEFAULT_COLOR;
        insert(DbCategory.TABLE, DbCategory.COL_ID, DbCategory.COL_NAME, DbCategory.COL_COLOR).values(id, name, color);
        return new DbCategory(this, id, name, color, null, null, null);
    }

    Set<Category> getCategoryChildren(String parentId) {
        return from(DbCategory.TABLE)
                .where(DbCategory.COL_PARENT + " = ?", parentId)
                .set(this::mapCategory, DbCategory.ALL_COLS);
    }
}
