/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.durablesubscriptionexample;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DurableSubscriptionExample {

    static int startindex = 0;
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/DurableConnectionFactory")
    private static ConnectionFactory durableConnectionFactory;
    @Resource(lookup = "jms/Topic")
    private static Topic topic;
    String conFacName1 = "java:comp/DefaultJMSConnectionFactory";
    String conFacName2 = "jms/DurableConnectionFactory";
    String topicName = "jms/Topic";

    /**
     * Instantiates the consumer and sender classes. Starts the consumer;
     * the sender sends some messages. Closes the consumer; while it is
     * closed, the sender sends some more messages. Restarts the
     * consumer and fetches the messages. Finally, closes the contexts.
     */
    public void run_program() {
        DurableConsumer durableConsumer;
        MultipleSender multipleSender;

        durableConsumer = new DurableConsumer();
        multipleSender = new MultipleSender();

        durableConsumer.startConsumer();
        multipleSender.sendMessages();
        durableConsumer.closeConsumer();
        multipleSender.sendMessages();
        durableConsumer.startConsumer();
        durableConsumer.closeConsumer();
        multipleSender.finish();
        durableConsumer.finish();
    }
    
    public static void prepareResources() {
    	
    	try {
			
    		Context ctx = new InitialContext();
			
    		if (connectionFactory == null) {
				connectionFactory = (ConnectionFactory) ctx.lookup("java:comp/DefaultJMSConnectionFactory"); 
			}
    		
    		if (durableConnectionFactory == null) {
    			durableConnectionFactory = (ConnectionFactory) ctx.lookup("jms/DurableConnectionFactory");
    		}
    		
    		if (topic == null) {
    			topic = (Topic) ctx.lookup("jms/Topic");
    		}
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
    	
    }

    /**
     * Calls the run_program method.
     *
     */
    public static void main(String[] args) {
        DurableSubscriptionExample dse = new DurableSubscriptionExample();
        
        prepareResources();

        if (args.length != 0) {
            System.out.println("Program takes no arguments.");
            System.exit(1);
        }

        System.out.println(
                "Connection factory without client ID is " + dse.conFacName1);
        System.out.println(
                "Connection factory with client ID is " + dse.conFacName2);
        System.out.println("Topic name is " + dse.topicName);

        dse.run_program();
        System.exit(0);
    }

    /**
     * The DurableConsumer class contains a constructor, a startConsumer
     * method, a closeConsumer method, and a finish method.
     *
     * The class fetches messages asynchronously, using a message listener,
     * TextListener.
     */
    public class DurableConsumer {

        JMSContext context;
        TextListener listener = null;
        JMSConsumer consumer = null;

        /**
         * Constructor: Uses the durable connection factory to create a
         * context.
         */
        public DurableConsumer() {
            try {
                context = durableConnectionFactory.createContext();
            } catch (JMSRuntimeException e) {
                System.err.println("Context creation problem: " + e.toString());
                if (context != null) {
                    try {
                        context.close();
                    } catch (JMSRuntimeException ee) {
                    }
                }
                System.exit(1);
            } 
        }

        /**
         * Stops context, then creates durable consumer, registers message
         * listener (TextListener), and starts message delivery; listener
         * displays the messages obtained.
         */
        public void startConsumer() {
            try {
                System.out.println("Starting consumer");
                context.stop();
                consumer = context.createDurableConsumer(topic, "MakeItLast");
                listener = new TextListener();
                consumer.setMessageListener(listener);
                context.start();
            } catch (JMSRuntimeException e) {
                System.err.println(
                        "startConsumer: Exception occurred: " + e.toString());
            }
        }

        /**
         * Blocks until sender issues a control message indicating end of
         * sending stream, then closes consumer.
         */
        public void closeConsumer() {
            try {
                listener.monitor.waitTillDone();
                System.out.println("Closing consumer");
                consumer.close();
            } catch (JMSRuntimeException e) {
                System.err.println(
                        "closeConsumer: Exception occurred: " + e.toString());
            }
        }

        /**
         * Closes the context.
         */
        public void finish() {
            try {
                System.out.println(
                        "Unsubscribing from durable subscription");
                context.unsubscribe("MakeItLast");
                context.close();
            } catch (JMSRuntimeException e) {
            }
        }

        /**
         * The TextListener class implements the MessageListener interface by
         * defining an onMessage method for the DurableConsumer class.
         */
        private class TextListener implements MessageListener {

            final SampleUtilities.DoneLatch monitor =
                    new SampleUtilities.DoneLatch();

            /**
             * Displays the message text. A non-text message is interpreted as
             * the end of the message stream, and the message listener sets its
             * monitor state to all done processing messages.
             *
             * @param message the incoming message
             */
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    try {
                        String msg = message.getBody(String.class);
                        System.out.println(
                                "CONSUMER: Reading message: " + msg);
                    } catch (JMSException e) {
                        System.err.println(
                                "Exception in onMessage(): " + e.toString());
                    }
                } else {
                    monitor.allDone();
                }
            }
        }
    }

    /**
     * The MultipleSender class sends several messages to a topic. It
     * contains a constructor, a sendMessages method, and a finish method.
     */
    public class MultipleSender {

        JMSContext context;

        /**
         * Constructor: Uses the default connection factory and the topic to
         * create a context.
         */
        public MultipleSender() {
            try {
                context = connectionFactory.createContext();
            } catch (Exception e) {
                System.err.println("Context creation problem: " + e.toString());
                if (context != null) {
                    try {
                        context.close();
                    } catch (Exception ee) {
                    }
                }
                System.exit(1);
            } 
        }

        /**
         * Creates text message. Creates a producer and sends some messages, 
         * varying text slightly. Messages must be persistent.
         */
        public void sendMessages() {
            String message;
            int i;
            final int NUMMSGS = 3;
            final String MSG_TEXT = "Here is a message";

            try {
                for (i = startindex; i < (startindex + NUMMSGS); i++) {
                    message = MSG_TEXT + " " + (i + 1);
                    System.out.println(
                            "SENDER: Sending message: " + message);
                    context.createProducer().send(topic, message);
                }

                /*
                 * Send a non-text control message indicating end
                 * of messages.
                 */
                context.createProducer().send(topic, context.createMessage());
                startindex = i;
            } catch (Exception e) {
                System.err.println(
                        "sendMessages: Exception occurred: " + e.toString());
            }
        }

        /**
         * Closes the context.
         */
        public void finish() {
            try {
                context.close();
            } catch (Exception e) {
            }
        }
    }
}
