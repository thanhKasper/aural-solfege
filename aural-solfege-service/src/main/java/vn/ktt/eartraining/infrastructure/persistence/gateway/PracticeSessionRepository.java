package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.stereotype.Repository;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;
import vn.ktt.eartraining.domain.session.repository.IPracticeSessionRepository;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.infrastructure.persistence.mapper.PracticeSessionEntityMapper;

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
        return jpaRepository.findTopByExerciseExerciseIdOrderByCreatedAtDesc(exerciseId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActiveSessionForExercise(UUID exerciseId) {
        return jpaRepository.existsByExerciseExerciseIdAndStatus(exerciseId, SessionStatus.IN_PROGRESS);
    }
}
