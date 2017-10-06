'use strict';

angular.module('moola').component('categorySelection', {
    bindings: {
        ngModel: '=',
        onSelect: '&',
        placeholder: '@'
    },
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 'CategoryService',
        function ($scope, $resource, $filter, $timeout, CategoryService) {
            var vm = this;
            vm.$onInit = function(){
                vm.repository = {
                    getAll: CategoryService.getCategories,
                    create: CategoryService.create
                };
            };

            vm.onComboSelect = function(item){
                vm.onSelect({item: item});
            }
        }],
    templateUrl: 'components/category-selection.component.html',
});