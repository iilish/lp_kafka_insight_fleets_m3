package com.jesseyates.manning.m2.api;

import com.jesseyates.manning.m1.api.CloseableManaged;
import com.jesseyates.manning.m1.api.DeviceDAO;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.dropwizard.Application;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jdbi.v3.core.Jdbi;

import java.util.Properties;

/**
 * API server to front connections for devices to Kafka for Milestone 2. Much of this is reused
 * from Milestone 1.
 */
public class ApiServer extends Application<ApiConfiguration> {

  public void run(ApiConfiguration conf, Environment environment) {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi = factory.build(environment, conf.getDataSourceFactory(), "device-db");

    KafkaProducer producer = createProducer(conf);
    environment.lifecycle().manage(new CloseableManaged(producer));

    environment.jersey().register(new DeviceEndpoint(producer, conf.getTopic(),
      jdbi.onDemand(DeviceDAO.class), conf.getDeviceTable(),
      conf.getMaxBodySize(), new S3(conf.getS3()), jdbi.onDemand(MetadataDAO.class)));

    // needed to serve static web pages from root.
    // it namespaces all the API endpoints under /api
    environment.jersey().setUrlPattern("/api");
  }

  private KafkaProducer createProducer(ApiConfiguration conf) {
    Properties props = new Properties();
    // reasonable defaults
    props.put(ProducerConfig.ACKS_CONFIG, "1");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    // overrides
    props.putAll(conf.getKafka());
    return new KafkaProducer(props);
  }

  public static void main(String[] args) throws Exception {
    new ApiServer().run(args);
  }
}
