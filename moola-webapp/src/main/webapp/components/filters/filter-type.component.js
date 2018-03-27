'use strict';

angular.module('moola').component('filterType', {
    bindings: {
        query: '<',
        types: '<',
        onApply: '&'
    },
    transclude: true,
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 
        function ($scope, $resource, $filter, $timeout) {
            var vm = this;

            vm.activate = function(){
                if (vm.active) return;
                vm.selected = {};
                var filter = vm.query.filters['type'] || [];
                vm.types.forEach(function(item){
                    vm.selected[item] = filter.indexOf(item) >= 0;
                });
                vm.group = vm.query.isGrouped('type');
                vm.active = true;
            }

            vm.cancel = function(){
                vm.active = false;
            }

            vm.apply = function(){
                var selection = Object.keys(vm.selected).filter(function(key){ return vm.selected[key]; })
                var newQuery = vm.query.withAddFilter('type', selection.length ? selection : null);
                newQuery.setGrouping('type', vm.group);
                vm.onApply({query: newQuery});
                vm.active = false;
            }
        }],
    templateUrl: 'components/filters/filter-type.component.html',
});