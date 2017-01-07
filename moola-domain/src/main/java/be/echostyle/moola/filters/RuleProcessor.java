package be.echostyle.moola.filters;

import be.echostyle.moola.AccountEntry;

public interface RuleProcessor {
    int scheduleForAll(FilterRule ref);
    int scheduleForNoCategory(FilterRule ref);
    int scheduleForNoPeer(FilterRule ref);
    int scheduleAll(AccountEntry entry);
    void schedule(AccountEntry entry, FilterRule rule);

    int getBacklog();

}
