package ru.practicum.stats.statsclient;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.ewm.stats.collector.UserActionControllerGrpc;
import ru.practicum.service.collector.ActionTypeProto;
import ru.practicum.service.collector.UserActionProto;

import java.time.Instant;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub controllerBlockingStub;

    public void collectUserAction(Long userId, Long eventId, String actionType, Instant instant) {
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.valueOf(actionType))
                .setTimestamp(timestamp)
                .build();
        controllerBlockingStub.collectUserAction(request);
    }
}
