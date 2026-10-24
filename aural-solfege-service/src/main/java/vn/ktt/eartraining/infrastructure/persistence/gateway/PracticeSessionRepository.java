package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import vn.ktt.eartraining.application.access.NotFoundException;
import vn.ktt.eartraining.application.outbound.ICurrentUserPort;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;
import vn.ktt.eartraining.domain.session.repository.IPracticeSessionRepository;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.infrastructure.persistence.mapper.PracticeSessionEntityMapper;

import java.util.Optional;
import java.util.UUID;

/** User-scoped: only sessions of the current user are visible or writable. */
@Repository
public class PracticeSessionRepository implements IPracticeSessionRepository {

    private final PracticeSessionJpaRepository jpaRepository;
    private final PracticeSessionEntityMapper mapper;
    private final ICurrentUserPort currentUser;

    public PracticeSessionRepository(PracticeSessionJpaRepository jpaRepository,
                                     PracticeSessionEntityMapper mapper,
                                     ICurrentUserPort currentUser) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Override
    public PracticeSession saveSession(PracticeSession session) {
        var entity = mapper.toEntity(session);
        // The domain has no owner: stamp it on insert, keep the stored one on update.
        entity.setLearnerId(session.getSessionId() == null
                ? currentUser.userId()
                : jpaRepository.findBySessionIdAndLearnerId(session.getSessionId(), currentUser.userId())
                        .orElseThrow(() -> new NotFoundException("Session not found: " + session.getSessionId()))
                        .getLearnerId());
        try {
            return mapper.toDomain(jpaRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            // uq_active_session: one IN_PROGRESS session per learner and exercise
            throw new IllegalStateException("An active session already exists for this exercise", e);
        }
    }

    @Override
    public Optional<PracticeSession> getSessionById(UUID sessionId) {
        return jpaRepository.findBySessionIdAndLearnerId(sessionId, currentUser.userId()).map(mapper::toDomain);
    }

    @Override
    public Optional<PracticeSession> findByExercise(UUID exerciseId) {
        return jpaRepository.findTopByLearnerIdAndExerciseExerciseIdOrderByCreatedAtDesc(currentUser.userId(), exerciseId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActiveSessionForExercise(UUID exerciseId) {
        return jpaRepository.existsByLearnerIdAndExerciseExerciseIdAndStatus(
                currentUser.userId(), exerciseId, SessionStatus.IN_PROGRESS);
    }
}
