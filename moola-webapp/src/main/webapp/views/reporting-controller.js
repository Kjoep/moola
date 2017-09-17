var Query = (function(){

    /**
     * Make a duplicate of an array, leaving out a single element.
     */
    var without = function (arr, value) {
        var r = [];
        for (var i = 0; i < arr.length; i++)
            if (arr[i] !== value)
                r.push(arr[i]);
        return r;
    }

    function Query(filters, grouping) {
        this.filters = filters || {};
        this.grouping = grouping || {};
    }
    Query.prototype = {
        asHash: function () {
            var q = [];
            var filters = this.filters;
            var grouping = this.grouping;
            q.push(...Object.keys(filters).map(function (key) { return `filter=${key}:${filters[key].join(',')}` }));
            q.push(...Object.keys(grouping).map(function (key) { return `grouping=${key}:${grouping[key]}` }));
            return q.join('&');
        },
        clone: function(){
            var filters = {};
            var grouping = {};
            Object.keys(this.filters).forEach(function(key){
                filters[key] = [...this.filters[key]];
            });
            Object.assign(grouping, this.grouping);
            return new Query(filters, grouping);
        },
        addFilter: function (key, value) {
            if (!filters[key]) filters[key] = [];
            filters[key].push(value);
        },
        removeFilter: function (key, value) {
            if (!filters[key]) return;
            filters[key] = without(filters[key], value);
            if (filters[key].length === 0) delete filters[key];
        },
        setGrouping: function (key, value) {
            if (!value && this.grouping[key])
                delete this.grouping[key];
            else
                this.grouping[key] = value;
        },
        withNewFilter: function (key, value) {
            if (value === '?') value = '';
            var filters = {};
            var grouping = {};
            filters[key] = [value];
            Object.assign(grouping, this.grouping);
            return new Query(filters, grouping);
        },
        withAddFilter: function (key, value) {
            if (value == '?') value = '';
            var r = this.clone();
            r.addFilter(key, value);
            return r;
        },
        withoutFilterValue: function (key, value) {
            if (value == '?') value = "";
            var q = this.clone();
            q.removeFilter(key, value);
            return q;
        },
        isGrouped: function (by) {
            if (Object.keys(this.grouping).length === 0) return false;
            if (by) return !!this.grouping[key];
            else return true;
        }
    }

    var parseFilter = function (query, string) {
        var string = string.split(":");
        var key = string[0];
        if (string.length == 1) {
            query.addFilter(key, '');
            return;
        }
        string[1].split(",").forEach(function(value){
            query.addFilter(key, value);
        });
    };

    var parseGrouping = function (query, string) {
        var string = string.split(":");
        var key = string[0];
        if (string.length == 1) {
            return;
        }
        query.setGrouping(key, string[1]);
    };

    Query.parseHash = function (hash) {
        var r = new Query();
        if (hash[0] == '/') hash = hash.substr(1);
        hash.split('&').forEach(function(part){
            var parts = hash[i].split("=");
            if (parts.length > 1) {
                var key = parts[0];
                var value = parts[1];
                if (key == "filter") {
                    parseFilter(r, value)
                }
                if (key == "grouping") {
                    parseGrouping(r, value);
                }
            }
        })
        return r
    }

    return Query;
}())





