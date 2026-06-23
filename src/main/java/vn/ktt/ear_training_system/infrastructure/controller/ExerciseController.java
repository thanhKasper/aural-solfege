package vn.ktt.ear_training_system.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.PracticeStepDTO;
import vn.ktt.ear_training_system.application.dtos.SessionStepDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;
import vn.ktt.ear_training_system.application.inbound.SessionPort;
import vn.ktt.ear_training_system.application.services.Page;
import vn.ktt.ear_training_system.infrastructure.dto.ApiCallSpec;
import vn.ktt.ear_training_system.infrastructure.dto.PracticeStepResponse;
import vn.ktt.ear_training_system.infrastructure.dto_prefill.StepApiCallProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/exercises")
public class ExerciseController {
    private final ExerciseCreationPort exerciseCreationService;
    private final ExerciseRetrievalPort exerciseRetrievalService;
    private final SessionPort sessionPort;
    private final Map<Class<? extends PracticeStepDTO>, StepApiCallProvider> stepApiCallProviderMap = new HashMap<>();

    public ExerciseController(ExerciseCreationPort exerciseCreationService,
                              ExerciseRetrievalPort exerciseRetrievalService,
                              SessionPort sessionPort,
                              List<StepApiCallProvider> apiCallProviders) {
        this.exerciseCreationService = exerciseCreationService;
        this.exerciseRetrievalService = exerciseRetrievalService;
        this.sessionPort = sessionPort;
        apiCallProviders.forEach((provider) -> {
            this.stepApiCallProviderMap.put(provider.getPracticeStepDTOClass(), provider);
        });
    }

    @GetMapping
    public ResponseEntity<Page<ExerciseDTO>> getExercises(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(exerciseRetrievalService.getExercises(page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> getExercises(@PathVariable String id) {
        return ResponseEntity.ok(exerciseRetrievalService.getExerciseById(id));
    }

    @PostMapping
    public ResponseEntity<?> createNewExercise(@RequestBody ExerciseDTO exerciseDTO) {
        exerciseCreationService.createExercise(exerciseDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{exerciseId}/sessions")
    public ResponseEntity<PracticeStepResponse> startSession(@PathVariable String exerciseId) {
        SessionStepDTO response = sessionPort.startSession(UUID.fromString(exerciseId));
        ApiCallSpec apiCall = getApiCallSpec(response.currentStep());
        return ResponseEntity.ok(new PracticeStepResponse(response.metadata(), response.currentStep(), apiCall));
    }

    private ApiCallSpec getApiCallSpec(PracticeStepDTO step) {
        StepApiCallProvider provider = retrieveProvider(step);
        return provider.provide(step);
    }

    private StepApiCallProvider retrieveProvider(PracticeStepDTO dto) {
        if (stepApiCallProviderMap.containsKey(dto.getClass())) {
            return stepApiCallProviderMap.get(dto.getClass());
        }
        throw new RuntimeException("Not found API call provider for DTO " + dto.getClass());
    }
}
