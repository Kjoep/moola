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
        function ($scope, $resource, $filter, $timeout, FilterService, TransactionService, Session) {

        var vm = this;

        var APPLY_ALL = "all";
        var APPLY_NO_CATEGORY = "noCategory";
        var APPLY_NO_PEER = "noPeer";
        var APPLY_NONE = "none";

        var SUBJECT_CATEGORY = "category";
        var SUBJECT_PEER = "peer";

        vm.active = false;

        vm.applyType = APPLY_ALL;
        vm.error = null;
        vm.subjectType = SUBJECT_CATEGORY;
        vm.subject = "";
        vm.filterExpression = "";
        vm.exampleTransaction = {};
        vm.exampleOutputLoading = false;
        vm.exampleOutput = [];
        vm.onSave = function () { };

        vm.formatPeer = function (trans) {
            if (trans.peer) return trans.peer.name;
            else if (trans.peerInfo) return '? ' + trans.peerInfo.name;
            else if (trans.terminalInfo) return '? ' + trans.terminalInfo.name + " " + trans.terminalInfo.location;
            else return '?';
        };

        vm.update = function(){
            vm.exampleOutputLoading = true;
            vm.error = undefined;
            doUpdate();
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
            vm.subjectType = SUBJECT_CATEGORY;
            vm.exampleTransaction = {};
            vm.filterExpression = '';
            vm.subject = undefined;
            vm.exampleOutput = [];
        };

        vm.save = function () {
            vm.saving = true;
            FilterService.saveNewFilter(vm.filterExpression, vm.subject, vm.applyType)
                .then(function(){
                    vm.saving = false;
                    vm.cancel();
                })
                .catch(function(error){
                    vm.saving = false;
                    alert(error); //TODO: add nice error handling
                });
        };

        vm.selectPeer = function (peer) {
            if (peer.id)
                vm.subject = peer;
            else {
                peer.id = getId(peer.name);
                peersResource.update({ id: newPeer.id }, newPeer).$promise.then(function () {
                    console.log("Created peer: " + JSON.stringify(newPeer))
                    vm.peerOptions.push(newPeer);
                    transactionsResource.update({ accountId: currentAccount.id, transactionId: transaction.id }, { peer: newPeer });
                })

            }
        };

        var getId = function (template) {
            return template.replace(" ", "_");
        }

        vm.selectCategory = function (category) {
            vm.subject = category;
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