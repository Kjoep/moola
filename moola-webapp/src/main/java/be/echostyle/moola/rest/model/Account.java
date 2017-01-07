package be.echostyle.moola.rest.model;

import be.echostyle.moola.rest.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    private String name;
    private String id;
    private AccountType type;
    private List<String> groupMembers;

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public List<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Account forName(String name) {
        Account r = new Account();
        r.name = name;
        return r;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Account fromModel(be.echostyle.moola.Account account) {
        Account r = new Account();
        r.name = account.getName();
        return r;
    }

}
