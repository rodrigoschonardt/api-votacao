package rodrigoschonardt.votingapi.vote.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.user.domain.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_id"}))
public class Vote {
    public enum VoteOption {
        YES("Sim"),
        NO("Não");

        private final String value;

        VoteOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @JsonCreator
        public static VoteOption fromString(String value) {
            for (VoteOption option : values()) {
                if (option.name().equalsIgnoreCase(value) ||
                        option.getValue().equalsIgnoreCase(value)) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Invalid vote option: " + value);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vote_option", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private VoteOption voteOption;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VoteOption getVoteOption() {
        return voteOption;
    }

    public void setVoteOption(VoteOption voteOption) {
        this.voteOption = voteOption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
