angular.module('moola')
.component('accountSelection', {
    templateUrl: 'components/account-selection.html',
    controller: ['Accounts', 'Session', function(Accounts, Session){

        var vm = this;
        vm.accounts = Accounts.query();

        vm.selectedAccount = Session.account();

        Session.onAccountChanged(function(newAccount){
            vm.selectedAccount = newAccount
        })

        vm.selectAccount = function(account){
            Session.account(account);
        }

    }],
    controllerAs: 'vm'
})
