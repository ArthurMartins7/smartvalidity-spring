package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.auth.AuthorizationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.TipoAlerta;
import br.com.smartvalidity.model.repository.AlertaRepository;
import br.com.smartvalidity.model.seletor.AlertaSeletor;

@Service
public class AlertaService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaService.class);

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private ItemProdutoService itemProdutoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private AuthorizationService authorizationService;

    // CRUD Básico

    public List<AlertaDTO.Listagem> buscarComSeletor(AlertaSeletor seletor) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        List<Alerta> alertas;

        if (seletor.temPaginacao()) {
            int numeroPagina = seletor.getPagina();
            int tamanhoPagina = seletor.getLimite();
            PageRequest pagina = PageRequest.of(numeroPagina - 1, tamanhoPagina);
            alertas = alertaRepository.findAll(seletor, pagina).getContent();
        } else {
            alertas = alertaRepository.findAll(seletor);
        }

        return alertas.stream()
                .map(this::converterParaListagem)
                .collect(Collectors.toList());
    }

    public List<AlertaDTO.Listagem> listarTodos() throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        return alertaRepository.findByAtivoTrue().stream()
                .map(this::converterParaListagem)
                .collect(Collectors.toList());
    }

    public AlertaDTO.Listagem buscarPorId(Integer id) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado"));
        return converterParaListagem(alerta);
    }

    @Transactional
    public AlertaDTO.Listagem salvar(AlertaDTO.Cadastro cadastroDTO, String usuarioCriadorId) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        validarCadastro(cadastroDTO);

        Alerta alerta = new Alerta();
        alerta.setTitulo(cadastroDTO.getTitulo());
        alerta.setDescricao(cadastroDTO.getDescricao());
        alerta.setDataHoraDisparo(cadastroDTO.getDataHoraDisparo());
        alerta.setTipo(cadastroDTO.getTipo());
        alerta.setDiasAntecedencia(cadastroDTO.getDiasAntecedencia());
        alerta.setRecorrente(cadastroDTO.getRecorrente());
        alerta.setConfiguracaoRecorrencia(cadastroDTO.getConfiguracaoRecorrencia());

        // Definir usuário criador (apenas para alertas PERSONALIZADO)
        if (cadastroDTO.getTipo() == TipoAlerta.PERSONALIZADO && usuarioCriadorId != null) {
            Usuario criador = usuarioService.buscarPorId(usuarioCriadorId);
            alerta.setUsuarioCriador(criador);
        }

        // Definir usuários destinatários
        if (cadastroDTO.getUsuariosIds() != null && !cadastroDTO.getUsuariosIds().isEmpty()) {
            Set<Usuario> usuarios = new HashSet<>();
            for (String usuarioId : cadastroDTO.getUsuariosIds()) {
                Usuario usuario = usuarioService.buscarPorId(usuarioId);
                usuarios.add(usuario);
            }
            alerta.setUsuariosAlerta(usuarios);
        }

        // Definir produtos relacionados (opcional)
        if (cadastroDTO.getProdutosIds() != null && !cadastroDTO.getProdutosIds().isEmpty()) {
            Set<Produto> produtos = new HashSet<>();
            for (String produtoId : cadastroDTO.getProdutosIds()) {
                Produto produto = produtoService.buscarPorId(produtoId);
                produtos.add(produto);
            }
            alerta.setProdutosAlerta(produtos);
        }

        Alerta alertaSalvo = alertaRepository.save(alerta);
        return converterParaListagem(alertaSalvo);
    }

    @Transactional
    public AlertaDTO.Listagem atualizar(Integer id, AlertaDTO.Edicao edicaoDTO) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado"));

        // Não permite alterar alertas automáticos
        if (alerta.getTipo() != TipoAlerta.PERSONALIZADO) {
            throw new SmartValidityException("Alertas automáticos não podem ser editados");
        }

        alerta.setTitulo(edicaoDTO.getTitulo());
        alerta.setDescricao(edicaoDTO.getDescricao());
        alerta.setDataHoraDisparo(edicaoDTO.getDataHoraDisparo());
        alerta.setDiasAntecedencia(edicaoDTO.getDiasAntecedencia());
        alerta.setAtivo(edicaoDTO.getAtivo());
        alerta.setRecorrente(edicaoDTO.getRecorrente());
        alerta.setConfiguracaoRecorrencia(edicaoDTO.getConfiguracaoRecorrencia());

        Alerta alertaAtualizado = alertaRepository.save(alerta);
        return converterParaListagem(alertaAtualizado);
    }

    @Transactional
    public void excluir(Integer id) throws SmartValidityException {
        authorizationService.verificarPerfilAcesso();
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado"));

        // Não permite excluir alertas automáticos, apenas desativar
        if (alerta.getTipo() != TipoAlerta.PERSONALIZADO) {
            alerta.setAtivo(false);
            alertaRepository.save(alerta);
        } else {
            alertaRepository.delete(alerta);
        }
    }

    // Geração Automática

    @Scheduled(cron = "0 0 8 * * *") // Todo dia às 8h
    @Transactional
    public void gerarAlertasAutomaticos() {
        logger.info("Iniciando geração de alertas automáticos...");

        try {
            gerarAlertasVencimentoHoje();
            gerarAlertasVencimentoAmanha();
            gerarAlertasVencimentoAtraso();
            
            logger.info("Geração de alertas automáticos concluída");
        } catch (Exception e) {
            logger.error("Erro na geração de alertas automáticos: {}", e.getMessage(), e);
        }
    }

    private void gerarAlertasVencimentoHoje() {
        LocalDateTime hoje = LocalDateTime.now();
        List<ItemProduto> produtosVendemHoje = itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getDataVencimento().toLocalDate().isEqual(hoje.toLocalDate()))
                .collect(Collectors.toList());

        if (!produtosVendemHoje.isEmpty()) {
            criarAlertaAutomatico(
                TipoAlerta.VENCIMENTO_HOJE,
                "Produtos vencem hoje",
                String.format("%d produto(s) vencem hoje e precisam de atenção", produtosVendemHoje.size()),
                produtosVendemHoje
            );
            logger.info("Criado alerta para {} produtos que vencem hoje", produtosVendemHoje.size());
        }
    }

    private void gerarAlertasVencimentoAmanha() {
        LocalDateTime amanha = LocalDateTime.now().plusDays(1);
        List<ItemProduto> produtosVencemAmanha = itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getDataVencimento().toLocalDate().isEqual(amanha.toLocalDate()))
                .collect(Collectors.toList());

        if (!produtosVencemAmanha.isEmpty()) {
            criarAlertaAutomatico(
                TipoAlerta.VENCIMENTO_AMANHA,
                "Produtos vencem amanhã",
                String.format("%d produto(s) vencem amanhã - planeje ações preventivas", produtosVencemAmanha.size()),
                produtosVencemAmanha
            );
            logger.info("Criado alerta para {} produtos que vencem amanhã", produtosVencemAmanha.size());
        }
    }

    private void gerarAlertasVencimentoAtraso() {
        LocalDateTime hoje = LocalDateTime.now();
        List<ItemProduto> produtosVencidosNaoInspecionados = itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getDataVencimento().isBefore(hoje.minusDays(1)) && // Vencido há 1+ dias
                               (item.getInspecionado() == null || !item.getInspecionado())) // Não inspecionado
                .collect(Collectors.toList());

        if (!produtosVencidosNaoInspecionados.isEmpty()) {
            criarAlertaAutomatico(
                TipoAlerta.VENCIMENTO_ATRASO,
                "Produtos vencidos não inspecionados",
                String.format("%d produto(s) vencidos há mais de 1 dia precisam ser inspecionados", 
                    produtosVencidosNaoInspecionados.size()),
                produtosVencidosNaoInspecionados
            );
            logger.info("Criado alerta para {} produtos vencidos não inspecionados", 
                produtosVencidosNaoInspecionados.size());
        }
    }

    private void criarAlertaAutomatico(TipoAlerta tipo, String titulo, String descricao, List<ItemProduto> itens) {
        // Verificar se já existe alerta do mesmo tipo para hoje
        boolean jaExisteHoje = alertaRepository.findAlertasVencimentoParaEnviar(Arrays.asList(tipo))
                .stream()
                .anyMatch(a -> a.getDataEnvio() != null && 
                              a.getDataEnvio().toLocalDate().isEqual(LocalDateTime.now().toLocalDate()));

        if (jaExisteHoje) {
            logger.debug("Alerta do tipo {} já foi enviado hoje", tipo);
            return;
        }

        try {
            Alerta alerta = new Alerta();
            alerta.setTipo(tipo);
            alerta.setTitulo(titulo);
            alerta.setDescricao(descricao);
            alerta.setDataHoraDisparo(LocalDateTime.now());
            alerta.setDataEnvio(LocalDateTime.now());

            // Buscar todos os usuários ativos para alertas automáticos
            List<Usuario> todosUsuarios = usuarioService.listarTodos();
            alerta.setUsuariosAlerta(new HashSet<>(todosUsuarios));

            // Relacionar produtos únicos
            Set<Produto> produtosUnicos = itens.stream()
                    .map(ItemProduto::getProduto)
                    .collect(Collectors.toSet());
            alerta.setProdutosAlerta(produtosUnicos);

            alertaRepository.save(alerta);
            logger.debug("Alerta automático criado: {}", titulo);

        } catch (Exception e) {
            logger.error("Erro ao criar alerta automático do tipo {}: {}", tipo, e.getMessage());
        }
    }

    // Conversões

    private AlertaDTO.Listagem converterParaListagem(Alerta alerta) {
        AlertaDTO.Listagem dto = new AlertaDTO.Listagem();
        dto.setId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
        dto.setTipo(alerta.getTipo());
        dto.setDiasAntecedencia(alerta.getDiasAntecedencia());
        dto.setAtivo(alerta.getAtivo());
        dto.setRecorrente(alerta.getRecorrente());
        dto.setConfiguracaoRecorrencia(alerta.getConfiguracaoRecorrencia());
        dto.setDataCriacao(alerta.getDataCriacao());
        dto.setDataEnvio(alerta.getDataEnvio());

        if (alerta.getUsuarioCriador() != null) {
            dto.setUsuarioCriador(alerta.getUsuarioCriador().getNome());
        }

        if (alerta.getUsuariosAlerta() != null) {
            dto.setUsuariosAlerta(alerta.getUsuariosAlerta().stream()
                    .map(Usuario::getNome)
                    .collect(Collectors.toList()));
        }

        if (alerta.getProdutosAlerta() != null) {
            dto.setProdutosAlerta(alerta.getProdutosAlerta().stream()
                    .map(Produto::getDescricao)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // Validações

    private void validarCadastro(AlertaDTO.Cadastro cadastroDTO) throws SmartValidityException {
        if (cadastroDTO.getTitulo() == null || cadastroDTO.getTitulo().trim().isEmpty()) {
            throw new SmartValidityException("Título é obrigatório");
        }

        if (cadastroDTO.getDescricao() == null || cadastroDTO.getDescricao().trim().isEmpty()) {
            throw new SmartValidityException("Descrição é obrigatória");
        }

        if (cadastroDTO.getDataHoraDisparo() == null) {
            throw new SmartValidityException("Data/hora de disparo é obrigatória");
        }

        if (cadastroDTO.getTipo() == null) {
            throw new SmartValidityException("Tipo de alerta é obrigatório");
        }

        // Validar recorrência
        if (cadastroDTO.getRecorrente() != null && cadastroDTO.getRecorrente() && 
            (cadastroDTO.getConfiguracaoRecorrencia() == null || cadastroDTO.getConfiguracaoRecorrencia().trim().isEmpty())) {
            throw new SmartValidityException("Configuração de recorrência é obrigatória para alertas recorrentes");
        }
    }
} 