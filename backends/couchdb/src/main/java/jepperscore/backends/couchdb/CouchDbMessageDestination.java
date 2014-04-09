package jepperscore.backends.couchdb;

import java.net.MalformedURLException;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.transport.TransportMessage;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

		LOG.info("Connecting to " + server + " (DB: " + dbName
				+ ") using the CouchDB backend.");

		db = CouchDbUtils.setupCouchDb(server, dbName);
	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {
		if (transportMessage.getSessionId() == null) {
			LOG.warn("Sending message without session ID.");
		}

		if (transportMessage.getId() == null) {
			transportMessage.setId(DateTime.now(DateTimeZone.UTC).toString());
			db.create(transportMessage);
		} else {
			try {
				TransportMessage oldMsg = db.get(TransportMessage.class,
						transportMessage.getId());
				transportMessage.setRevision(oldMsg.getRevision());

				db.update(transportMessage);
			} catch (DocumentNotFoundException e) {
				db.create(transportMessage);
			}
		}
	}
}
