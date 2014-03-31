//CouchDB API --> http://java.dzone.com/news/couchdb-jquery-plugin

var cdb_backlog = new Array();
var cdb_holdBacklog = true;

$(document).ready(function() {
	$.couch.db("pepperscore").changes().onChange(function(data) {
		for ( var index in data.results) {
			var row = data.results[index];
			$.couch.db("pepperscore").openDoc(row.id, {
				success : function(msg) {
					if (cdb_holdBacklog) {
						cdb_backlog.push(msg);
					} else {
						cdb_processMessage(msg);
					}
				},
				error : function(status) {
					alert(status);
				}
			});
		}
	});

	cdb_connectToGame();
});

function cdb_connectToGame() {
	cdb_holdBacklog = true;
	$.couch.db("pepperscore").view("pepperscore/inprogressround", {
		success : function(data) {
			var tempRounds = new Array();

			var latestStart = "";
			var latestRound = "";

			for ( var index in data.rows) {
				var row = data.rows[index];

				if ((latestStart == "") || (latestStart < row.value)) {
					latestRound = row.key;
					latestStart = row.value;
				}

				tempRounds[row.key] = row.value;
			}

			roundsInProgress = tempRounds;

			if (latestRound != "") {
				newGame();
				cdb_setRoundFocus(latestRound);
			} else {
				setTimeout(cdb_connectToGame, 500);
			}
		},
		error : function(status) {
			alert(status);
		},
		reduce : false
	});
}

function cdb_setRoundFocus(roundId) {
	roundFocus = roundId;

	$.couch.db("pepperscore").view("pepperscore/bysessionid", {
		success : function(data) {
			for ( var index in data.rows) {
				var row = data.rows[index];
				cdb_processMessage(row.value);
			}

			cdb_processBacklog();
		},
		error : function(status) {
			alert(status);
		},
		keys : [roundFocus],
		reduce : false
	});
}

function cdb_processBacklog() {
	for ( var index in cdb_backlog) {
		var msg = cdb_backlog[index];
		cdb_processMessage(msg);
	}
	cdb_holdBacklog = false;
}

function cdb_processMessage(msg) {
	processMessage(msg);
	if ((msg.sessionId == roundFocus) && (msg.round)) {
		if ((msg.round.end) && (msg.round.end != null)) {
			setTimeout(cdb_connectToGame, 500);
		}
	}
}
