package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import br.com.smartvalidity.model.enums.TipoAlerta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        private List<String> usuariosAlerta; // Nomes dos usuários
        private List<String> produtosAlerta; // Nomes dos produtos
        private List<String> usuariosAlertaIds; // IDs dos usuários
        private List<String> produtosAlertaIds; // IDs dos produtos
        private Boolean lida; // Status de leitura
    }

    @Data
    public static class Cadastro {
        @NotBlank
        private String titulo;
        @NotBlank
        private String descricao;
        @NotNull
        private LocalDateTime dataHoraDisparo;
        @NotNull
        private TipoAlerta tipo;
        private Integer diasAntecedencia;
        private Boolean recorrente = false;
        private String configuracaoRecorrencia;
        private List<String> usuariosIds; //
        private List<String> produtosIds; //
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
        private int pagina = 1;
        private int limite = 10;
        private String sortBy = "dataCriacao";
        private String sortDirection = "desc";
        public boolean temPaginacao() {
            return limite > 0 && pagina > 0;
        }
    }

    @Data
    public static class Request {
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private boolean disparoRecorrente;
        private FrequenciaDisparo frequenciaDisparo;

        public Alerta toEntity() {
            Alerta alerta = new Alerta();
            alerta.setTitulo(this.titulo);
            alerta.setDescricao(this.descricao);
            alerta.setDataHoraDisparo(this.dataHoraDisparo);
            alerta.setDisparoRecorrente(this.disparoRecorrente);
            if (this.frequenciaDisparo != null) {
                alerta.setFrequenciaDisparo(this.frequenciaDisparo.name());
            }
            return alerta;
        }
    }

    @Data
    public static class Response {
        private Integer id;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private boolean disparoRecorrente;
        private FrequenciaDisparo frequenciaDisparo;

        public static Response fromEntity(Alerta alerta) {
            Response dto = new Response();
            dto.setId(alerta.getId());
            dto.setTitulo(alerta.getTitulo());
            dto.setDescricao(alerta.getDescricao());
            dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
            dto.setDisparoRecorrente(alerta.getDisparoRecorrente() != null ? alerta.getDisparoRecorrente() : false);
            if (alerta.getFrequenciaDisparo() != null) {
                try {
                    dto.setFrequenciaDisparo(FrequenciaDisparo.valueOf(alerta.getFrequenciaDisparo()));
                } catch (IllegalArgumentException e) {
                    dto.setFrequenciaDisparo(null);
                }
            }
            return dto;
        }
    }
} 