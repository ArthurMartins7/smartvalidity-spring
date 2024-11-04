package br.com.smartvalidity.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table
@Data
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'logradouro' não pode ser vazio ou apenas espaços em branco.")
    private String logradouro;

    @NotBlank(message = "O campo 'número' não pode ser vazio ou apenas espaços em branco.")
    private String numero;

    private String complemento;

    @NotBlank(message = "O campo 'bairro' não pode ser vazio ou apenas espaços em branco.")
    private String bairro;

    @NotBlank(message = "O campo 'cidade' não pode ser vazio ou apenas espaços em branco.")
    private String cidade;

    @NotBlank(message = "O campo 'estado' não pode ser vazio ou apenas espaços em branco.")
    private String estado;

    @NotBlank(message = "O campo 'país' não pode ser vazio ou apenas espaços em branco.")
    private String pais;

    @NotBlank(message = "O campo 'cep' não pode ser vazio ou apenas espaços em branco.")
    private String cep;

    @OneToOne(mappedBy = "endereco")
    private Fornecedor fornecedor;



}
