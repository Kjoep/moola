angular.module('moola').service('PeerService', ['$http', 'config',
    function ($http, config) {

        var self = this;

        self.getPeers = function () {
            return $http.get(url(`/peers?q=`))
                .then(unwrapResponse)
                .catch(unwrapError);
        };

        self.create = function (name) {
            var newEntry = { id: getId(name), name: name };
            return self.upsert(newEntry)
                .then(function () {
                    return newEntry;
                });
        };

        self.upsert = function (peer) {
            return $http.put(url(`/peers/${peer.id}`), peer);
        };

        var getId = function (template) {
            return template.replace(" ", "_");
        };

        var url = function (relative, queryParameters) {
            var r = config.apiUrl + relative;
            if (queryParameters && Object.keys(queryParameters).length > 0) {
                r += '?' + Object
                    .keys(queryParameters)
                    .filter(function (key) {
                        return !!queryParameters[key];
                    })
                    .map(function (key) {
                        return encodeURIComponent(key) + '=' + encodeURIComponent(queryParameters[key]);
                    })
                    .join('&');
            }
            return r;
        };

        var unwrapResponse = function (response) {
            return response.data;
        };

        var unwrapError = function (response) {
            throw response.data;
        };

    }]);