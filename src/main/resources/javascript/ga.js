// ga.js : GA web UI
// depends on jQuery

var GA = {};

GA.keys = function (someObject) {
    var key, theKeys = [];
    for (key in someObject) {
        if (someObject.hasOwnProperty(key)) {
            theKeys.push(key);
        }
    }
    return theKeys;
};

GA.pingSuccess = function (results) {
    console.log('pingSuccess: results => ');
    console.dir(results);
    $.each(results, function(key,val) {
               var locator = '#' + key,
                   elt = $(locator);
               console.log('locator=' + locator);
               console.log('elt=' + elt);
               if (elt)
                   elt.html(results[key]);
           });
    // window.setTimeout(GA.pingStats, 10000);
};

GA.pingError = function (data) {
    console.log('Error! data is => ');
    console.dir(data);
    // window.setTimeout(GA.pingStats, 10000);
};

GA.pingStats = function () {
    $.ajax({
               url: "/ga/current-generation.json",
               type: 'GET',
               dataType: 'json',
               success: GA.pingSuccess,
               error:   GA.pingError,
           });
};

GA.init = function () {
    // window.setTimeout(GA.pingStats, 1000);
    $('#refresh-button').click(GA.pingStats);
};

$(document).ready(GA.init);