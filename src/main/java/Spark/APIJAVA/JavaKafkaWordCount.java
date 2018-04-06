package Spark.APIJAVA;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;

public class JavaKafkaWordCount {

	public static void main(String[] args) throws InterruptedException {

		SparkConf sparkConf = new SparkConf().setAppName("JavaNetworkWordCount").setMaster("local[*]");
		
		JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(1));
		
		Map<String, Object> kafkaParams = new HashMap<>();
		
		kafkaParams.put("bootstrap.servers", "malek-pc:6667");
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");
		kafkaParams.put("auto.offset.reset", "earliest");
		kafkaParams.put("enable.auto.commit", true);

		Collection<String> topics = Arrays.asList("MyTopic");
		
		JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(jsc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

		stream.foreachRDD(rdd -> {
			OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
			rdd.foreachPartition(consumerRecords -> {
				OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
				System.out.println(o.topic() + " " + o.partition() + " " + o.fromOffset() + " " + o.untilOffset());
			});
		});

		jsc.start();
		jsc.awaitTermination();
	}
}