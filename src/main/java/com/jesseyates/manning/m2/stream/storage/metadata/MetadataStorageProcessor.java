package com.jesseyates.manning.m2.stream.storage.metadata;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m2.api.MetadataDAO;
import com.jesseyates.manning.m2.stream.storage.StorageConfiguration;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalMetadata;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.time.Instant;
import java.util.Properties;

/**
 * Simple processor that just writes each metadata record to the configured database.
 */
public class MetadataStorageProcessor extends StreamProcessor<StorageConfiguration> {

    private MetadataDAO db;

    protected MetadataStorageProcessor(String name, String description) {
        super(name, description);
    }

    @Override
    protected KafkaStreams buildStream(StorageConfiguration conf, Properties props) {
        setupDatabase(conf);
        final Serde<CanonicalKey> keySpecificAvroSerde = configureAvroSerde(conf, new SpecificAvroSerde<>(), true);
        final Serde<CanonicalMetadata> valueSpecificAvroSerde = configureAvroSerde(conf, new SpecificAvroSerde<>(), false);

        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(conf.getSource(), Consumed.with(keySpecificAvroSerde, valueSpecificAvroSerde))
                // write the metadata to the database
                .foreach((key, value) -> {
                    long arrivalTime = (Long) value.getArrivalTimeMs();
                    long startTime = (Long) value.getStartTimeMs();
                    long endTime = (Long) value.getEndTimeMs();
                    Boolean slow = value.getSlow();
                    Boolean withError = value.getWithError();

                    boolean parsed = !slow && !withError;
                    long elapsedTime = endTime - arrivalTime;

                    db.setDeviceMetadata(key.getUuid().toString(),
                            Instant.ofEpochMilli(arrivalTime).toString(),
                            Instant.ofEpochMilli(startTime).toString(),
                            Instant.ofEpochMilli(endTime).toString(),
                            parsed,
                            value.getNumberEvents(),
                            (int) elapsedTime,
                            slow,
                            withError);
                });

        return new KafkaStreams(builder.build(), props);
    }

    private void setupDatabase(StorageConfiguration conf) {
        MetricRegistry registry = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        final Jdbi jdbi = Jdbi.create(conf.getDataSourceFactory().build(registry, "device-metadata-db"));
        jdbi.installPlugin(new SqlObjectPlugin());
        this.db = jdbi.onDemand(MetadataDAO.class);
    }

}
