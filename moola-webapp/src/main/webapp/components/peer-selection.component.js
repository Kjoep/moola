'use strict';

angular.module('moola').component('peerSelection', {
    bindings: {
        ngModel: '=',
        onSelect: '&',
        placeholder: '@'
    },
    controllerAs: 'vm',
    controller: ['$scope', '$resource', '$filter', '$timeout', 'PeerService',
        function ($scope, $resource, $filter, $timeout, PeerService) {
            var vm = this;
            vm.$onInit = function () {
                vm.repository = {
                    getAll: PeerService.getPeers,
                    create: PeerService.create
                };
            };

            vm.onComboSelect = function(item){
                vm.onSelect({item: item});
            };
        }],
    templateUrl: 'components/peer-selection.component.html',
});