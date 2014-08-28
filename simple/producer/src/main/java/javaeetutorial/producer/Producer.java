/**
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.producer;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;

import org.connector.Connector;

/**
 * The Producer class consists only of a main method, which sends several
 * messages to a queue or topic.
 *
 * Run this program in conjunction with SynchConsumer or AsynchConsumer. Specify
 * "queue" or "topic" on the command line when you run the program. By default,
 * the program sends one message. Specify a number after the destination name to
 * send that number of messages.
 */
public class Producer extends Connector {
	
    /*@Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/Queue")
    private static Queue queue;
    @Resource(lookup = "jms/Topic")
    private static Topic topic;*/
	

    /**
     * Main method.
     *
     * @param args the destination used by the example and, optionally, the
     * number of messages to send
     */
    public static void main(String[] args) throws Exception {
    	
    	new Producer();
    	
        final int NUM_MSGS;
        String message;
      
        if ((args.length < 1) || (args.length > 2)) {
            System.err.println(
                    "Program takes one or two arguments: "
                    + "<dest_type> [<number-of-messages>]");
            System.exit(1);
        }

        String destType = args[0];
        System.out.println("Destination type is " + destType);

        if (!(destType.equals("queue") || destType.equals("topic"))) {
            System.err.println("Argument must be \"queue\" or " + "\"topic\"");
            System.exit(1);
        }

        if (args.length == 2) {
            NUM_MSGS = (new Integer(args[1])).intValue();
        } else {
            NUM_MSGS = 1;
        }

        Destination dest = null;

        try {
            if (destType.equals("queue")) {
                dest = (Destination) queue;
            } else {
                dest = (Destination) topic;
            }
        } catch (JMSRuntimeException e) {
            System.err.println("Error setting destination: " + e.toString());
            System.exit(1);
        }

        /*
         * Within a try-with-resources block, create context.
         * Create producer and message.
         * Send messages, varying text slightly.
         * Send end-of-messages message.
         */
        try (JMSContext context = connectionFactory.createContext();) {
            int count = 0;

            for (int i = 0; i < NUM_MSGS; i++) {
                message = "This is message " + (i + 1) + " from producer";
                // Comment out the following line to send many messages
                System.out.println("Sending message: " + message);
                context.createProducer()
                        .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                        .send(dest, message);
                count += 1;
            }
            System.out.println("Messages sent: " + count);
            
            /*
             * Envia uma mensagem de controle vazia para indicar o final do fluxo de mensagem:
             */
            context.createProducer().send(dest, context.createMessage());
            // Uncomment the following line if you are sending many messages
            // to two synchronous consumers
            // context.createProducer().send(dest, context.createMessage());
        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}