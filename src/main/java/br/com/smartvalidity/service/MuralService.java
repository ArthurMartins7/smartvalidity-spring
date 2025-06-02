package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralDTO;
import br.com.smartvalidity.model.entity.ItemProduto;

@Service
public class MuralService {
    private static final Logger logger = LoggerFactory.getLogger(MuralService.class);

    @Autowired
    private ItemProdutoService itemProdutoService;

    @Autowired
    private ExcelService excelService;

    private static final List<String> MOTIVOS_INSPECAO_VALIDOS = Arrays.asList(
        "Avaria/Quebra",
        "Promoção",
        "Outro"
    );

    private void validarMotivoInspecao(String motivo, String motivoCustomizado) throws SmartValidityException {
        if (!StringUtils.hasText(motivo)) {
            throw new SmartValidityException("O motivo da inspeção é obrigatório");
        }

        if (!MOTIVOS_INSPECAO_VALIDOS.contains(motivo)) {
            throw new SmartValidityException("Motivo de inspeção inválido");
        }

        if ("Outro".equals(motivo) && !StringUtils.hasText(motivoCustomizado)) {
            throw new SmartValidityException("É necessário informar um motivo customizado quando selecionada a opção 'Outro'");
        }
    }


    private String obterMotivoFinal(String motivo, String motivoCustomizado) {
        return "Outro".equals(motivo) ? motivoCustomizado : motivo;
    }

    public List<String> getMotivosInspecaoValidos() {
        return MOTIVOS_INSPECAO_VALIDOS;
    }


