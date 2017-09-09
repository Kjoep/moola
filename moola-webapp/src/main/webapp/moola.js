Array.prototype.singleResultOr = function(fallback){
    if (this.length === 0) return fallback;
    return this[0];
}

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
        controller: 'ReportingController',
        reloadOnSearch: false
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
        Session.initAccount(accounts, accounts.singleResultOr(undefined));
    })

})