angular.module('moola').controller('ReportingController',
    ['$scope', '$location', '$resource', '$filter', '$http', 'Categories', 'Session',
    function ($scope, $location, $resource, $filter, $http, Categories, Session) {

    var self = this;

    self.transactions = [];
    self.categoryOptions = [];
    self.showCat = {};
    self.title = 'All transactions';
    var page=0;

    self.query = new Query();

    var currentAccount;
    var parentController = $scope.controller;

    var onAccountChanged = function(account) {
        currentAccount = account;
        if (account) {
            loadTransactions().then(function(){
                adaptCategories(self.transactions);
                createChartData();
            });
        }
        else {
            self.transactions = [];
        }
    };

    var loadTransactions = function(){
        console.log('loading transactions');
        var query = self.query;
        if (!currentAccount) return {then:function(){}};
        var q = [];
        for (var key in query.filters){
            if (!query.filters.hasOwnProperty(key)) continue;
            q.push("filter="+key+":"+query.filters[key].join(','));
        }
        for (var key in query.grouping){
            if (!query.grouping.hasOwnProperty(key)) continue;
            if (query.grouping[key]===true || query.grouping[key]==="true")
                q.push("grouping="+key);
            else
                q.push("grouping="+key+":"+query.grouping[key]);
        }
        q = q.length > 0 ? '?'+q.join('&') : "";
        self.transactions = [];
        return $http({
            method:'GET',
            url: 'http://localhost:8080/moola/rest/accounts/'+currentAccount.id+'/reports/adhoc/'+(page)+q
        }).then(function(r){
            self.transactions = r.data;
        });
    };


    self.chart = {};

    var createChartData = function(){

        if (self.query.isGrouped('date')){

            var groupName = function(transaction){
                return groupKeys.map(function(gk){
                    return transaction[gk]
                }).join('/');
            }

            var timeSlices = [];
            var lastTs;
            var groups = {};
            var groupKeys = Object.keys(self.query.grouping).filter(function(key){ return key !== 'date'});
            self.transactions.forEach(function(transaction){
                groups[groupName(transaction)] = [];
            });
            var tsIdx = -1;
            self.transactions.forEach(function(transaction){
                // we assume the timeslices are ordered
                var ts = transaction['timeSlice'];
                if (ts !== lastTs) {
                    timeSlices.push(ts);
                    Object.keys(groups).forEach(function(name){
                        groups[name].push(0);
                    })
                    tsIdx++;
                }

                groups[groupName(transaction)][tsIdx] = transaction.total;

                //this assumes we want total - but it could be count, or average that we're looking for.
            });

            self.chart = {
                type: 'bar',
                labels: timeSlices,
                groups: groups
            }
        }

    };

    self.values = function(key){
        return self.transactions.map(function(transaction){
            return transaction[key];
        });
    };

    self.transactionTypes = [
        "transfer",
        "cardPayment",
        "fixedOrder",
        "unknown",
        "managementCost",
        "withdrawal"
    ];

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

    self.newFilter = function(key, value){
        if (value=='?') value="";
        var newFilter = duplicate(filters);
        newFilters[key] = [value];
        loadTransactions().then(function(data){self.transactions = data});
    };

    self.addToFilter = function(key, value){
        if (value=='?') value="";
        if (!filters[key]) filters[key] = [];
        filters[key].push(value);
        loadTransactions().then(function(data){self.transactions = data});
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
                .get('http://localhost:8080/moola/rest/filters/rulesBacklog')
                .then(
                    function ok(resp) {
                        if (resp.data==0)
                            refresh();
                        else
                            refreshOnEmptyBacklog();
                    });
        }, 500);
    };

    self.getFilters = function(){
        return self.query.filters;
    }

    self.applyQuery = function(key, filter, group){
        console.log("Applying query on "+key);
        console.log("Filter: "+JSON.stringify(filter));
        console.log("Group: "+JSON.stringify(group));
        var newQ = duplicateQuery();
        newQ.filters[key] = filter;
        newQ.grouping[key] = group;
        if (!filter) delete newQ.filters[key];
        if (!group) delete newQ.grouping[key];
        var r = asHash(newQ);
        console.log("Going to hash: "+r);
        $location.hash("/"+r);
    }

    Session.onAccountChanged(onAccountChanged);

    $scope.$watch(function(){return $location.hash();}, function(){
        parseHash($location.hash());
        loadTransactions();
    });

    parseHash($location.hash());
    onAccountChanged(Session.account());

}]);

angular.module('moola').directive('reportQueryPanel', ['$parse', function ($parse) {

    var self = this;

    return {
        restrict: 'A',
        scope: {},
        link: function($scope, $elem, attrs){
            var key=attrs.reportQueryPanel;
            var filters = $scope.$parent.report.getFilters()[key];
            if (!filters) filters = {};
            var shown = false;
            $elem.hide();
            $elem.addClass('report-query-panel');
            $elem.parent().mousedown(function(e){
                if (shown) return;
                $elem.find('.filter-item').each(function(){
                    $(this).prop('checked',$.inArray(this.value,filters)>=0);
                });
                $elem.fadeIn(150, function(){
                    shown = true;
                    $('body').bind('mousedown', hideFunction);
                    console.log("Handler bound");
                });
            });

            if (filters.hasOwnProperty(key) && filters[key].length>0) $elem.parent().addClass('filtered');

            var apply = function(){
                var values = $elem.find('.filter-item')
                    .filter(ifChecked)
                    .map(function(idx, e){return e.value;});
                $scope.$parent.report.applyQuery(key, values);
                hideFunction();
            };

            var ifChecked = function(idx, e){
                return $(e).prop('checked');
            }

            $elem.find('.query-apply').click(apply);

            var hideFunction = function(e){
                if (e && e.target && $(e.target).parents().filter(function(idx, e){return e == $elem[0]}).length>0) return;
                $('body').unbind('mousedown', hideFunction);
                console.log('Hiding!');
                console.log(e);
                if (!shown) return;
                shown = false;
                console.log("Handler unbound");
                setTimeout(function(){
                    $elem.fadeOut(250);
                },50);
            };


        }
    };


}]);
