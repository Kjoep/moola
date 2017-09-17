angular.module('moola').component('backlogIndicator', {
    bindings: {},
    controllerAs: 'vm',
    controller: function($scope, BacklogService){
        var vm = this;
        vm.rulesLeft = 0;
        
        $scope.$on(BacklogService.EVENT_BACKLOGCOUNT_UPDATE, function(_, value){
            vm.rulesLeft = value;
        })
        $scope.$on(BacklogService.EVENT_BACKLOG_EMPTY, function () {
            vm.rulesLeft = 0;
        })
    },
    templateUrl: 'components/backlog-indicator.html',
});