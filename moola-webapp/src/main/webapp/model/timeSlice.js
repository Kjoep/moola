(function(){

    if (!window.moola) window.moola = {};

    moola.TimeSlice = {
        parse: function(string){
            var hourMatcher = /^(\d\d\d\d)-(\d\d)-(\d\d):(\d\d)$/.exec(string);
            if (hourMatcher) 
                return new moola.TimeSlice.Hour(hourMatcher[1], hourMatcher[2], hourMatcher[3], hourMatcher[4]);
            var dayMatcher = /^(\d\d\d\d)-(\d\d)-(\d\d)$/.exec(string);
            if (dayMatcher) 
                return new moola.TimeSlice.Day(dayMatcher[1], dayMatcher[2], dayMatcher[3]);
            var monthMatcher = /^(\d\d\d\d)-(\d\d)$/.exec(string);
            if (monthMatcher) 
                return new moola.TimeSlice.Month(monthMatcher[1], monthMatcher[2]);
            var weekMatcher = /^(\d\d\d\d)\/(\d\d)$/.exec(string);
            if (weekMatcher) 
                return new moola.TimeSlice.Week(weekMatcher[1], weekMatcher[2]);
            var yearMatcher = /^(\d\d\d\d)$/.exec(string);
            if (yearMatcher) 
                return new moola.TimeSlice.Year(yearMatcher[1]);
        },
        Hour: function(year, month, day, hour){
            this.asRange = function(){
                var start = JSJoda.LocalDateTime.ofNumbers(year, month, day, hour, 0, 0, 0);
                var stop = start.plusHours(1);
                return start.toString()+'-'+stop.toString();
            }
        },
        Day: function(year, month, day){
            this.asRange = function(){
                var start = JSJoda.LocalDate.of(year, month, day);
                var stop = start.plusDays(1);
                return start.toString()+'-'+stop.toString();
            }
        },
        Month: function(year, month){
            this.asRange = function(){
                var start = JSJoda.LocalDate.of(year, month, 1);
                var stop = start.plusMonths(1);
                return start.toString()+'-'+stop.toString();
            }
        },
        Year: function(year){
            this.asRange = function(){
                var start = JSJoda.LocalDate.of(year, 1, 1);
                var stop = start.plusYears(1);
                return start.toString()+'-'+stop.toString();
            }
        },
        Week: function(year, week){
            this.asRange = function(){
                var start = JSJoda.LocalDate.of(year, 1, 1)
                    .withFieldAndValue(JSJoda.ChronoField.ALIGNED_WEEK_OF_YEAR, week);
                var stop = start.plusWeeks(1);
                return start.toString()+'-'+stop.toString();
            }
        },
    }

}());
