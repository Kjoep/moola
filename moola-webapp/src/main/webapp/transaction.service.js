angular.module('moola').service('TransactionService', 
    ['$http', 'config', function($http, config){

        var self = this;

        self.getTransactionsByFilter = function(accountId, filter, limit){
            return $http.get(url(`/accounts/${accountId}/transactions`, {filter: filter, limit: limit}))
                .then(unwrapResponse)
                .catch(unwrapError);
        };

        var unwrapResponse = function (response) {
            return response.data;
        };

        var unwrapError = function (response) {
            throw response.data;
        };

        var url = function(relative, queryParameters) {
            var r = config.apiUrl + relative;
            if (queryParameters && Object.keys(queryParameters).length > 0){
                r += '?' + Object
                    .keys(queryParameters)
                    .filter(function(key){
                        return !!queryParameters[key];
                    })
                    .map(function(key){
                        return encodeURIComponent(key) + '=' + encodeURIComponent(queryParameters[key]);
                    })
                    .join('&');
            }
            return r;
        };


    }]);