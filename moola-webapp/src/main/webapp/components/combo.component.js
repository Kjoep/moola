'use strict';

angular.module('moola').component('combo', {
    bindings: {
        ngModel: '=',
        filterStyle: '@',
        onSelect: '&',
        repository: '<',
        placeholder: '@'
    },
    transclude: true,
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout',
        function ($scope, $resource, $filter, $timeout) {

            var KEYS = {
                TAB: 9,
                UP: 38,
                DOWN: 40,
                ENTER: 13,
                ESCAPE: 27
            };

            var vm = this;
            vm.active = false;
            vm.selectedIndex = -1;
            vm.options = [];
            vm.filteredOptions = [];
            vm.newOption = {};

            var getCurrentSelection = function () {
                if (!vm.ngModel) return { idx: -1 };
                var index = indexById(vm.filteredOptions, vm.ngModel.id);
                return { idx: index, object: vm.ngModel };
            };

            var selectOption = function (idx) {
                vm.selectedIndex = clip(idx, -1, vm.filteredOptions.length - 1);
            };

            vm.filterKeyDown = function (e) {
                if (e.keyCode == KEYS.ESCAPE) {
                    vm.hide();
                }
                else if (e.keyCode == KEYS.TAB) {
                    /* NO OP */
                }
                else if (e.keyCode == KEYS.UP) {
                    if (!vm.active) vm.show();
                    else {
                        selectOption(vm.selectedIndex - 1);
                        e.preventDefault(); return false;
                    }
                }
                else if (e.keyCode == KEYS.DOWN) {
                    if (!vm.active) vm.show();
                    else {
                        selectOption(vm.selectedIndex + 1);
                        e.preventDefault(); return false;
                    }
                }
                else if (e.keyCode == KEYS.ENTER) {
                    if (vm.selectedIndex < 0)
                        vm.applyNewOption();
                    else
                        vm.applyOption(vm.filteredOptions[vm.selectedIndex]);
                    e.preventDefault(); return false;
                    vm.hide();
                }
                else {
                    if (!vm.active) vm.show();
                }
            };

            vm.filterKeyUp = function (e) {
                if (e.keyCode == KEYS.ESCAPE) { /*no-op*/ }
                else if (e.keyCode == KEYS.UP) { /*no-op*/ }
                else if (e.keyCode == KEYS.DOWN) { /*no-op*/ }
                else if (e.keyCode == KEYS.ENTER) { /*no-op*/ }
                else filter();
            };

            vm.show = function () {
                vm.repository.getAll()
                    .then(function (items) {
                        vm.options = items || [];
                        vm.filteredOptions = vm.options;
                    });

                var preselect = getCurrentSelection();
                vm.selectedIndex = preselect.idx;
                if (preselect.object) vm.filter = preselect.object.name;
                vm.active = true;
            };

            var filter = function () {
                var filterExp = vm.filter;
                vm.newOption = { name: filterExp, new: true };
                vm.filteredOptions = applyFilter(vm.options, filterExp);
                if (vm.filteredOptions.length == 0) vm.selectedIndex = -1;
                else {
                    vm.selectedIndex = indexByName(vm.filteredOptions, filterExp);
                    if (vm.selectedIndex < 0) vm.selectedIndex = 0;
                }
            };

            vm.$doCheck = function () {
                if (!vm.active)
                    vm.filter = vm.ngModel ? vm.ngModel.name : vm.placeholder;
            };

            /**
             * Create a new option based on the current filter expression and
             * apply it.
             * @return a Promise that resolves when the operation is complete
             */
            vm.applyNewOption = function () {
                return vm.repository.create(vm.filter)
                    .then(vm.applyOption);
            };

            vm.applyOption = function (item) {
                vm.ngModel = item;
                if (vm.onSelect) vm.onSelect({ item: item });
                vm.hide();
                vm.filter = item.name;
            };

            var indexById = function (objects, id) {
                if (!objects) return -1;
                for (var i = 0; i < objects.length; i++) {
                    if (objects[i].id == id) return i;
                }
                return -1;
            };

            var indexByName = function (objects, name) {
                if (!objects) return -1;
                for (var i = 0; i < objects.length; i++) {
                    if (objects[i].name.toLowerCase() == name.toLowerCase()) return i;
                }
                return -1;
            };

            vm.allowNew = function () {
                if (!vm.filter) return false;
                if (vm.filter.length == 0) return false;
                if (vm.filter[0] == '?') return false;
                if (exactMatch()) return false;
                return true;
            };

            var exactMatch = function () {
                return indexByName(vm.filteredOptions, vm.filter) >= 0;
            };

            var applyFilter = function (options, filterExp) {
                if (!filterExp) return options;
                var r = [];
                for (var i = 0; i < options.length; i++) {
                    var option = options[i];
                    if (option.name.toLowerCase().indexOf(filterExp.toLowerCase()) >= 0)
                        r.push(option);
                }
                return r;
            };


            vm.hide = function () {
                return $timeout(function () {
                    vm.active = false;
                }, 150);
            };

            var clip = function (value, min, max) {
                if (value < min) return min;
                if (value > max) return max;
                return value;
            };

        }],
    templateUrl: 'components/combo.component.html',
})

// see https://github.com/angular/angular.js/issues/7874
.directive('inject', function () {
    return {
        link: function ($scope, $element, $attrs, controller, $transclude) {
            if (!$transclude) {
                throw minErr('ngTransclude')('orphan',
                    'Illegal use of ngTransclude directive in the template! ' +
                    'No parent directive that requires a transclusion found. ' +
                    'Element: {0}',
                    startingTag($element));
            }
            var bindings = $attrs['inject'];
            function update(){
                var values = $scope.$eval(bindings);
                var innerScope = $scope.$new(true);
                for(var key in values){
                    innerScope[key] = values[key];
                }
                $transclude(innerScope, function (clone) {
                    $element.empty();
                    $element.append(clone);
                    $element.on('$destroy', function () {
                        innerScope.$destroy();
                    });
                });
            }
            $scope.$watch(bindings, update, true);
            update();
        }
    };
});