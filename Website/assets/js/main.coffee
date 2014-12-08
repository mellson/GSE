surveySeries = {
	"a": [1, 3, 5, 7, 9, 11, 13],
	"b": [2, 4, 6, 8, 10, 12, 14],
	"c": [15, 17, 19, 21, 23, 25, 27],
	"d": [29, 18, 31, 22, 33, 26, 35],
	"e": [16, 30, 20, 32, 24, 34, 28]
}

surveyResult = {
	"answers": [],
	"survey" : surveySeries.a,
	"nextClips": surveySeries.a,
	"playing": false
}

surveyToShow = null

videoSource = (clip) ->
	return '<video width="640" height="480" autoplay preload>
				<source src="videos/'+clip+'.mp4" type="video/mp4">
			</video>'

randomNumber = (min, max) ->
	return Math.floor(Math.random() * (max - min + 1) + min)

generateSeries = (series) ->
	arr = []
	arr.push i for i in series
	arr.push(series[randomNumber(0, 6)]) for i in [0..2]

	return arr

insertResult = (answer) ->
	surveyResult.answers.push answer

getSelectedAnswer = ->
	return parseInt($('#form input[name="prediction"]:checked').val())

nextClip = ->
	insertResult getSelectedAnswer()

	if surveyResult.answers.length == 10
		endSurvey()
	else
		clip = surveyResult.nextClips[0]
		surveyResult.nextClips.shift()

		showClip(clip)
	
showClip = (clip) ->
	$('#video').html(videoSource(clip))
	videoTag = $("#video video")[0]
	videoTag.addEventListener 'ended', (e) ->
		surveyResult.playing = false
	videoTag.addEventListener 'play', (e) ->
		surveyResult.playing = true

	$('#form input[name="prediction"]:checked').each ->
		$(this).prop("checked", false)

	$('#current_clip').html(10 - surveyResult.nextClips.length)

	if surveyResult.nextClips.length == 0
		$('#done').val("Submit answers")

startSurvey = (survey) ->
	$('#end-survey').hide()
	$('#video-survey').show()
	$('.howto-container').hide()
	$('.setup-container').hide()

	surveyResult.survey = generateSeries(survey)
	surveyResult.answers = []
	surveyResult.nextClips = owl.clone(surveyResult.survey)
	clip = surveyResult.nextClips[0]
	surveyResult.nextClips.shift()

	showClip(clip)

endSurvey = () ->
	$('#video-survey').hide()
	$('#end-survey').show()

answerFromCode = (code) ->
	switch code
		when 49 then return 1
		when 50 then return 2
		when 51 then return 3
		when 52 then return 4
		when 53 then return 5
		when 97 then return 1
		when 98 then return 2
		when 99 then return 3
		when 100 then return 4
		when 101 then return 5
		else return null

selectAnswer = (answer) ->
	$('input[name="prediction"]:checked').each ->
		$(this).prop("checked", false)
	$('#prediction_' + answer).prop("checked", true)

canProceed = ->
	return false if $('#form input[name="prediction"]:checked').val() == undefined
	# return false if surveyResult.playing == true
	return true

$(document).ready ->
	$('#end-survey').hide()
	$('#video-survey').hide()
	$('.howto-container').hide()
	$('.setup-container').show()

	$(document).keyup (e) ->
		answer = answerFromCode e.keyCode
		selectAnswer(answer) if answer != null

		nextClip() if e.keyCode == 13 && canProceed()

	$('#done').click (e) ->
		e.preventDefault()

		console.log surveyResult

		return if !canProceed()
		nextClip()

	$('#print').click (e) ->
		strings = surveyResult.answers.map (a) -> a.toString()
		email = $('#email').val()
		$('#results').html(email + ",[" + surveyResult.survey + "], " + strings.join ", ")

	$('#start').click (e) ->
		startSurvey(surveyToShow)

	$('#prepare').click (e) ->
		survey = null
		switch $('#which').val()
			when "a" then survey = surveySeries.a
			when "b" then survey = surveySeries.b
			when "c" then survey = surveySeries.c
			when "d" then survey = surveySeries.d
			when "e" then survey = surveySeries.e

		surveyToShow = survey

		$('.howto-container').show()
		$('.setup-container').hide()