    public List<MuralDTO.Listagem> getProximosVencer() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itens.stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite);
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MuralDTO.Listagem> getVencemHoje() {
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

    public List<MuralDTO.Listagem> getVencidos() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        
        return itens.stream()
                .filter(item -> item.getDataVencimento().isBefore(hoje))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MuralDTO.Listagem> buscarComFiltro(MuralDTO.Filtro filtro) {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        List<MuralDTO.Listagem> dtos = itens.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        List<MuralDTO.Listagem> itensFiltrados = aplicarFiltros(dtos, filtro);
        List<MuralDTO.Listagem> itensOrdenados = ordenarItens(itensFiltrados, filtro.getSortBy(), filtro.getSortDirection());
        
        if (filtro.temPaginacao()) {
            int inicio = (filtro.getPagina() - 1) * filtro.getLimite();
            inicio = Math.max(0, inicio);
            
            if (inicio >= itensOrdenados.size()) {
                return new ArrayList<>();
            }
            
            int fim = Math.min(inicio + filtro.getLimite(), itensOrdenados.size());
            return itensOrdenados.subList(inicio, fim);
        }
        
        return itensOrdenados;
    }
    
    private List<MuralDTO.Listagem> aplicarFiltros(List<MuralDTO.Listagem> itens, MuralDTO.Filtro filtro) {
        if (filtro == null) {
            return itens;
        }
        
        List<MuralDTO.Listagem> resultado = new ArrayList<>(itens);
        
        if (filtro.getMotivoInspecao() != null) {
            filtro.setMotivoInspecao(filtro.getMotivoInspecao().trim());
        }
        if (filtro.getUsuarioInspecao() != null) {
            filtro.setUsuarioInspecao(filtro.getUsuarioInspecao().trim());
        }
        
        if (StringUtils.hasText(filtro.getSearchTerm())) {
            resultado = aplicarFiltroBusca(resultado, filtro.getSearchTerm());
        }
        
        resultado = aplicarFiltrosTexto(resultado, filtro);
        resultado = aplicarFiltrosData(resultado, filtro);
        
        if (filtro.getInspecionado() != null) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getInspecionado().equals(item.getInspecionado()))
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getStatus())) {
            LocalDateTime hoje = LocalDateTime.now();
            LocalDateTime limite = hoje.plusDays(15);
            
            resultado = resultado.stream()
                    .filter(item -> {
                        LocalDateTime vencimento = item.getDataValidade();
                        
                        switch (filtro.getStatus()) {
                            case "proximo":
                                return vencimento.isAfter(hoje) && 
                                      !vencimento.toLocalDate().isEqual(hoje.toLocalDate()) &&
                                      vencimento.isBefore(limite);
                            case "hoje":
                                return vencimento.toLocalDate().isEqual(hoje.toLocalDate());
                            case "vencido":
                                return vencimento.isBefore(hoje);
                            default:
                                return filtro.getStatus().equals(item.getStatus());
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        if (StringUtils.hasText(filtro.getMotivoInspecao())) {
            resultado = resultado.stream()
                    .filter(item -> {
                        if ("Outro".equals(filtro.getMotivoInspecao())) {
                            return item.getMotivoInspecao() != null && 
                                   !item.getMotivoInspecao().equals("Avaria/Quebra") && 
                                   !item.getMotivoInspecao().equals("Promoção");
                        } else {
                            return filtro.getMotivoInspecao().equals(item.getMotivoInspecao());
                        }
                    })
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(filtro.getUsuarioInspecao())) {
            resultado = resultado.stream()
                    .filter(item -> filtro.getUsuarioInspecao().equals(item.getUsuarioInspecao()))
                    .collect(Collectors.toList());
        }
        
        return resultado;
    }
    
    private List<MuralDTO.Listagem> aplicarFiltrosTexto(List<MuralDTO.Listagem> itens, MuralDTO.Filtro filtro) {
        List<MuralDTO.Listagem> resultado = new ArrayList<>(itens);
        
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
    
    private List<MuralDTO.Listagem> aplicarFiltrosData(List<MuralDTO.Listagem> itens, MuralDTO.Filtro filtro) {
        List<MuralDTO.Listagem> resultado = new ArrayList<>(itens);
        
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
    
    private List<MuralDTO.Listagem> aplicarFiltroBusca(List<MuralDTO.Listagem> itens, String termo) {
        if (!StringUtils.hasText(termo)) {
            return itens;
        }
        
        String termoBusca = termo.toLowerCase();
        return itens.stream()
                .filter(item -> 
                    (item.getProduto() != null && item.getProduto().getDescricao() != null && 
                            item.getProduto().getDescricao().toLowerCase().contains(termoBusca)) ||
                    (item.getProduto() != null && item.getProduto().getCodigoBarras() != null && 
                            item.getProduto().getCodigoBarras().toLowerCase().contains(termoBusca)) ||
                    (item.getProduto() != null && item.getProduto().getMarca() != null && 
                            item.getProduto().getMarca().toLowerCase().contains(termoBusca)) ||
                    (item.getLote() != null && item.getLote().toLowerCase().contains(termoBusca))
                )
                .collect(Collectors.toList());
    }
    
    private List<MuralDTO.Listagem> ordenarItens(List<MuralDTO.Listagem> itens, String campo, String direcao) {
        if (!StringUtils.hasText(campo)) {
            return itens;
        }
        
        boolean ascendente = "asc".equalsIgnoreCase(direcao);
        
        Comparator<MuralDTO.Listagem> comparator;
        
        switch (campo.toLowerCase()) {
            case "datavencimento":
                comparator = (item1, item2) -> {
                    if (item1.getDataValidade() == null && item2.getDataValidade() == null) {
                        return 0;
                    }
                    if (item1.getDataValidade() == null) {
                        return 1;
                    }
                    if (item2.getDataValidade() == null) {
                        return -1;
                    }

                    return item1.getDataValidade().compareTo(item2.getDataValidade());
                };
                break;
                
            case "nome":
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
                
            case "status":
                comparator = Comparator.comparing(item -> item.getStatus());
                break;
                
            case "motivoInspecao":
                comparator = Comparator.comparing(item -> 
                    item.getMotivoInspecao() != null ? item.getMotivoInspecao().toLowerCase() : "");
                break;
                
            case "usuarioInspecao":
                comparator = Comparator.comparing(item -> 
                    item.getUsuarioInspecao() != null ? item.getUsuarioInspecao().toLowerCase() : "");
                break;
                
            default:
                comparator = Comparator.comparing(item -> 
                    item.getProduto() != null ? 
                    (item.getProduto().getDescricao() != null ? item.getProduto().getDescricao().toLowerCase() : "") : "");
        }
        
        if (!ascendente) {
            comparator = comparator.reversed();
        }
        
        return itens.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    

    public List<String> getMarcasDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> item.getProduto() != null && StringUtils.hasText(item.getProduto().getMarca()))
                .map(item -> item.getProduto().getMarca())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
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
    
    public List<String> getLotesDisponiveis() {
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> StringUtils.hasText(item.getLote()))
                .map(item -> item.getLote())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private MuralDTO.Listagem mapToDTO(ItemProduto item) {
        String status = determinarStatus(item.getDataVencimento());
        
        MuralDTO.Produto produtoDTO = MuralDTO.Produto.builder()
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

        return MuralDTO.Listagem.builder()
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
    
    private MuralDTO.Listagem marcarItemInspecionado(String id, String motivo, String motivoCustomizado, String usuarioInspecao) throws SmartValidityException {
        try {
            validarMotivoInspecao(motivo, motivoCustomizado);
            
            ItemProduto item = itemProdutoService.buscarPorId(id);
            
            String motivoFinal = obterMotivoFinal(motivo, motivoCustomizado);
            
            String nomeUsuario = usuarioInspecao;
            if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
                try {
                    nomeUsuario = System.getProperty("user.name");
                } catch (Exception e) {
                    nomeUsuario = "Sistema";
                }
            }
            
            item.setInspecionado(true);
            item.setMotivoInspecao(motivoFinal);
            item.setUsuarioInspecao(nomeUsuario);
            item.setDataHoraInspecao(LocalDateTime.now());
            
            ItemProduto itemSalvo = itemProdutoService.salvarItemInspecionado(item);
            return mapToDTO(itemSalvo);
        } catch (Exception e) {
            if (e instanceof SmartValidityException) {
                throw (SmartValidityException) e;
            }
            System.err.println("Erro ao marcar item como inspecionado: " + e.getMessage());
            e.printStackTrace();
            throw new SmartValidityException("Erro ao marcar item como inspecionado: " + e.getMessage());
        }
    }
    
    public MuralDTO.Listagem marcarInspecionado(String id, String motivo, String motivoCustomizado, String usuarioInspecao) throws SmartValidityException {
        return marcarItemInspecionado(id, motivo, motivoCustomizado, usuarioInspecao);
    }

    private List<MuralDTO.Listagem> marcarVariosItensInspecionados(List<String> ids, String motivo, String motivoCustomizado, String usuarioInspecao) throws SmartValidityException {
        if (ids == null || ids.isEmpty()) {
            throw new SmartValidityException("Nenhum item selecionado para inspeção");
        }
        
        validarMotivoInspecao(motivo, motivoCustomizado);
        String motivoFinal = obterMotivoFinal(motivo, motivoCustomizado);
        
        String nomeUsuario = usuarioInspecao;
        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            try {
                nomeUsuario = System.getProperty("user.name");
            } catch (Exception e) {
                nomeUsuario = "Sistema";
            }
        }
        
        LocalDateTime dataHoraInspecao = LocalDateTime.now();
        
        List<MuralDTO.Listagem> itensAtualizados = new ArrayList<>();
        
        for (String id : ids) {
            try {
                ItemProduto item = itemProdutoService.buscarPorId(id);
                item.setInspecionado(true);
                item.setMotivoInspecao(motivoFinal);
                item.setUsuarioInspecao(nomeUsuario);
                item.setDataHoraInspecao(dataHoraInspecao);
                
                ItemProduto itemSalvo = itemProdutoService.salvarItemInspecionado(item);
                itensAtualizados.add(mapToDTO(itemSalvo));
            } catch (Exception e) {
                System.err.println("Erro ao marcar item " + id + " como inspecionado: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (itensAtualizados.isEmpty() && !ids.isEmpty()) {
            throw new SmartValidityException("Não foi possível marcar nenhum dos itens como inspecionado");
        }
        
        return itensAtualizados;
    }
    
    public List<MuralDTO.Listagem> marcarVariosInspecionados(List<String> ids, String motivo, String motivoCustomizado, String usuarioInspecao) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, motivo, motivoCustomizado, usuarioInspecao);
    }

    public MuralDTO.Listagem getItemById(String id) throws SmartValidityException {
        ItemProduto item = itemProdutoService.buscarPorId(id);
        return mapToDTO(item);
    }
    public int contarPaginas(MuralDTO.Filtro filtro) {
        if (filtro != null && filtro.temPaginacao()) {
            long totalRegistros = contarTotalRegistros(filtro);
            return (int) Math.ceil((double) totalRegistros / filtro.getLimite());
        }
        return 1;
    }
    
    public long contarTotalRegistros(MuralDTO.Filtro filtro) {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        List<MuralDTO.Listagem> dtos = itens.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        List<MuralDTO.Listagem> itensFiltrados = aplicarFiltros(dtos, filtro);
        
        return itensFiltrados.size();
    }

    private List<String> usuariosInspecaoCache;
    private LocalDateTime ultimaAtualizacaoCache;
    private static final long CACHE_DURACAO_MINUTOS = 5;

    private String formatarNomeCamelCase(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }

        String[] palavras = nome.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                resultado.append(palavra.substring(0, 1).toUpperCase())
                        .append(palavra.substring(1))
                        .append(" ");
            }
        }

        return resultado.toString().trim();
    }

    public List<String> getUsuariosInspecaoDisponiveis() {
        if (usuariosInspecaoCache != null && 
            ultimaAtualizacaoCache != null && 
            ultimaAtualizacaoCache.plusMinutes(CACHE_DURACAO_MINUTOS).isAfter(LocalDateTime.now())) {
            return usuariosInspecaoCache;
        }

        List<String> usuarios = itemProdutoService.buscarTodos().stream()
                .filter(item -> StringUtils.hasText(item.getUsuarioInspecao()))
                .map(ItemProduto::getUsuarioInspecao)
                .distinct()
                .map(this::formatarNomeCamelCase)
                .sorted()
                .collect(Collectors.toList());

        usuariosInspecaoCache = usuarios;
        ultimaAtualizacaoCache = LocalDateTime.now();

        return usuarios;
    }

    public List<MuralDTO.Listagem> buscarPorIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        return ids.stream()
            .map(id -> {
                try {
                    ItemProduto item = itemProdutoService.buscarPorId(id);
                    return mapToDTO(item);
                } catch (Exception e) {
                    System.err.println("Erro ao buscar item " + id + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Gera o título do relatório com base no tipo e status
     */
    private String gerarTituloRelatorio(String status, String tipo, int quantidade) {
        StringBuilder titulo = new StringBuilder();
        
        // Define o título base com base no status
        switch (status) {
            case "proximo":
                titulo.append("Relatório de Produtos Próximos do Vencimento");
                break;
            case "hoje":
                titulo.append("Relatório de Produtos que Vencem Hoje");
                break;
            case "vencido":
                titulo.append("Relatório de Produtos Vencidos");
                break;
            default:
                titulo.append("Relatório de Produtos");
        }
        
        // Adiciona informação sobre a quantidade de itens
        switch (tipo) {
            case "SELECIONADOS":
                titulo.append(String.format(" (%d item(ns) selecionado(s))", quantidade));
                break;
            case "PAGINA":
                titulo.append(String.format(" (%d item(ns) da página atual)", quantidade));
                break;
            case "TODOS":
                titulo.append(String.format(" (%d item(ns) no total)", quantidade));
                break;
        }
        
        return titulo.toString();
    }

    /**
     * Gera relatório Excel com base nos parâmetros fornecidos
     */
    public byte[] gerarRelatorioExcel(MuralDTO.RelatorioRequest request) throws SmartValidityException {
        logger.info("Iniciando geracao de relatorio. Tipo: {}, Status: {}", request.getTipo(), request.getStatus());
        
        List<MuralDTO.Listagem> itens;
        
        try {
            switch (request.getTipo()) {
                case "SELECIONADOS":
                    if (request.getIds() == null || request.getIds().isEmpty()) {
                        logger.warn("Tentativa de gerar relatorio sem itens selecionados");
                        throw new SmartValidityException("Nenhum item selecionado para o relatório");
                    }
                    logger.debug("Gerando relatorio para {} itens selecionados", request.getIds().size());
                    itens = buscarPorIds(request.getIds());
                    break;
                    
                case "PAGINA":
                    logger.debug("Gerando relatorio para itens da pagina atual. Filtros: {}", request.getFiltro());
                    itens = buscarComFiltro(request.getFiltro());
                    break;
                    
                case "TODOS":
                    logger.debug("Gerando relatorio para todos os itens. Filtros aplicados: {}", request.getFiltro());
                    // Remove paginação para buscar todos os itens
                    MuralDTO.Filtro filtroSemPaginacao = request.getFiltro();
                    filtroSemPaginacao.setPagina(null);
                    filtroSemPaginacao.setLimite(null);
                    itens = buscarComFiltro(filtroSemPaginacao);
                    break;
                    
                default:
                    logger.error("Tipo de relatorio invalido: {}", request.getTipo());
                    throw new SmartValidityException("Tipo de relatório inválido");
            }

            if (itens.isEmpty()) {
                logger.warn("Nenhum item encontrado para gerar o relatorio");
                throw new SmartValidityException("Nenhum item encontrado para gerar o relatório");
            }

            String titulo = gerarTituloRelatorio(request.getStatus(), request.getTipo(), itens.size());
            logger.info("Gerando Excel para {} itens com titulo: {}", itens.size(), titulo);
            
            byte[] resultado = excelService.gerarExcelMural(itens, titulo);
            logger.info("Relatorio gerado com sucesso. Tamanho: {} bytes", resultado.length);
            
            return resultado;
            
        } catch (SmartValidityException e) {
            logger.warn("Erro de validacao ao gerar relatorio: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar relatorio: {}", e.getMessage(), e);
            throw new SmartValidityException("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    public void cancelarSelecao(List<String> ids) {
        // Implemente aqui a lógica de negócio para cancelar seleção, se necessário.
        // Exemplo: itemProdutoService.cancelarSelecao(ids);
    }
} 