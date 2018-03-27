(function(){

    if (!window.moola) window.moola = {};

    /**
     * Make a duplicate of an array, leaving out a single element.
     */
    var without = function (arr, value) {
        var r = [];
        for (var i = 0; i < arr.length; i++)
            if (arr[i] !== value)
                r.push(arr[i]);
        return r;
    }

    moola.Query = function(filters, grouping) {
        this.filters = filters || {};
        this.grouping = grouping || {};
    }

    moola.Query.prototype = {
        asHash: function () {
            var q = [];
            var filters = this.filters;
            var grouping = this.grouping;
            q.push(...Object.keys(filters).map(function (key) { return `filter=${key}:${filters[key].join(',')}` }));
            q.push(...Object.keys(grouping).map(function (key) { return `grouping=${key}:${grouping[key]}` }));
            return q.join('&');
        },
        clone: function(){
            var self = this;
            var filters = {};
            var grouping = {};
            Object.keys(this.filters).forEach(function(key){
                filters[key] = [...self.filters[key]];
            });
            Object.assign(grouping, this.grouping);
            return new moola.Query(filters, grouping);
        },
        addFilter: function (key, value) {
            if (Array.isArray(value)) this.filters[key] = [...value];
            else {
                if (!this.filters[key]) this.filters[key] = [];
                this.filters[key].push(value);
            }
        },
        removeFilter: function (key, value) {
            if (!this.filters[key]) return;
            this.filters[key] = without(this.filters[key], value);
            if (value === undefined || this.filters[key].length === 0) delete this.filters[key];
        },
        setGrouping: function (key, value) {
            if (value === undefined) value = true;
            if (!value && this.grouping[key])
                delete this.grouping[key];
            else
                this.grouping[key] = value;
        },
        withNewFilter: function (key, value) {
            if (value === '?') value = '';
            var filters = {};
            var grouping = {};
            filters[key] = [value];
            Object.assign(grouping, this.grouping);
            return new Query(filters, grouping);
        },
        withAddFilter: function (key, value) {
            if (value == '?') value = '';
            var r = this.clone();
            if (value === null)
                r.removeFilter(key);
            else
                r.addFilter(key, value);
            return r;
        },
        withoutFilterValue: function (key, value) {
            if (value == '?') value = "";
            var q = this.clone();
            q.removeFilter(key, value);
            return q;
        },
        isGrouped: function (by) {
            if (Object.keys(this.grouping).length === 0) return false;
            if (by) return !!this.grouping[by];
            else return true;
        }
    }
}());
