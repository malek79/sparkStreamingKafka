package Spark.APIJAVA;


import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import scala.Tuple2;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

/**
 * Consumes messages from one or more topics in Kafka and does wordcount.
 *
 * Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>
 *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
 *   <group> is the name of kafka consumer group
 *   <topics> is a list of one or more kafka topics to consume from
 *   <numThreads> is the number of threads the kafka consumer should use
 *
 * To run this example:
 *   `$ bin/run-example org.apache.spark.examples.streaming.JavaKafkaWordCount zoo01,zoo02, \
 *    zoo03 my-consumer-group topic1,topic2 1`
 */

public final class KafkaWordCount {
  private static final Pattern SPACE = Pattern.compile(" ");

  private KafkaWordCount() {
  }

  public static void main(String[] args) throws Exception {


    Map<String, Object> kafkaParams = new HashMap<>();
	
	kafkaParams.put("bootstrap.servers", "malek-pc:6667");
	kafkaParams.put("key.deserializer", StringDeserializer.class);
	kafkaParams.put("value.deserializer", StringDeserializer.class);
	kafkaParams.put("group.id", "malek");
	kafkaParams.put("auto.offset.reset", "earliest");
	kafkaParams.put("enable.auto.commit", true);
	
    SparkConf sparkConf = new SparkConf().setAppName("JavaKafkaWordCount").setMaster("local[2]");
    // Create the context with 2 seconds batch size
    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(1));
    
	Collection<String> topics = Arrays.asList("sparkkafka");
    
    JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(jssc,
			LocationStrategies.PreferConsistent(),
			ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

    JavaPairDStream<String, String> messages = stream.mapToPair(record -> new Tuple2<>(record.key(), record.value()));


    JavaDStream<String> lines = messages.map(Tuple2::_2);

    JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());

    JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1))
        .reduceByKey((i1, i2) -> i1 + i2);

    wordCounts.print();
    jssc.start();
    jssc.awaitTermination();
  }
}
