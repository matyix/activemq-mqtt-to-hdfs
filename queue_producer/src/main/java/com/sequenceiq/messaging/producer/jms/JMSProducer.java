package com.sequenceiq.messaging.producer.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
 
public class JMSProducer {

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
 
    public JMSProducer(ConnectionFactory factory, String queueName) throws JMSException
    {
        this.factory = factory;
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        producer = session.createProducer(destination);
    }
 
    public void run() throws JMSException
    {
        for (int i = 0; i < 100; i++)
        {
            System.out.println("Creating Message " + i);
            Message message = session.createTextMessage("Hello World!");
            producer.send(message);
        }
        Message message = session.createTextMessage("=========================");
        producer.send(message);
    }
 
    public void close() throws JMSException
    {
        if (connection != null)
        {
            connection.close();
        }
    }
}	
