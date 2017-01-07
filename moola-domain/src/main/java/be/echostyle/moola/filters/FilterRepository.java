package be.echostyle.moola.filters;

import java.util.List;

public interface FilterRepository {
    FilterRule getRule(String id) ;
    FilterRule createRule(String expression);
    void deleteRule(FilterRule ref);
    List<FilterRule> getAllRules();

}
