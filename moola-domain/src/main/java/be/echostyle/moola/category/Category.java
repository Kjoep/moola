package be.echostyle.moola.category;

public abstract class Category {

    public static final Category UNKNOWN = new Category() {
        public Direction getDirection() {
            return Direction.BOTH;
        }
        public String getName() {
            return "?";
        }
        public String getId() {
            return "?";
        }
        public String getFgColor() {
            return "white";
        }
        public String getBgColor() {
            return "rgba(128,128,128,0.3)";
        }
        public void setName(String name) { throw new UnsupportedOperationException("Cannot change the unknown category");}
        public void setColor(String fgColor, String bgColor) { throw new UnsupportedOperationException("Cannot change the unknown category");}
    };

    public abstract Direction getDirection();

    public abstract String getName();

    public abstract String getId();

    public abstract String getFgColor();
    public abstract String getBgColor();

    public abstract void setName(String name);
    public abstract void setColor(String fgColor, String bgColor);


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Category)) return false;

        Category that = (Category) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String toString(){
        return getId();
    }

}
