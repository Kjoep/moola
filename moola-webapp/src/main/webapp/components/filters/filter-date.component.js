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

            vm.selectRange = function(fromExp, toExp){
                vm.from = parseExpression(fromExp);
                vm.to = parseExpression(toExp);
            }
            
            var parseExpression = function(expression){
                var matcher = /^(-?\d+)(d|w|m|y)$/.exec(expression);
                if (!matcher) return undefined;
                var now = moment();
                if (matcher[2] === 'd'){
                    return now.add(matcher[1], 'days').toDate();
                }
                if (matcher[2] === 'w'){
                    return now.add(matcher[1], 'weeks').toDate();
                }
                if (matcher[2] === 'm'){
                    return now.add(matcher[1], 'months').toDate();
                }
                if (matcher[2] === 'y'){
                    return now.add(matcher[1], 'years').toDate();
                }
            }

            var format = function(date){
                if (date === undefined) return '';
                return moment(date).toISOString().split('T')[0];
            }

            vm.activate = function(){
                if (vm.active) return;
                vm.filter = vm.query.filters['date'] || 'none';
                vm.group = vm.query.grouping['date'] || 'none';
                var rangeMatcher = /(\d\d\d\d-\d\d-\d\d)-(\d\d\d\d-\d\d-\d\d)/.exec(vm.filter[0]);
                if (rangeMatcher){
                    vm.from = rangeMatcher[1];
                    vm.to = rangeMatcher[2];
                }
                vm.active = true;
            };

            vm.cancel = function(){
                vm.active = false;
            };

            vm.apply = function(){
                var filter = null;
                if (vm.from || vm.to){
                    filter = format(vm.from)+'-'+format(vm.to);
                }
                var newQuery = vm.query.withAddFilter('date', filter === null ? null : [filter]);
                newQuery.setGrouping('date', vm.group === 'none' ? null : vm.group);
                vm.onApply({query: newQuery});
                vm.active = false;
            };
        }],
    templateUrl: 'components/filters/filter-date.component.html',
});