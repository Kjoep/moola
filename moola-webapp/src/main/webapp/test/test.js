angular.module('moola').controller('TestController', ['$scope', '$resource', '$filter', '$http', 'Categories', function ($scope, $resource, $filter, $http, Categories) {

    var self = this;

    self.query = new moola.Query({type: ['miny', 'moe']},{type: true, date: 'month' });

    self.types = [
        'eenie', 'meenie', 'miny', 'moe'

    ];

    self.apply = function(newQuery){
        self.query = newQuery;
    };

}]);