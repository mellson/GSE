(function() {
  var answerDistance, answerFromCode, canProceed, endSurvey, generateSeries, getSelectedAnswer, insertResult, isConfident, nextClip, randomNumber, selectAnswer, showClip, startSurvey, surveyResult, surveySeries, surveyToShow, videoSource;

  surveySeries = {
    "a": [1, 3, 5, 7, 9, 11, 13],
    "b": [2, 4, 6, 8, 10, 12, 14],
    "c": [15, 17, 19, 21, 23, 25, 27],
    "d": [29, 18, 31, 22, 33, 26, 35],
    "e": [16, 30, 20, 32, 24, 34, 28]
  };

  surveyResult = {
    "answers": [],
    "survey": surveySeries.a,
    "nextClips": surveySeries.a,
    "playing": false
  };

  surveyToShow = null;

  videoSource = function(clip) {
    return '<video width="640" height="480" autoplay preload controls>\
				<source src="videos/' + clip + '.mp4" type="video/mp4">\
			</video>';
  };

  randomNumber = function(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
  };

  generateSeries = function(series) {
    var arr, i, _i, _j, _len;
    arr = [];
    for (_i = 0, _len = series.length; _i < _len; _i++) {
      i = series[_i];
      arr.push(i);
    }
    for (i = _j = 0; _j <= 2; i = ++_j) {
      arr.push(series[randomNumber(0, 6)]);
    }
    return arr;
  };

  insertResult = function(answer) {
    return surveyResult.answers.push(answer);
  };

  getSelectedAnswer = function() {
    return parseInt($('#form input[name="prediction"]:checked').val());
  };

  nextClip = function() {
    var clip;
    insertResult(getSelectedAnswer());
    if (surveyResult.answers.length === 10) {
      return endSurvey();
    } else {
      clip = surveyResult.nextClips[0];
      surveyResult.nextClips.shift();
      return showClip(clip);
    }
  };

  showClip = function(clip) {
    var videoTag;
    $('#video').html(videoSource(clip));
    videoTag = $("#video video")[0];
    videoTag.addEventListener('ended', function(e) {
      return surveyResult.playing = false;
    });
    videoTag.addEventListener('play', function(e) {
      return surveyResult.playing = true;
    });
    $('#form input[name="prediction"]:checked').each(function() {
      return $(this).prop("checked", false);
    });
    $('#current_clip').html(10 - surveyResult.nextClips.length);
    if (surveyResult.nextClips.length === 0) {
      return $('#done').val("Submit answers");
    }
  };

  startSurvey = function(survey) {
    var clip;
    $('#end-survey').hide();
    $('#video-survey').show();
    $('.howto-container').hide();
    $('.setup-container').hide();
    surveyResult.survey = generateSeries(survey);
    surveyResult.answers = [];
    surveyResult.nextClips = owl.clone(surveyResult.survey);
    clip = surveyResult.nextClips[0];
    surveyResult.nextClips.shift();
    return showClip(clip);
  };

  endSurvey = function() {
    $('#video-survey').hide();
    return $('#end-survey').show();
  };

  answerFromCode = function(code) {
    switch (code) {
      case 49:
        return 1;
      case 50:
        return 2;
      case 51:
        return 3;
      case 52:
        return 4;
      case 53:
        return 5;
      case 97:
        return 1;
      case 98:
        return 2;
      case 99:
        return 3;
      case 100:
        return 4;
      case 101:
        return 5;
      default:
        return null;
    }
  };

  selectAnswer = function(answer) {
    $('input[name="prediction"]:checked').each(function() {
      return $(this).prop("checked", false);
    });
    return $('#prediction_' + answer).prop("checked", true);
  };

  canProceed = function() {
    if ($('#form input[name="prediction"]:checked').val() === void 0) {
      return false;
    }
    if (surveyResult.playing === true) {
      return false;
    }
    return true;
  };

  isConfident = function(result) {
    var answers, checks, d, discarded, dist, i, _i, _j, _len;
    answers = result.answers;
    checks = result.survey.slice(7, 10);
    for (i = _i = 1; _i <= 3; i = ++_i) {
      dist = answerDistance(answers, checks, i);
    }
    discarded = 0;
    for (_j = 0, _len = dist.length; _j < _len; _j++) {
      d = dist[_j];
      if (d >= 2) {
        discarded++;
      }
    }
    return discarded < 2;
  };

  answerDistance = function(answers, checks, i) {
    return Math.abs(answers[i] - checks[i]);
  };

  $(document).ready(function() {
    $('#end-survey').hide();
    $('#video-survey').hide();
    $('.howto-container').hide();
    $('.setup-container').show();
    $(document).keyup(function(e) {
      var answer;
      answer = answerFromCode(e.keyCode);
      if (answer !== null) {
        selectAnswer(answer);
      }
      if (e.keyCode === 13 && canProceed()) {
        return nextClip();
      }
    });
    $('#done').click(function(e) {
      e.preventDefault();
      console.log(surveyResult);
      if (!canProceed()) {
        return;
      }
      return nextClip();
    });
    $('#print').click(function(e) {
      var email, i, strings, valid, _i, _ref, _results;
      strings = surveyResult.answers.map(function(a) {
        return a.toString();
      });
      email = $('#email').val();
      valid = isConfident(surveyResult) ? "valid" : "invalid";
      $('#p_age').html(surveyResult.age);
      $('#p_sex').html(surveyResult.sex);
      $('#p_country').html(surveyResult.country);
      $('#p_email').html(email);
      $('#p_set').html("[" + surveyResult.survey + "]");
      $('#p_confidence').html(valid);
      _results = [];
      for (i = _i = 1, _ref = surveyResult.answers.length; 1 <= _ref ? _i <= _ref : _i >= _ref; i = 1 <= _ref ? ++_i : --_i) {
        _results.push($('#p_a' + i.toString()).html(surveyResult.answers[i - 1]));
      }
      return _results;
    });
    $('#start').click(function(e) {
      surveyResult.age = parseInt($('#age').val());
      surveyResult.sex = $('#sex').val();
      surveyResult.country = $('#country').val();
      surveyResult;
      return startSurvey(surveyToShow);
    });
    return $('#prepare').click(function(e) {
      var survey;
      survey = null;
      switch ($('#which').val()) {
        case "a":
          survey = surveySeries.a;
          break;
        case "b":
          survey = surveySeries.b;
          break;
        case "c":
          survey = surveySeries.c;
          break;
        case "d":
          survey = surveySeries.d;
          break;
        case "e":
          survey = surveySeries.e;
      }
      surveyToShow = survey;
      $('.howto-container').show();
      return $('.setup-container').hide();
    });
  });

}).call(this);
