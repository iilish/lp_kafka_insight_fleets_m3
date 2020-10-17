package com.jesseyates.manning.m1.api;

import com.google.common.collect.ImmutableMap;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * API endpoint for collecting devices
 */
@Path("/")
public class DeviceEndpoint {
  protected final KafkaProducer producer;
  private final DeviceDAO db;
  private final String table;
  protected final String topic;

  public DeviceEndpoint(KafkaProducer producer, String topic, DeviceDAO db, String table) {
    this.producer = producer;
    this.topic = topic;
    this.db = db;
    this.table = table;
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
    ByteBuffer body = ByteBuffer.wrap(toByteArray(request.getInputStream()));
    RawRecord.Builder builder = RawRecord.newBuilder()
            .setUuid(uuid)
            .setArrivalTimeMs(Instant.now().toEpochMilli());
    RawRecord payload = builder.build();
            // RawRecord payload = new RawRecord(uuid, , body);

    ProducerRecord record = new ProducerRecord(topic, uuid, payload);
    Future<RecordMetadata> metadata = producer.send(record);

    return Response.ok().entity(serialize(metadata.get())).build();
  }

  protected Map<String, Object> serialize(RecordMetadata metadata) {
    return ImmutableMap.<String, Object>builder()
      .put("offset", metadata.offset())
      .put("partition", metadata.partition())
      .put("topic", metadata.topic())
      .put("timestamp", metadata.timestamp())
      .build();
  }

  @GET
  @Path("/state")
  public Response getStatus(@QueryParam("uuid") String uuid) {
    return Response.ok().entity(db.getDeviceState(table, uuid)).build();
  }
}
