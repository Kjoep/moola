moolaApp.controller('TransactionController', ['$scope', '$resource', '$filter', '$http', 'Categories', function ($scope, $resource, $filter, $http, Categories) {

    var self = this;

    var transactionsResource = $resource('rest/accounts/:accountId/transactions', {}, {
        all: {method:'GET', params:{}, isArray:true},
        filtered: {method:'GET', params:{filter: ':filter', limit: ':limit'}, isArray:true},
        slices: {method:'GET', url:'rest/accounts/:accountId/slices', params:{from: ':from', to: ':to'}},
        update: {method:'POST', url:'rest/accounts/:accountId/transactions/:transactionId'}
    });

    var filtersResource = $resource('rest/filters/:id', {}, {
        add: {method:'POST', params:{}},
        backlog: {method:'GET', url:'rest/filters/rulesBacklog', isArray: false}
    });

    self.transactions = [];
    self.timeSlices = {timeSliceName: ""};
    self.categoryOptions = [];
    self.showCat = {};

    var currentAccount = $scope.controller.activeAccount;
    var parentController = $scope.controller;

    self.selectAccount = function(account) {
        currentAccount = account;
        if (account) {
            self.transactions = transactionsResource.all({accountId: account.id});
            self.timeSlices = {timeSliceName: ""};
            self.transactions.$promise.then(function () {
                adaptCategories(self.transactions);
                loadSlices();
            });
        }
        else {
            self.transactions = [];
            self.timeSlices = {timeSliceName: ""};
        }
    };

    var adaptCategories = function(transactions) {
        for (var i=0; i<transactions.length; i++){
            transactions[i].category = parentController.internCategory(transactions[i].category);
        }
    }

    self.updateDescription = function(transaction) {
        transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {description: transaction.description});

        //TODO: handle failure
    }

    self.formatPeer = function(trans){
        if (trans.peer) return trans.peer.name;
        else if (trans.peerInfo) return '? '+trans.peerInfo.name;
        else if (trans.terminalInfo) return '? '+trans.terminalInfo.name+" "+trans.terminalInfo.location;
        else return '?';
    };

    self.formatPeerLong = function(trans){
        var r = ''
        if (trans.peer) r = trans.peer.name+' :: ';

        if (trans.peerInfo) return r+trans.peerInfo.name+' ('+trans.peerInfo.account+')';
        if (trans.terminalInfo) return r+trans.terminalInfo.name+" "+trans.terminalInfo.location+' (card '+trans.terminalInfo.card+')';

        return r;
    };

    self.formatPeerClass = function(trans){
        if (trans.peer) return 'peer-'+trans.peer.class;
        else return 'peer-unknown';
    };

    var loadSlices = function(){
        if (self.transactions.length<2) {
            self.timeSlices = {timeSliceName: ""};
            return;
        }

        var endTime = self.transactions[0].timestamp;
        var startTime = self.transactions[self.transactions.length-1].timestamp;

        self.timeSlices = transactionsResource.slices({accountId: currentAccount.id, from: startTime, to:endTime});
    };

    self.getFilteredTransactions = function(expression, limit) {
        return transactionsResource.filtered({accountId: currentAccount.id, filter: expression, limit: limit});
    }

    self.updatePeer = function(transaction){
        console.log("Selecting peer for "+JSON.stringify(transaction));
        return function(newPeer){
            transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {peer: newPeer});
        }
    };

    self.updateTransactionCategory = function(transaction){
        console.log("Selecting category for "+JSON.stringify(transaction));
        return function(newCat){
            transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {category: newCat});
            self.showCat[newCat.id] = true;
        }
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
        self.showFilterEditor('peer', trans, proposed, trans.peer, addPeerFilter);
    };

    self.createCategoryFilter = function(trans){
        var proposed;
        if (trans.peer)
            proposed = "peer.id=='"+trans.peer.id+"'";
        self.showFilterEditor('category', trans, proposed, trans.category, addCategoryFilter);
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

    self.showFilterEditor = function(type, exampleTransaction, proposedFilter, subject){
        //this is a stub
    };

    $scope.$watch('controller.activeAccount ', function(){
        self.selectAccount($scope.controller.activeAccount);
    })

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

    self.showAllCategories = function(val){
        self.showCat['?'] = val;
        for (var i=0; i<self.categoryOptions.length; i++)
            self.showCat[self.categoryOptions[i].id] = val;
    }

    self.categoryOptions = Categories.get();
    self.categoryOptions.$promise.then(function(){
        for (var i=0; i<self.categoryOptions.length; i++){
            self.categoryOptions[i] = parentController.internCategory(self.categoryOptions[i]);
        }
        self.showAllCategories(true);
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
        transactionsResource.all({accountId: currentAccount.id}, function(data){
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
                .get('rest/filters/rulesBacklog')
                .then(
                    function ok(resp) {
                        if (resp.data==0)
                            refresh();
                        else
                            refreshOnEmptyBacklog();
                    });
        }, 500);
    }

}]);

moolaApp.directive('colorPicker',['$parse', function($parse) {
    return {
        restrict: 'A',
        scope: {
            onSelect: '&'
        },
        link: function($scope, $elem, attrs){
            var $picker = $elem.find('.colorPicker');

            var modelExp = attrs.colorPicker;

            $picker.insertAfter($elem);
            $picker.hide();

            $scope.colors = ['#000000','#404040','#808080','#C0C0C0','#FFFFFF',
                '#800000','#D00000','#FF0000',
                '#FFFF00','#FFFF60','#FFFFA0',
                '#008000','#00D000','#00FF00',
                '#000080','#0000D0','#0000FF']

           $elem.click(function(){
               $picker.show();
               var right =$picker.offset().left + $picker.width();
               if (right > $(window).width()){
                   $picker.css({left: '-'+(right - $(window).width())+'px'})
               }
           })

            $scope.pick = function(fg, bg){
               var selected = {fg:fg, bg:bg};

                if ($scope.onSelect && typeof $scope.onSelect == "function" ){
                    var apply = $scope.onSelect($scope.$parent);
                    if (typeof apply == "function") {
                        var adapted = $scope.onSelect($scope.$parent)(selected);
                        if (adapted) selected = adapted;
                    }
                }
                if (modelExp) {
                    var $model = $parse(modelExp);
                    $model.assign($scope.$parent, selected);
                }

                $picker.hide();
            }
        },
        templateUrl: 'components/colorPicker.html'
    }

}]);
