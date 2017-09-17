angular.module('moola').controller('FilterEditorController', ['$scope', '$resource', '$filter', function ($scope, $resource, $filter) {

    var self = this;

    var APPLY_ALL = "all";
    var APPLY_NO_CATEGORY = "noCategory";
    var APPLY_NO_PEER = "noPeer";
    var APPLY_NONE = "none";

    var SUBJECT_CATEGORY = "category";
    var SUBJECT_PEER = "peer";

    self.applyType = APPLY_ALL;
    self.error = null;
    self.subjectType = SUBJECT_CATEGORY;
    self.subject = "";
    self.filterExpression = "";
    self.exampleTransaction = {};
    self.exampleOutput = [];
    self.onSave = function(){};

    var updateTimer;

    self.update = function(){
        clearTimeout(updateTimer);
        updateTimer = setTimeout(function(){
            self.exampleOutput = $scope.transactions.getFilteredTransactions(self.filterExpression, 10);
            handleErrors(self.exampleOutput);
        },300);
    };

    self.cancel = function(){
        $('.filter-editor').hide();
    };

    self.save = function(){
        var r = self.onSave(self.filterExpression, self.subject, self.applyType);
        if (r) {
            alert(r);
        }
        else {
            $('.filter-editor').hide();
        }
    };

    self.selectPeer = function(peer){
        if (peer.id)
            self.subject = peer;
        else {
            peer.id = getId(peer.name);
            peersResource.update({id:newPeer.id}, newPeer).$promise.then(function(){
                console.log("Created peer: "+JSON.stringify(newPeer))
                self.peerOptions.push(newPeer);
                transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {peer: newPeer});
            })

        }
    };

    var getId = function(template){
        return template.replace(" ", "_");
    }

    self.selectCategory = function(category){
        self.subject = category;
    }

    if ($scope.transactions) {
        $scope.transactions.showFilterEditor = function (type, exampleTransaction, proposedFilter, subject, onSave) {
            self.subjectType = type;
            self.exampleTransaction = cleanup(exampleTransaction);
            self.onSave = onSave;
            if (subject) {
                self.subject = subject;
            }
            if (proposedFilter) {
                self.filterExpression = proposedFilter;
                self.exampleOutput = $scope.transactions.getFilteredTransactions(self.filterExpression, 10);
                handleErrors(self.exampleOutput);
            }
            $('.filter-editor').show();
        }
    }

    var handleErrors = function($resource){
        $resource.$promise.then(function(){
            self.error = null;
        }, function(e){
            self.error = "Invalid expression: "+e.data;
        })
    }

    var cleanup = function(t){
        if (!t) return undefined;
        var r = {
            id:t.id,
            type:t.type,
            amount: t.amount,
            timeStamp: t.timeStamp,
            comment: t.comment
        };
        if (t.peer) r.peer = {
            id: t.peer.id,
            name: t.peer.name
        };
        if (t.category && t.category.id!='?') r.category= {
            id: t.category.id,
            name: t.category.name,
            direction: t.category.direction
        };
        if (t.peerInfo) r.peerInfo = {
            name: t.peerInfo.name,
            accountNr: t.peerInfo.account
        };
        if (t.terminalInfo) r.terminalInfo = {
            name: t.terminalInfo.name,
            location: t.terminalInfo.location,
            card: t.terminalInfo.card
        };
        return r;
    }
}]);

