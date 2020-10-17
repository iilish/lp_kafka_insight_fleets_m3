package com.jesseyates.manning.m1.stream;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.jesseyates.manning.common.App;
import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m1.api.DeviceDAO;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.dropwizard.setup.Bootstrap;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.Properties;

public class DeviceStateStream extends App<DeviceStateStreamConf> {

  @Override
  public void initialize(Bootstrap<DeviceStateStreamConf> bootstrap) {
    bootstrap.addCommand(new DeviceStateStorageProcessor("stream",
      "Runs a Kafka stream application"));
  }

  public static void main(String[] args) throws Exception {
    new DeviceStateStream().run(args);
  }

  /**
   * Stream Processor that takes {@link RawRecord} and writes them directly to the database
   */
  class DeviceStateStorageProcessor extends StreamProcessor<DeviceStateStreamConf> {

    private String table;
    private DeviceDAO db;

    protected DeviceStateStorageProcessor(String name, String description) {
      super(name, description);
    }

    @Override
    protected KafkaStreams buildStream(DeviceStateStreamConf conf, Properties props) {
      createDatabase(conf);

      final Serde<RawRecord> valueSpecificAvroSerde = new SpecificAvroSerde<>();
      configureAvroSerde(conf, valueSpecificAvroSerde, false);

      BytesToJson toJson = new BytesToJson();
      StreamsBuilder builder = new StreamsBuilder();
      builder.stream(conf.getSource(),
        Consumed.with(Serdes.String(), valueSpecificAvroSerde))
             .mapValues(rawRecord -> {
               return toJson.apply(rawRecord.getBody());
             })
             .flatMapValues(i -> i)
             .foreach((uuid, value) -> {
               long charging = Long.valueOf(value.get("charging").toString());
               db.setDeviceState(table, uuid, charging > 0);
             });


      return new KafkaStreams(builder.build(), props);
    }

    private void createDatabase(DeviceStateStreamConf conf) {
      MetricRegistry registry = new MetricRegistry();
      final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
      reporter.start();

      this.table = conf.getDeviceTable();
      final Jdbi jdbi = Jdbi.create(conf.getDataSourceFactory().build(registry, "device-db"));
      jdbi.installPlugin(new SqlObjectPlugin());
      this.db = jdbi.onDemand(DeviceDAO.class);
    }
  }
}
