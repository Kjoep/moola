package be.echostyle.dbQueries;

public abstract class MergeBuilder {

    protected String[] keyColumns = {};
    protected Object[] keyValues = {};
    protected Object[] values = {};
    protected String[] columns = {};

    public MergeBuilder(String... keyColumns){

        this.keyColumns = keyColumns;
    }

    public MergeBuilder keyValues(Object... keyValues){
        this.keyValues = keyValues;
        return this;
    }

    public MergeBuilder columns(String... columns){
        this.columns = columns;
        return this;
    }

    public MergeBuilder values(Object... values){
        this.values = values;
        return this;
    }

    public abstract void perform();

}
