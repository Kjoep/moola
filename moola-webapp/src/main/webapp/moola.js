angular.module('moola', ['ngResource', 'ngRoute']);

angular.module('moola').config(function($routeProvider, $locationProvider) {

  $routeProvider
    .when('/', {
        templateUrl: 'views/transactions.html',
        controllerAs: 'transactions',
        controller: 'TransactionController'
    })
    .when('/filters', {
        templateUrl: 'views/filters.html',
        controllerAs: 'filters',
        controller: 'FilterController'
    })

})

.run(function(Accounts, Session){

    Accounts.query().$promise.then(function(accounts){
        if (accounts.length > 0)
            Session.account(accounts[0]);
    })

})
