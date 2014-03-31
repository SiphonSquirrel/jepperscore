var roundsInProgress = new Array();
var roundFocus = "";

function newGame() {
	$("#teamscores").empty();
	$("#scores").empty();
	$("#messages").empty();
}

function logGameMessage(msg) {
	var msgElement = document.createElement("div");
	msgElement.appendChild(document.createTextNode(msg));

	$("#messages").prepend(msgElement);
}

function processMessage(msg) {
	if (msg.sessionId != roundFocus) {
		return;
	}

	if (msg.round) {
		processRound(msg.round);
	} else if (msg.team) {
		processTeam(msg.team);
	} else if (msg.score) {
		processScore(msg.score);
	}
}

function makeId(s) {
	return s.replace(/ /g, '_');
}

function processRound(round) {
	logGameMessage("Round message!");
}

function processTeam(team) {
	updateTeam(team);
}

function processScore(score) {
	updatePlayerScore(score.alias, score.score);
}

function getTeamBlockId(team) {
	return "TEAM_BLOCK_" + makeId(team.teamName);
}

function getScoreBlockId(team) {
	return "SCORE_BLOCK_" + makeId(team.teamName);
}

function getAliasId(alias) {
	return "ALIAS_" + makeId(alias.id);
}

function ensureTeamCreated(team) {
	var teamBlock = document.getElementById(getTeamBlockId(team));
	if (teamBlock == null) {
		// <div id="team1" class="teamBlock">
		// <span class="teamName">Red</span> <span class="teamScore">4</span>

		teamBlock = document.createElement("div");
		document.getElementById("teamscores").appendChild(teamBlock);
		teamBlock.id = getTeamBlockId(team);
		teamBlock.className = "teamBlock";

		var teamName = document.createElement("span");
		teamBlock.appendChild(teamName);
		teamName.className = "teamName";
		teamName.appendChild(document.createTextNode(team.teamName));

		var teamScore = document.createElement("span");
		teamBlock.appendChild(teamScore);
		teamScore.className = "teamScore";
		teamScore.appendChild(document.createTextNode(team.score));
	}

	var scoreBlock = document.getElementById(getScoreBlockId(team));
	if (scoreBlock == null) {
		// <div id="team1scores" class="scoreBlock">
		scoreBlock = document.createElement("div");
		document.getElementById("scores").appendChild(scoreBlock);
		scoreBlock.id = getScoreBlockId(team);
		scoreBlock.className = "scoreBlock";
	}
}

function ensureAliasCreated(alias) {
	var aliasElement = document.getElementById(getAliasId(alias));
	if (aliasElement == null) {
		// <div id="player1" class="playerBlock">
		// <span class="playerName">Player1</span>
		// <span class="playerScore">5</span>

		var scoreBlock = document.getElementById(getScoreBlockId(alias.team));

		aliasElement = document.createElement("div");
		scoreBlock.appendChild(aliasElement);
		aliasElement.id = getAliasId(alias);
		aliasElement.className = "playerBlock";
		$(aliasElement).data("name", alias.name);

		var playerName = document.createElement("span");
		aliasElement.appendChild(playerName);
		playerName.className = "playerName";
		playerName.appendChild(document.createTextNode(alias.name));

		var playerScore = document.createElement("span");
		aliasElement.appendChild(playerScore);
		playerScore.className = "playerScore";
	}
}

function updateTeam(team) {
	ensureTeamCreated(team);
	$("#" + getTeamBlockId(team) + " .teamScore").text(team.score);
}

function updatePlayerScore(alias, score) {
	ensureTeamCreated(alias.team);
	ensureAliasCreated(alias);
	$("#" + getAliasId(alias) + " .playerScore").text(score);
	$("#" + getAliasId(alias)).data("score", score);
	$(".scoreBlock").each(function(index) {
		var players = $(this).children(".playerBlock");
		players.detach().sort(function(a, b) {
			var s1 = $(a).data("score");
			var s2 = $(b).data("score");
			if (s1 != s2) {
				return s1 > s2 ? -1: 1;
			} else {
				return $(a).data("name") < $(b).data("name") ? -1 : 1;
			}
		});
		$(this).append(players);
	});
}
