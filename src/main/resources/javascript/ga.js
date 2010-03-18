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
  return "<img src='/" + GA.iconLookup[attr] + "' />";

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

GA.displayGenome = function (genome) {
    $('#best-score').html(genome[0]);
    $('#best-genome').html(Jaml.render('genome',[genome]));
};

GA.pingSuccess = function (results) {
    // console.log('pingSuccess: results => ');
    // console.dir(results);
    $('#generation-number').html(results['generation-number']);
    GA.displayGenome(results['best-genome']);
    // window.setTimeout(GA.pingStats, 10000);
    $('#messages').html("Retrieved current stats.");
};

GA.pingError = function (data) {
    console.log('Error! data is => ');
    console.dir(data);
    $('#messages').html("Error retreiving simulation info: " + data);
    // window.setTimeout(GA.pingStats, 10000);
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
    // window.setTimeout(GA.pingStats, 1000);
    $('#refresh-button').click(GA.pingStats);
    GA.pingStats();
};

$(document).ready(GA.init);