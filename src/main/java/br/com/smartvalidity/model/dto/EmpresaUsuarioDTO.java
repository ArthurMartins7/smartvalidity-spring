package br.com.smartvalidity.model.dto;

import br.com.smartvalidity.model.enums.PerfilAcesso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;
import lombok.Data;

@Data
public class EmpresaUsuarioDTO {

    @CNPJ
    @NotBlank(message = "O CNPJ não pode ser vazio ou apenas espaços em branco.")
    private String cnpj;

    @NotBlank(message = "A razão social não pode ser vazia ou apenas espaços em branco.")
    private String razaoSocial;

    @NotBlank(message = "O nome não pode ser vazio ou apenas espaços em branco.")
    private String nomeUsuario;

    @Email
    @NotBlank(message = "O e-mail não pode ser vazio ou apenas espaços em branco.")
    private String email;

    @NotBlank(message = "A senha não pode ser vazia ou apenas espaços em branco.")
    private String senha;

    @NotBlank(message = "O cargo não pode ser vazio ou apenas espaços em branco.")
    private String cargo;

    // Código de verificação de e-mail (OTP)
    @NotBlank(message = "O código de verificação não pode ser vazio.")
    private String token;

    // Nenhum campo para perfil, pois será fixo como ASSINANTE
    public PerfilAcesso getPerfilAcessoAssinante() {
        return PerfilAcesso.ASSINANTE;
    }
} 