package com.sequenceiq.messaging.producer.mqtt;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;


public class MQTTProducer {

    private static final int MESSAGE_DELAY_MILLISECONDS = 50;
    private static final int NUM_MESSAGES_TO_BE_SENT = 1000;
    private static final String DESTINATION_NAME = "sensor";
    private static final String MQTT_HOST = "tcp://0.0.0.0:1883";


    public static void main(String args[]) {
        BlockingConnection connection = null;
        try {
            
        	Options options = new Options();
            options.addOption("h", "help", false, "help:");
            options.addOption("host", true, "hostname for the broker to connect to");
            options.addOption("u", "username", true, "User name for connection");
            options.addOption("p", "password", true, "password for connection");
            options.addOption("d", "destination", true, "destination to send to");
            options.addOption("n", "number", true, "number of messages to send");
            options.addOption("delay", true, "delay between each send");

            CommandLineParser parser = new BasicParser();
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("h")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("OptionsTip", options);
                System.exit(0);
            }
            

            final MQTT mqtt = new MQTT();

            String host = commandLine.hasOption("host") ? commandLine.getOptionValue("host") :
                    MQTT_HOST;


            String userName = commandLine.hasOption("u") ? commandLine.getOptionValue("u") : "admin";
            String password = commandLine.hasOption("p") ? commandLine.getOptionValue("p") : "admin";
            String destinationName = commandLine.hasOption("d") ? commandLine.getOptionValue("d") : DESTINATION_NAME;
            int numberOfMessages = commandLine.hasOption("n") ? Integer.parseInt(commandLine.getOptionValue("n")) : NUM_MESSAGES_TO_BE_SENT;
            int delay = commandLine.hasOption("delay") ? Integer.parseInt(commandLine.getOptionValue("d")) : MESSAGE_DELAY_MILLISECONDS;


            mqtt.setHost(host);
            if (userName != null && !userName.isEmpty()) {
                mqtt.setUserName(userName);
            }

            if (password != null && !password.isEmpty()) {
                mqtt.setPassword(password);
            }
            connection = mqtt.blockingConnection();
            connection.connect();

            double cycleIncrement = 1.0/delay;
            double cyclePosition = 0;

            for (int i = 0; i < numberOfMessages; i++) {
                int audio = (int) (500 * Math.sin(2*Math.PI * cyclePosition));
                audio = Math.abs(audio);
                String payload = "" + audio;
                System.out.println("Payload :" + payload);
                connection.publish(destinationName, payload.getBytes(), QoS.AT_LEAST_ONCE, false);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                System.err.println("Sent " + audio);
                cyclePosition += cycleIncrement;
                if (cyclePosition > 1){
                    cyclePosition -= 1;
                }
            }

            // Cleanup
            connection.disconnect();
            ;

        } catch (Throwable t) {
            System.out.println("Error sending message:" + t);
        } finally {
            // Cleanup code
            // In general, you should always close producers, consumers,
            // sessions, and connections in reverse order of creation.
            // For this simple example, a JMS connection.close will
            // clean up all other resources.
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                	System.out.println("Error closing connection: "+ e);
                }
            }
        }
    }
}