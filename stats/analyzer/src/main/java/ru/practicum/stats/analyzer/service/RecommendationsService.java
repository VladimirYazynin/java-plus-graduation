package ru.practicum.stats.analyzer.service;

import ru.practicum.service.dashboard.InteractionsCountRequestProto;
import ru.practicum.service.dashboard.RecommendedEventProto;
import ru.practicum.service.dashboard.SimilarEventsRequestProto;
import ru.practicum.service.dashboard.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationsService {

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
