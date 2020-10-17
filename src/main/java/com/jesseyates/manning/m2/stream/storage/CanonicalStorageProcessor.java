package com.jesseyates.manning.m2.stream.storage;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m1.api.DeviceDAO;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalValue;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.Properties;

/**
 * Simple processor that just writes each record (whether charging or not) to the configured
 * database.
 */
public class CanonicalStorageProcessor extends StreamProcessor<StorageConfiguration> {

    private String table;
    private DeviceDAO db;

    protected CanonicalStorageProcessor(String name, String description) {
        super(name, description);
    }

    @Override
    protected KafkaStreams buildStream(StorageConfiguration conf, Properties props) {
        setupDatabase(conf);
        final Serde<CanonicalKey> keySpecificAvroSerde = configureAvroSerde(conf, new SpecificAvroSerde<>(), true);
        final Serde<CanonicalValue> valueSpecificAvroSerde = configureAvroSerde(conf, new SpecificAvroSerde<>(), false);

        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(conf.getSource(), Consumed.with(keySpecificAvroSerde, valueSpecificAvroSerde))
                // ensure that we only see events that have charging information
                .filter(new ContainsChargingFilter<>())
                // write the state to the database
                .foreach((key, value) -> {
                    long charging = Long.valueOf(value.getEvents().get("charging").toString());
                    db.setDeviceState(table, key.getUuid().toString(), charging > 0);
                });

        return new KafkaStreams(builder.build(), props);
    }

    private void setupDatabase(StorageConfiguration conf) {
        MetricRegistry registry = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        this.table = conf.getDeviceTable();
        final Jdbi jdbi = Jdbi.create(conf.getDataSourceFactory().build(registry, "device-db"));
        jdbi.installPlugin(new SqlObjectPlugin());
        this.db = jdbi.onDemand(DeviceDAO.class);
    }
}
