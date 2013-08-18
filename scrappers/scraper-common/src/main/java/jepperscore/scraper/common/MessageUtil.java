package jepperscore.scraper.common;

import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jepperscore.dao.transport.TransportMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for messaging.
 *
 * @author Chuck
 *
 */
public final class MessageUtil {
	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(MessageUtil.class);

	/**
	 * JAXB Context.
	 */
	private static final JAXBContext jaxbContext;

	static {
		JAXBContext ctx = null;
		try {
			ctx = JAXBContext.newInstance(TransportMessage.class);
		} catch (JAXBException e) {
			LOG.error(e.getMessage(), e);
		}
		jaxbContext = ctx;
	}

	/**
	 * Private constructor.
	 */
	private MessageUtil() {
	}

	/**
	 * This function sends a message to ActiveMQ.
	 *
	 * @param producer
	 *            The ActiveMQ producer.
	 * @param session
	 *            The ActiveMQ session.
	 * @param transportMessage
	 *            The message to send.
	 */
	public static void sendMessage(final MessageProducer producer, final Session session,
			final TransportMessage transportMessage) {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();

			StringWriter writer = new StringWriter();
			marshaller.marshal(transportMessage, writer);

			producer.send(session.createTextMessage(writer.toString()));
		} catch (JMSException | JAXBException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
