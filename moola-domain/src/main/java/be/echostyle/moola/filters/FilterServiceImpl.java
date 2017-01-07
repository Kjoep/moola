package be.echostyle.moola.filters;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;

import java.util.List;

public class FilterServiceImpl implements FilterService {

    private TransactionFilterFactory transactionFilterFactory;
    private FilterRepository filterRepository;
    private RuleProcessor ruleProcessor;
    private PeerRepository peerRepository;
    private CategoryRepository categoryRepository;

    public TransactionFilter filter(String filterExpression){
        return transactionFilterFactory.createFilter(filterExpression);
    }

    @Override
    public List<FilterRule> getAllFilters() {
        return filterRepository.getAllRules();
    }

    @Override
    public FilterRule getFilter(String id) {
        return filterRepository.getRule(id);
    }

    @Override
    public void deleteFiler(FilterRule ref) {
        filterRepository.deleteRule(ref);
    }

    public FilterRule registerCategoryFilter(String filterExpression, String categoryId) {
        Category category = categoryRepository.getCategory(categoryId);
        if (category==null) throw new IllegalArgumentException("Unknown category: "+categoryId);
        FilterRule filter = filterRepository.createRule(filterExpression);
        filter.setCategoryToSet(category);
        return filter;
    }

    @Override
    public FilterRule registerPeerFilter(String filterExpression, String peerId) {
        Peer peer = peerRepository.getPeer(peerId);
        if (peer==null) throw new IllegalArgumentException("Unknown peer: "+peerId);
        FilterRule filter = filterRepository.createRule(filterExpression);
        filter.setPeerToSet(peer);
        return filter;
    }

    @Override
    public int scheduleForAll(FilterRule ref) {
        return ruleProcessor.scheduleForAll(ref);
    }

    @Override
    public int scheduleForNoCategory(FilterRule ref) {
        return ruleProcessor.scheduleForNoCategory(ref);
    }

    @Override
    public int scheduleForNoPeer(FilterRule ref) {
        return ruleProcessor.scheduleForNoPeer(ref);
    }

    @Override
    public int scheduleAllFilters(AccountEntry entry) {
        return ruleProcessor.scheduleAll(entry);
    }

    @Override
    public int getRulesBacklog() {
        return ruleProcessor.getBacklog();
    }

    public void setTransactionFilterFactory(TransactionFilterFactory transactionFilterFactory) {
        this.transactionFilterFactory = transactionFilterFactory;
    }

    public void setFilterRepository(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    public void setRuleProcessor(RuleProcessor ruleProcessor) {
        this.ruleProcessor = ruleProcessor;
    }

    public void setPeerRepository(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
}
