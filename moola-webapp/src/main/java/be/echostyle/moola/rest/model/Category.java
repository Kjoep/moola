package be.echostyle.moola.rest.model;

public class Category {

    private String id;
    private String name;
    private Color color;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static class Color {

        private String fg, bg;

        public Color() {
        }

        public Color(String fgColor, String bgColor) {
            this.fg = fgColor;
            this.bg = bgColor;
        }

        public String getFg() {
            return fg;
        }

        public void setFg(String fg) {
            this.fg = fg;
        }

        public String getBg() {
            return bg;
        }

        public void setBg(String bg) {
            this.bg = bg;
        }
    }

    public static Category fromModel(be.echostyle.moola.category.Category category) {
        if (category==null) return null;
        Category r = new Category();
        r.setName(category.getName());
        r.setId(category.getId());
        r.setColor(new Color(category.getFgColor(), category.getBgColor()));
        return r;
    }


}
