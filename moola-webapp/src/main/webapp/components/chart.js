angular.module('moola')

.component('chart', {
    bindings: {
        labels:"<"
    },
    transclude: true,
    controllerAs: 'chart',
    controller: ['$element', '$scope',  function($elem, $scope){
        var self = this;
        var datasets = [];

        self.$onChanges = function(){
            scheduleUpdate();
        };

        self.addDataSet = function(name, data, type, axis, bgColor, fgColor, borderColor) {
            if (!data || !(data instanceof Array))
                throw 'Data should be a valid array';
            var dataset = {
                label: name,
                backgroundColor: bgColor,
                foregroundColor: fgColor,
                borderColor: borderColor,
                data: data,
                type: type || 'bar',
                yAxisID: axis
            };
            datasets.push(dataset);
            scheduleUpdate();
            return {
                updateData: function(data){
                    dataset.data = data;
                }
            }
        }

        var updateTimeout;
        var update;
        var scheduleUpdate = function(){
            if (updateTimeout)
                clearTimeout(updateTimeout);

            updateTimeout = setTimeout(function(){
                update();
            }, 250);

        }

        var chart;

        self.$onInit = function(){
            console.log('performing chart init');

            var cfg = {
                type: 'bar',
                data: {},
                options: {
                    responsive: false,
                    legend: {display: false},
                    scales: {
                        xAxes: [{stacked:true}],
                        yAxes: [
                            {id: 'amounts',stacked:true,position:'left'},
                            {id: 'balance', position:'right'}
                        ]
                    }
                }
            };

            update = function(){
                if (!self.labels || datasets.length === 0 || self.labels.length === 0) return;
                //console.log('Doing update with '+JSON.stringify(datasets));

                cfg.data.labels = self.labels;
                cfg.data.datasets = datasets;

                if (!chart)
                    chart = new Chart($elem.find('canvas')[0], cfg);
                else
                    chart.update();
            }
            scheduleUpdate();
        }
    }],

    template: '<canvas class="chart" width="600" height="300" ng-transclude></canvas>'
})
.directive('series', ['$log', function($log){
    function findInParents($scope, key){
        var value = $scope[key];
        while (!value){
            if (!$scope.$parent) return undefined;
            $scope = $scope.$parent;
            value = $scope[key];
        }
        return value;
    }

    return {
        restrict: 'E',
        link: function($scope, $element, attrs){
            try {
                var chart = findInParents($scope, 'chart');
                if (!chart) throw 'Series should be nested inside a chart.';
                var chartData = $scope.$eval(attrs.data);
                var bgColor = attrs.bgColor; 
                if (attrs.bgColors) bgColor = $scope.$eval(attrs.bgColors);
                var dataSet = chart.addDataSet(attrs.name, chartData, attrs.type, attrs.axis,
                    bgColor, attrs.fgColor, attrs.borderColor);
                $scope.$watch(attrs.data, function(value){
                    dataSet.updateData(value);
                }, true);
            } catch (e) {
                $log.error('Could not add dataset', e);
            }
        }
    }

}])
.component('pieChart', {
    bindings: {
        labels:"<",
        data:"<",
        colors:"<",
        title:"@"
    },
    controllerAs: 'chart',
    controller: ['$element', '$scope',  function($elem, $scope){
        var self = this;
        var datasets = [];

        self.$onChanges = function(){
            scheduleUpdate();
        };

        var updateTimeout;
        var update;
        var scheduleUpdate = function(){
            if (updateTimeout)
                clearTimeout(updateTimeout);

            updateTimeout = setTimeout(function(){
                update();
            }, 250);
        }

        var chart;

        self.$onInit = function(){
            console.log('performing piechart init');

            var cfg = {
                type: 'doughnut',
                title: self.title,
                data: {},
                options: {
                    responsive: false,
                    legend: {display: false}
                }
            };

            update = function(){
                if (!self.labels || !self.data || self.data.length === 0 || self.labels.length === 0) return;

                cfg.data.labels = self.labels;
                cfg.data.datasets = [{data: self.data, backgroundColor: self.colors}];

                //console.log('Doing piechart update: '+JSON.stringify(cfg));

                if (!chart)
                    chart = new Chart($elem.find('canvas')[0], cfg);
                else
                    chart.update();
            }
            scheduleUpdate();
        }
    }],

    template: '<h3>{{chart.title}}</h3><canvas class="chart" width="300" height="300"></canvas>'
})
