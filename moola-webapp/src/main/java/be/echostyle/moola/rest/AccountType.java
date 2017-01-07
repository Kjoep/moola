package be.echostyle.moola.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AccountType {

    SAVINGS,
    CHECKING,
    INVESTMENT,
    GROUPED;

    private static final Logger log = LoggerFactory.getLogger(AccountType.class);

    public static AccountType fromModel(be.echostyle.moola.AccountType type) {
        switch (type){
            case CHECKING: return AccountType.CHECKING;
            case GROUPED: return AccountType.GROUPED;
            case INVESTMENT: return AccountType.INVESTMENT;
            case SAVINGS: return AccountType.SAVINGS;
            default:
                log.warn("Unmappable account type: {}. Returning CHECKING as a default", type);
                return AccountType.CHECKING;
        }
    }



}
