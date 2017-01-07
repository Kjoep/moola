moolaApp.directive('chart',['$parse', function($parse) {

    return {
        restrict: 'A',
        scope: {
        },
        link:  function($scope, $elem, attrs){

            var exp = attrs['chart'];

            $scope.$parent.$watch(exp, function(data, oldData){
                if (data && data.$promise){
                    data.$promise.then(function(resp){showChart(resp)});
                }
                else
                    showChart(data);
            });

            var showChart = function(reportData){
                if (!reportData) return;
                if (!reportData.data) return;
                var colCount = 0;
                var data = Stream(reportData.categories).map(function(cat){
                    return {
                        label: cat.name,
                        backgroundColor: cat.color.bg,
                        foregroundColor: cat.color.fg,
                        yAxisID: 'amounts',
                        data: Stream(reportData.data).map(function(row){if (row.categories[cat.id]) return row.categories[cat.id]/100; else return 0;}).toArray()
                    }
                }).toArray();
                data.unshift({
                    label: 'Balance',
                    type:'line',
                    backgroundColor: 'transparent',
                    borderColor: 'black',
                    yAxisID: 'balance',
                    data: Stream(reportData.data).map(function(row){return row.balance/100;}).toArray()
                });

                var labels = Stream(reportData.data).map("slice").toArray();

                var chart = new Chart($elem, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: data
                    },
                    options: {
                        responsive: false,
                        legend: {display: false},
                        scales: {
                            xAxes: [{stacked:true}],
                            yAxes: [
                                {id: 'amounts',stacked:true,position:'left'},
                                {id:'balance', position:'right'}
                            ]
                        }
                    }
                });

            };


        }
    }
}]);
