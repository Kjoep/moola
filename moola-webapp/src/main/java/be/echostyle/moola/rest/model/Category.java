package be.echostyle.moola.rest.model;

public class Category {

    private String id;
    private String name;
    private Color color;
    private Direction direction;
    private Recurrence recurrence;
    private String parentId;

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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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
        r.setParentId(category.getParent() == null ? null : category.getParent().getId());
        r.setDirection(directionFromModel(category.getDirection()));
        r.setRecurrence(recurrenceFromModel(category.getRecurrence()));
        return r;
    }

    private static Recurrence recurrenceFromModel(be.echostyle.moola.category.Recurrence recurrence) {
        if (recurrence == null) return null;
        switch (recurrence) {
            case monthly: return Recurrence.monthly;
            case yearly: return Recurrence.yearly;
            default: throw new IllegalArgumentException("Unsupported recurrence: "+recurrence );
        }
    }

    private static Direction directionFromModel(be.echostyle.moola.category.Direction direction) {
        if (direction == null) return null;
        switch (direction) {
            case INCOME: return Direction.incoming;
            case EXPENSE: return Direction.outgoing;
            case BOTH: return null;
            default:
                throw new IllegalArgumentException("Unsupported direction: "+direction);
        }
    }

    public enum Direction {
        incoming, outgoing
    }

    public enum Recurrence {
        monthly, yearly
    }

}
