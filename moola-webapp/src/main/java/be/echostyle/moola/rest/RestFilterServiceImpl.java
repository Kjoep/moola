package be.echostyle.moola.rest;

import be.echostyle.moola.filters.FilterService;
import be.echostyle.moola.filters.FilterRule;
import be.echostyle.moola.rest.model.ApplyMode;
import be.echostyle.moola.rest.model.Filter;
import be.echostyle.moola.rest.model.FilterSpec;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class RestFilterServiceImpl implements RestFilterService {

    private FilterService filterService;

    @Override
    public void addFilter(FilterSpec filter) {
        FilterRule ref = createFilter(filter);
        switch (filter.getApply()){
            case all:
                filterService.scheduleForAll(ref); return;
            case noCategory:
                filterService.scheduleForNoCategory(ref); return;
            case noPeer:
                filterService.scheduleForNoPeer(ref); return;
        }
    }

    @Override
    public void applyFilter(String id, String applyMode) {
        ApplyMode mode = ApplyMode.valueOf(applyMode);
        FilterRule ref = filterService.getFilter(id);
        if (ref==null) throw new NotFoundException("No such filter: "+id);
        switch (mode){
            case all:
                filterService.scheduleForAll(ref); return;
            case noCategory:
                filterService.scheduleForNoCategory(ref); return;
            case noPeer:
                filterService.scheduleForNoPeer(ref); return;
        }
    }

    @Override
    public Filter getFilter(String id) {
        FilterRule ref = filterService.getFilter(id);
        if (ref==null) throw new NotFoundException("No such filter: "+id);
        return toRestModel(ref);
    }

    @Override
    public void deleteFilter(String id) {
        FilterRule ref = filterService.getFilter(id);
        if (ref==null) throw new NotFoundException("No such filter: "+id);
        filterService.deleteFiler(ref);
    }

    @Override
    public List<Filter> list() {
        List<FilterRule> filters = filterService.getAllFilters();
        return filters.stream().map(this::toRestModel).collect(Collectors.toList());
    }

    @Override
    public int getRulesToProcess() {
        return filterService.getRulesBacklog();
    }

    private Filter toRestModel(FilterRule filterRule) {
        Filter r = new Filter();
        r.setExpression(filterRule.getExpression());
        r.setId(filterRule.getId());
        if (filterRule.getCategoryToSet() !=null)
            r.setCategoryId(filterRule.getCategoryToSet().getId());
        if (filterRule.getPeerToSet() !=null)
            r.setPeerId(filterRule.getPeerToSet().getId());
        return r;
    }

    private FilterRule createFilter(FilterSpec filter) {
        if (filter.getCategoryId()!=null)
            return filterService.registerCategoryFilter(filter.getExpression(), filter.getCategoryId());
        else if (filter.getPeerId()!=null)
            return filterService.registerPeerFilter(filter.getExpression(), filter.getPeerId());
        else
            throw new IllegalArgumentException("Either a category or a peer need to be specified.");
    }

    public void setFilterService(FilterService filterService) {
        this.filterService = filterService;
    }
}
