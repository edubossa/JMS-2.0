/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.sharedconsumer;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The SharedConsumer class consists only of a main method, which fetches one or
 * more messages from a topic using asynchronous message delivery. It uses the 
 * message listener TextListener. Run two instances of this program at the same
 * time, in conjunction with Producer. 
 */
public class SharedConsumer {

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/Topic")
    private static Topic topic;
    
    public static void prepareResources() {
    	
    	try {
			
    		Context ctx = new InitialContext();
			
    		if (connectionFactory == null) {
				connectionFactory = (ConnectionFactory) ctx.lookup("java:comp/DefaultJMSConnectionFactory"); 
			}
    		
    		if (topic == null) {
    			topic = (Topic) ctx.lookup("jms/Topic");
    		}
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
    	
    }
    
    public static void main(String[] args) {
    	prepareResources();
    	
        JMSConsumer consumer;
        TextListener listener;
        InputStreamReader inputStreamReader;
        char answer = '\0';
        /*
         * In a try-with-resources block, create context.
         * Create shared consumer.
         * Receive all text messages from destination until
         * a non-text message is received indicating end of
         * message stream.
         * Report number of messages received.
         */
        try (JMSContext context = connectionFactory.createContext();) {
            consumer = context.createSharedConsumer(topic, "SubName");
            System.out.println("Waiting for messages on topic");
            
            listener = new TextListener();
            consumer.setMessageListener(listener);
            System.out.println(
                    "To end program, type Q or q, " + "then <return>");
            inputStreamReader = new InputStreamReader(System.in);

            while (!((answer == 'q') || (answer == 'Q'))) {
                try {
                    answer = (char) inputStreamReader.read();
                } catch (IOException e) {
                    System.err.println("I/O exception: " + e.toString());
                }
            }
            System.out.println("Messages received: " + listener.getCount());
        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
