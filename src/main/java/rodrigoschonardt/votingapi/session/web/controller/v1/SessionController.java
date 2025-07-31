package rodrigoschonardt.votingapi.session.web.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rodrigoschonardt.votingapi.orchestrator.VotingOrchestratorService;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.session.web.dto.AddSessionData;
import rodrigoschonardt.votingapi.session.web.dto.SessionDetailsData;
import rodrigoschonardt.votingapi.session.web.mapper.SessionMapper;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessão")
public class SessionController {
    private final SessionService sessionService;
    private final SessionMapper sessionMapper;
    private final VotingOrchestratorService votingOrchestratorService;

    public SessionController(SessionService sessionService, SessionMapper sessionMapper,
                             VotingOrchestratorService votingOrchestratorService) {
        this.sessionService = sessionService;
        this.sessionMapper = sessionMapper;
        this.votingOrchestratorService = votingOrchestratorService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar sessão")
    public ResponseEntity<SessionDetailsData> add(@RequestBody @Valid AddSessionData sessionData, UriComponentsBuilder uriBuilder) {
        Session session = sessionService.add(sessionData);

        URI uri = uriBuilder.path("/api/v1/sessions/{id}").buildAndExpand(session.getId()).toUri();

        return ResponseEntity.created(uri).body(sessionMapper.toSessionDetails(session));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Deletar sessão")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        votingOrchestratorService.deleteSessionAndVotes(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão")
    public ResponseEntity<SessionDetailsData> get(@PathVariable Long id) {
        Session session = sessionService.get(id);

        return ResponseEntity.ok(sessionMapper.toSessionDetails(session));
    }

    @GetMapping("/topic/{topicId}")
    @Operation(summary = "Buscar todas sessões da pauta")
    public ResponseEntity<Page<SessionDetailsData>> getAllByTopic(@PathVariable Long topicId, Pageable pageable) {
        Page<Session> sessions = sessionService.getAllByTopic(topicId, pageable);

        return ResponseEntity.ok(sessions.map(sessionMapper::toSessionDetails));
    }
}
