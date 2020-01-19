package be.echostyle.moola.persistence.db;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.Direction;
import be.echostyle.moola.category.Recurrence;

import java.util.Set;

public class DbCategory extends Category{

    static final String TABLE = "category";
    static final String COL_ID = "id";
    static final String COL_NAME = "name";
    static final String COL_COLOR = "color";
    static final String COL_DIRECTION = "direction";
    static final String COL_RECURRENCE = "recurrence";
    static final String COL_PARENT = "parent_id";
    static final String[] ALL_COLS = {COL_ID, COL_NAME, COL_COLOR, COL_DIRECTION, COL_RECURRENCE, COL_PARENT};

    static final String DEFAULT_COLOR = "#FFF|#00B";

    private final DbCategoryRepository repository;
    private final String id;
    private final String name;
    private String color;
    private Direction direction;
    private Recurrence recurrence;
    private String parentId;

    public DbCategory(DbCategoryRepository repository, String id, String name, String color, Direction direction, Recurrence recurrence, String parentId) {
        this.repository = repository;
        this.id = id;
        this.name = name;
        this.color = color;
        this.recurrence = recurrence;
        this.direction = direction;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getFgColor() {
        return color==null?null: color.split("\\|")[0];
    }

    @Override
    public String getBgColor() {
        return color==null?null:color.split("\\|")[1];
    }

    @Override
    public Category getParent() {
        return parentId == null ? null : repository.getCategory(parentId);
    }

    @Override
    public Set<Category> getChildren() {
        return repository.getCategoryChildren(id);
    }

    @Override
    public void setColor(String fgColor, String bgColor) {
        update(COL_COLOR, fgColor+"|"+bgColor);
    }

    @Override
    public void setName(String name) {
        update(COL_NAME, name);
    }

    @Override
    public void setDirection(Direction direction) {
        update(COL_DIRECTION, direction == null ? null : direction.name());
    }

    @Override
    public void setRecurrence(Recurrence recurrence) {
        update(COL_RECURRENCE, recurrence == null ? null : recurrence.name());
    }

    @Override
    public void setParent(Category parent) {
        update(COL_PARENT, parent == null ? null : parent.getId());
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public Recurrence getRecurrence() {
        return recurrence;
    }

    private void update(String column, Object value){
        repository.update(TABLE, COL_ID, this.id).set(column, value).perform();
    }
}
