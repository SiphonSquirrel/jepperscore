package jepperscore.tools.webboard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

public class InstallerMain {

	private static final String SITE_DOC_ID = "main";

	/**
	 * Specifies the CouchDB url.
	 */
	private static final String COUCHDB_URL_ARG = "u";

	/**
	 * Files to install.
	 */
	private static final String[] COUCHDB_FILES = new String[] {
			"index.html", "jepperscore-couchdb.js", "jepperscore.js", "scoreboard.css"
	};

	/**
	 * The main function.
	 * 
	 * @param args
	 *            [Active MQ Connection String]
	 * @throws ParseException
	 *             Exception throw from parsing problems.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(COUCHDB_URL_ARG, true, "Specifies the CouchDB URL.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(COUCHDB_URL_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -u [CouchDB URL]");
		}

		String couchdbString = cmd.getOptionValue(COUCHDB_URL_ARG);

		String[] configArray = couchdbString.split(";");
		String server = configArray[0];
		String dbName = "pepperscore-webboard";

		if (configArray.length >= 2) {
			dbName = configArray[1];
		}

		System.out.println("Connecting to " + server + " (DB: " + dbName + ") using the CouchDB backend.");

		HttpClient httpClient;
		try {
			httpClient = new StdHttpClient.Builder().url(server).build();

			CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
			StdCouchDbConnector db = new StdCouchDbConnector(dbName, dbInstance);
			db.createDatabaseIfNotExists();
			System.out.println("Connected to CouchDB. Relax.");

			Map<Object, Object> doc = null;

			try {
				doc = db.get(Map.class, SITE_DOC_ID);
			} catch (DocumentNotFoundException e) {
				db.create(SITE_DOC_ID, new HashMap<String, String>());
			}

			for (String file : COUCHDB_FILES) {
				System.out.println(" Installing " + file);
				String contentType = "text/plain";
				if (file.endsWith(".js")) {
					contentType = "text/javascript";
				} else if (file.endsWith(".html")) {
					contentType = "text/html";
				} else if (file.endsWith(".css")) {
					contentType = "text/css";
				}
				InputStream is = null;
				try {
					is = InstallerMain.class.getResourceAsStream("/" + file);
					AttachmentInputStream data = new AttachmentInputStream(file,
							is, contentType);

					doc = db.get(Map.class, SITE_DOC_ID);
					String rev = (String) doc.get("_rev");
					Map<String, Object> attachmentList = (Map<String, Object>) doc.get("_attachments");
					if (attachmentList != null) {
						Map<String, Object> attachment = (Map<String, Object>) attachmentList.get(file);
						if (attachment != null) {
							db.deleteAttachment(SITE_DOC_ID, rev, file);
							doc = db.get(Map.class, SITE_DOC_ID);
							rev = (String) doc.get("_rev");
						}
					}

					db.createAttachment(SITE_DOC_ID, rev, data);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

}
