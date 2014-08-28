/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.ackequivexample;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class AckEquivExample {

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/DurableConnectionFactory")
    private static ConnectionFactory durableConnectionFactory;
    @Resource(lookup = "jms/ControlQueue")
    private static Queue controlQueue;
    @Resource(lookup = "jms/Queue")
    private static Queue queue;
    @Resource(lookup = "jms/Topic")
    private static Topic topic;
    final String CONTROL_QUEUE = "jms/ControlQueue";
    final String conFacName = "jms/DurableConnectionFactory";
    final String queueName = "jms/Queue";
    final String topicName = "jms/Topic";

    /**
     * Instantiates the sender and synchronous consumer class for a queue
     * and the sender and asynchronous consumer for a topic, then
     * starts their threads. Calls the join method to wait for the threads to
     * die.
     */
    public void run_threads() {
        QueueSender queueSender = new QueueSender();
        SynchConsumer synchConsumer = new SynchConsumer();
        AsynchConsumer asynchConsumer = new AsynchConsumer();
        TopicSender topicSender = new TopicSender();

        queueSender.start();
        synchConsumer.start();

        try {
            queueSender.join();
            synchConsumer.join();
        } catch (InterruptedException e) {
        }

        try {
            asynchConsumer.start();
            Thread.sleep(1000);
            topicSender.start();
        } catch (InterruptedException e) {
        }

        try {
            asynchConsumer.join();
            topicSender.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Calls the run_threads method to execute the program threads.
     *
     * @param args the topic used by the example
     */
    public static void main(String[] args) {
        AckEquivExample aee = new AckEquivExample();

        if (args.length != 0) {
            System.err.println("Program takes no arguments.");
            System.exit(1);
        }

        System.out.println("Queue name is " + aee.CONTROL_QUEUE);
        System.out.println("Queue name is " + aee.queueName);
        System.out.println("Topic name is " + aee.topicName);
        System.out.println("Connection factory name is " + aee.conFacName);

        aee.run_threads();
        System.exit(0);
    }

    /**
     * The AsynchConsumer class creates a context in AUTO_ACKNOWLEDGE mode and
     * fetches several messages from a topic asynchronously, using a message
     * listener, TextListener.
     *
     * Each message is acknowledged after the onMessage method completes.
     */
    public class AsynchConsumer extends Thread {

        /**
         * Runs the thread.
         */
        @Override
        public void run() {

            JMSConsumer consumer;
            TextListener listener;

            /*
             * Create auto-acknowledge consumer on durable subscription.
             * Register message listener (TextListener).
             * Start message delivery.
             * Send synchronize message to sender, then wait
             *   till all messages have arrived.
             * Listener displays the messages obtained.
             */
            try (JMSContext context = durableConnectionFactory.createContext();) {
                System.out.println("ASYNCHCONSUMER: Created auto-acknowledge JMSContext");
                consumer = context.createDurableConsumer(topic, "AckSub");
                listener = new TextListener();
                consumer.setMessageListener(listener);

                // Let sender know that consumer is ready.
                try {
                    SampleUtilities.sendSynchronizeMessage(
                            "ASYNCHCONSUMER: ",
                            connectionFactory,
                            controlQueue);
                } catch (JMSRuntimeException e) {
                    System.err.println(
                            "Context or queue problem with consumer: "
                            + e.toString());
                    System.exit(1);
                }

                /*
                 * Asynchronously process messages.
                 * Block until sender issues a control message
                 * indicating end of message stream.
                 */
                listener.monitor.waitTillDone();
                consumer.close();
                context.unsubscribe("AckSub");
            } catch (Exception e) {
                System.err.println(
                        "Exception occurred: " + e.toString());
            }
        }

        /**
         * The TextListener class implements the MessageListener interface by
         * defining an onMessage method for the AsynchConsumer class.
         */
        private class TextListener implements MessageListener {

            final SampleUtilities.DoneLatch monitor =
                    new SampleUtilities.DoneLatch();

            /**
             * Casts the message to a TextMessage and displays its text. A
             * non-text message is interpreted as the end of the message stream,
             * and the message listener sets its monitor state to all done
             * processing messages.
             *
             * @param message the incoming message
             */
            @Override
            public void onMessage(Message message) {
                String msg;
                if (message instanceof TextMessage) {
                    try {
                        msg = message.getBody(String.class);
                        System.out.println(
                                "ASYNCHCONSUMER: Processing message: " + msg);
                    } catch (Exception e) {
                        System.err.println(
                                "Exception in onMessage(): "
                                + e.toString());
                    }
                } else {
                    monitor.allDone();
                }
            }
        }
    }

    /**
     * The TopicSender class creates a context and sends three
     * messages to a topic.
     */
    public class TopicSender extends Thread {

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            String message;
            final int NUMMSGS = 3;
            final String MSG_TEXT = 
                    "Here is a message";

            /*
             * After synchronizing with consumer, create
             *   sender.
             * Send 3 messages, varying text slightly.
             * Send end-of-messages message.
             */
            try (JMSContext context = connectionFactory.createContext();) {
                System.out.println("TOPICSENDER: Created JMSContext");
                /*
                 * Synchronize with consumer.  Wait for message
                 * indicating that consumer is ready to receive
                 * messages.
                 */
                SampleUtilities.receiveSynchronizeMessages(
                        "TOPICSENDER: ",
                        connectionFactory,
                        controlQueue,
                        1);
                for (int i = 0; i < NUMMSGS; i++) {
                    message = MSG_TEXT + " " + (i + 1);
                    System.out.println(
                            "TOPICSENDER: Sending message: " + message);
                    context.createProducer().send(topic, message);
                }

                /*
                 * Send a non-text control message indicating
                 * end of messages.
                 */
                context.createProducer().send(topic, context.createMessage());
            } catch (Exception e) {
                System.err.println("Exception occurred: " + e.toString());
            }
        }
    }

    /**
     * The SynchReceiver class creates a context in CLIENT_ACKNOWLEDGE mode and
     * receives the message sent by the SynchSender class.
     */
    public class SynchConsumer extends Thread {

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            JMSConsumer consumer;
            String message;

            /*
             * Create client-acknowledge context for receiver.
             * Receive message and process it.
             * Acknowledge message.
             */
            try (JMSContext context =
                            connectionFactory.createContext(JMSContext.CLIENT_ACKNOWLEDGE);) {
                System.out.println(
                        "  SYNCHCONSUMER: Created client-acknowledge JMSContext");
                consumer = context.createConsumer(queue);
                message = consumer.receiveBody(String.class);
                System.out.println(
                        "  SYNCHCONSUMER: Processing message: "
                        + message);
                System.out.println(
                        "  SYNCHCONSUMER: Now I'll acknowledge the message");
                context.acknowledge();
            } catch (Exception e) {
                System.err.println("Exception occurred: " + e.toString());
            }
        }
    }

    /**
     * The SynchSender class creates a context in CLIENT_ACKNOWLEDGE mode and
     * sends a message.
     */
    public class QueueSender extends Thread {

        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            String message = "Here is a message";
            
            /*
             * Create client-acknowledge sender.
             * Create and send message.
             */
            try (JMSContext context = connectionFactory.createContext();) {
                System.out.println(
                        "  QUEUESENDER: Created JMSContext");
                System.out.println(
                        "  QUEUESENDER: Sending message: " + message);
                context.createProducer().send(queue, message);
            } catch (Exception e) {
                System.err.println("Exception occurred: " + e.toString());
            }
        }
    }
}
