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

GA.slotNum = function (houseNo, propIdx) {
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

GA.answer = ["green","German","coffee","Prince","fish","white","Swede","bier","BlueMaster","dogs","red","Englishman","milk","Blend","horses","yellow","Norwegian","water","Dunhill","cats","blue","Dane","tea","PallMall","birds"];

// TODO: also put on tool tips or other text to show what each val is so users don't have to memorize the pictures...
GA.cellContent = function( xpos, ypos, genome ) {
    var openTag = '<span ',
        pos = GA.slotNum(xpos,ypos),
        val = genome[1][pos];
    if ( val === GA.answer[pos]) {
        openTag += "class='correct'";
    }
    else {
        openTag += "class='incorrect'";
    }
    return openTag + '>' + GA.imageUrl(val) + '<br/>' + val + '</span>';
};

Jaml.register(
    'genome',
    function (genome) {
        table(
            // house icons
            tr(td(GA.cellContent(1,0,genome)),
               td(GA.cellContent(2,0,genome)),
               td(GA.cellContent(3,0,genome)),
               td(GA.cellContent(4,0,genome)),
               td(GA.cellContent(5,0,genome))),
            // nationality
            tr(td(GA.cellContent(1,1,genome)),
               td(GA.cellContent(2,1,genome)),
               td(GA.cellContent(3,1,genome)),
               td(GA.cellContent(4,1,genome)),
               td(GA.cellContent(5,1,genome))),
            // drink
            tr(td(GA.cellContent(1,2,genome)),
               td(GA.cellContent(2,2,genome)),
               td(GA.cellContent(3,2,genome)),
               td(GA.cellContent(4,2,genome)),
               td(GA.cellContent(5,2,genome))),
            // cigar
            tr(td(GA.cellContent(1,3,genome)),
               td(GA.cellContent(2,3,genome)),
               td(GA.cellContent(3,3,genome)),
               td(GA.cellContent(4,3,genome)),
               td(GA.cellContent(5,3,genome))),
            // pet
            tr(td(GA.cellContent(1,4,genome)),
               td(GA.cellContent(2,4,genome)),
               td(GA.cellContent(3,4,genome)),
               td(GA.cellContent(4,4,genome)),
               td(GA.cellContent(5,4,genome)))
       );
    });

GA.appendMessage = function (msg,t) {
    t = t || 'p';
    var tag = $('<' + t + '>');
    tag.html(msg);
    $('#messages').append(tag);
    return tag;
};

GA.displayGenome = function (results) {
    var genome = results['best-genome'];

    // GA.appendMessage(JSON.stringify(results['best-genome'][1]),'pre');

    // display the list of passed / failed predicates with some markup
    $.each(results['best-fitness'], function (idx, pred) {
               // var tag = GA.appendMessage(JSON.stringify(pred), 'p');
               var tag = GA.appendMessage(pred.name, 'p');
               tag.addClass( pred.passed ? 'correct' : 'incorrect' );
           });

    $('#best-score').html((genome[0] * 100) + '%');
    $('#avg-score').html((results['avg-score'] * 100) + '%');
    $('#best-genome').html(Jaml.render('genome',[genome]));

    $('#raphael-graph').html('');
    var raphael = Raphael("raphael-graph");
    raphael.g.txtattr.font = "12px 'Fontin Sans', Fontin-Sans, sans-serif";

    var generations = [], best_scores = [], avg_scores = [];
    var numScores = results['best-scores'].length;
    temp = results;
    var max = numScores;
    for (var i = 0; i < max; i++) {
        generations[i] = i;
        best_scores[i] = results['best-scores'][i][1];
        if ( 'undefined' === typeof(best_scores[i])) best_scores[i] = 0;
        best_scores[i] = best_scores[i] * 100;

        avg_scores[i] = 0;
        if ( results['avg-scores'][i] ) {
            avg_scores[i] = results['avg-scores'][i][1];
            if ( 'undefined' === typeof(avg_scores[i])) avg_scores[i] = 0;
            avg_scores[i] = avg_scores[i] * 100;
        }
        else {
            avg_scores[i] = avg_scores[i-1] || 0;
        }
    }

    raphael.g.text(160, 10, "Best Score and Avg Score by generation");
    raphael.g.linechart(10, 10, 320, 300, generations, [best_scores, avg_scores], {shade: true});
};

GA.pingSuccess = function (results) {
    $('#generation-number').html(results['generation-number']);
    GA.displayGenome(results);
    // window.setTimeout(GA.pingStats, 2500);
    // $('#messages').append($('<p>').html("Retrieved current stats."));
    // $('#messages').html("Retrieved current stats.");
    GA.appendMessage("Retreive current stats");
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