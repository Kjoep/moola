angular.module('moola').service('TypeService', ['config', 
    function(config){

        var DEFAULT_COLOR = { fg: '#FFF', bg: '#00D' };
        
        var self = this;

        self.getCategories = function(){
            return $http.get(url(`/categories?q=`))
                .then(unwrapResponse)
                .catch(unwrapError);
        };

        self.create = function(name){
            var newEntry = { id: getId(name), name:name, color: DEFAULT_COLOR };
            return self.upsert(newEntry)
                .then(function(){
                    return newEntry;
                });
        };

        self.upsert = function(category){
            return $http.put(url(`/categories/${category.id}`), category);
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