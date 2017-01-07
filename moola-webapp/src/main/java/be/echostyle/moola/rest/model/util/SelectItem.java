package be.echostyle.moola.rest.model.util;

import be.echostyle.moola.util.Labelled;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectItem {

    private String value;
    private String label;

    public SelectItem(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public SelectItem() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static <T extends Enum<T> & Labelled> List<SelectItem> fromEnum(Class<T> clazz) {
        T[] constants = clazz.getEnumConstants();

        return Stream.of(constants).map(SelectItem::fromEnumConstant).collect(Collectors.toList());
    }

    public static <T extends Enum<T> & Labelled> SelectItem fromEnumConstant(T item){
        return new SelectItem(item.name(), item.label());
    }
}
