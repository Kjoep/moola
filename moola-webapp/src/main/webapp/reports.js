angular.module('moola')
    .controller('reportController', ['$scope','$http', function($scope, $http){
        var reports = this;

        reports.reportData = {"title":"Overview","timeSliceName":"Week","categories":["?","Loon_Joeri","Loon_Lieve","telecom"],"data":[{"slice":"17","categories":{"?":192627},"balance":619158},{"slice":"18","categories":{"?":2660},"balance":1017209},{"slice":"19","categories":{"?":-143497},"balance":827373},{"slice":"20","categories":{"?":-14395},"balance":659889},{"slice":"21","categories":{"?":414414},"balance":825396},{"slice":"22","categories":{"?":-24132},"balance":1041567},{"slice":"23","categories":{"?":-60961},"balance":1035735},{"slice":"24","categories":{"?":-10075},"balance":993274},{"slice":"25","categories":{"?":-206564},"balance":961699},{"slice":"26","categories":{"?":179440},"balance":1009157},{"slice":"27","categories":{"?":-177988},"balance":951075},{"slice":"28","categories":{"?":-38049},"balance":781787},{"slice":"29","categories":{"?":-308160},"balance":730888},{"slice":"30","categories":{"Loon_Joeri":235368,"?":-42839},"balance":395078},{"slice":"31","categories":{"Loon_Lieve":184619,"?":-219158},"balance":600101},{"slice":"32","categories":{"?":-24974},"balance":597396},{"slice":"33","categories":{"?":-21906},"balance":546730},{"slice":"34","categories":{"telecom":-1000,"?":-51164},"balance":538888}]};
        reports.reportData.timeSliceName = "Jos";

        reports.categories = {
            'salary': {
                color: 'rgba(0,0,255,0.5)'
            },
            'vacation': {
                color: 'rgba(0,255,255,0.5)'
            },
            'groceries': {
                color: 'rgba(255,0,0,0.5)'
            }
        };

        var showChart = function(){
            var colors = [
                'rgba(255,0,0,0.5)',
                'rgba(0,255,0,0.5)',
                'rgba(0,0,255,0.5)',
                'rgba(255,0,255,0.5)',
                'rgba(255,255,0,0.5)',
            ];
            var colCount = 0;
            var data = Stream(reports.reportData.categories).map(function(cat){
                return {
                    label: cat,
                    backgroundColor: colors[colCount++],
                    yAxisID: 'amounts',
                    data: Stream(reports.reportData.data).map(cat).toArray()
                }
            }).toArray();
            data.unshift({
                label: 'Balance',
                type:'line',
                backgroundColor: 'transparent',
                borderColor: 'black',
                yAxisID: 'balance',
                data: Stream(reports.reportData.data).map(function(row){return row.Balance;}).toArray()
            });

            var chart = new Chart($('#chart'), {
                type: 'bar',
                data: {
                    labels: Stream(reports.reportData.data).map(reports.reportData.timeSliceName).toArray(),
                    datasets: data
                },
                options: {
                    responsive: false,
                    scales: {
                        xAxes: [{stacked:true}],
                        yAxes: [{id: 'amounts',stacked:true,position:'left'}, {id:'balance', position:'right'}]
                    }
                }
            });

        };

        // $http
        //     .get('reportData2.csv')
        //     .then(
        //         function ok(resp) {
        //             reports.reportData = dataFromCsv(resp.data);
        //             //showChart();
        //         },
        //         function error(resp) {
        //             alert('The server made a boo boo: ' + resp.status);
        //         });

        var dataFromCsv = function(csv){
            var papaFied = Papa.parse(csv, {
                header:true,
                dynamicTyping:true,
                skipEmptyLines:true
            });

            var fields = papaFied.meta.fields;
            var timeSliceName = fields.shift();
            fields.pop();

            return {
                timeSliceName: timeSliceName,
                data: papaFied.data,
                categories:  fields
            };
        }

        reports.getForCategory = function(row, category){
            return row[category];
        };

        reports.getRowTotal = function (row) {
            var total=0;
            for(var i =0; i<reports.reportData.categories.length; i++){
                var cat = reports.reportData.categories[i];
                total += row[cat];
            }
            return total;
        }

    }]);