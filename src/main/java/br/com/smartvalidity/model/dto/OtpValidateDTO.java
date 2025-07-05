package br.com.smartvalidity.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpValidateDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String token;
} 