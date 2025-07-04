package br.com.smartvalidity.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "O e-mail não pode ser vazio ou apenas espaços em branco.")
    @Email
    private String email;

    @NotBlank(message = "O código não pode ser vazio ou apenas espaços em branco.")
    private String token;

    @NotBlank(message = "A senha não pode ser vazia ou apenas espaços em branco.")
    private String novaSenha;
} 