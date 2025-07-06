package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import br.com.smartvalidity.model.entity.Alerta;
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
        private Date dataHoraDisparo;
        private TipoAlerta tipo;
        private Date dataCriacao;
        private Date dataEnvio;
        private String usuarioCriador;
        private List<String> usuariosAlerta;
        private List<String> produtosAlerta;
        private List<String> usuariosAlertaIds;
        private List<String> produtosAlertaIds;
        private Boolean lida;
        private Boolean itemInspecionado;
        private Integer diasVencidos;
        private Date dataVencimentoItem;
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
        private List<String> usuariosIds;
        private List<String> produtosIds;
    }
    @Data
    public static class Edicao {
        private Integer id;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private List<String> usuariosIds;
        private List<String> produtosIds;
    }
    @Data
    public static class Filtro {
        private String titulo;
        private TipoAlerta tipo;
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
        public Alerta toEntity() {
            Alerta alerta = new Alerta();
            alerta.setTitulo(this.titulo);
            alerta.setDescricao(this.descricao);
            alerta.setDataHoraDisparo(this.dataHoraDisparo);
            return alerta;
        }
    }
    @Data
    public static class Response {
        private Integer id;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        public static Response fromEntity(Alerta alerta) {
            Response dto = new Response();
            dto.setId(alerta.getId());
            dto.setTitulo(alerta.getTitulo());
            dto.setDescricao(alerta.getDescricao());
            dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
            return dto;
        }
    }
}