package be.echostyle.moola;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.category.CategoryFactory;
import be.echostyle.moola.category.CategoryRepository;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerFactory;
import be.echostyle.moola.peer.PeerRepository;

import java.util.ArrayList;
import java.util.List;

public class AccountService {

    private AccountRepository accountRepository;
    private AccountFactory accountFactory;

    private PeerRepository peerRepository;
    private PeerFactory peerFactory;

    private CategoryRepository categoryRepository;
    private CategoryFactory categoryFactory;

    public Account getAccount(String id){
        return accountRepository.getAccounts().stream()
                .filter(acc -> acc.getId().equals(id))
                .findAny().orElse(null);
    }

    public void deleteAccount(String id){
        Account acc = getAccount(id);
        if (acc==null) return;
        accountRepository.removeAccount(acc);
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accountRepository.getAccounts());
    }

    /**
     * @param id requested Id of the account
     * @param name name for the new account
     * @param type type for the new account
     * @param groupMembers members of the group -- only relevant for a group account
     */
    public Account createAccount(String id, String name, AccountType type, List<String> groupMembers) {
        if (type==AccountType.GROUPED){
            GroupedAccount account =accountFactory.createGrouped(id, name);
            for (String memberId:groupMembers){
                Account member = getAccount(memberId);
                if (member!=null)
                    account.addMember(member);
            }
            return account;
        }
        else {
            Account account = accountFactory.create(id, name, type);
            return account;
        }
    }

    public Peer getPeer(String peerId) {
        return peerRepository.getPeer(peerId);
    }

    public Peer createPeer(String id, String name) {
        Peer newPeer = peerFactory.createPeer(id, name);
        return newPeer;
    }

    public Category createCategory(String id, String name) {
        Category newCategory = categoryFactory.createCategory(id, name);
        return newCategory;
    }


    /**
     * Find peers matching the given query string.  An empty or null query string will return all peers.
     * Peers are sorted by 'popularity'
     */
    public List<Peer> findPeers(String q) {
        return peerRepository.findPeers(q);
    }

    public List<Category> findCategories(String q) {
        return categoryRepository.findCategories(q);
    }


    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void setAccountFactory(AccountFactory accountFactory) {
        this.accountFactory = accountFactory;
    }

    public void setPeerRepository(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void setPeerFactory(PeerFactory peerFactory) {
        this.peerFactory = peerFactory;
    }

    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void setCategoryFactory(CategoryFactory categoryFactory) {
        this.categoryFactory = categoryFactory;
    }

    public Category getCategory(String id) {
        return categoryRepository.getCategory(id);
    }

}
