function event(){

    var listeners = [];

    var registrar = function(listener){
        listeners.push(listener);
    };

    var fire = function(){
        var args = arguments;
        listeners.forEach(function(listener){ listener.apply(null, args); })
    };

    return {
        fire: fire,
        registrar: registrar
    };
}