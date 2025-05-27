package br.com.smartvalidity.web.dto;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertaResponseDTO {
    private Integer id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataHoraDisparo;
    private boolean disparoRecorrente;
    private FrequenciaDisparo frequenciaDisparo;

    public static AlertaResponseDTO fromEntity(Alerta alerta) {
        AlertaResponseDTO dto = new AlertaResponseDTO();
        dto.setId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
        dto.setDisparoRecorrente(alerta.isDisparoRecorrente());
        dto.setFrequenciaDisparo(alerta.getFrequenciaDisparo());
        return dto;
    }
} 