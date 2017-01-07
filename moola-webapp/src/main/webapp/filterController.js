moolaApp.controller('FilterController', ['$scope', '$resource', '$filter', function ($scope, $resource, $filter) {

    var self = this;

    var filtersResource = $resource('rest/filters/:filterId', {}, {
        all: {method:'GET', params:{'filterId':''}, isArray:true},
        create: {method:'GET', params:{'filterId':'new'}, isArray:false},
        apply: {method:'POST', url:'rest/filters/:filterId/apply', isArray:false},
        delete: {method:'DELETE', isArray:false},
    });

    self.filters = [];
    self.filters = filtersResource.all();

    self.updateTransactionCategory = function(transaction){
        console.log("Selecting category for "+JSON.stringify(transaction));
        return function(newCat){
            if (newCat.id){
                transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {category: newCat});
            }
            else {
                newCat.id = getId(newCat.name);
                categoriesResource.update({id:newCat.id}, newCat).$promise.then(function(){
                    console.log("Created category: "+JSON.stringify(newCat))
                    self.categoryOptions.push(newCat);
                    self.showCat[newCat.id] = true;
                    transactionsResource.update({accountId: currentAccount.id, transactionId: transaction.id}, {category: newCat});
                })
            }
        }
    };

    self.updateCategory = function(category){
        categoriesResource.update({id:category.id}, category).$promise.then(function(){
            console.log("Updated category: "+JSON.stringify(category))
        })
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
            proposed = "peer=='"+trans.peer+"'";
        self.showFilterEditor('category', trans, proposed, trans.category, addCategoryFilter);
    };

    var addCategoryFilter = function(filterExp, categoryToSet, applyMode){
        filtersResource.add({id:'new'}, {expression: filterExp, categoryId: categoryToSet.id, apply: applyMode}).$promise.then(function(){growl('filter ddded')});
    };

    var addPeerFilter = function(filterExp, peerToSet, applyMode){
        filtersResource.add({id:'new'}, {expression: filterExp, peerId: peerToSet.id, apply: applyMode}).$promise.then(function(){growl('filter ddded')});
    };

    self.showFilterEditor = function(type, exampleTransaction, proposedFilter, subject){
        //this is a stub
    };

    self.describeResult = function(filter){
        var r = "";
        if (filter.peerId) r += "Set peer: "+filter.peerId+" ";
        if (filter.categoryId) r += "Set category: "+filter.categoryId+" ";
        return r;
    };

    self.deleteFilter = function(filter){
        if (!confirm('Delete filter?')) return false;
        filtersResource.delete({filterId: filter.id});
        self.filters = self.filters.splice(self.filters.indexOf(filter), 1);
    };

    self.applyFilter = function(filter, applyMode){
        filtersResource.apply({filterId: filter.id}, applyMode);
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

    var growl = function(message){
        alert(message);
    }

}]);

