package be.echostyle.moola.category;

import java.util.Collections;
import java.util.Set;

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
        public Recurrence getRecurrence() {
            return null;
        }
        public Category getParent() {
            return null;
        }
        public Set<Category> getChildren() {
            return Collections.emptySet();
        }
        public void setDirection(Direction direction) { throw new UnsupportedOperationException("Cannot change the unknown category"); }
        public void setRecurrence(Recurrence recurrence) { throw new UnsupportedOperationException("Cannot change the unknown category"); }
        public void setParent(Category parent) { throw new UnsupportedOperationException("Cannot change the unknown category"); }
        public void setName(String name) { throw new UnsupportedOperationException("Cannot change the unknown category");}
        public void setColor(String fgColor, String bgColor) { throw new UnsupportedOperationException("Cannot change the unknown category");}
    };

    public abstract String getName();
    public abstract String getId();

    public abstract String getFgColor();
    public abstract String getBgColor();

    public abstract Direction getDirection();
    public abstract Recurrence getRecurrence();

    public abstract Category getParent();
    public abstract Set<Category> getChildren();

    public abstract void setName(String name);
    public abstract void setColor(String fgColor, String bgColor);

    public abstract void setDirection(Direction direction );
    public abstract void setRecurrence(Recurrence recurrence );
    public abstract void setParent(Category parent);

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
