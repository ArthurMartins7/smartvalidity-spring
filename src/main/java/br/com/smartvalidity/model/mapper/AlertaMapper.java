package br.com.smartvalidity.model.mapper;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Usuario;


public final class AlertaMapper {

    private AlertaMapper() {
    }

    public static AlertaDTO.Listagem toListagemDTO(Alerta alerta) {
        if (alerta == null) {
            return null;
        }
        AlertaDTO.Listagem dto = new AlertaDTO.Listagem();
        dto.setId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setTipo(alerta.getTipo());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo() != null ? 
            Timestamp.valueOf(alerta.getDataHoraDisparo()) : null);
        dto.setDataCriacao(alerta.getDataHoraCriacao() != null ? 
            Timestamp.valueOf(alerta.getDataHoraCriacao()) : null);

        // Campos de recorrência removidos - alertas personalizados são mais simples

        if (alerta.getItemProduto() != null) {
            String nomeProduto = alerta.getItemProduto().getProduto() != null
                    ? alerta.getItemProduto().getProduto().getDescricao()
                    : null;
            if (nomeProduto != null) {
                dto.setProdutosAlerta(List.of(nomeProduto));
            }
            dto.setItemInspecionado(alerta.getItemProduto().getInspecionado());
            
            if (alerta.getItemProduto().getDataVencimento() != null) {
                LocalDate dataVencimento = alerta.getItemProduto().getDataVencimento().toLocalDate();
                LocalDate hoje = LocalDate.now();
                
                long diasVencidos = ChronoUnit.DAYS.between(dataVencimento, hoje);
                dto.setDiasVencidos((int) diasVencidos);
                
                dto.setDataVencimentoItem(Timestamp.valueOf(alerta.getItemProduto().getDataVencimento()));
            }
        }

        if (alerta.getUsuarioCriador() != null) {
            dto.setUsuarioCriador(alerta.getUsuarioCriador().getNome());
        }

        if (alerta.getProdutosAlerta() != null && !alerta.getProdutosAlerta().isEmpty()) {
            dto.setProdutosAlerta(alerta.getProdutosAlerta().stream()
                    .map(Produto::getDescricao)
                    .toList());
            dto.setProdutosAlertaIds(alerta.getProdutosAlerta().stream()
                    .map(Produto::getId)
                    .toList());
        }

        if (alerta.getUsuariosAlerta() != null && !alerta.getUsuariosAlerta().isEmpty()) {
            dto.setUsuariosAlerta(alerta.getUsuariosAlerta().stream()
                    .map(Usuario::getNome)
                    .toList());
            dto.setUsuariosAlertaIds(alerta.getUsuariosAlerta().stream()
                    .map(Usuario::getId)
                    .toList());
        }

        return dto;
    }
}