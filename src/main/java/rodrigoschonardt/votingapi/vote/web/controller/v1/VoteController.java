package rodrigoschonardt.votingapi.vote.web.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.service.VoteService;
import rodrigoschonardt.votingapi.vote.web.dto.AddVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.UpdateVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.VoteDetailsData;
import rodrigoschonardt.votingapi.vote.web.mapper.VoteMapper;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/votes")
@Tag(name = "Voto")
public class VoteController {
    private final VoteService voteService;
    private final VoteMapper voteMapper;

    public VoteController(VoteService voteService, VoteMapper voteMapper) {
        this.voteService = voteService;
        this.voteMapper = voteMapper;
    }

    @PostMapping
    @Operation(summary = "Cadastrar voto")
    public ResponseEntity<VoteDetailsData> add(@RequestBody @Valid AddVoteData voteData, UriComponentsBuilder uriBuilder) {
        Vote vote = voteService.add(voteData);

        URI uri = uriBuilder.path("/api/v1/votes/{id}").buildAndExpand(vote.getId()).toUri();

        return ResponseEntity.created(uri).body(voteMapper.toVoteDetails(vote));
    }

    @PutMapping
    @Operation(summary = "Atualizar voto")
    public ResponseEntity<VoteDetailsData> update(@RequestBody @Valid UpdateVoteData voteData) {
        Vote vote = voteService.update(voteData);

        return ResponseEntity.ok(voteMapper.toVoteDetails(vote));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar voto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voteService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar voto")
    public ResponseEntity<VoteDetailsData> get(@PathVariable Long id) {
        Vote vote = voteService.get(id);

        return ResponseEntity.ok(voteMapper.toVoteDetails(vote));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Buscar todos votos da sessão")
    public ResponseEntity<Page<VoteDetailsData>> getAllBySession(@PathVariable Long sessionId, Pageable pageable) {
        Page<Vote> votes = voteService.getAllBySession(sessionId, pageable);

        return ResponseEntity.ok(votes.map(voteMapper::toVoteDetails));
    }
}
