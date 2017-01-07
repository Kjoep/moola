package be.echostyle.moola.filters;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.peer.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter expression registered to perform actions, such as automatically setting a cateogory.
 *
 * Essentially, this is a rule.
 */
public abstract class FilterRule {

    private static final Logger log = LoggerFactory.getLogger(FilterRule.class);

    protected String id;
    protected String expression;
    protected FilterService filterService;
    protected Peer peerToSet;
    protected Category categoryToSet;

    private TransactionFilter compiled;
    private TransactionFilterFactory transactionFilterFactory;

    public FilterRule(TransactionFilterFactory transactionFilterFactory) {
        this.transactionFilterFactory = transactionFilterFactory;
    }

    private TransactionFilter getFilter(){
        if (compiled==null)
            compiled = transactionFilterFactory.createFilter(expression);
        return compiled;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setPeerToSet(Peer peerToSet) {
        this.peerToSet = peerToSet;
    }

    public void setCategoryToSet(Category categoryToSet) {
        this.categoryToSet = categoryToSet;
    }

    public String getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public Peer getPeerToSet() {
        return peerToSet;
    }

    public Category getCategoryToSet() {
        return categoryToSet;
    }

    public FilterService getFilterService() {
        return filterService;
    }

    public void apply(AccountEntry entry) {
        if (getFilter().match(entry)){
            log.debug("Filter matched for entry#{}. Setting peer {}, category {}", entry.getId(), peerToSet, categoryToSet);
            if (peerToSet!=null) entry.setPeer(peerToSet);
            if (categoryToSet!=null) entry.setCategory(categoryToSet);
        }
    }
}
