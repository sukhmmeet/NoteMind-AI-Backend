package com.dhaliwal.notemind.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_refresh_token_revoked", columnList = "token, revoked")
})
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String token;

    @ManyToOne
    private User user;

    private Instant expiryDate;

    private boolean revoked;
}
