import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Created by CharlesEndicott on 6/5/2017.
 */
public class KafkaProd {

    public static void main(String[] args) {

        // To do, place properties in a config file and load them
        Properties props = new Properties();
        // Specify kafka node(s)
        props.put("bootstrap.servers", "localhost:9092");
        // Specify group-id
        props.put("group.id", "producer-test");
        //Specify appropriate serializers
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        // Initialize producer
        KafkaProducer<String, String> producer;
        producer = new KafkaProducer<String, String>(props);

        try {
            for (int i = 0; i < 5; i++) {
                // Send tuple to IMMoRTALS topic
                producer.send(new ProducerRecord<String, String>("IMMoRTALS", "IMMoRTALS:hasReturnValueToSemanticType\n" +
                        "        a             owl:ObjectProperty ;\n" +
                        "        rdfs:comment  \"The return value mappings\" ;\n" +
                        "        rdfs:domain   IMMoRTALS_dfu_instance:FunctionalAspectInstance ;\n" +
                        "        rdfs:range    IMMoRTALS_dfu_instance:ReturnValueToSemanticTypeBinding ."));
            }

        } finally {
            // Always clean up your toys...
            producer.close();
        }

    }

}
