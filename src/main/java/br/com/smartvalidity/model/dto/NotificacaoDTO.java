package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.smartvalidity.model.enums.TipoAlerta;
import lombok.Data;

public class NotificacaoDTO {

    @Data
    public static class Listagem {
        private Long notificacaoId;
        private Integer alertaId;
        private String titulo;
        private String descricao;
        private LocalDateTime dataHoraDisparo;
        private TipoAlerta tipo;
        private Integer diasAntecedencia;
        private Boolean ativo;
        private Boolean recorrente;
        private String configuracaoRecorrencia;
        private LocalDateTime dataCriacaoAlerta;
        private LocalDateTime dataCriacaoNotificacao;
        private LocalDateTime dataHoraLeitura;
        private String usuarioCriador;
        private List<String> usuariosAlerta;
        private List<String> produtosAlerta;
        private List<String> usuariosAlertaIds;
        private List<String> produtosAlertaIds;
        private Boolean lida;
    }
} 