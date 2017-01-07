package be.echostyle.moola.filters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedFilterRepository implements FilterRepository {

    private Map<String, FilterRule> cache = new ConcurrentHashMap<>();
    private FilterRepository target;

    @Override
    public FilterRule getRule(String id) {
        return cache.computeIfAbsent(id, target::getRule);
    }

    @Override
    public FilterRule createRule(String expression) {
        FilterRule r = target.createRule(expression);
        cache.put(r.getId(), r);
        return r;
    }

    @Override
    public void deleteRule(FilterRule ref) {
        target.deleteRule(ref);
        cache.remove(ref.getId());
    }

    @Override
    public List<FilterRule> getAllRules() {
        return target.getAllRules();
    }

    public void setTarget(FilterRepository target) {
        this.target = target;
    }
}
