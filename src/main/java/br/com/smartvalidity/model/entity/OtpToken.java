package br.com.smartvalidity.model.entity;

import java.time.LocalDateTime;

import br.com.smartvalidity.model.enums.OtpPurpose;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // email ligado a este OTP (não usamos relacionamento JPA para simplificar)
    private String email;

    private String token; // código numérico ou alfanumérico

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    public OtpToken(String email, String token, LocalDateTime expiresAt, OtpPurpose purpose) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
        this.purpose = purpose;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
} 