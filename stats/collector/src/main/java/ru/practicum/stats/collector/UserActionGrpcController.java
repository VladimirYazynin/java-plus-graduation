package ru.practicum.stats.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.ewm.stats.collector.UserActionControllerGrpc;
import ru.practicum.service.collector.UserActionProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.debug("Обработка действия пользователя. user:{}, action:{}", request.getUserId(), request.getActionType());
            producer.send(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.fromThrowable(e)
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
