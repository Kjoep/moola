package be.echostyle.moola.rest.model;

import java.util.List;

public class SlicedReport {

    String title;
    String timeSliceName;
    List<Category> categories;
    List<Slice> data;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimeSliceName() {
        return timeSliceName;
    }

    public void setTimeSliceName(String timeSliceName) {
        this.timeSliceName = timeSliceName;
    }

    public List<Slice> getData() {
        return data;
    }

    public void setData(List<Slice> data) {
        this.data = data;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
