package be.echostyle.moola.filters.groovy;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.PeerInfo;
import be.echostyle.moola.TerminalInfo;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.Direction;
import be.echostyle.moola.filters.FilterExpressionException;
import be.echostyle.moola.filters.TransactionFilter;
import be.echostyle.moola.peer.Peer;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * This filter is intended to be used as a parent class for a groovy script.
 */
public abstract class GroovyTransactionFilter extends Script implements TransactionFilter {

    private static final Logger log = LoggerFactory.getLogger(GroovyTransactionFilter.class);

    private AccountEntry targetEntry;

    //aliases for easy access to types
    protected AccountEntryType transfer = AccountEntryType.transfer;
    protected AccountEntryType cardPayment = AccountEntryType.cardPayment;
    protected AccountEntryType fixedOrder = AccountEntryType.fixedOrder;
    protected AccountEntryType unknown = AccountEntryType.unknown;
    protected AccountEntryType managementCost = AccountEntryType.managementCost;
    protected AccountEntryType withdrawal = AccountEntryType.withdrawal;
    private String filterExpression;

    public void setTransaction(AccountEntry transaction) {
        this.targetEntry = transaction;
    }

    public String getId() {
        return targetEntry.getId();
    }

    public long getAmount() {
        return targetEntry.getAmount();
    }

    public RoPeer getPeer() {
        return RoPeer.of(targetEntry.getPeer());
    }

    public LocalDateTime getTimestamp() {
        return targetEntry.getTimestamp();
    }

    public String getComment() {
        return targetEntry.getComment();
    }

    public long getBalance() {
        return targetEntry.getBalance();
    }

    public AccountEntryType getType() {
        return targetEntry.getType();
    }

    public PeerInfo getPeerInfo() {
        return targetEntry.getPeerInfo() == null ? new PeerInfo("","") : targetEntry.getPeerInfo();
    }

    public TerminalInfo getTerminalInfo() {
        return targetEntry.getTerminalInfo() == null ? new TerminalInfo("","","") : targetEntry.getTerminalInfo();
    }

    public RoCategory getCategory() {
        return RoCategory.of(targetEntry.getCategory());
    }

    public String getDescription() {
        return targetEntry.getDescription();
    }

    @Override
    public synchronized boolean match(AccountEntry entry) {
        try {
            setTransaction(entry);
            return Boolean.TRUE.equals(run());
        } catch (Exception e){
            log.debug("Filter exception on "+entry, e);
            throw new FilterExpressionException(filterExpression);
        }
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    private static class RoPeer {
        private Peer peer;

        public RoPeer(Peer peer) {
            this.peer = peer;
        }

        static RoPeer of(Peer peer){
            return new RoPeer(peer);
        }

        public String getId() {
            return peer==null ? "": peer.getId();
        }

        public String getName() {
            return peer==null? "": peer.getName();
        }
    }

    private static class RoCategory {
        private Category category;

        public RoCategory(Category category) {
            this.category = category;
        }

        public static RoCategory of(Category category) {
            return new RoCategory(category);
        }

        public Direction getDirection() {
            return category==null ? Direction.BOTH : category.getDirection();
        }

        public String getName() {
            return category==null ? "" : category.getName();
        }

        public String getId() {
            return category==null ? "" : category.getId();
        }

    }
}
