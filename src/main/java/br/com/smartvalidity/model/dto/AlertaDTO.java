package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.smartvalidity.model.enums.TipoAlerta;
import lombok.Data;

public class AlertaDTO {

    @Data
    public static class Listagem {
        private Integer id;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private TipoAlerta tipo;
        private Integer diasAntecedencia;
        private Boolean ativo;
        private Boolean recorrente;
        private String configuracaoRecorrencia;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataEnvio;
        private String usuarioCriador; // Nome do usuário criador
        private List<String> usuariosAlerta; // Nomes dos usuários que receberão o alerta
        private List<String> produtosAlerta; // Nomes dos produtos relacionados
    }

    @Data
    public static class Cadastro {
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private TipoAlerta tipo;
        private Integer diasAntecedencia;
        private Boolean recorrente = false;
        private String configuracaoRecorrencia;
        private List<String> usuariosIds; // IDs dos usuários que receberão o alerta
        private List<String> produtosIds; // IDs dos produtos relacionados (opcional)
    }

    @Data
    public static class Edicao {
        private Integer id;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private Integer diasAntecedencia;
        private Boolean ativo;
        private Boolean recorrente;
        private String configuracaoRecorrencia;
        private List<String> usuariosIds;
        private List<String> produtosIds;
    }

    @Data
    public static class Filtro {
        private String titulo;
        private TipoAlerta tipo;
        private Boolean ativo;
        private Boolean recorrente;
        private LocalDateTime dataInicialDisparo;
        private LocalDateTime dataFinalDisparo;
        private String usuarioCriador;
        
        // Paginação
        private int pagina = 1;
        private int limite = 10;
        
        // Ordenação
        private String sortBy = "dataCriacao";
        private String sortDirection = "desc";
        
        public boolean temPaginacao() {
            return limite > 0 && pagina > 0;
        }
    }
} 