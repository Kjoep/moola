angular.module('moola').service('FilterService', ['$http', 'config', 'BacklogService', 
    function ($http, config, BacklogService){

    var self = this;

    self.SUBJECT_PEER = 'peer';
    self.SUBJECT_CATEGORY = 'category';

    self.saveNewFilter = function(expression, subjectType, subject, applyMode){

        var data = {
            expression: expression,
            peerId: subject.id,
            apply: applyMode
        }

        if (subjectType === self.SUBJECT_PEER)
            data.peerId = subject.id
        else if (subjectType === self.SUBJECT_CATEGORY)
            data.categoryId = subject.id
        else throw `Unsupported type: ${subjectType}`;

        return $http.post(url('/filters/new'), data)
            .then(unwrapResponse)
            .then(function(){
                BacklogService.whenBacklogEmpty();
            })
            .catch(unwrapError);
    }

    var unwrapResponse = function (response) {
        return response.data;
    };

    var unwrapError = function (response) {
        throw response.data;
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

}]);