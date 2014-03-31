package jepperscore.backends.couchdb;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbUtils {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(CouchDbUtils.class);

	private CouchDbUtils() {
	}

	public static CouchDbConnector setupCouchDb(String server, String dbName) throws MalformedURLException {
		HttpClient httpClient = new StdHttpClient.Builder().url(server).build();

		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		StdCouchDbConnector db = new StdCouchDbConnector(dbName, dbInstance);
		db.createDatabaseIfNotExists();
		LOG.info("Connected to CouchDB. Relax.");

		try {
			db.getDesignDocInfo("pepperscore");
		} catch (DocumentNotFoundException e) {
			Map<String, Object> doc = new HashMap<String, Object>();
			doc.put("language", "javascript");

			Map<String, Object> views = new HashMap<String, Object>();
			doc.put("views", views);
			
			HashMap<String, String> bysessionidView = new HashMap<String, String>();
			bysessionidView.put("map",
					"function(doc) {\n" +
							"  emit(doc.sessionId, doc);\n" +
							"}");
			views.put("bysessionid", bysessionidView);
			
			HashMap<String, String> inprogressroundView = new HashMap<String, String>();
			inprogressroundView.put("map",
					"function(doc) {\n" +
							"  if (doc.round) {\n" +
							"    if (!doc.round.end) {\n" +
							"      emit(doc.sessionId, doc.round.start);\n" +
							"    }\n" +
							"  }" +
							"\n}");
			views.put("inprogressround", inprogressroundView);

			db.create("_design/pepperscore", doc);
		}

		return db;
	}
}
