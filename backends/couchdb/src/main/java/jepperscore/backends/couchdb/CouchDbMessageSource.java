package jepperscore.backends.couchdb;

import java.io.IOException;
import java.net.MalformedURLException;

import jepperscore.dao.AbstractMessageSource;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.transport.TransportMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link IMessageSource} using CouchDb.
 *
 * @author Chuck
 *
 */
public class CouchDbMessageSource extends AbstractMessageSource implements Runnable {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(CouchDbMessageSource.class);

	/**
	 * The database to connect to.
	 */
	private CouchDbConnector db;

	/**
	 * The thread checking the feed.
	 */
	private Thread feedThread;

	/**
	 * Creates the message destination.
	 *
	 * @param couchdbString
	 *            The connection string to use for CouchDb.
	 * @throws MalformedURLException
	 */
	public CouchDbMessageSource(String couchdbString) throws MalformedURLException {
		String[] configArray = couchdbString.split(";");
		String server = configArray[0];
		String dbName = "pepperscore";

		if (configArray.length >= 2) {
			dbName = configArray[1];
		}

		LOG.info("Connecting to " + server + " (DB: " + dbName + ") using the CouchDB backend.");

		HttpClient httpClient = new StdHttpClient.Builder().url(server).build();

		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		db = new StdCouchDbConnector(dbName, dbInstance);
		db.createDatabaseIfNotExists();
		LOG.info("Connected to CouchDB. Relax.");

		feedThread = new Thread(this);
		feedThread.setDaemon(true);
		feedThread.start();
	}

	@Override
	public void run() {
		ChangesCommand cmd = new ChangesCommand.Builder().includeDocs(true).continuous(true).heartbeat(100).build();
		ChangesFeed feed = db.changesFeed(cmd);

		ObjectMapper mapper = new ObjectMapper();

		while (feed.isAlive()) {
			try {
				DocumentChange item = feed.next();
				String doc = item.getDoc();
				TransportMessage msg = mapper.readValue(doc, TransportMessage.class);

				call(msg);
			} catch (InterruptedException e) {
				break;
			} catch (IOException e) {
				 LOG.error(e.getMessage(), e);
				 break;
			}
		}
	}

}
