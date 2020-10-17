package com.jesseyates.manning.m2.api;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface MetadataDAO {

    @SqlQuery("SELECT count(*) FROM device_metadata where uuid = :uuid and has_error = true")
    int getWithError(@Bind("uuid") String uuid);

    @SqlQuery("select count(uuid) from device_metadata where arrival_time > transaction_timestamp() - :tm ::interval")
    int getConnectedWithinTime(@Bind("tm") String tm);

    @SqlQuery("select count(uuid) from device_metadata " +
            "where arrival_time > transaction_timestamp() - :tm ::interval and is_parsed = true")
    int getParsedWithinTime(@Bind("tm") String tm);

    @SqlUpdate("INSERT INTO device_metadata (UUID, ARRIVAL_TIME, START_PARSING_TIME, END_PARSING_TIME, IS_PARSED, " +
            "EVENTS_NUMBER, ELAPSED_PARSING_TIME, IS_SLOW, HAS_ERROR) " +
            "VALUES (:uuid, :arrivalTime, :startTime, :endTime, :parsed, :eventsNumber, :elapsedTime, :slow, :error)")
    void setDeviceMetadata(@Bind("uuid") String uuid,
                           @Bind("arrivalTime") String arrivalTime,
                           @Bind("startTime") String startTime,
                           @Bind("endTime") String endTime,
                           @Bind("parsed") boolean isParsed,
                           @Bind("eventsNumber") int eventsNumber,
                           @Bind("elapsedTime") int elapsedTime,
                           @Bind("slow") boolean isSlow,
                           @Bind("error") boolean hasError);
}
