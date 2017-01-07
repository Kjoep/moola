package be.echostyle.moola.peristence.setup;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class Setup {

    private DataSource datasource;

    public void setup(){
        Flyway flyway = new Flyway();
        flyway.setDataSource(datasource);
        flyway.baseline();
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }
}
