package ru.practicum.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.entity.ActionType;
import ru.practicum.stats.analyzer.entity.UserActionEntity;
import ru.practicum.stats.analyzer.mapper.UserActionMapper;
import ru.practicum.stats.analyzer.repository.UserActionRepository;
import ru.practicum.stats.analyzer.service.UserActionService;
import ru.practicum.stats.analyzer.service.WeightProperties;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final WeightProperties weightProperties;
    private final UserActionRepository userActionRepository;
    private final UserActionMapper userActionMapper;

    @Override
    @Transactional
    public void handleUserAction(UserActionAvro avro) {
        log.debug("Сохраняем действие: {} пользователя: {} для события: {}", avro, avro.getUserId(), avro.getEventId());
        Optional<UserActionEntity> userActionOpt =
                userActionRepository.findByUserIdAndEventId(avro.getUserId(), avro.getEventId());
        ActionType newType = avroTypeToEntity(avro.getActionType());
        if (userActionOpt.isPresent()) {
            UserActionEntity userAction = userActionOpt.get();
            double weight = getWeightForAction(userAction.getActionType());
            double newWeight = getWeightForAction(newType);
            if (Double.compare(newWeight, weight) > 0) {
                userAction.setActionType(newType);
                userAction.setTimestamp(avro.getTimestamp());
                userAction.setActionWeight(newWeight);
            }
        } else {
            UserActionEntity userAction = userActionMapper.toUserActionEntity(avro, getWeightForAction(newType));
            userAction.setActionType(newType);
            userActionRepository.save(userAction);
        }
    }

    private ActionType avroTypeToEntity(ActionTypeAvro avroType) {
        return switch (avroType) {
            case VIEW   -> ActionType.VIEW;
            case REGISTER -> ActionType.REGISTER;
            case LIKE   -> ActionType.LIKE;
        };
    }

    private double getWeightForAction(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> weightProperties.getView();
            case REGISTER -> weightProperties.getRegister();
            case LIKE -> weightProperties.getLike();
            default -> throw new IllegalArgumentException(String.format("Вес для типа %s не определен", actionType));
        };
    }
}
