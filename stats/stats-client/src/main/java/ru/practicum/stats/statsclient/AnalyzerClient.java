package ru.practicum.stats.statsclient;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.ewm.dashboard.analyzer.RecommendationsControllerGrpc;
import ru.practicum.service.dashboard.InteractionsCountRequestProto;
import ru.practicum.service.dashboard.RecommendedEventProto;
import ru.practicum.service.dashboard.SimilarEventsRequestProto;
import ru.practicum.service.dashboard.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub controllerBlockingStub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, Integer maxResults) {
        final UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        final Iterator<RecommendedEventProto> iterator = controllerBlockingStub.getRecommendationsForUser(request);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, Integer maxResults) {
        final SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        final Iterator<RecommendedEventProto> iterator = controllerBlockingStub.getSimilarEvents(request);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        final InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        final Iterator<RecommendedEventProto> iterator = controllerBlockingStub.getInteractionsCount(request);
        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
