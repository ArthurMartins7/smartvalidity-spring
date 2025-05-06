package br.com.smartvalidity.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ProdutoDTO {

    private UUID id;
    private String codigoBarras;
    private String descricao;
    private String marca;
    private String unidadeMedida;
    private int quantidade;

    // Simples: categoria com id e nome
    private Map<String, Object> categoria;

    // Lista de fornecedores com estrutura completa
    private List<Map<String, Object>> fornecedores;

}
