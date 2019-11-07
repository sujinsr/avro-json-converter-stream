package com.github.sujinsr.kafka.avro;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AvroJsonStream {

    public static void main(String []args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Error expected more arguments to the program...");
            return;
        }
        String bootStrapServer = args[0];
        String schemaRegistry = args[1];
        String topicArgs = args[2];

        List<String> topicList = Arrays.asList(topicArgs.split(","));
        List<KafkaStreams> streamsList = new ArrayList<>();
        topicList.forEach(topic -> {
            System.out.println("Creating stream for topic " + topic);
            AvroJsonStream avroJsonStream = new AvroJsonStream();
            Properties properties = avroJsonStream.getStreamConfiguration(bootStrapServer, schemaRegistry, topic);
            StreamsBuilder builder = avroJsonStream.createTopology(topic);
            KafkaStreams streams = new KafkaStreams(builder.build(), properties);
            streamsList.add(streams);
            streams.start();
            System.out.println("Stream started for topic " + topic);

        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streamsList.forEach(stream -> stream.close());
        }));

        while(true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private Properties getStreamConfiguration(String bootstrapServer, String schemRegistry, String topic) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "avro_json_stream_application_" + topic);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        //props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        props.put("schema.registry.url", schemRegistry);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    private StreamsBuilder createTopology(String topic) {
        String sinkTopic = topic + "_json_converted";
        StreamsBuilder builder = new StreamsBuilder();
        //KStream<String, GenericRecord> firstStream = builder.stream(topic);
        KStream<String, String> firstStream = builder.stream(topic);
        final Serde<String> stringSerde = Serdes.String();
        firstStream.mapValues(record -> record.toString())
                .to(sinkTopic, Produced.valueSerde(stringSerde));
        return builder;
    }
}
