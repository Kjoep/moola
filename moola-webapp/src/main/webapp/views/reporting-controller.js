angular.module('moola').controller('ReportingController',
    ['$scope', '$q', '$location', '$resource', '$filter', '$http', 'CategoryService', 'Session',
    function ($scope, $q, $location, $resource, $filter, $http, CategoryService, Session) {

    var self = this;

    self.transactions = [];
    self.showCat = {};
    self.title = 'All transactions';
    var page=0;

    self.query = new moola.Query();

    var currentAccount;

    var onAccountChanged = function(account) {
        currentAccount = account;
        loadTransactions().then(function(){
            createChartData();
        });
    };

    var loadTransactions = function(){
        console.log('loading transactions');
        var query = self.query;
        if (!currentAccount) return $q.when([]);
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
            adaptCategories(r.data);
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

            var transactions = self.transactions.slice(0);
            transactions.reverse();

            var timeSlices = [];
            var balances = undefined;
            var lastTs;
            var groups = {};
            var groupKeys = Object.keys(self.query.grouping).filter(function(key){ return key !== 'date'});
            transactions.forEach(function(transaction){
                groups[groupName(transaction)] = [];
            });
            var tsIdx = -1;
            transactions.forEach(function(transaction){
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
            if (self.query.isTimeSeries()){
                balances = transactions.map(function(transaction){
                    return transaction.balance;
                });
            }

            self.chart = {
                type: 'bar',
                labels: timeSlices,
                groups: groups,
                balances: balances
            }
        }

        else if (self.query.isGrouped()){

            var CAMPFIRE = ['#588C7E', '#F2E394', '#F2AE72' ,'#D96459' ,'#8C4646'];

            var groupName = function(transaction){
                return groupKeys.map(function(gk){
                    var value = transaction[gk];
                    return value ? (value.id || value) : value;
                }).join('/');
            }

            var groupKeys = Object.keys(self.query.grouping);
            var colorProvider = function(){
                var idx = 0;
                return function(transaction){
                    var i = idx++;
                    return CAMPFIRE[i%CAMPFIRE.length];
                };
            }();
            if (groupKeys.length === 1 && groupKeys[0] === 'category')
                colorProvider = function(transaction){
                    if (!transaction.category) return '#606060';
                    if (!transaction.category.color) return '#606060';
                    return transaction.category.color.bg;
                };

            var data = {
                type: 'pie',
                income: {data: [],labels: [], colors:[]},
                expense: {data: [],labels: [], colors: []}
            };
            self.transactions.forEach(function(transaction){
                transaction.colorHint = colorProvider(transaction);
                if (transaction.total > 0){
                    data.income.labels.push(groupName(transaction));
                    data.income.data.push(transaction.total);
                    data.income.colors.push(transaction.colorHint);
                }
                else {
                    data.expense.labels.push(groupName(transaction));
                    data.expense.data.push(0-transaction.total);
                    data.expense.colors.push(transaction.colorHint);
                }
            });

            self.chart = data
        }
        else {
            self.chart = {};
        }

    };

    self.inOutColors = function(data){
        return data.map(function(entry){
            return entry<0 ? "#D96459" : "#588C7E";
        })
    }

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
        // TODO: this really should not be done at this point
        for (var i=0; i<transactions.length; i++){
            transactions[i].category = CategoryService.internCategory(transactions[i].category);
        }
    }

    self.updateDescription = function(transaction) {
        transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {description: transaction.description});

        //TODO: handle failure
    }


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

    self.rangeFor = function(slice){
        if (!slice) return '';
        var slice = moola.TimeSlice.parse(slice);
        return slice.asRange();
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

    self.applyQuery = function(newQ){
        var r = newQ.asHash();
        console.log("Going to hash: "+r);
        $location.hash("/"+r);
    }

    Session.onAccountChanged(onAccountChanged);

    $scope.$watch(function(){return $location.hash();}, function(){
        self.query = moola.Query.parseHash($location.hash());
        loadTransactions().then(function(){
            createChartData();
        });
    });

    self.query = moola.Query.parseHash($location.hash());
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
