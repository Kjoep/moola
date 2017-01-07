package be.echostyle.dbQueries;

public interface UpdateBuilder {

    UpdateBuilder set(String column, Object value);
    void perform();

}
