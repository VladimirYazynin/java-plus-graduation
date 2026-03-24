package ru.practicum.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.ewm.dashboard.analyzer.RecommendationsControllerGrpc;
import ru.practicum.service.dashboard.InteractionsCountRequestProto;
import ru.practicum.service.dashboard.RecommendedEventProto;
import ru.practicum.service.dashboard.SimilarEventsRequestProto;
import ru.practicum.service.dashboard.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.service.RecommendationsService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequestProto,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.debug("Начинаем обрабатывать запрос на получение пользовательских рекомендаций");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getRecommendationsForUser(userPredictionsRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.debug("Начинаем обрабатывать запрос на получение похожих событий");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getSimilarEvents(similarEventsRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequestProto,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.debug("Начинаем обрабатывать запрос на получение количества взаимодействий");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getInteractionsCount(interactionsCountRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
