angular.module('moola').component('currentAccount', {
    templateUrl: 'components/current-account.html',
    controller: ['Accounts', 'Session', function(Accounts, Session){

        var vm = this;
        vm.account = Session.account();

        Session.onAccountChanged(function(newAccount){
            vm.account = newAccount
        })
    }],
    controllerAs: 'vm'
})
