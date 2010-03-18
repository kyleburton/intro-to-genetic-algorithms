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

GA.slot = function (houseNo, propIdx) {
    return (houseNo-1) * 5 + propIdx;
};

// http://localhost:8001/images/pet-icons/horse-button.png

GA.iconLookup = {
  "Blend":      "images/cigar-icons/blend.png",
  "BlueMaster": "images/cigar-icons/blue-master.png",
  "Dunhill":    "images/cigar-icons/dunhill.png",
  "PallMall":   "images/cigar-icons/pall-mall.png",
  "Prince":     "images/cigar-icons/prince.png",
  "bier":       "images/drink-icons/beer-button.png",
  "coffee":     "images/drink-icons/coffee-button.png",
  "milk":       "images/drink-icons/milk-button.png",
  "tea":        "images/drink-icons/tea-button.png",
  "water":      "images/drink-icons/water-button.png",
  "Dane":       "images/flag-icons/denmark.png",
  "German":     "images/flag-icons/germany.png",
  "Norwegian":  "images/flag-icons/norway.png",
  "Swede":      "images/flag-icons/sweden.png",
  "Englishman": "images/flag-icons/uk.png",
  "blue":       "images/house-icons/house-blue.png",
  "green":      "images/house-icons/house-green.png",
  "red":        "images/house-icons/house-red.png",
  "white":      "images/house-icons/house-white.png",
  "yellow":     "images/house-icons/house-yellow.png",
  "cats":       "images/pet-icons/cat-button.png",
  "dogs":       "images/pet-icons/dog-button.png",
  "fish":       "images/pet-icons/fish-button.png",
  "horses":     "images/pet-icons/horse-button.png",
  "birds":      "images/pet-icons/parrot-button.png"
};

GA.imageUrl = function (attr) {
  return "<img height=\"64\" width=\"64\" src='/" + GA.iconLookup[attr] + "' />";

};

Jaml.register(
    'genome',
    function (genome) {
        table(
            // house icons
            tr(td(GA.imageUrl(genome[1][GA.slot(1,0)])),
               td(GA.imageUrl(genome[1][GA.slot(2,0)])),
               td(GA.imageUrl(genome[1][GA.slot(3,0)])),
               td(GA.imageUrl(genome[1][GA.slot(4,0)])),
               td(GA.imageUrl(genome[1][GA.slot(5,0)]))),
            // nationality
            tr(td(GA.imageUrl(genome[1][GA.slot(1,1)])),
               td(GA.imageUrl(genome[1][GA.slot(2,1)])),
               td(GA.imageUrl(genome[1][GA.slot(3,1)])),
               td(GA.imageUrl(genome[1][GA.slot(4,1)])),
               td(GA.imageUrl(genome[1][GA.slot(5,1)]))),
            // drink
            tr(td(GA.imageUrl(genome[1][GA.slot(1,2)])),
               td(GA.imageUrl(genome[1][GA.slot(2,2)])),
               td(GA.imageUrl(genome[1][GA.slot(3,2)])),
               td(GA.imageUrl(genome[1][GA.slot(4,2)])),
               td(GA.imageUrl(genome[1][GA.slot(5,2)]))),
            // cigar
            tr(td(GA.imageUrl(genome[1][GA.slot(1,3)])),
               td(GA.imageUrl(genome[1][GA.slot(2,3)])),
               td(GA.imageUrl(genome[1][GA.slot(3,3)])),
               td(GA.imageUrl(genome[1][GA.slot(4,3)])),
               td(GA.imageUrl(genome[1][GA.slot(5,3)]))),
            // pet
            tr(td(GA.imageUrl(genome[1][GA.slot(1,4)])),
               td(GA.imageUrl(genome[1][GA.slot(2,4)])),
               td(GA.imageUrl(genome[1][GA.slot(3,4)])),
               td(GA.imageUrl(genome[1][GA.slot(4,4)])),
               td(GA.imageUrl(genome[1][GA.slot(5,4)])))
       );
    });

GA.displayGenome = function (results) {
    var genome = results['best-genome'];
    $('#best-score').html((genome[0] * 100) + '%');
    $('#avg-score').html((results['avg-score'] * 100) + '%');
    $('#best-genome').html(Jaml.render('genome',[genome]));

    $('#raphael-graph').html('');
    var r = Raphael("raphael-graph");
    r.g.txtattr.font = "12px 'Fontin Sans', Fontin-Sans, sans-serif";

    var generations = [], best_scores = [], avg_scores = [];
    var numScores = results['best-scores'].length;
    temp = results;
    var max = numScores; //  > 20 ? 20 : numScores;
    //for (var i = 0; i < results['best-scores'].length; i++) {
    for (var i = 0; i < max; i++) {
        generations[i] = i;
        best_scores[i] = results['best-scores'][i][1];
        if ( 'undefined' === typeof(best_scores[i])) best_scores[i] = 0;
        best_scores[i] = best_scores[i] * 100;

        avg_scores[i] = 0;
        if (  results['avg-scores'][i] ) {
            avg_scores[i] = results['avg-scores'][i][1];
            if ( 'undefined' === typeof(avg_scores[i])) avg_scores[i] = 0;
            avg_scores[i] = avg_scores[i] * 100;
        }
    }

    r.g.text(160, 10, "Best Score and Avg Score by generation");
    r.g.linechart(10, 10, 320, 300, generations, [best_scores, avg_scores], {shade: true});

    /*
    r.g.text(160, 10, "Simple Line Chart");
    r.g.text(480, 10, "shade = true");
    r.g.text(160, 250, "shade = true & nostroke = true");
    r.g.text(480, 250, "Symbols, axis and hover effect");
    r.g.linechart(10, 10, 300, 220, x, [y, y2, y3]);
    r.g.linechart(330, 10, 300, 220, x, [y, y2, y3], {shade: true});
    r.g.linechart(10, 250, 300, 220, x, [y, y2, y3], {nostroke: true, shade: true});
    var lines = r.g.linechart(330, 250, 300, 220,
                              [[1, 2, 3, 4, 5, 6, 7],
                               [3.5, 4.5, 5.5, 6.5, 7, 8]],
                              [[12, 32, 23, 15, 17, 27, 22],
                               [10, 20, 30, 25, 15, 28]],
                              {nostroke: false, axis: "0 0 1 1", symbol: "o"}
                             ).hoverColumn(function () {
                                               this.tags = r.set();
                                               for (var i = 0, ii = this.y.length; i < ii; i++) {
                                                   this.tags.push(r.g.tag(this.x, this.y[i], this.values[i], 160, 10).insertBefore(this).attr([{fill: "#fff"}, {fill: this.symbols[i].attr("fill")}]));
                                               }
                                           }, function () {
                                               this.tags && this.tags.remove();
                                           });
     */
};

GA.pingSuccess = function (results) {
    $('#generation-number').html(results['generation-number']);
    GA.displayGenome(results);
    // window.setTimeout(GA.pingStats, 2500);
    $('#messages').html("Retrieved current stats.");
};

GA.pingError = function (data) {
    console.log('Error! data is => ');
    console.dir(data);
    $('#messages').html("Error retreiving simulation info: " + data);
};

GA.pingStats = function () {
    $.ajax({
               url: "/ga/current-generation.json",
               type: 'GET',
               dataType: 'json',
               success: GA.pingSuccess,
               error:   GA.pingError
           });
};

GA.init = function () {
    $('#refresh-button').click(GA.pingStats);
    GA.pingStats();
};

$(document).ready(GA.init);