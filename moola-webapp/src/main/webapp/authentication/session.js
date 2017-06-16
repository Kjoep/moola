angular.module('moola').service('Session',function(){

    var account;
    var onAccountChanged = event();

    this.onAccountChanged = onAccountChanged.registrar;

    this.account = function(value){
        if (value !==  undefined){
            account = value;
            onAccountChanged.fire(account);
        }
        return account;
    }

})