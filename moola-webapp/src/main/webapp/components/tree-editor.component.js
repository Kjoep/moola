angular.module('moola').component('treeEditor', {
    bindings: {
        nodes: '<',
        expose: '<',
        setParent: '&'
    },
    controller: function($element, $rootScope, $transclude){
        var vm = this;

        vm.$postLink = function(){
            $element.find('.tree-item').mousedown(function(){
                console.log('Starting drag of '+this.treeItem);
            })
        }
        vm.dragStart = function(node){
            console.log('DragStart', node);
            vm.dragging = node;
        }
        vm.dragStop = function(node){
            if (!vm.dragging) return;
            console.log('DragStop', node);
            if (node !== vm.dragging)
                vm.setParent({parent: node, child: vm.dragging});
            vm.dragging = false;
        }
        vm.mouseMove = function(event){
            if (vm.dragging && event.buttons === 0)
                vm.dragging = false;
        }
        vm.includeContent = function($target, node){
            var scope = $rootScope.$new();
            scope.node = node;
            if (vm.expose) angular.extend(scope, vm.expose);
            $transclude(scope, function($clone){
                $target.append($clone);
            });
        }
    },
    controllerAs: 'vm',
    transclude: true,
    template: '<div class="root" ng-mouseup="vm.dragStop();">[ROOT]</div>'+
              '<tree-editor-branch ng-class="{dragging: vm.dragging}" node="node" ng-mousemove="vm.mouseMove($event)" '+
              '                    drag-start="vm.dragStart(item)" drag-stop="vm.dragStop(item)" '+
              '                    ng-repeat="node in vm.nodes" dragging="vm.dragging">'+
              '</tree-editor-branch>'

})
.component('treeEditorBranch', {
    bindings: {
        node: '<',
        dragging: '<',
        dragStart: '&',
        dragStop: '&'
    },
    transclude: true,
    controllerAs: 'vm',
    controller: ['$element', function($element){
        var vm = this;
        vm.onMouseDown = function(event){
            event.preventDefault();
            vm.dragStart({item: vm.node});
        }
    }],
    template: '<div class="tree-branch">'+
              '  <div class="drag-handle" ng-mousedown="vm.onMouseDown($event)">&nbsp;</div> '+
              '  <tree-content  class="tree-item" '+
              '                 node="vm.node" ng-mouseup="vm.dragStop({item: vm.node})" ng-class="{dragged: vm.dragging === vm.node}"></tree-content>'+
              '  <div class="children">'+
              '    <tree-editor-branch drag-start="vm.dragStart({item: item})" drag-stop="vm.dragStop({item: item})" '+
              '                        node="node" dragging="vm.dragging" ng-repeat="node in (vm.node.children || [])">'+
              '    </tree-editor-branch>'+
              '  </div>'+
              '</div>'

})
.component('treeContent', {
    require: {'treeEditor': '^'},
    bindings: {'node':'<'},
    controller: function($element){
        this.$postLink = function(){
            this.treeEditor.includeContent($element, this.node);
        }
    }

})