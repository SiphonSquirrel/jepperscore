var roundsInProgress = new Array();
var roundFocus = "";
var quake3colours = [ "#000000", "#ff0000", "#00ff00", "#ffff00", "#0000ff",
		"#00ffff", "#ff00ff", "#ffffff", "#ff7f00", "#7f7f7f", "#bfbfbf",
		"#bfbfbf", "#007f00", "#7f7f00", "#00007f", "#7f0000", "#7f3f00",
		"#ff9919", "#007f7f", "#7f007f", "#007fff", "#7f00ff", "#3399cc",
		"#ccffcc", "#006633", "#ff0033", "#b21919", "#993300", "#cc9933",
		"#999933", "#ffffbf", "#ffff7f" ];

function newGame() {
	$("#teamscores").empty();
	$("#scores").empty();
	$("#messages").empty();
}

function logGameMessage(msg) {
	$("#messages").prepend("<div>" + msg + "</div>");
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
	} else if (msg.event) {
		processEvent(msg.event);
	} else if (msg.alias) {
		processAlias(msg.alias);
	}
}

function makeId(s) {
	if (s) {
		return s.replace(/ /g, '_').replace(/:/g, '_');
	} else {
		return s;
	}
}

function parseEventText(event) {
	var text = event.eventText;
	text = text.replace(/{attacker}/g, getHtmlForAliasName(event.attacker));
	text = text.replace(/{victim}/g, getHtmlForAliasName(event.victim));
	return text;
}

function processRound(round) {
	logGameMessage("Round message!");
}

function processTeam(team) {
	updateTeam(team);
}

function processScore(score) {
	updatePlayerScore(score.alias, score.score);
	processAlias(score.alias);
}

function processEvent(event) {
	logGameMessage(parseEventText(event));
}

function getHtmlForAliasName(alias) {
	if (alias.decorationStyle && (alias.decorationStyle == "quake3")) {
		var html = '<span>';
		var i = 0;
		while (i < alias.name.length) {
			if (alias.name[i] == "^") {
				i++;
				if (html == "<span>") {
					html = "";
				} else {
					html += "</span>"
				}
				html += "<span style='color: " + quake3colours[(alias.name.charCodeAt(i) - 16) % 32] + "'>";
			} else {
				html += alias.name[i];
			}
			i++;
		}
		html += '</span>';
		return html;
	} else {
		return '<span>'+alias.name+'</span>';
	}
}

function processAlias(alias) {
	if (alias.present) {
		ensureTeamCreated(alias.team);
		ensureAliasCreated(alias);
		var aliasElement = $(document.getElementById(getAliasId(alias)));
		if (aliasElement.data("name") != alias.name) {
			aliasElement.data("name", alias.name);
			
			var nameElement = aliasElement.children(".playerName");
			
			nameElement.append(getHtmlForAliasName(alias));
		}
	} else {
		var element = document.getElementById(getAliasId(alias));
		if (element) {
			element.parentNode.removeChild(element);
		}
	}
}

function getTeamBlockId(team) {
	if (team && team.teamName) {
		return "TEAM_BLOCK_" + makeId(team.teamName);
	} else {
		return "TEAM_BLOCK_";
	}
}

function getScoreBlockId(team) {
	if (team && team.teamName) {
		return "SCORE_BLOCK_" + makeId(team.teamName);
	} else {
		return "SCORE_BLOCK_";
	}
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
		if (team && team.teamName) {
			teamName.appendChild(document.createTextNode(team.teamName));
		}

		var teamScore = document.createElement("span");
		teamBlock.appendChild(teamScore);
		teamScore.className = "teamScore";
		if (team && team.score) {
			teamScore.appendChild(document.createTextNode(team.score));
		}
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
		
		var playerName = document.createElement("span");
		aliasElement.appendChild(playerName);
		playerName.className = "playerName";

		var playerScore = document.createElement("span");
		aliasElement.appendChild(playerScore);
		playerScore.className = "playerScore";
	} else {
		var parent = document.getElementById(getScoreBlockId(alias.team));
		if (document.getElementById(getScoreBlockId(alias.team)) != aliasElement.parentNode) {
			aliasElement.parentNode.removeChild(aliasElement);
			parent.appendChild(aliasElement);
		}
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
				return s1 > s2 ? -1 : 1;
			} else {
				return $(a).data("name") < $(b).data("name") ? -1 : 1;
			}
		});
		$(this).append(players);
	});

}
