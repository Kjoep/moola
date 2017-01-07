package be.echostyle.dbQueries;

public interface Mapper<T> {
    T map(RowAdapter row);
}
