package br.com.smartvalidity.model.dto;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertaRequestDTO {
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
        alerta.setFrequenciaDisparo(this.frequenciaDisparo);
        return alerta;
    }
} 