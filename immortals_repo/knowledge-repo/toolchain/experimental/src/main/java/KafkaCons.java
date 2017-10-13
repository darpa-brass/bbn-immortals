import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by CharlesEndicott on 6/2/2017.
 */
public class KafkaCons {

    public static void main(String[] args) {

        // To do, place properties in a config file and load them
        Properties props = new Properties();
        // Specify kafka node(s)
        props.put("bootstrap.servers", "localhost:9092");
        // Specify group-id
        props.put("group.id", "consumer-tutorial");
        //Specify appropriate deserializers
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("session.timeout.ms", "60000");

        // Initialize kafka consumer and subscribe to IMMoRTALS topic
        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList("IMMoRTALS"));

        int timeouts = 0;

        try {
            while (true) {
                // See if topic has new content, and timeout after 200 ms
                ConsumerRecords<String,String> records = consumer.poll(200);

                if (records.count() == 0) {
                    timeouts++;
                } else {

                    System.out.printf("Got %d records after %d timeouts\n", records.count(), timeouts);
                    timeouts = 0;
                }
                for (ConsumerRecord<String, String> record : records) {
                    // Print records consumed
                    System.out.println(record.offset() + ": " + record.value());
                }
                // No more input :`(
                if (timeouts > 100) {
                    return;
                }
            }
        } finally {
            // Close resources
            consumer.close();
        }
    }

}
