package ru.practicum.core.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.core.interactionapi.enums.State;
import ru.practicum.core.requestservice.model.ParticipationRequest;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventAndRequester(Long event, Long requester);

    List<ParticipationRequest> findAllByRequester(Long requester);

    List<ParticipationRequest> findByEvent(Long eventId);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.event IN :eventIds AND r.status = :status")
    List<ParticipationRequest> findConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds,
                                                               @Param("status") State status);

    List<ParticipationRequest> findAllByEventAndIdIn(Long event, List<Long> requestIds);
}
