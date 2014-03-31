package jepperscore.backends.couchdb;

import java.net.MalformedURLException;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.transport.TransportMessage;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link IMessageDestination} using ActiveMQ.
 *
 * @author Chuck
 *
 */
public class CouchDbMessageDestination implements IMessageDestination {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(CouchDbMessageDestination.class);

	/**
	 * The database to connect to.
	 */
	private CouchDbConnector db;

	/**
	 * Creates the message destination.
	 *
	 * @param couchdbString
	 *            The couchdb setup string.
	 * @throws MalformedURLException
	 */
	public CouchDbMessageDestination(String couchdbString)
			throws MalformedURLException {
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
	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {
		if (transportMessage.getSessionId() == null) {
			LOG.warn("Sending message without session ID.");
		}
		
		if (transportMessage.getId() == null) {
			db.create(transportMessage);
		} else {
			TransportMessage oldMsg = db.get(TransportMessage.class,
					transportMessage.getId());
			transportMessage.setRevision(oldMsg.getRevision());
			db.update(transportMessage);
		}
	}
}
