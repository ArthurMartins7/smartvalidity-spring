package br.com.smartvalidity.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.OtpToken;
import br.com.smartvalidity.model.enums.OtpPurpose;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {

    Optional<OtpToken> findByEmailAndTokenAndPurpose(String email, String token, OtpPurpose purpose);

    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);
} 