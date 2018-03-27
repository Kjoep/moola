'use strict';

angular.module('moola').controller('TransactionController', 
    ['$scope', '$resource', '$filter', '$http', 'Categories', 'Session', 'BacklogService',
    function ($scope, $resource, $filter, $http, Categories, Session, BacklogService) {

    var self = this;

    var transactionsResource = $resource('http://localhost:8080/moola/rest/accounts/:accountId/transactions', {}, {
        all: {method:'GET', params:{}, isArray:true},
        page: {method:'GET', params:{from: ':from', limit: ':limit'}, isArray:true},
        filtered: {method:'GET', params:{filter: ':filter', limit: ':limit'}, isArray:true},
        update: {method:'POST', url:'http://localhost:8080/moola/rest/accounts/:accountId/transactions/:transactionId'}
    });

    var filtersResource = $resource('http://localhost:8080/moola/rest/filters/:id', {}, {
        add: {method:'POST', params:{}},
        backlog: {method:'GET', url:'http://localhost:8080/moola/rest/filters/rulesBacklog', isArray: false}
    });

    self.transactions = [];
    self.categoryOptions = [];

    var currentAccount = $scope.controller.activeAccount;
    var parentController = $scope.controller;
    var page = 0;

    var onAccountChanged = function(account) {
        currentAccount = account;
        if (account) {
            self.transactions = transactionsResource.all({accountId: account.id});
            self.transactions.$promise.then(function () {
                adaptCategories(self.transactions);
            });
        }
        else {
            self.transactions = [];
        }
    };

    self.loadMore = function(){
        page++;
        self.transactions = transactionsResource.page({accountId: currentAccount.id, from: page * 200, limit: 200});
    };

    Session.onAccountChanged(onAccountChanged);
    onAccountChanged(Session.account());

    var adaptCategories = function(transactions) {
        for (var i=0; i<transactions.length; i++){
            transactions[i].category = parentController.internCategory(transactions[i].category);
        }
    }

    self.updateDescription = function(transaction) {
        transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {description: transaction.description});

        //TODO: handle failure
    }

    self.getFilteredTransactions = function(expression, limit) {
        return transactionsResource.filtered({accountId: currentAccount.id, filter: expression, limit: limit});
    }

    self.updatePeer = function(transaction, newPeer){
        transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {peer: newPeer});
    };

    self.updateTransactionCategory = function(transaction, newCategory){
        transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {category: newCategory});
    };

    self.updateCategory = function(category){
        Categories.update(category);
    }

    self.createPeerFilter = function(trans){
        var proposed;
        if (trans.peerInfo)
            proposed = "peerInfo.accountNr=='"+trans.peerInfo.account+"'";
        else if (trans.terminalInfo)
            proposed = "terminalInfo.name=='"+trans.terminalInfo.name+"' && terminalInfo.location=='"+trans.terminalInfo.location+"'";
        self.filterEditor.newFilter('peer', trans, proposed, trans.peer);
    };

    self.createCategoryFilter = function(trans){
        var proposed;
        if (trans.peer)
            proposed = "peer.id=='"+trans.peer.id+"'";
        self.filterEditor.newFilter('category', trans, proposed, trans.category);
    };

    var addCategoryFilter = function(filterExp, categoryToSet, applyMode){
        filtersResource.add({id:'new'}, {expression: filterExp, categoryId: categoryToSet.id, apply: applyMode}).$promise
            .then(function(){
                growl('filter added');
                refreshOnEmptyBacklog();
            });
    };

    var addPeerFilter = function(filterExp, peerToSet, applyMode){
        filtersResource.add({id:'new'}, {expression: filterExp, peerId: peerToSet.id, apply: applyMode}).$promise
            .then(function(){
                growl('filter added');
                refreshOnEmptyBacklog();
            });
    };

    self.formatDateShort = function(dateString){
        var date = new Date(dateString);
        var now = new Date();
        if (now.getFullYear() == date.getFullYear())
            return $filter('date')(date, 'dd MMM');
        else
            return $filter('date')(date, 'dd MMM yy');
    };

    var getId = function(template){
        return template.replace(" ", "_");
    }

    self.categoryOptions = Categories.get();
    self.categoryOptions.$promise.then(function(){
        for (var i=0; i<self.categoryOptions.length; i++){
            self.categoryOptions[i] = parentController.internCategory(self.categoryOptions[i]);
        }
    });

    self.showDetails = function(transaction){
        self.detailTransaction = transaction;
        setTimeout(function(){
            $('.details-pane').fadeIn(100);
            $('body').bind('mousedown', hideDetails);
        },10);
    };

    var hideDetails = function(e){
        if ($(e.target).parents().filter('.details-pane').length>0) return;
        $('.details-pane').fadeOut(100);
        $('body').unbind('mousedown', hideDetails);
    }

    var growl = function(message){
        console.log("GROWL: "+message);
        //TODO: add growl
    };

    var refresh = function(){
        transactionsResource.page({accountId: currentAccount.id, from: page * 200, limit: 200}, function(data){
            self.transactions = data;
        });
    };

    var refreshTimer;

    var refreshOnEmptyBacklog = function(){
        if (refreshTimer) return;
        refreshTimer = setTimeout(function(){
            refreshTimer = null;
            console.log("Checking backlog")
            $http
                .get('http://localhost:8080/moola/rest/filters/rulesBacklog')
                .then(
                    function ok(resp) {
                        if (resp.data==0)
                            refresh();
                        else
                            refreshOnEmptyBacklog();
                    });
        }, 500);
    }

    $scope.$on(BacklogService.EVENT_BACKLOGCOUNT_UPDATE, refresh);

}]);
