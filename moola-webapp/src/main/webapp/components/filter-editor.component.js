var debounce = function(fn, $timeout, delay){
    var promise;

    return function(){
        if (promise) $timeout.cancel(promise);
        promise = $timeout(fn, delay || 500);
    }
};

angular.module('moola').component('filterEditor', {
    bindings: {
        ngModel: '=',
        onSave: '&'
    },
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 'TransactionService', 'FilterService', 'Session',
        function ($scope, $resource, $filter, $timeout, TransactionService, FilterService, Session) {

        var vm = this;

        var APPLY_ALL = "all";
        var APPLY_NO_CATEGORY = "noCategory";
        var APPLY_NO_PEER = "noPeer";
        var APPLY_NONE = "none";

        vm.active = false;

        vm.applyType = APPLY_ALL;
        vm.error = null;
        vm.subjectType = FilterService.SUBJECT_CATEGORY;
        vm.subject = {};
        vm.filterExpression = "";
        vm.exampleTransaction = {};
        vm.exampleOutputLoading = false;
        vm.exampleOutput = [];
        vm.onSave = function () { };

        vm.update = function(){
            vm.exampleOutputLoading = true;
            vm.error = undefined;
            doUpdate();
        };

        vm.setFilter = function(expr){
            vm.filterExpression = expr;
            vm.update();
        };

        var doUpdate = debounce(function () {
            TransactionService.getTransactionsByFilter(Session.account().id, vm.filterExpression, 10)
                .then(function (transactions) {
                    vm.exampleOutput = transactions;
                    vm.exampleOutputLoading = false;
                })
                .catch(function (error) {
                    vm.error = "Invalid expression: " + error;
                    vm.exampleOutputLoading = false;
                });
        }, $timeout, 500);

        vm.cancel = function () {
            vm.active = false;
            vm.subjectType = FilterService.SUBJECT_CATEGORY;
            vm.exampleTransaction = {};
            vm.filterExpression = '';
            vm.subject = undefined;
            vm.exampleOutput = [];
        };

        vm.save = function () {
            vm.saving = true;
            FilterService.saveNewFilter(vm.filterExpression, vm.subjectType, vm.subject, vm.applyType)
                .then(function(){
                    vm.saving = false;
                    vm.cancel();
                    vm.onSave({});
                })
                .catch(function(error){
                    vm.saving = false;
                    alert(error); //TODO: add nice error handling
                });
        };

        var getId = function (template) {
            return template.replace(" ", "_");
        }

        vm.$onInit = function(){
            vm.ngModel = {
                newFilter: function (type, exampleTransaction, proposedFilter, subject){
                    vm.active = true;
                    vm.subjectType = type;
                    vm.exampleTransaction = exampleTransaction;
                    vm.filterExpression = proposedFilter;
                    vm.subject = subject;
                    vm.update();
                }
            };
        }
    }],
    templateUrl: 'components/filter-editor.component.html',
});