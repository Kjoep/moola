package be.echostyle.moola.filters;

import be.echostyle.moola.AccountEntry;

import java.util.List;

public interface FilterService {
    TransactionFilter filter(String filterExpression);

    FilterRule registerCategoryFilter(String filterExpression, String categoryId);
    FilterRule registerPeerFilter(String filterExpression, String peerId);

    List<FilterRule> getAllFilters();
    FilterRule getFilter(String id);
    void deleteFiler(FilterRule ref);

    int scheduleForAll(FilterRule ref);
    int scheduleForNoCategory(FilterRule ref);
    int scheduleForNoPeer(FilterRule ref);
    int scheduleAllFilters(AccountEntry entry);

    /**
     * @return the number of rules left to process
     */
    int getRulesBacklog();

}
