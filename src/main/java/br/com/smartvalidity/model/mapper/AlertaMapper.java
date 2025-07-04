package br.com.smartvalidity.model.mapper;

import java.util.List;

import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Usuario;

/**
 * Conversões entre a entidade {@link Alerta} e seus DTOs.
 * Implementação manual e enxuta para evitar dependências externas.
 */
public final class AlertaMapper {

    private AlertaMapper() {
    }

    /**
     * Converte a entidade {@link Alerta} para {@link AlertaDTO.Listagem}.
     */
    public static AlertaDTO.Listagem toListagemDTO(Alerta alerta) {
        if (alerta == null) {
            return null;
        }
        AlertaDTO.Listagem dto = new AlertaDTO.Listagem();
        dto.setId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setTipo(alerta.getTipo());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
        dto.setDataCriacao(alerta.getDataHoraCriacao());

        if (alerta.getItemProduto() != null) {
            String nomeProduto = alerta.getItemProduto().getProduto() != null
                    ? alerta.getItemProduto().getProduto().getDescricao()
                    : null;
            if (nomeProduto != null) {
                dto.setProdutosAlerta(List.of(nomeProduto));
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