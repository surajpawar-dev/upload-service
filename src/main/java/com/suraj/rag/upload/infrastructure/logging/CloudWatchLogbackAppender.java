package com.suraj.rag.upload.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DataAlreadyAcceptedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidSequenceTokenException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

public class CloudWatchLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private CloudWatchLogsClient client;
    private String region;
    private String logGroup;
    private String logStream;
    private String sequenceToken;

    @Override
    public void start() {
        if (region == null || logGroup == null || logStream == null) {
            addError("CloudWatch logging requires region, logGroup and logStream");
            return;
        }

        client = CloudWatchLogsClient.builder().region(Region.of(region)).build();
        createLogGroupIfMissing();
        createLogStreamIfMissing();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        InputLogEvent event =
                InputLogEvent.builder()
                        .timestamp(eventObject.getTimeStamp())
                        .message(toJson(eventObject))
                        .build();

        PutLogEventsRequest.Builder request =
                PutLogEventsRequest.builder()
                        .logGroupName(logGroup)
                        .logStreamName(logStream)
                        .logEvents(List.of(event));

        if (sequenceToken != null) {
            request.sequenceToken(sequenceToken);
        }

        try {
            sequenceToken = client.putLogEvents(request.build()).nextSequenceToken();
        } catch (InvalidSequenceTokenException ex) {
            sequenceToken = ex.expectedSequenceToken();
        } catch (DataAlreadyAcceptedException ex) {
            sequenceToken = ex.expectedSequenceToken();
        } catch (RuntimeException ex) {
            addError("Failed to append log event to CloudWatch", ex);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (client != null) {
            client.close();
        }
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setLogGroup(String logGroup) {
        this.logGroup = logGroup;
    }

    public void setLogStream(String logStream) {
        this.logStream = logStream;
    }

    private void createLogGroupIfMissing() {
        try {
            client.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroup).build());
        } catch (ResourceAlreadyExistsException ignored) {
            // Log group already exists.
        }
    }

    private void createLogStreamIfMissing() {
        try {
            client.createLogStream(
                    CreateLogStreamRequest.builder()
                            .logGroupName(logGroup)
                            .logStreamName(logStream)
                            .build());
        } catch (ResourceAlreadyExistsException ignored) {
            // Log stream already exists.
        }
    }

    private String toJson(ILoggingEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        appendJsonField(
                builder, "timestamp", Instant.ofEpochMilli(event.getTimeStamp()).toString());
        builder.append(',');
        appendJsonField(builder, "level", event.getLevel().toString());
        builder.append(',');
        appendJsonField(builder, "logger", event.getLoggerName());
        builder.append(',');
        appendJsonField(builder, "thread", event.getThreadName());
        builder.append(',');
        appendJsonField(builder, "message", event.getFormattedMessage());
        appendMdc(builder, event.getMDCPropertyMap());
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            builder.append(',');
            appendJsonField(builder, "exception", throwableProxy.getClassName());
            builder.append(',');
            appendJsonField(builder, "exceptionMessage", throwableProxy.getMessage());
        }
        builder.append('}');
        return builder.toString();
    }

    private void appendMdc(StringBuilder builder, Map<String, String> mdcProperties) {
        mdcProperties.forEach(
                (key, value) -> {
                    builder.append(',');
                    appendJsonField(builder, key, value);
                });
    }

    private void appendJsonField(StringBuilder builder, String name, String value) {
        builder.append('"').append(escape(name)).append("\":");
        if (value == null) {
            builder.append("null");
            return;
        }
        builder.append('"').append(escape(value)).append('"');
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
