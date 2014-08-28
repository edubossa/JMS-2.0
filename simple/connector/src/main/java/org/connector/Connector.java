package org.connector;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Connector {

	protected static ConnectionFactory connectionFactory;
	protected static Queue queue;
	protected static Topic topic;

	public Connector() {
		prepareConnection();
	}

	public void prepareConnection() {

		try {

			Context ctx = new InitialContext();

			if (connectionFactory == null) {
				connectionFactory = (ConnectionFactory) ctx.lookup("java:comp/DefaultJMSConnectionFactory");
			}

			if (queue == null) {
				queue = (Queue) ctx.lookup("jms/Queue");
			}

			if (topic == null) {
				topic = (Topic) ctx.lookup("jms/Topic");
			}

		} catch (NamingException e) {

			e.printStackTrace();
		}

	}

}
