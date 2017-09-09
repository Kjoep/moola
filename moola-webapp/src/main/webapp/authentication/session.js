angular.module('moola').service('Session', ['$log', function($log){

    var account;
    var onAccountChanged = event();

    this.onAccountChanged = onAccountChanged.registrar;

    this.account = function(value){
        if (value !==  undefined){
            account = value;
            updateStorage();
            onAccountChanged.fire(account);
        }
        return account;
    }

    this.initAccount = function(candidates, fallback){
        var initialName = localStorage.getItem('account') || '';
        if (initialName === '')
            account = fallback;
        else
            account = candidates
                .filter(function(candidate){ return candidate.name === initialName})
                .singleResultOr(fallback)
    }

    var updateStorage = function(){
        localStorage.setItem('account', account ? account.name : '');
    }

}]);
