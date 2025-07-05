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
        
        try {  // Busca todos os itens-produto não inspecionados
           
            List<ItemProduto> itensNaoInspecionados = itemProdutoRepository.findByInspecionadoFalse();
            log.info("Encontrados {} itens não inspecionados", itensNaoInspecionados.size());

            LocalDate hoje = LocalDate.now();
            LocalDate amanha = hoje.plusDays(1);
            LocalDate ontem = hoje.minusDays(1);

            int alertasCriados = 0;

            for (ItemProduto item : itensNaoInspecionados) {
                LocalDate dataVencimento = item.getDataVencimento().toLocalDate();
                
                // Verificar se deve criar alerta
                TipoAlerta tipoAlerta = null;
                
                if (dataVencimento.isEqual(ontem)) {
                    tipoAlerta = TipoAlerta.VENCIMENTO_ATRASO;
                } else if (dataVencimento.isEqual(hoje)) {
                    tipoAlerta = TipoAlerta.VENCIMENTO_HOJE;
                } else if (dataVencimento.isEqual(amanha)) {
                    tipoAlerta = TipoAlerta.VENCIMENTO_AMANHA;
                }

                if (tipoAlerta != null) { // Verifica se já existe alerta não excluído para este item e tipo
                    
                    boolean alertaJaExiste = alertaRepository.existsByItemProdutoAndTipoAndExcluidoFalse(item, tipoAlerta);
                    
                    if (!alertaJaExiste) {
                        criarAlertaAutomatico(item, tipoAlerta);
                        alertasCriados++;
                    }
                }
            }

            log.info("Verificação concluída. {} novos alertas criados", alertasCriados);
            
        } catch (Exception e) {
            log.error("Erro durante verificação de vencimentos: {}", e.getMessage(), e);
        }
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

            // Definir título e descrição baseado no tipo
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
                    alerta.setDescricao(String.format("O item '%s' (Lote: %s) venceu ontem (%s). Remova do estoque imediatamente!", 
                        produtoNome, itemProduto.getLote(), 
                        itemProduto.getDataVencimento().toLocalDate().toString()));
                    break;
            }

            // Adicionar todos os usuários do sistema para receber o alerta
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            Set<Usuario> usuariosAlerta = new HashSet<>();
            for (Usuario usuario : todosUsuarios) {
                usuariosAlerta.add(usuario);
            }
            alerta.setUsuariosAlerta(usuariosAlerta);

            // Salvar o alerta
            alertaRepository.save(alerta);
            
            // Criar notificações individuais para cada usuário
            notificacaoService.criarNotificacoesParaAlerta(alerta);
            
            log.info("Alerta automático criado: {} para item {} (Lote: {})", 
                tipoAlerta, produtoNome, itemProduto.getLote());
                
        } catch (Exception e) {
            log.error("Erro ao criar alerta automático para item {}: {}", 
                itemProduto.getId(), e.getMessage(), e);
        }
    }

    /**
     * Limpar alertas antigos resolvidos (executado diariamente às 2h da manhã)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void limparAlertasAntigos() {
        log.info("=== Iniciando limpeza de alertas antigos ===");
        
        try {
            // Excluir logicamente alertas de itens que foram inspecionados
            List<Alerta> alertasDeItensInspecionados = alertaRepository
                .findByItemProdutoInspecionadoTrueAndExcluidoFalse();
            
            int alertasExcluidos = 0;
            for (Alerta alerta : alertasDeItensInspecionados) {
                alerta.setExcluido(true);
                alertaRepository.save(alerta);
                alertasExcluidos++;
            }
            
            log.info("Limpeza concluída. {} alertas excluídos logicamente (itens inspecionados)", alertasExcluidos);
            
        } catch (Exception e) {
            log.error("Erro durante limpeza de alertas antigos: {}", e.getMessage(), e);
        }
    }
}
