angular.module('moola', ['ngResource', 'ngRoute']);

angular.module('moola').config(function($routeProvider, $locationProvider) {

  $routeProvider
    .when('/', {
        templateUrl: 'views/transactions.html',
        controllerAs: 'transactions',
        controller: 'TransactionController'
    })
    .when('/reporting', {
        templateUrl: 'views/reporting.html',
        controllerAs: 'report',
        controller: 'ReportingController'
    })
    .when('/filters', {
        templateUrl: 'views/filters.html',
        controllerAs: 'filters',
        controller: 'FilterController'
    })
    .when('/categories', {
        templateUrl: 'views/categories.html',
        controllerAs: 'categories',
        controller: 'CategoryController'
    })
    .when('/accounts', {
        templateUrl: 'views/accounts.html',
        controllerAs: 'accounts',
        controller: 'AccountController'
    })

})

.run(function(Accounts, Session){

    Accounts.query().$promise.then(function(accounts){
        if (accounts.length > 0)
            Session.account(accounts[0]);
    })

})
