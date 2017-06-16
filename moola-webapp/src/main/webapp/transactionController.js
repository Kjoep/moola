

angular.module('moola').directive('colorPicker',['$parse', function($parse) {
    return {
        restrict: 'A',
        scope: {
            onSelect: '&'
        },
        link: function($scope, $elem, attrs){
            var $picker = $elem.find('.colorPicker');

            var modelExp = attrs.colorPicker;

            $picker.insertAfter($elem);
            $picker.hide();

            $scope.colors = ['#000000','#404040','#808080','#C0C0C0','#FFFFFF',
                '#800000','#D00000','#FF0000',
                '#FFFF00','#FFFF60','#FFFFA0',
                '#008000','#00D000','#00FF00',
                '#000080','#0000D0','#0000FF']

           $elem.click(function(){
               $picker.show();
               var right =$picker.offset().left + $picker.width();
               if (right > $(window).width()){
                   $picker.css({left: '-'+(right - $(window).width())+'px'})
               }
           })

            $scope.pick = function(fg, bg){
               var selected = {fg:fg, bg:bg};

                if ($scope.onSelect && typeof $scope.onSelect == "function" ){
                    var apply = $scope.onSelect($scope.$parent);
                    if (typeof apply == "function") {
                        var adapted = $scope.onSelect($scope.$parent)(selected);
                        if (adapted) selected = adapted;
                    }
                }
                if (modelExp) {
                    var $model = $parse(modelExp);
                    $model.assign($scope.$parent, selected);
                }

                $picker.hide();
            }
        },
        templateUrl: 'components/colorPicker.html'
    }

}]);
