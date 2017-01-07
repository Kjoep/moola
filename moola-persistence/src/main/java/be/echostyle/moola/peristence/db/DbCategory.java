package be.echostyle.moola.peristence.db;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.Direction;

public class DbCategory extends Category{

    static final String TABLE = "category";
    static final String COL_ID = "id";
    static final String COL_NAME = "name";
    static final String COL_COLOR = "color";
    static final String[] ALL_COLS = {COL_ID, COL_NAME, COL_COLOR};

    static final String DEFAULT_COLOR = "#FFF|#00B";

    private final DbCategoryRepository repository;
    private final String id;
    private final String name;
    private String color;
    private Direction direction;

    public DbCategory(DbCategoryRepository repository, String id, String name, String color) {
        this.repository = repository;
        this.id = id;
        this.name = name;
        this.color = color;
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
    public void setColor(String fgColor, String bgColor) {
        repository.update(TABLE, COL_ID, this.id).set(COL_COLOR, fgColor+"|"+bgColor).perform();
    }

    @Override
    public void setName(String name) {
        repository.update(TABLE, COL_ID, this.id).set(COL_NAME, name).perform();
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

}
