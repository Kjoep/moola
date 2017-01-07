moolaApp.directive('autoComplete', ['$parse', function ($parse) {

    var self = this;

    return {
        restrict: 'A',
        scope: {},
        link: function($scope, $elem, attrs){

            var $autocomplete = $elem.find('.autocomplete');
            var optionsExp = attrs.acOptions;
            var modelExp = attrs.autoComplete;
            var selectedOption = -1;

            $autocomplete.insertAfter($elem);
            $autocomplete.hide();

            var getCurrentSelection = function(){
                if (!modelExp) return {idx: -1};
                var $model = $parse(modelExp);
                var selected = $model($scope.$parent);
                var index = selected ? indexById($scope.filteredOptions, selected.id) : -1;
                return {idx:index, object: selected}
            };

            var selectOption = function(idx){
                if (idx<-1) idx=-1;
                if (idx>=$scope.filteredOptions.length) idx = $scope.filteredOptions.length-1;
                selectedOption = idx;
            }

            $scope.getOptionClass = function(optionId) {
                if (selectedOption == -1 && optionId == '__new') return "active";
                if (selectedOption>=0 && optionId == $scope.filteredOptions[selectedOption].id) return "active";
                else return "";
            };


            $elem.keydown(function(){
                    applyFilter();
            });

            var filter = function(){
                var filterExp = $elem.val();
                $scope.filteredOptions = applyFilter($scope.options, filterExp);
                if ($scope.filteredOptions.length==0) selectedOption = -1;
                else selectedOption = indexByName($scope.filteredOptions, filterExp);
            };

            var applyFilter = function(options, filterExp){
                if (!filterExp) return options;
                var r = [];
                for (var i=0; i<options.length; i++){
                    var option = options[i];
                    if (option.name.toLowerCase().indexOf(filterExp.toLowerCase())>=0)
                        r.push(option);
                }
                return r;
            };

            var indexById = function(objects, id){
                if (!objects) return -1;
                for (var i=0; i<objects.length; i++){
                    if (objects[i].id == id) return i;
                }
                return -1;
            };

            var indexByName = function(objects, name){
                if (!objects) return -1;
                for (var i=0; i<objects.length; i++){
                    if (objects[i].name.toLowerCase() == name.toLowerCase()) return i;
                }
                return -1;
            };
        },
        templateUrl: 'autocomplete/autocomplete.html'
    }
}]);

