angular.module('moola').component('colorDisplay', {
    bindings: { bg: '@', fg: '@' },
    controllerAs: 'vm',
    controller: function ($scope, BacklogService) {},
    templateUrl: 'components/color-display.component.html',
});