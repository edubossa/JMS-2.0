/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.messagebrowser;

import java.util.Enumeration;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.connector.Connector;

/**
 * The MessageBrowser class inspects a queue and displays the messages it holds.
 */
public class MessageBrowser extends Connector {

  /*  @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/Queue")
    private static Queue queue;*/

    /**
     * Main method.
     *
     * @param args not used
     */
    @SuppressWarnings("rawtypes")
	public static void main(String[] args) {
    	
    	new MessageBrowser();
    	
        QueueBrowser browser;

        /*
         * In a try-with-resources block, create context.
         * Create QueueBrowser.
         * Check for messages on queue.
         */
        try (JMSContext context = connectionFactory.createContext();) {
            browser = context.createBrowser(queue);
            Enumeration msgs = browser.getEnumeration();

            if (!msgs.hasMoreElements()) {
                System.out.println("No messages in queue");
            } else {
                while (msgs.hasMoreElements()) {
                    Message tempMsg = (Message) msgs.nextElement();
                    System.out.println("\nMessage: " + tempMsg);
                    System.out.println("======================================="
 + "==========================================================================");
                }
            }
        } catch (JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
