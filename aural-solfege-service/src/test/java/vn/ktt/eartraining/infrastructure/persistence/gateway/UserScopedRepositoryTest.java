package vn.ktt.eartraining.infrastructure.persistence.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.ktt.eartraining.application.access.NotFoundException;
import vn.ktt.eartraining.application.outbound.ICurrentUserPort;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.session.entity.PracticeSession;
import vn.ktt.eartraining.domain.session.valueobject.SessionStatus;
import vn.ktt.eartraining.infrastructure.persistence.entity.ExerciseEntity;
import vn.ktt.eartraining.infrastructure.persistence.entity.PracticeSessionEntity;
import vn.ktt.eartraining.infrastructure.persistence.mapper.ExerciseEntityMapper;
import vn.ktt.eartraining.infrastructure.persistence.mapper.PracticeSessionEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Repositories must only ever see and write the current user's rows. */
class UserScopedRepositoryTest {

    static final String ALICE = "alice";
    static final String BOB = "bob";

    final ICurrentUserPort currentUser = mock(ICurrentUserPort.class);

    final ExerciseJpaRepository exerciseJpa = mock(ExerciseJpaRepository.class);
    final ExerciseEntityMapper exerciseMapper = mock(ExerciseEntityMapper.class);
    ExerciseRepository exerciseRepository;

    final PracticeSessionJpaRepository sessionJpa = mock(PracticeSessionJpaRepository.class);
    final PracticeSessionEntityMapper sessionMapper = mock(PracticeSessionEntityMapper.class);
    PracticeSessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        when(currentUser.userId()).thenReturn(ALICE);
        exerciseRepository = new ExerciseRepository(exerciseJpa, exerciseMapper, currentUser);
        sessionRepository = new PracticeSessionRepository(sessionJpa, sessionMapper, currentUser);
    }

    // ---- exercises ----

    @Test
    void exerciseOfAnotherUserIsNotFound() {
        var id = UUID.randomUUID();
        when(exerciseJpa.findByExerciseIdAndOwnerId(id, ALICE)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> exerciseRepository.getExerciseById(id.toString()));
        verify(exerciseJpa, never()).findById(any());
    }

    @Test
    void listIsFilteredByCurrentUser() {
        when(exerciseJpa.findByOwnerId(ALICE, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));

        exerciseRepository.getPagedExercises(0, 10);

        verify(exerciseJpa).findByOwnerId(ALICE, PageRequest.of(0, 10));
        verify(exerciseJpa, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void newExerciseIsStampedWithCurrentUser() {
        var domain = mock(Exercise.class);
        when(domain.getExerciseId()).thenReturn(null);
        when(exerciseMapper.toEntity(domain)).thenReturn(new ExerciseEntity());

        exerciseRepository.saveExercise(domain);

        var saved = ArgumentCaptor.forClass(ExerciseEntity.class);
        verify(exerciseJpa).save(saved.capture());
        assertEquals(ALICE, saved.getValue().getOwnerId());
    }

    @Test
    void existingExerciseKeepsItsOwnerAndCannotBeSavedByAnotherUser() {
        var id = UUID.randomUUID();
        var domain = mock(Exercise.class);
        when(domain.getExerciseId()).thenReturn(id);
        when(exerciseMapper.toEntity(domain)).thenReturn(new ExerciseEntity());

        // Bob tries to overwrite Alice's exercise
        when(currentUser.userId()).thenReturn(BOB);
        when(exerciseJpa.findByExerciseIdAndOwnerId(id, BOB)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> exerciseRepository.saveExercise(domain));
        verify(exerciseJpa, never()).save(any());
    }

    // ---- sessions ----

    @Test
    void sessionLookupsAreScopedByLearner() {
        var sessionId = UUID.randomUUID();
        var exerciseId = UUID.randomUUID();
        when(sessionJpa.findBySessionIdAndLearnerId(sessionId, ALICE)).thenReturn(Optional.empty());
        when(sessionJpa.findTopByLearnerIdAndExerciseExerciseIdOrderByCreatedAtDesc(ALICE, exerciseId)).thenReturn(Optional.empty());
        when(sessionJpa.existsByLearnerIdAndExerciseExerciseIdAndStatus(ALICE, exerciseId, SessionStatus.IN_PROGRESS)).thenReturn(false);

        assertTrue(sessionRepository.getSessionById(sessionId).isEmpty());
        assertTrue(sessionRepository.findByExercise(exerciseId).isEmpty());
        assertFalse(sessionRepository.existsActiveSessionForExercise(exerciseId));

        verify(sessionJpa).findBySessionIdAndLearnerId(sessionId, ALICE);
        verify(sessionJpa).findTopByLearnerIdAndExerciseExerciseIdOrderByCreatedAtDesc(ALICE, exerciseId);
        verify(sessionJpa).existsByLearnerIdAndExerciseExerciseIdAndStatus(ALICE, exerciseId, SessionStatus.IN_PROGRESS);
        verify(sessionJpa, never()).findById(any());
    }

    @Test
    void deleteOnlyRemovesTheCurrentLearnersSession() {
        var id = UUID.randomUUID();
        when(sessionJpa.deleteBySessionIdAndLearnerId(id, ALICE)).thenReturn(0L);
        assertFalse(sessionRepository.deleteSession(id));      // someone else's or missing

        when(sessionJpa.deleteBySessionIdAndLearnerId(id, ALICE)).thenReturn(1L);
        assertTrue(sessionRepository.deleteSession(id));

        verify(sessionJpa, never()).deleteById(any());
    }

    @Test
    void newSessionIsStampedWithCurrentUser() {
        var domain = mock(PracticeSession.class);
        when(domain.getSessionId()).thenReturn(null);
        when(sessionMapper.toEntity(domain)).thenReturn(new PracticeSessionEntity());

        sessionRepository.saveSession(domain);

        var saved = ArgumentCaptor.forClass(PracticeSessionEntity.class);
        verify(sessionJpa).save(saved.capture());
        assertEquals(ALICE, saved.getValue().getLearnerId());
    }

    @Test
    void sessionOfAnotherLearnerCannotBeSaved() {
        var id = UUID.randomUUID();
        var domain = mock(PracticeSession.class);
        when(domain.getSessionId()).thenReturn(id);
        when(sessionMapper.toEntity(domain)).thenReturn(new PracticeSessionEntity());
        when(sessionJpa.findBySessionIdAndLearnerId(id, ALICE)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sessionRepository.saveSession(domain));
        verify(sessionJpa, never()).save(any());
    }

    @Test
    void duplicateActiveSessionBecomesConflict() {
        var domain = mock(PracticeSession.class);
        when(domain.getSessionId()).thenReturn(null);
        when(sessionMapper.toEntity(domain)).thenReturn(new PracticeSessionEntity());
        when(sessionJpa.save(any())).thenThrow(new DataIntegrityViolationException("uq_active_session"));

        assertThrows(IllegalStateException.class, () -> sessionRepository.saveSession(domain));
    }
}
