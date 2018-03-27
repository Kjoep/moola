'use strict';

angular.module('moola').component('filterDate', {
    bindings: {
        query: '<',
        onApply: '&'
    },
    transclude: true,
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 'CategoryService',
        function ($scope, $resource, $filter, $timeout, CategoryService) {
            var vm = this;

            vm.activate = function(){
                if (vm.active) return;
                vm.filter = vm.query.filters['date'] || 'none';
                vm.group = vm.query.grouping['date'] || 'none';
                vm.active = true;
            };

            vm.cancel = function(){
                vm.active = false;
            };

            vm.apply = function(){
                var filter = vm.filter;
                if (filter === 'custom')
                    filter = vm.customFrom+'-'+vm.customNr+vm.customType;
                var newQuery = vm.query.withAddFilter('date', filter === 'none' ? null : [filter]);
                newQuery.setGrouping('date', vm.group === 'none' ? null : vm.group);
                vm.onApply({query: newQuery});
                vm.active = false;
            };
        }],
    templateUrl: 'components/filters/filter-date.component.html',
});