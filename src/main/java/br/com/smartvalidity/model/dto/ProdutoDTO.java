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
    private Map<String, Object> categoria; //TODO: Entender o uso do Map
    private List<Map<String, Object>> fornecedores; //TODO: Entender a relação com o front

}
