/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.ackequivexample;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;

public class SampleUtilities {
    /**
     * Waits for 'count' messages on controlQueue before
     * continuing.  Called by a publisher to make sure that
     * subscribers have started before it begins publishing
     * messages.
     *
     * If controlQueue does not exist, the method throws an
     * exception.
     *
     * @param prefix    prefix (publisher or subscriber) to be
     *                  displayed
     * @param controlQueue   control queue
     * @param count     number of messages to receive
     */
    public static void receiveSynchronizeMessages(
        String prefix,
        ConnectionFactory connectionFactory,
        Queue controlQueue,
        int count) throws Exception {
        
        try (JMSContext context = connectionFactory.createContext();) {
            System.out.println(
                    prefix + "Receiving synchronize messages from "
                    + "control queue; count = " + count);
            JMSConsumer receiver = context.createConsumer(controlQueue);

            while (count > 0) {
                receiver.receive();
                count--;
                System.out.println(
                        prefix + "Received synchronize message; expect "
                        + count + " more");
            }
        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            throw e;
        }
    }

    /**
     * Sends a message to controlQueue.  Called by a subscriber
     * to notify a publisher that it is ready to receive
     * messages.
     * <p>
     * If controlQueue doesn't exist, the method throws an
     * exception.
     *
     * @param prefix    prefix (publisher or subscriber) to be
     *                  displayed
     * @param controlQueue  control queue
     */
    public static void sendSynchronizeMessage(
        String prefix,
        ConnectionFactory connectionFactory,
        Queue controlQueue) throws Exception {
        String message;

        try (JMSContext context = connectionFactory.createContext();) {
            message = "synchronize";
            System.out.println(
                    prefix + "Sending synchronize message to control queue");
            context.createProducer().send(controlQueue, message);
        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            throw e;
        }
    }

    /**
     * Monitor class for asynchronous examples.  Producer signals
     * end of message stream; listener calls allDone() to notify
     * consumer that the signal has arrived, while consumer calls
     * waitTillDone() to wait for this notification.
     */
    public static class DoneLatch {
        boolean done = false;

        /**
         * Waits until done is set to true.
         */
        public void waitTillDone() {
            synchronized (this) {
                while (!done) {
                    try {
                        this.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        /**
         * Sets done to true.
         */
        public void allDone() {
            synchronized (this) {
                done = true;
                this.notify();
            }
        }
    }
}
