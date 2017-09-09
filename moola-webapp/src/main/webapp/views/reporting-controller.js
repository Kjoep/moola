angular.module('moola').controller('ReportingController',
    ['$scope', '$location', '$resource', '$filter', '$http', 'Categories', 'Session',
    function ($scope, $location, $resource, $filter, $http, Categories, Session) {

    var self = this;

    self.transactions = [];
    self.timeSlices = {timeSliceName: ""};
    self.categoryOptions = [];
    self.showCat = {};
    self.title = 'All transactions';
    var page=0;

    var query = {
        filters: {},
        grouping: {}
    };

    var currentAccount;
    var parentController = $scope.controller;

    var onAccountChanged = function(account) {
        currentAccount = account;
        if (account) {
            self.timeSlices = {timeSliceName: ""};
            loadTransactions().then(function(){
                adaptCategories(self.transactions);
            });
        }
        else {
            self.transactions = [];
            self.timeSlices = {timeSliceName: ""};
        }
    };

    var implode = function(array, joiner){
        if (array.length == 0) return "";
        var r = array[0];
        for (var i=1; i<array.length; i++)
            r+=joiner+array[i];
        return r;
    }

    var loadTransactions = function(){
        if (!currentAccount) return {then:function(){}};
        var q = [];
        for (var key in query.filters){
            if (!query.filters.hasOwnProperty(key)) continue;
            q.push("filter="+key+":"+implode(query.filters[key],','));
        }
        for (var key in query.grouping){
            if (!query.grouping.hasOwnProperty(key)) continue;
            if (query.grouping[key]===true || query.grouping[key]==="true")
                q.push("grouping="+key);
            else
                q.push("grouping="+key+":"+query.grouping[key]);
        }
        q = q.length > 0 ? '?'+implode(q,'&') : "";
        self.transactions = [];
        return $http({
            method:'GET',
            url: 'http://localhost:8080/moola/rest/accounts/'+currentAccount.id+'/reports/adhoc/'+(page)+q
        }).then(function(r){
            self.transactions = r.data;
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


    self.selectAccount = function(account) {
        currentAccount = account;
        if (account) {
            loadTransactions().then(function(loaded){
                self.transactions = loaded;
            });
        }
        else {
            self.transactions = [];
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
    self.withNewFilter = function(key, value){
        if (value=='?') value="";
        var r = {};
        r[key] = [value];
        return asHash({filters:r, grouping : query.grouping});
    };

    self.withAddFilter = function(key, value){
        if (value=='?') value="";
        var q = duplicateQuery();
        if (!q.filters[key]) q.filters[key] = [];
        q.filters[key].push(value);
        return asHash(q);
    };

    self.withoutFilterValue = function(key, value){
        if (value=='?') value="";
        var q = duplicateQuery();
        if (!q.filters[key]) return asHash(q);
        q.filters[key] = without(q.filters[key], value);
        if (q.filters[key].length==0) delete q.filters[key];
        return asHash(q);
    };

    var without = function(arr, value){
        var r = [];
        for (var i=0; i<arr.length; i++)
            if (arr[i]!==value)
                r.push(arr[i]);
        return r;
    }

    var duplicateQuery = function(){
        if (!query) return {};
        var r = {
            filters: {},
            grouping: {}
        };
        for (var key in query.filters){
            if (!query.filters.hasOwnProperty(key)) continue;
            if (query.filters[key].length==0) continue;
            r.filters[key]=[];
            for (var i=0; i<query.filters[key].length; i++){
                r.filters[key].push(query.filters[key][i]);
            }
        }
        for (var key in query.grouping){
            if (!query.grouping.hasOwnProperty(key)) continue;
            if (query.grouping[key].length==0) continue;
            r.grouping[key]=[query.grouping[key]];
        }
        return r;
    };

    var asHash = function (query){
        if (!query) return "";
        var q = [];
        for (var key in query.filters){
            if (!query.filters.hasOwnProperty(key)) continue;
            q.push("filter="+key+":"+implode(query.filters[key],','));
        }
        for (var key in query.grouping){
            if (!query.grouping.hasOwnProperty(key)) continue;
            q.push("grouping="+key+":"+query.grouping[key]);
        }
        return implode(q, '&');
    };

    var parseHash = function(hash){
        var hFilters = {};
        var hGrouping = {};
        if (hash[0]=='/') hash = hash.substr(1);
        hash = hash.split('&');
        for (var i=0; i<hash.length; i++){
            var parts = hash[i].split("=");
            if (parts.length>1) {
                var key = parts[0];
                var value = parts[1];
                if (key == "filter") {
                    parseFilter(hFilters, value)
                }
                if (key == "grouping") {
                    parseGrouping(hGrouping, value);
                }
            }
        }
        console.log("Done parsing: "+JSON.stringify(hFilters));
        query = {filters: hFilters, grouping: hGrouping};
    }

    var parseFilter = function(filters, string){
        var string = string.split(":");
        var key = string[0];
        if (string.length==1) {
            filters[key]=[""];
            return;
        }
        var values = string[1].split(",");
        for (var j = 0; j < values.length; j++) {
            if (filters.hasOwnProperty(key))
                filters[key].push(values[j]);
            else
                filters[key] = [values[j]];
        }
    };

    var parseGrouping = function(grouping, string){
        var string = string.split(":");
        var key = string[0];
        if (string.length==1) {
            return;
        }
        grouping[key] = string[1];
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
        return query.filters;
    }

    self.getQuery = function(){
        return query;
    }

    self.grouped = function(by){
        if (!query.grouping || objKeys(query.grouping).length == 0) return false;
        if (by) return query.grouping.hasOwnProperty(by);
        else return true;
    }

    var objKeys = function(obj){
        var keys = [];
        for (var k in obj) keys.push(k);
        return keys;
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
    onAccountChanged(Session.account());

    $scope.$watch(function(){return $location.hash();}, function(){
        parseHash($location.hash());
        loadTransactions().then(function(data){self.transactions = data});
    });

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
