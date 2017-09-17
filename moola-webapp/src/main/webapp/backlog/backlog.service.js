angular.module('moola').service('BacklogService', function($http, $q, $log, $rootScope, $timeout,  config){
    var self = this;

    self.EVENT_BACKLOGCOUNT_UPDATE = 'backlog-count';
    self.EVENT_BACKLOG_EMPTY = 'backlog-empty';

    var backlogCount;

    /**
     * Schedule a filter for processing.  
     * @return a Promise that will resolve when all rules have been processed.
     */
    self.scheduleFilter = function(filterId, applyMode){
        return $http.post(`${config.apiUrl}/filters/${filterId}/apply?applyMode=${applyMode}`)
            .then(function(){
                return self.whenBacklogEmpty();
            })
    };

    var emptyBacklogDeferred;

    var fetchBacklogCount = function(){
        return $http
            .get(`${config.apiUrl}/filters/rulesBacklog`)
            .then(function(response){ return response.data })
            .then(function(count){ 
                backlogCount = count; 
                $rootScope.$broadcast(self.EVENT_BACKLOGCOUNT_UPDATE, count);
                return count;
            });
    };

    var pollBacklogCount = function(){
        if (!emptyBacklogDeferred) emptyBacklogDeferred = $q.defer();
        fetchBacklogCount().then(function(count){
            if (count === 0){
                $rootScope.$broadcast(self.EVENT_BACKLOG_EMPTY);
                emptyBacklogDeferred.resolve();
                emptyBacklogDeferred = null;
            }
            else
                $timeout(pollBacklogCount, 1000);
        })
    }

    /**
     * @return a Promise<> that resolves when the backlog is empty
     */
    self.whenBacklogEmpty = function(){
        if (!emptyBacklogDeferred)
            pollBacklogCount();
        return emptyBacklogDeferred.promise;
    };


})