package be.echostyle.moola.rest;

import be.echostyle.moola.AccountService;
import be.echostyle.moola.rest.model.Category;
import be.echostyle.moola.rest.model.Peer;

import java.util.List;
import java.util.stream.Collectors;

import static be.echostyle.moola.rest.model.Category.fromModel;

public class RestCategoryServiceImpl implements RestCategoryService {

    private AccountService accountService;

    @Override
    public void updateCategory(String id, Category spec) {
        be.echostyle.moola.category.Category category = accountService.getCategory(id);
        if (category==null){
            be.echostyle.moola.category.Category cat = accountService.createCategory(id, spec.getName());
            cat.setColor(spec.getColor().getFg(), spec.getColor().getBg());
        }
        else {
            category.setName(spec.getName());
            category.setColor(spec.getColor().getFg(), spec.getColor().getBg());
        }
    }

    @Override
    public Category getCategory(String id) {
        return fromModel(accountService.getCategory(id));
    }

    @Override
    public List<Category> findCategories(String q) {
        return accountService.findCategories(q).stream().map(Category::fromModel).collect(Collectors.toList());
    }


    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }}
