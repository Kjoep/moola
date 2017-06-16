angular.module('moola').controller('TestController', ['$scope', '$resource', '$filter', '$http', 'Categories', function ($scope, $resource, $filter, $http, Categories) {

    var self = this;

    this.apply = function(key, filter, group){
        self.query.filters[key]=filter;
        self.query.grouping[key]=group;
        if (!filter) delete self.query.filters[key];
        if (!group) delete self.query.grouping[key];
    };

    self.query = {
        filters: { date: ["2016-01-01-7weeks"]},
        grouping: {
            date: 'month'
        }

    }

}]);