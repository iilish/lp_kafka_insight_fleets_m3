package com.jesseyates.manning.m2.api;

import com.jesseyates.manning.m1.api.DeviceDAO;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * API endpoint for collecting devices, which sends large requests to external storage in S3
 */
public class DeviceEndpoint extends com.jesseyates.manning.m1.api.DeviceEndpoint {
    private final int maxSize;
    private final S3 s3;
    private final MetadataDAO metaDB;

    public DeviceEndpoint(KafkaProducer producer, String topic, DeviceDAO db, String table,
                          int maxBodySize, S3 s3, MetadataDAO metaDB) {
        super(producer, topic, db, table);
        this.maxSize = maxBodySize;
        this.s3 = s3;
        this.metaDB = metaDB;
    }

    /**
     * Sends the payload to Kafka as a message, keyed on the device UUID. This ensures that devices
     * always land in the same partition, in order, giving us a consistent historical view of the
     * device over time.
     *
     * @return the {@link RecordMetadata} for the record sent to Kafka. This is more for debugging,
     * in production you wouldn't want to waste bandwidth sending this data since devices in the
     * field do not have access to the backend instance/
     */
    @POST
    @Path("/send/{uuid}")
    @Consumes({APPLICATION_OCTET_STREAM, APPLICATION_JSON})
    @Produces(APPLICATION_JSON)
    public Response send(@PathParam("uuid") String uuid, @Context HttpServletRequest request)
            throws ExecutionException, InterruptedException, IOException {
        long now = Instant.now().toEpochMilli();
        RawRecord.Builder builder = RawRecord.newBuilder()
                .setUuid(uuid)
                .setArrivalTimeMs(now);

        // send either the payload or or a reference to the payload
        byte[] max = toByteArray(request.getInputStream());
        int inputLength = max.length;

        // InputStream stream = request.getInputStream();
        // byte[] max = toByteArray(stream, maxSize);
        // we got all the bytes we can send the body along
        // if (stream.available() <= 0) {
        if (inputLength <= maxSize) {
            builder.setBody(ByteBuffer.wrap(max));
        }
        // oops, we got sent a payload over the max size, so we need to store a reference
        else {
            // copy the data to a tempfile
            String path = format("%s-%d-%s", uuid, now, UUID.randomUUID());
            java.nio.file.Path tmp = Files.createTempFile("send", path);
            // Files.copy(new SequenceInputStream(new ByteArrayInputStream(max), request.getInputStream()), tmp);

            InputStream input = new ByteArrayInputStream(max);
      /*
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength();
        client.putObject(defaultBucket, path, input, metadata);
       */
            String ref = s3.put(uuid, path, input, max.length);
            builder.setBodyReference(ref);
        }

        ProducerRecord record = new ProducerRecord(topic, uuid, builder.build());
        Future<RecordMetadata> metadata = producer.send(record);

        return Response.ok().entity(serialize(metadata.get())).build();
    }

    @GET
    @Path("/bad/{uuid}")
    public Response getBadMessages(@PathParam("uuid") String uuid) {
        return Response.ok().entity(metaDB.getWithError(uuid)).build();
    }

    @GET
    @Path("/connected/{days}")
    public Response getConnected(@PathParam("days") String days) {
        return Response.ok().entity(metaDB.getConnectedWithinTime(String.format("'%s day'", days))).build();
    }

    @GET
    @Path("/parsed/{days}")
    public Response getParsed(@PathParam("days") String days) {
        return Response.ok().entity(metaDB.getParsedWithinTime(String.format("'%s day'", days))).build();
    }
}
