package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.filters.FilterRepository;
import be.echostyle.moola.filters.FilterRule;
import be.echostyle.moola.filters.TransactionFilterFactory;
import be.echostyle.moola.peer.PeerRepository;

import java.util.List;
import java.util.UUID;

public class DbFilterRepository extends JdbcRepository implements FilterRepository {

    private TransactionFilterFactory filterFactory;
    private PeerRepository peerRepository;
    private CategoryRepository categoryRepository;

    @Override
    public FilterRule getRule(String id) {
        return from(DbFilterRule.TABLE)
                .where(DbFilterRule.COL_ID + "=?", id)
                .one(this::mapFilter, DbFilterRule.ALL_COLS)
                .orElse(null);
    }

    @Override
    public List<FilterRule> getAllRules() {
        return from(DbFilterRule.TABLE)
                .list(this::mapFilter, DbFilterRule.ALL_COLS);
    }

    @Override
    public void deleteRule(FilterRule ref) {
        from(DbFilterRule.TABLE)
                .where(DbFilterRule.COL_ID + "=?", ref.getId())
                .delete();
    }

    @Override
    public FilterRule createRule(String expression) {
        String id = UUID.randomUUID().toString();
        insert(DbFilterRule.TABLE, DbFilterRule.COL_ID, DbFilterRule.COL_EXPRESSION).values(id, expression);
        return new DbFilterRule(filterFactory, this, id, expression, null, null);
    }

    private DbFilterRule mapFilter(RowAdapter rowAdapter) {
        return new DbFilterRule(filterFactory, this, rowAdapter.string(DbFilterRule.COL_ID),
                rowAdapter.string(DbFilterRule.COL_EXPRESSION),
                categoryRepository.getCategory(rowAdapter.string(DbFilterRule.COL_CATEGORY)),
                peerRepository.getPeer(rowAdapter.string(DbFilterRule.COL_PEER)));
    }

    public void setFilterFactory(TransactionFilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    public void setPeerRepository(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
}
