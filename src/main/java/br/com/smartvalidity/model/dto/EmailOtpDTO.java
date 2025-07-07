package br.com.smartvalidity.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailOtpDTO {
    @NotBlank
    @Email
    private String email;
} 