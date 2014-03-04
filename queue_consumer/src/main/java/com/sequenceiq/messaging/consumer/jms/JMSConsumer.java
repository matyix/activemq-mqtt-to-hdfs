package com.sequenceiq.messaging.consumer.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;

import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.TransportConnector;
 
public class JMSConsumer implements MessageListener
{
    public static String brokerURL = "tcp://127.0.0.1:1883";
 
    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private volatile boolean shutdown = false;
    private TransportConnector amqpConnector;
 
    public static void main( String[] args )
    {
    	JMSConsumer app = new JMSConsumer();
        app.run();
    }
 
    public void run()
    {
        try {
            //ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
            //connection = factory.createConnection();
        	connection = createAmqpConnection();
            
            //MessageConsumer consumer = session.createConsumer(session.createTopic("topic://FOO"));
            //connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("sensor");
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
            while(!shutdown) {
                Thread.sleep(1000);
            }
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }
    
    public Connection createAmqpConnection() throws Exception {
        final ConnectionFactoryImpl factory = new ConnectionFactoryImpl("localhost", 1883, "admin", "admin");
        final Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
 
    public void onMessage(Message message)
    {
        try
        {
            if (message instanceof TextMessage)
            {
                TextMessage txtMessage = (TextMessage)message;
                String body = txtMessage.getText();
                if(!body.startsWith("=")) {
                    System.out.printf("Message received: ");
                }
                System.out.println(body);
                if("terminate".equals(body)) {
                    System.out.println("Shutting down");
                    shutdown = true;
                }
            }
            else if (message instanceof BytesMessage ) {
            	 BytesMessage bmsg = (BytesMessage) message;
                 byte[] actual = new byte[(int) bmsg.getBodyLength()];
                 bmsg.readBytes(actual);
            }
            else
            {
                System.out.println("Invalid message received.");
            }
        }
        catch (JMSException e)
        {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }
}