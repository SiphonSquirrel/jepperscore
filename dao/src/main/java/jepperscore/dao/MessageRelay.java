package jepperscore.dao;

import jepperscore.dao.transport.TransportMessage;

public class MessageRelay implements IMessageCallback {
	private AbstractMessageSource source;
	private IMessageDestination dest;

	public MessageRelay(AbstractMessageSource source, IMessageDestination dest) {
		this.source = source;
		this.dest = dest;
		
		this.source.registerCallback(this);	
	}

	@Override
	public void onMessage(TransportMessage message) {
		dest.sendMessage(message);
	}
}
