'use strict';

angular.module('moola').component('filterCategory', {
    bindings: {
        query: '<',
        onApply: '&'
    },
    transclude: true,
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 'CategoryService',
        function ($scope, $resource, $filter, $timeout, CategoryService) {
            var vm = this;
            var categories = [];

            vm.options = [];

            vm.$onInit = function(){
                CategoryService.getCategories().then(function(result){
                    categories = result;
                });
            };

            vm.activate = function(){
                if (vm.active) return;
                vm.options = [...categories]
                vm.parentOptions = [
                    {id: '__income', name: 'Income'},
                    {id: '__expenses', name: 'Expenses'}
                ];
                vm.selected = {};
                var filter = vm.query.filters['category'] || [];
                vm.options.forEach(function(item){
                    vm.selected[item.id] = filter.indexOf(item.id) >= 0;
                });
                vm.parentOptions.forEach(function(item){
                    vm.selected[item.id] = filter.indexOf(item.id) >= 0;
                });
                vm.group = vm.query.grouping['category'] || 'none';
                vm.active = true;
            };

            vm.deselectSpecific = function(){
                vm.options.forEach(function(item){
                    vm.selected[item.id] = false;
                });
            };

            vm.deselectGeneral = function(){
                vm.parentOptions.forEach(function(item){
                    vm.selected[item.id] = false;
                });
            };

            vm.cancel = function(){
                vm.active = false;
            };

            vm.apply = function(){
                var selection = Object.keys(vm.selected).filter(function(key){ return vm.selected[key]; })
                var newQuery = vm.query.withAddFilter('category', selection.length ? selection : null);
                newQuery.setGrouping('category', vm.group);
                vm.onApply({query: newQuery});
                vm.active = false;
            };
        }],
    templateUrl: 'components/filters/filter-category.component.html',
});