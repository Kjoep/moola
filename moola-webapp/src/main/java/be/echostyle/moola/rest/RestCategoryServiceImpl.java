package be.echostyle.moola.rest;

import be.echostyle.moola.AccountService;
import be.echostyle.moola.category.Direction;
import be.echostyle.moola.category.Recurrence;
import be.echostyle.moola.rest.model.Category;
import be.echostyle.moola.rest.model.Peer;

import java.util.List;
import java.util.stream.Collectors;

import static be.echostyle.moola.rest.model.Category.fromModel;

public class RestCategoryServiceImpl implements RestCategoryService {

    private AccountService accountService;

    @Override
    public void updateCategory(String id, Category spec) {
        be.echostyle.moola.category.Category category = accountService.getCategory(id)
                .orElseGet(()->accountService.createCategory(id, spec.getName()));
        category.setName(spec.getName());
        category.setColor(spec.getColor().getFg(), spec.getColor().getBg());
        category.setDirection(toModel(spec.getDirection()));
        category.setRecurrence(toModel(spec.getRecurrence()));
        category.setParent(spec.getParentId() == null ? null : accountService.getCategory(spec.getParentId())
                .orElseThrow(()->new IllegalArgumentException("Unknown category for parent: "+spec.getParentId())));
    }

    @Override
    public Category getCategory(String id) {
        return fromModel(accountService.getCategory(id).orElse(null));
    }

    @Override
    public List<Category> findCategories(String q) {
        return accountService.findCategories(q).stream().map(Category::fromModel).collect(Collectors.toList());
    }

    private Direction toModel(Category.Direction direction){
        if (direction == null) return Direction.BOTH;
        switch (direction) {
            case incoming:
                return Direction.INCOME;
            case outgoing:
                return Direction.EXPENSE;
            default:
                throw new IllegalArgumentException("Unrecognized direction: "+direction);
        }
    }

    private Recurrence toModel(Category.Recurrence recurrence){
        if (recurrence == null) return null;
        switch (recurrence) {
            case monthly:
                return Recurrence.monthly;
            case yearly:
                return Recurrence.yearly;
            default:
                throw new IllegalArgumentException("Unrecognized direction: "+recurrence);
        }
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }}
