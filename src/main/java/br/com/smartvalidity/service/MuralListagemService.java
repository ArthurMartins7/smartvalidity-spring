package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralFiltroDTO;
import br.com.smartvalidity.model.dto.MuralListagemDTO;
import br.com.smartvalidity.model.entity.ItemProduto;

@Service
public class MuralListagemService {

    @Autowired
    private ItemProdutoService itemProdutoService;

    /**
     * Busca os itens próximos a vencer (até 15 dias)
     */
    public List<MuralListagemDTO> getProximosVencer() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itens.stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite);
                })
                .map(this::mapToDTO) //TODO: Pesquisar sobre Method Reference
                .collect(Collectors.toList());
    }

    /**
     * Busca os itens que vencem hoje
     */
    public List<MuralListagemDTO> getVencemHoje() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        
        return itens.stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.toLocalDate().isEqual(hoje.toLocalDate());
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca os itens já vencidos
     */
    public List<MuralListagemDTO> getVencidos() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        
        return itens.stream()
                .filter(item -> item.getDataVencimento().isBefore(hoje))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca filtrada de itens
     * @param filtro Objeto com os parâmetros de filtro
     * @return Lista de itens filtrados
     */
    public List<MuralListagemDTO> buscarComFiltro(MuralFiltroDTO filtro) {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        List<MuralListagemDTO> dtos = itens.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        // Aplicar filtros
        List<MuralListagemDTO> itensFiltrados = aplicarFiltros(dtos, filtro);
        
        // Aplicar ordenação
        List<MuralListagemDTO> itensOrdenados = ordenarItens(itensFiltrados, filtro.getSortBy(), filtro.getSortDirection());
        
        // Aplicar paginação se necessário
        if (filtro.temPaginacao()) {
            int inicio = (filtro.getPagina() - 1) * filtro.getLimite();
            
            // Garantir que o índice inicial não seja negativo
            inicio = Math.max(0, inicio);
            
            // Se o índice inicial for maior que o tamanho da lista, retornar lista vazia
            if (inicio >= itensOrdenados.size()) {
                return new ArrayList<>();
            }
            
            // Calcular o índice final garantindo que não ultrapasse o tamanho da lista
            int fim = Math.min(inicio + filtro.getLimite(), itensOrdenados.size());
            
            return itensOrdenados.subList(inicio, fim);
        }
        
        return itensOrdenados;
    }
    
    /**
     * Aplica filtros aos itens
     */
    private List<MuralListagemDTO> aplicarFiltros(List<MuralListagemDTO> itens, MuralFiltroDTO filtro) {
        if (filtro == null) {
            return itens;
        }
        
        List<MuralListagemDTO> resultado = new ArrayList<>(itens);
        
        // Aplicar filtro de busca textual
        if (StringUtils.hasText(filtro.getSearchTerm())) {
            resultado = aplicarFiltroBusca(resultado, filtro.getSearchTerm());
        }
        
        // Aplicar filtros de texto (marca, corredor, categoria, fornecedor, lote)
        resultado = aplicarFiltrosTexto(resultado, filtro);
        
        // Aplicar filtros de data
        resultado = aplicarFiltrosData(resultado, filtro);
        
        // Aplicar filtro de inspeção
        if (filtro.getInspecionado() != null) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getInspecionado().equals(item.getInspecionado()))
                    .collect(Collectors.toList());
        }
        
        // Aplicar filtro de status
        if (StringUtils.hasText(filtro.getStatus())) {
            LocalDateTime hoje = LocalDateTime.now();
            LocalDateTime limite = hoje.plusDays(15);
            
            resultado = resultado.stream()
                    .filter(item -> {
                        LocalDateTime vencimento = item.getDataValidade();
                        
                        switch (filtro.getStatus()) {
                            case "proximo":
                                // Filtrar produtos próximos a vencer (com data futura, mas dentro de 15 dias)
                                return vencimento.isAfter(hoje) && 
                                      !vencimento.toLocalDate().isEqual(hoje.toLocalDate()) &&
                                      vencimento.isBefore(limite);
                            case "hoje":
                                // Filtrar produtos que vencem hoje exatamente
                                return vencimento.toLocalDate().isEqual(hoje.toLocalDate());
                            case "vencido":
                                // Filtrar produtos vencidos (data anterior a hoje)
                                return vencimento.isBefore(hoje);
                            default:
                                // Caso padrão, usa o status
                                return filtro.getStatus().equals(item.getStatus());
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        return resultado;
    }
    
    /**
     * Aplica filtros de texto (marca, corredor, categoria, fornecedor, lote)
     */
    private List<MuralListagemDTO> aplicarFiltrosTexto(List<MuralListagemDTO> itens, MuralFiltroDTO filtro) {
        List<MuralListagemDTO> resultado = new ArrayList<>(itens);
        
        if (StringUtils.hasText(filtro.getMarca())) {
            resultado = resultado.stream()
                    .filter(item -> item.getProduto() != null && 
                            filtro.getMarca().equals(item.getProduto().getMarca()))
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getCorredor())) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getCorredor().equals(item.getCorredor()))
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getCategoria())) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getCategoria().equals(item.getCategoria()))
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getFornecedor())) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getFornecedor().equals(item.getFornecedor()))
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getLote())) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getLote().equals(item.getLote()))
                    .collect(Collectors.toList());
        }
        
        return resultado;
    }
    
    /**
     * Aplica filtros de data (dataVencimento, dataFabricacao, dataRecebimento)
     */
    private List<MuralListagemDTO> aplicarFiltrosData(List<MuralListagemDTO> itens, MuralFiltroDTO filtro) {
        List<MuralListagemDTO> resultado = new ArrayList<>(itens);
        
        // Filtro de data de vencimento
        if (filtro.getDataVencimentoInicio() != null || filtro.getDataVencimentoFim() != null) {
            resultado = resultado.stream()
                    .filter(item -> {
                        LocalDateTime dataVencimento = item.getDataValidade();
                        boolean aposInicio = filtro.getDataVencimentoInicio() == null || 
                                dataVencimento.isAfter(filtro.getDataVencimentoInicio()) || 
                                dataVencimento.isEqual(filtro.getDataVencimentoInicio());
                        boolean antesFim = filtro.getDataVencimentoFim() == null || 
                                dataVencimento.isBefore(filtro.getDataVencimentoFim()) || 
                                dataVencimento.isEqual(filtro.getDataVencimentoFim());
                        return aposInicio && antesFim;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filtro de data de fabricação
        if (filtro.getDataFabricacaoInicio() != null || filtro.getDataFabricacaoFim() != null) {
            resultado = resultado.stream()
                    .filter(item -> {
                        LocalDateTime dataFabricacao = item.getDataFabricacao();
                        if (dataFabricacao == null) {
                            return false;
                        }
                        boolean aposInicio = filtro.getDataFabricacaoInicio() == null || 
                                dataFabricacao.isAfter(filtro.getDataFabricacaoInicio()) || 
                                dataFabricacao.isEqual(filtro.getDataFabricacaoInicio());
                        boolean antesFim = filtro.getDataFabricacaoFim() == null || 
                                dataFabricacao.isBefore(filtro.getDataFabricacaoFim()) || 
                                dataFabricacao.isEqual(filtro.getDataFabricacaoFim());
                        return aposInicio && antesFim;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filtro de data de recebimento
        if (filtro.getDataRecebimentoInicio() != null || filtro.getDataRecebimentoFim() != null) {
            resultado = resultado.stream()
                    .filter(item -> {
                        LocalDateTime dataRecebimento = item.getDataRecebimento();
                        if (dataRecebimento == null) {
                            return false;
                        }
                        boolean aposInicio = filtro.getDataRecebimentoInicio() == null || 
                                dataRecebimento.isAfter(filtro.getDataRecebimentoInicio()) || 
                                dataRecebimento.isEqual(filtro.getDataRecebimentoInicio());
                        boolean antesFim = filtro.getDataRecebimentoFim() == null || 
                                dataRecebimento.isBefore(filtro.getDataRecebimentoFim()) || 
                                dataRecebimento.isEqual(filtro.getDataRecebimentoFim());
                        return aposInicio && antesFim;
                    })
                    .collect(Collectors.toList());
        }
        
        return resultado;
    }
    
    /**
     * Aplica o filtro de busca textual em múltiplos campos
     */
    private List<MuralListagemDTO> aplicarFiltroBusca(List<MuralListagemDTO> itens, String termo) {
        if (!StringUtils.hasText(termo)) {
            return itens;
        }
        
        String termoBusca = termo.toLowerCase();
        return itens.stream()
                .filter(item -> 
                    // Descrição do produto (usada como nome também)
                    (item.getProduto() != null && item.getProduto().getDescricao() != null && 
                            item.getProduto().getDescricao().toLowerCase().contains(termoBusca)) ||
                    // Código de barras
                    (item.getProduto() != null && item.getProduto().getCodigoBarras() != null && 
                            item.getProduto().getCodigoBarras().toLowerCase().contains(termoBusca)) ||
                    // Marca
                    (item.getProduto() != null && item.getProduto().getMarca() != null && 
                            item.getProduto().getMarca().toLowerCase().contains(termoBusca)) ||
                    // Lote
                    (item.getLote() != null && item.getLote().toLowerCase().contains(termoBusca))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Ordena os itens pelo campo especificado
     */
    private List<MuralListagemDTO> ordenarItens(List<MuralListagemDTO> itens, String campo, String direcao) {
        if (!StringUtils.hasText(campo)) {
            return itens; // Sem ordenação
        }
        
        boolean ascendente = !"desc".equalsIgnoreCase(direcao);
        
        Comparator<MuralListagemDTO> comparator = null;
        
        switch (campo.toLowerCase()) {
            case "nome":
                comparator = Comparator.comparing(item -> 
                    item.getProduto() != null ? 
                    (item.getProduto().getDescricao() != null ? item.getProduto().getDescricao().toLowerCase() : "") : "");
                break;
            case "descricao":
                comparator = Comparator.comparing(item -> 
                    item.getProduto() != null ? 
                    (item.getProduto().getDescricao() != null ? item.getProduto().getDescricao().toLowerCase() : "") : "");
                break;
            case "marca":
                comparator = Comparator.comparing(item -> 
                    item.getProduto() != null ? 
                    (item.getProduto().getMarca() != null ? item.getProduto().getMarca().toLowerCase() : "") : "");
                break;
            case "categoria":
                comparator = Comparator.comparing(item -> 
                    item.getCategoria() != null ? item.getCategoria().toLowerCase() : "");
                break;
            case "corredor":
                comparator = Comparator.comparing(item -> 
                    item.getCorredor() != null ? item.getCorredor().toLowerCase() : "");
                break;
            case "fornecedor":
                comparator = Comparator.comparing(item -> 
                    item.getFornecedor() != null ? item.getFornecedor().toLowerCase() : "");
                break;
            case "dataVencimento":
                comparator = Comparator.comparing(item -> item.getDataValidade());
                break;
            case "status":
                comparator = Comparator.comparing(item -> item.getStatus());
                break;
            default:
                // Por padrão, ordena por descrição (já que não temos nome)
                comparator = Comparator.comparing(item -> 
                    item.getProduto() != null ? 
                    (item.getProduto().getDescricao() != null ? item.getProduto().getDescricao().toLowerCase() : "") : "");
        }
        
        // Inverte a ordenação se for descendente
        if (!ascendente) {
            comparator = comparator.reversed();
        }
        
        return itens.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém todas as marcas distintas dos produtos disponíveis
     */
    public List<String> getMarcasDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getProduto() != null && StringUtils.hasText(item.getProduto().getMarca()))
                .map(item -> item.getProduto().getMarca())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém todos os corredores distintos dos produtos disponíveis
     */
    public List<String> getCorredoresDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getProduto() != null && 
                        item.getProduto().getCategoria() != null && 
                        item.getProduto().getCategoria().getCorredor() != null && 
                        StringUtils.hasText(item.getProduto().getCategoria().getCorredor().getNome()))
                .map(item -> item.getProduto().getCategoria().getCorredor().getNome())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém todas as categorias distintas dos produtos disponíveis
     */
    public List<String> getCategoriasDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getProduto() != null && 
                        item.getProduto().getCategoria() != null && 
                        StringUtils.hasText(item.getProduto().getCategoria().getNome()))
                .map(item -> item.getProduto().getCategoria().getNome())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém todos os fornecedores distintos dos produtos disponíveis
     */
    public List<String> getFornecedoresDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getProduto() != null && 
                        item.getProduto().getFornecedores() != null && 
                        !item.getProduto().getFornecedores().isEmpty() &&
                        StringUtils.hasText(item.getProduto().getFornecedores().get(0).getNome()))
                .map(item -> item.getProduto().getFornecedores().get(0).getNome())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém todos os lotes distintos dos produtos disponíveis
     */
    public List<String> getLotesDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> StringUtils.hasText(item.getLote()))
                .map(item -> item.getLote())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Mapeia um ItemProduto para MuralListagemDTO
     */
    private MuralListagemDTO mapToDTO(ItemProduto item) {
        String status = determinarStatus(item.getDataVencimento());
        
        MuralListagemDTO.ProdutoDTO produtoDTO = MuralListagemDTO.ProdutoDTO.builder()
                .id(item.getProduto() != null ? item.getProduto().getId() : "")
                .nome(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .descricao(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .codigoBarras(item.getProduto() != null ? item.getProduto().getCodigoBarras() : "")
                .marca(item.getProduto() != null ? item.getProduto().getMarca() : "")
                .unidadeMedida(item.getProduto() != null ? item.getProduto().getUnidadeMedida() : "")
                .build();
        
        String categoria = item.getProduto() != null && item.getProduto().getCategoria() != null ?
                item.getProduto().getCategoria().getNome() : "";

        String corredor = item.getProduto() != null && item.getProduto().getCategoria() != null &&
                item.getProduto().getCategoria().getCorredor() != null ?
                item.getProduto().getCategoria().getCorredor().getNome() : "";

        String fornecedor = item.getProduto() != null && item.getProduto().getFornecedores() != null &&
                !item.getProduto().getFornecedores().isEmpty() ?
                item.getProduto().getFornecedores().get(0).getNome() : "";

        return MuralListagemDTO.builder()
                .id(item.getId())
                .itemProduto(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .produto(produtoDTO)
                .categoria(categoria)
                .corredor(corredor)
                .fornecedor(fornecedor)
                .dataValidade(item.getDataVencimento())
                .dataFabricacao(item.getDataFabricacao())
                .dataRecebimento(item.getDataRecebimento())
                .lote(item.getLote())
                .precoVenda(item.getPrecoVenda())
                .status(status)
                .inspecionado(item.getInspecionado())
                .motivoInspecao(item.getMotivoInspecao())
                .usuarioInspecao(item.getUsuarioInspecao())
                .dataHoraInspecao(item.getDataHoraInspecao())
                .build();
    }

    /**
     * Determina o status do item com base na data de validade
     */
    private String determinarStatus(LocalDateTime dataVencimento) {
        LocalDateTime hoje = LocalDateTime.now();
        if (dataVencimento.isBefore(hoje)) {
            return "vencido";
        } else if (dataVencimento.toLocalDate().isEqual(hoje.toLocalDate())) {
            return "hoje";
        } else {
            return "proximo";
        }
    }
    
    /**
     * Método base para marcar um item como inspecionado
     * @param id ID do item a ser marcado
     * @param motivo Motivo da inspeção
     * @param usuarioInspecao Nome do usuário que realizou a inspeção
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    private MuralListagemDTO marcarItemInspecionado(String id, String motivo, String usuarioInspecao) throws SmartValidityException {
        try {
            ItemProduto item = itemProdutoService.buscarPorId(id);
            
            // Validação do motivo
            if (motivo == null || motivo.trim().isEmpty()) {
                throw new SmartValidityException("O motivo da inspeção é obrigatório");
            }
            
            // Determinar o nome do usuário que está realizando a inspeção
            String nomeUsuario = usuarioInspecao;
            if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
                // Tentar obter o usuário autenticado através de outra abordagem como fallback
                try {
                    nomeUsuario = System.getProperty("user.name");
                } catch (Exception e) {
                    // Em caso de erro, usar um valor padrão
                    nomeUsuario = "Sistema";
                }
            }
            
            // Marcar o item como inspecionado
            item.setInspecionado(true);
            item.setMotivoInspecao(motivo);
            item.setUsuarioInspecao(nomeUsuario);
            item.setDataHoraInspecao(LocalDateTime.now());
            
            // Salvar o item com tratamento de exceções
            ItemProduto itemSalvo = itemProdutoService.salvarItemInspecionado(item);
            return mapToDTO(itemSalvo);
        } catch (Exception e) {
            if (e instanceof SmartValidityException) {
                throw (SmartValidityException) e;
            }
            // Logar o erro para diagnóstico
            System.err.println("Erro ao marcar item como inspecionado: " + e.getMessage());
            e.printStackTrace();
            throw new SmartValidityException("Erro ao marcar item como inspecionado: " + e.getMessage());
        }
    }
    
    /**
     * Marca um item como inspecionado
     * @param id ID do item a ser marcado
     * @param motivo Motivo da inspeção
     * @param usuarioInspecao Nome do usuário que realizou a inspeção
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id, String motivo, String usuarioInspecao) throws SmartValidityException {
        return marcarItemInspecionado(id, motivo, usuarioInspecao);
    }
    
    /**
     * Marca um item como inspecionado (método de compatibilidade)
     * @param id ID do item a ser marcado
     * @param motivo Motivo da inspeção
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id, String motivo) throws SmartValidityException {
        return marcarItemInspecionado(id, motivo, null);
    }
    
    /**
     * Marca um item como inspecionado (método de compatibilidade)
     * @param id ID do item a ser marcado
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id) throws SmartValidityException {
        return marcarItemInspecionado(id, null, null);
    }
    
    /**
     * Método base para marcar vários itens como inspecionados
     * @param ids Lista de IDs dos itens a serem marcados
     * @param motivo Motivo da inspeção
     * @param usuarioInspecao Nome do usuário que realizou a inspeção
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    private List<MuralListagemDTO> marcarVariosItensInspecionados(List<String> ids, String motivo, String usuarioInspecao) throws SmartValidityException {
        // Validação do motivo
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new SmartValidityException("O motivo da inspeção é obrigatório");
        }
        
        // Determinar o nome do usuário que está realizando a inspeção
        String nomeUsuario = usuarioInspecao;
        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            // Tentar obter o usuário autenticado através de outra abordagem como fallback
            try {
                nomeUsuario = System.getProperty("user.name");
            } catch (Exception e) {
                // Em caso de erro, usar um valor padrão
                nomeUsuario = "Sistema";
            }
        }
        
        // Data e hora da inspeção (mesma para todos os itens do lote)
        LocalDateTime dataHoraInspecao = LocalDateTime.now();
        
        List<MuralListagemDTO> itensAtualizados = new ArrayList<>();
        
        for (String id : ids) {
            try {
                ItemProduto item = itemProdutoService.buscarPorId(id);
                item.setInspecionado(true);
                item.setMotivoInspecao(motivo);
                item.setUsuarioInspecao(nomeUsuario);
                item.setDataHoraInspecao(dataHoraInspecao);
                
                ItemProduto itemSalvo = itemProdutoService.salvarItemInspecionado(item);
                itensAtualizados.add(mapToDTO(itemSalvo));
            } catch (Exception e) {
                // Loga o erro mas continua processando os outros IDs
                System.err.println("Erro ao marcar item " + id + " como inspecionado: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (itensAtualizados.isEmpty() && !ids.isEmpty()) {
            throw new SmartValidityException("Não foi possível marcar nenhum dos itens como inspecionado");
        }
        
        return itensAtualizados;
    }
    
    /**
     * Marca vários itens como inspecionados
     * @param ids Lista de IDs dos itens a serem marcados
     * @param motivo Motivo da inspeção
     * @param usuarioInspecao Nome do usuário que realizou a inspeção
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    public List<MuralListagemDTO> marcarVariosInspecionados(List<String> ids, String motivo, String usuarioInspecao) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, motivo, usuarioInspecao);
    }
    
    /**
     * Marca vários itens como inspecionados (método de compatibilidade)
     * @param ids Lista de IDs dos itens a serem marcados
     * @param motivo Motivo da inspeção
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    public List<MuralListagemDTO> marcarVariosInspecionados(List<String> ids, String motivo) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, motivo, null);
    }
    
    /**
     * Marca vários itens como inspecionados (método de compatibilidade)
     * @param ids Lista de IDs dos itens a serem marcados
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    public List<MuralListagemDTO> marcarVariosInspecionados(List<String> ids) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, null, null);
    }
    
    /**
     * Busca um item específico por ID
     * @param id ID do item a ser buscado
     * @return O item encontrado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO getItemById(String id) throws SmartValidityException {
        ItemProduto item = itemProdutoService.buscarPorId(id);
        return mapToDTO(item);
    }

    /**
     * Conta o número total de páginas com base no filtro e no limite de itens por página
     * @param filtro Objeto com os parâmetros de filtro
     * @return Número total de páginas
     */
    public int contarPaginas(MuralFiltroDTO filtro) {
        if (filtro != null && filtro.temPaginacao()) {
            long totalRegistros = contarTotalRegistros(filtro);
            return (int) Math.ceil((double) totalRegistros / filtro.getLimite());
        }
        // Se não tiver paginação, tudo cabe em uma página
        return 1;
    }
    
    /**
     * Conta o número total de registros que atendem aos critérios de filtro
     * @param filtro Objeto com os parâmetros de filtro
     * @return Número total de registros
     */
    public long contarTotalRegistros(MuralFiltroDTO filtro) {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        List<MuralListagemDTO> dtos = itens.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        // Aplicar filtros
        List<MuralListagemDTO> itensFiltrados = aplicarFiltros(dtos, filtro);
        
        return itensFiltrados.size();
    }
} 