package br.com.smartvalidity.model.mapper;

import br.com.smartvalidity.model.dto.NotificacaoDTO;
import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Usuario;


public final class NotificacaoMapper {

    private NotificacaoMapper() {
    }


    public static NotificacaoDTO.Listagem toListagemDTO(Notificacao notificacao) {
        if (notificacao == null || notificacao.getAlerta() == null) {
            return null;
        }

        NotificacaoDTO.Listagem dto = new NotificacaoDTO.Listagem();
        
        dto.setNotificacaoId(notificacao.getId());
        dto.setLida(notificacao.getLida());
        dto.setDataCriacaoNotificacao(notificacao.getDataHoraCriacao());
        dto.setDataHoraLeitura(notificacao.getDataHoraLeitura());
        var alerta = notificacao.getAlerta();
        dto.setAlertaId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setTipo(alerta.getTipo());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
        dto.setDataCriacaoAlerta(alerta.getDataHoraCriacao());


        if (alerta.getItemProduto() != null) {
            String nomeProduto = alerta.getItemProduto().getProduto() != null
                    ? alerta.getItemProduto().getProduto().getDescricao()
                    : null;
            if (nomeProduto != null) {
                dto.setProdutosAlerta(java.util.List.of(nomeProduto));
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