package be.echostyle.moola.peristence.db;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.filters.FilterRule;
import be.echostyle.moola.filters.TransactionFilterFactory;
import be.echostyle.moola.peer.Peer;

public class DbFilterRule extends FilterRule {
    public static final String TABLE = "rule";
    public static final String COL_ID = "id";
    public static final String COL_EXPRESSION = "expression";
    public static final String COL_CATEGORY = "category_id";
    public static final String COL_PEER = "peer_id";
    public static final String[] ALL_COLS = {COL_ID, COL_EXPRESSION, COL_PEER, COL_CATEGORY};
    private DbFilterRepository repository;

    public DbFilterRule(TransactionFilterFactory factory, DbFilterRepository repository, String id, String expression, Category categoryToSet, Peer peerToSet) {
        super(factory);
        this.repository = repository;
        this.id = id;
        this.expression = expression;
        this.categoryToSet = categoryToSet;
        this.peerToSet = peerToSet;
    }

    @Override
    public void setExpression(String expression) {
        super.setExpression(expression);
        repository.update(TABLE, COL_ID, id).set(COL_EXPRESSION, expression).perform();
    }

    @Override
    public void setPeerToSet(Peer peerToSet) {
        super.setPeerToSet(peerToSet);
        repository.update(TABLE, COL_ID, id).set(COL_PEER, peerToSet.getId()).perform();
    }

    @Override
    public void setCategoryToSet(Category categoryToSet) {
        super.setCategoryToSet(categoryToSet);
        repository.update(TABLE, COL_ID, id).set(COL_CATEGORY, categoryToSet.getId()).perform();
    }
}
