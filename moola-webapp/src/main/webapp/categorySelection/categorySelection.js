angular.module('moola').directive('categorySelection',['$parse', 'Categories', function($parse, Categories) {
    var padding=30;
    var KEY_UP=38;
    var KEY_DOWN=40;
    var KEY_ENTER=13;
    var KEY_ESCAPE=27;

    return {
        restrict: 'A',
        scope: {
            onSelect: '&'
        },
        link:  function($scope, $elem, attrs){

            var $categorySelection = $elem.find('.categorySelection');
            var $filter = $categorySelection.find('input.ps-filter');
            var modelExp = attrs.categorySelection;
            var selectedOption = -1;

            $categorySelection.insertAfter($elem);
            $categorySelection.hide();

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

            $filter.keydown(function(e){
                if (e.keyCode==KEY_ESCAPE) {
                    $categorySelection.hide()
                }
                else if (e.keyCode==KEY_UP) {
                    $scope.$apply(function(){
                        selectOption(selectedOption-1);
                    });
                    e.preventDefault(); return false;
                }
                else if (e.keyCode==KEY_DOWN) {
                    $scope.$apply(function(){
                        selectOption(selectedOption+1);
                    });
                    e.preventDefault(); return false;
                }
                else if (e.keyCode==KEY_ENTER){
                    $scope.$apply(function(){
                        applyOption();
                    });
                    e.preventDefault(); return false;
                }
            });

            $filter.keyup(function (e){
                if (e.keyCode==KEY_ESCAPE) {
                }
                else if (e.keyCode==KEY_UP) {
                }
                else if (e.keyCode==KEY_DOWN) {
                }
                else if (e.keyCode==KEY_ENTER){
                }
                else
                    $scope.$apply(filter);
            });

            $categorySelection.find('.options-section').click(function(e){
                var $target = $(e.target);
                if (!$target.is('.option')) $target = $target.closest('.option');
                var id = $target.data('categoryid');
                var object = $scope.options[indexById($scope.options, id)];
                $scope.$apply(function(){
                    applyOption(object);
                });
            });

            var show = function(){
                $scope.options = Categories.get();
                if (!$scope.options) $scope.options = [];
                $scope.filteredOptions = $scope.options;
                $scope.filter = $elem.val();

                var preselect = getCurrentSelection();
                selectedOption = preselect.idx;
                if (preselect.object) $scope.filter = preselect.object.name;

                var pos = $elem.position();

                $categorySelection.css('width', ($elem.width()+padding+padding)+'px');
                $categorySelection.css('top', (pos.top - padding)+'px');
                $categorySelection.css('left', (pos.left - padding)+'px');
                $categorySelection.fadeIn(100, function(){
                    $filter.focus();
                    $filter.select();
                    $filter.bind('blur',hide);
                });

                $('body').bind('mousedown', hide);
            };

            var filter = function(){
                var filterExp = $filter.val();
                $scope.filteredOptions = applyFilter($scope.options, filterExp);
                if ($scope.filteredOptions.length==0) selectedOption = -1;
                else {
                    selectedOption = indexByName($scope.filteredOptions, filterExp);
                    if (selectedOption<0) selectedOption=0;
                }
            }

            var applyOption = function(selected) {
                if (!selected && selectedOption>=0)
                    selected = $scope.filteredOptions[selectedOption];

                if (!selected && selectedOption<0){
                    Categories.addNew($scope.filter).then(applyOption);
                    return;
                }

                if ($scope.onSelect && typeof $scope.onSelect == "function" ){
                    var apply = $scope.onSelect($scope.$parent);
                    if (typeof apply == "function") {
                        var adapted = apply(selected);
                        if (adapted) selected = adapted;
                    }
                }
                if (modelExp) {
                    var $model = $parse(modelExp);
                    $model.assign($scope.$parent, selected);
                }

                hide();
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

            $scope.showNew = function(){
                if (!$scope.filter) return false;
                if ($scope.filter.length==0) return false;
                if ($scope.filter[0]=='?') return false;
                if ($scope.exactMatch()) return false;
                return true;
            }

            $scope.exactMatch = function(){
                var r = indexByName($scope.filteredOptions, $scope.filter)>=0;
                return r;
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
            }

            $elem.focus(function(){
                $scope.$apply(function() {
                    show();
                });
            });


            var hide = function(e) {
                if (e && e.target && $(e.target).parents().filter(function(e){return e == $categorySelection[0] || e == $elem[0]}).length>0) return;
                $categorySelection.fadeOut(100);
                $('body').unbind('mousedown', hide);
                $filter.unbind('blur', hide);
            };

            var preselect = getCurrentSelection();
            if (preselect && preselect.object)
                $elem.data('category-id', preselect.object.id);

        },
        templateUrl: 'categorySelection/categorySelection.html'
    }
}]);
