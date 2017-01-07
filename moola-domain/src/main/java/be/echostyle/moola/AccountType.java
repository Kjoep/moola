package be.echostyle.moola;

import be.echostyle.moola.util.Labelled;

public enum AccountType implements Labelled {

    SAVINGS("Savings"),
    CHECKING("Checking"),
    INVESTMENT("Investment"),
    GROUPED("Grouped");


    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
