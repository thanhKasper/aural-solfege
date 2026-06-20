package vn.ktt.ear_training_system.infrastructure.repository.gateway;

import org.springframework.stereotype.Repository;
import vn.ktt.ear_training_system.domain.practice_session.entity.PracticeSession;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.value_object.SessionStatus;
import vn.ktt.ear_training_system.infrastructure.repository.mapper.PracticeSessionEntityMapper;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PracticeSessionRepository implements IPracticeSessionRepository {

    private final PracticeSessionJpaRepository jpaRepository;
    private final PracticeSessionEntityMapper mapper;

    public PracticeSessionRepository(PracticeSessionJpaRepository jpaRepository, PracticeSessionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PracticeSession saveSession(PracticeSession session) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<PracticeSession> getSessionById(UUID sessionId) {
        return jpaRepository.findBySessionId(sessionId).map(mapper::toDomain);
    }

    @Override
    public Optional<PracticeSession> findByExercise(UUID exerciseId) {
        return jpaRepository.findTopByExerciseIdOrderByCreatedAtDesc(exerciseId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActiveSessionForExercise(UUID exerciseId) {
        return jpaRepository.existsByExerciseIdAndStatus(exerciseId, SessionStatus.IN_PROGRESS);
    }
}
