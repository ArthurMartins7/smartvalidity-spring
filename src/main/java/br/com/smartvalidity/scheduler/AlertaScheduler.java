package br.com.smartvalidity.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.enums.TipoAlerta;
import br.com.smartvalidity.model.repository.AlertaRepository;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import br.com.smartvalidity.service.NotificacaoService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AlertaScheduler {

    @Autowired
    private ItemProdutoRepository itemProdutoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Scheduled(fixedRate = 30000) // 30 segundos
    @Transactional
    public void verificarVencimentosECriarAlertas() {
        log.info("=== Iniciando verificação de vencimentos ===");
        
        try {
            List<ItemProduto> itensNaoInspecionados = itemProdutoRepository.findByInspecionadoFalse();
            log.info("Encontrados {} itens não inspecionados", itensNaoInspecionados.size());

            LocalDate hoje = LocalDate.now();
            LocalDate amanha = hoje.plusDays(1);

            int alertasCriados = 0;
            int alertasAtualizados = 0;

            for (ItemProduto item : itensNaoInspecionados) {
                LocalDate dataVencimento = item.getDataVencimento().toLocalDate();
                
                // Determinar o tipo de alerta baseado na data
                TipoAlerta tipoAlerta = determinarTipoAlerta(dataVencimento, hoje);
                
                if (tipoAlerta != null) {
                    // Verificar se já existe um alerta para este item-produto
                    var alertaExistente = alertaRepository.findFirstByItemProdutoAndExcluidoFalse(item);
                    
                    if (alertaExistente.isPresent()) {
                        // Atualizar alerta existente se o tipo mudou
                        if (atualizarAlertaSeNecessario(alertaExistente.get(), tipoAlerta, item)) {
                            alertasAtualizados++;
                        }
                    } else {
                        // Criar novo alerta
                        criarAlertaAutomatico(item, tipoAlerta);
                        alertasCriados++;
                    }
                }
            }

            log.info("Verificação concluída. {} novos alertas criados, {} alertas atualizados", 
                alertasCriados, alertasAtualizados);
            
        } catch (Exception e) {
            log.error("Erro durante verificação de vencimentos: {}", e.getMessage(), e);
        }
    }

    /**
     * Determina o tipo de alerta baseado na data de vencimento
     */
    private TipoAlerta determinarTipoAlerta(LocalDate dataVencimento, LocalDate hoje) {
        if (dataVencimento.isBefore(hoje)) {
            return TipoAlerta.VENCIMENTO_ATRASO;
        } else if (dataVencimento.isEqual(hoje)) {
            return TipoAlerta.VENCIMENTO_HOJE;
        } else if (dataVencimento.isEqual(hoje.plusDays(1))) {
            return TipoAlerta.VENCIMENTO_AMANHA;
        }
        return null; // Não precisa de alerta ainda
    }

    /**
     * Atualiza um alerta existente se necessário
     * @return true se o alerta foi atualizado, false caso contrário
     */
    @Transactional
    private boolean atualizarAlertaSeNecessario(Alerta alerta, TipoAlerta novoTipo, ItemProduto itemProduto) {
        if (alerta.getTipo() != novoTipo) {
            // Tipo mudou, atualizar o alerta
            alerta.setTipo(novoTipo);
            atualizarTituloEDescricao(alerta, novoTipo, itemProduto);
            alerta.setDataHoraDisparo(LocalDateTime.now()); // Atualizar timestamp
            
            alertaRepository.save(alerta);
            
            log.info("Alerta atualizado: {} → {} para item {} (Lote: {})", 
                alerta.getTipo(), novoTipo, 
                itemProduto.getProduto() != null ? itemProduto.getProduto().getDescricao() : "Produto", 
                itemProduto.getLote());
            
            return true;
        }
        return false;
    }

    /**
     * Criar um alerta automático para um item-produto específico
     */
    @Transactional
    private void criarAlertaAutomatico(ItemProduto itemProduto, TipoAlerta tipoAlerta) {
        try {
            Alerta alerta = new Alerta();
            
            // Informações básicas do alerta
            alerta.setTipo(tipoAlerta);
            alerta.setItemProduto(itemProduto);
            alerta.setDataHoraDisparo(LocalDateTime.now());

            // Definir título e descrição usando método comum
            atualizarTituloEDescricao(alerta, tipoAlerta, itemProduto);

            // Adicionar apenas ASSINANTES e ADMINS para receber o alerta
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            Set<Usuario> usuariosAlerta = new HashSet<>();
            Set<Usuario> usuariosNotificacao = new HashSet<>();
            
            for (Usuario usuario : todosUsuarios) {
                // ASSINANTES e ADMINS recebem alertas e notificações
                if (usuario.getPerfilAcesso() == PerfilAcesso.ASSINANTE || 
                    usuario.getPerfilAcesso() == PerfilAcesso.ADMIN) {
                    usuariosAlerta.add(usuario);
                }
                // TODOS os usuários (incluindo OPERADORES) recebem notificações
                usuariosNotificacao.add(usuario);
            }
            
            alerta.setUsuariosAlerta(usuariosAlerta);

            // Salvar o alerta
            alertaRepository.save(alerta);
            
            // Criar notificações individuais para TODOS os usuários
            // Temporariamente substituir usuários do alerta para incluir todos
            Set<Usuario> usuariosOriginais = alerta.getUsuariosAlerta();
            alerta.setUsuariosAlerta(usuariosNotificacao);
            notificacaoService.criarNotificacoesParaAlerta(alerta);
            // Restaurar usuários originais do alerta
            alerta.setUsuariosAlerta(usuariosOriginais);
            
            String produtoNome = itemProduto.getProduto() != null ? 
                itemProduto.getProduto().getDescricao() : "Produto";
            log.info("Alerta automático criado: {} para item {} (Lote: {})", 
                tipoAlerta, produtoNome, itemProduto.getLote());
                
        } catch (Exception e) {
            log.error("Erro ao criar alerta automático para item {}: {}", 
                itemProduto.getId(), e.getMessage(), e);
        }
    }

    /**
     * Atualiza o título e descrição de um alerta com base no tipo de alerta
     */
    private void atualizarTituloEDescricao(Alerta alerta, TipoAlerta tipoAlerta, ItemProduto itemProduto) {
            String produtoNome = itemProduto.getProduto() != null ? 
                itemProduto.getProduto().getDescricao() : "Produto";
            
            switch (tipoAlerta) {
                case VENCIMENTO_AMANHA:
                    alerta.setTitulo("Produto vence amanhã");
                    alerta.setDescricao(String.format("O item '%s' (Lote: %s) vence amanhã (%s). Verifique o estoque!", 
                        produtoNome, itemProduto.getLote(), 
                        itemProduto.getDataVencimento().toLocalDate().toString()));
                    break;
                    
                case VENCIMENTO_HOJE:
                    alerta.setTitulo("Produto vence hoje");
                    alerta.setDescricao(String.format("O item '%s' (Lote: %s) vence HOJE (%s). Ação imediata necessária!", 
                        produtoNome, itemProduto.getLote(), 
                        itemProduto.getDataVencimento().toLocalDate().toString()));
                    break;
                    
                case VENCIMENTO_ATRASO:
                    alerta.setTitulo("Produto vencido");
                alerta.setDescricao(String.format("O item '%s' (Lote: %s) está vencido desde %s. Remova do estoque imediatamente!", 
                        produtoNome, itemProduto.getLote(), 
                        itemProduto.getDataVencimento().toLocalDate().toString()));
                    break;
        }
    }
}
