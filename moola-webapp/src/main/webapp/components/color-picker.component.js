angular.module('moola').component('colorPicker', {
    bindings: { ngModel: '=', ngChange: '&' },
    controllerAs: 'vm',
    controller: function ($timeout) {
        var vm = this;

        vm.options = [
            {bg: '#FFF', fg: '#333'},
            {bg: '#CCC', fg: '#333'},
            {bg: '#AAA', fg: '#333'},
            {bg: '#888', fg: '#333'},
            {bg: '#666', fg: '#FFF'},
            {bg: '#444', fg: '#FFF'},
            {bg: '#222', fg: '#FFF'},
            {bg: '#000', fg: '#FFF'},

            {bg: '#DB333F', fg: '#FFF'},
            {bg: '#F9BCC1', fg: '#000'},
            {bg: '#FB7B85', fg: '#FFF'},
            {bg: '#6A020A', fg: '#FFF'},
            {bg: '#A9000C', fg: '#FFF'},
            {bg: '#E18434', fg: '#FFF'},
            {bg: '#FFDDC1', fg: '#000'},
            {bg: '#FFB97D', fg: '#FFF'},

            {bg: '#6C3302', fg: '#FFF'},
            {bg: '#AD5000', fg: '#FFF'},
            {bg: '#208388', fg: '#FFF'},
            {bg: '#81A8AA', fg: '#FFF'},
            {bg: '#63C3C8', fg: '#FFF'},
            {bg: '#023F41', fg: '#FFF'},
            {bg: '#006469', fg: '#FFF'},
            {bg: '#3FBA2B', fg: '#FFF'},

            {bg: '#ACDAA4', fg: '#333'},
            {bg: '#81E771', fg: '#0E5A02'},
            {bg: '#0E5A02', fg: '#FFF'},
            {bg: '#138F00', fg: '#FFF'},
            {bg: '#3E359C', fg: '#FFF'},
            {bg: '#9995BD', fg: '#FFF'},
            {bg: '#8077D4', fg: '#FFF'},
            {bg: '#110C4B', fg: '#FFF'},

            {bg: '#150B78', fg: '#FFF'},
            {bg: '#922292', fg: '#FFF'},
            {bg: '#B488B4', fg: '#FFF'},
            {bg: '#CE65CE', fg: '#FFF'},
            {bg: '#460146', fg: '#FFF'},
            {bg: '#700070', fg: '#FFF'},
            {bg: '#BCD933', fg: '#700070'},
            {bg: '#EDF8BB', fg: '#700070'},

            {bg: '#E4FA7B', fg: '#576902'},
            {bg: '#576902', fg: '#FFF'},
            {bg: '#8AA700', fg: '#FFF'},
            {bg: '#E1BA34', fg: '#6C5502'},
            {bg: '#FFF1C1', fg: '#6C5502'},
            {bg: '#FFE27D', fg: '#6C5502'},
            {bg: '#6C5502', fg: '#FFF'},
            {bg: '#AD8600', fg: '#FFF'}
        ];

        vm.pick = function(color){
            vm.ngModel = color;
            vm.showPicker = false;
            if (vm.ngChange) $timeout(function(){ vm.ngChange({color: color}); }); 
        }

    },
    templateUrl: 'components/color-picker.component.html',
});