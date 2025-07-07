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

    @Autowired
    private UsuarioService usuarioService;

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
                                return vencimento.toLocalDate().isBefore(hoje.toLocalDate());
                            default:
                                return filtro.getStatus().equals(item.getStatus());
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        // filtro por motivo de inspeção
        List<String> motivosInspecao = filtro.getMotivosInspecaoEfetivos();
        if (!motivosInspecao.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> {
                        String motivoItem = item.getMotivoInspecao();
                        if (motivoItem == null) return false;
                        
                        for (String motivo : motivosInspecao) {
                            if ("Outro".equals(motivo)) {
                                // para o motivo "outro" inclui todos que não são "avaria/quebra" nem "promoção"
                                if (!motivoItem.equals("Avaria/Quebra") && !motivoItem.equals("Promoção")) {
                                    return true;
                                }
                            } else {
                                // para motivos específicos (outro), verifica igualdade
                                if (motivo.equals(motivoItem)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // filtro de usuário que fez a inspeção
        List<String> usuariosInspecao = filtro.getUsuariosInspecaoEfetivos();
        if (!usuariosInspecao.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> {
                        String usuario = item.getUsuarioInspecao();
                        if (usuario == null) return false;
                        
                        String usuarioNormalizado = usuario.trim().toLowerCase();
                        return usuariosInspecao.stream()
                                .anyMatch(filtroUsuario -> 
                                    filtroUsuario.trim().toLowerCase().equals(usuarioNormalizado));
                    })
                    .collect(Collectors.toList());
        }
        
        return resultado;
    }
    
    private List<MuralDTO.Listagem> aplicarFiltrosTexto(List<MuralDTO.Listagem> itens, MuralDTO.Filtro filtro) {
        List<MuralDTO.Listagem> resultado = new ArrayList<>(itens);
        
        // filtro de marca
        List<String> marcas = filtro.getMarcasEfetivas();
        if (!marcas.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> item.getProduto() != null && 
                            marcas.contains(item.getProduto().getMarca()))
                    .collect(Collectors.toList());
        }
        
        // corredor
        List<String> corredores = filtro.getCorredoresEfetivos();
        if (!corredores.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> corredores.contains(item.getCorredor()))
                    .collect(Collectors.toList());
        }
        
        // categoria
        List<String> categorias = filtro.getCategoriasEfetivas();
        if (!categorias.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> categorias.contains(item.getCategoria()))
                    .collect(Collectors.toList());
        }
        
        // fornecedor
        List<String> fornecedores = filtro.getFornecedoresEfetivos();
        if (!fornecedores.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> fornecedores.contains(item.getFornecedor()))
                    .collect(Collectors.toList());
        }
        
        // lote
        List<String> lotes = filtro.getLotesEfetivos();
        if (!lotes.isEmpty()) {
            resultado = resultado.stream()
                    .filter(item -> lotes.contains(item.getLote()))
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
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                })
                .filter(item -> item.getProduto() != null && StringUtils.hasText(item.getProduto().getMarca()))
                .map(item -> item.getProduto().getMarca())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public List<String> getCorredoresDisponiveis() {
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                })
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
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                })
                .filter(item -> item.getProduto() != null && 
                        item.getProduto().getCategoria() != null && 
                        StringUtils.hasText(item.getProduto().getCategoria().getNome()))
                .map(item -> item.getProduto().getCategoria().getNome())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public List<String> getFornecedoresDisponiveis() {
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                })
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
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itemProdutoService.buscarTodos().stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                })
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
        if (dataVencimento.toLocalDate().isBefore(hoje.toLocalDate())) {
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

    public List<String> getUsuariosInspecaoDisponiveis() {
        try {
            LocalDateTime hoje = LocalDateTime.now();
            LocalDateTime limite = hoje.plusDays(15);
            
            // Buscar usuários que inspecionaram produtos que vencerão nos próximos 15 dias
            List<String> usuariosComInspecaoRelevante = itemProdutoService.buscarTodos().stream()
                    .filter(item -> {
                        LocalDateTime vencimento = item.getDataVencimento();
                        return vencimento.isAfter(hoje) && vencimento.isBefore(limite.plusDays(1));
                    })
                    .filter(item -> item.getInspecionado() && StringUtils.hasText(item.getUsuarioInspecao()))
                    .map(item -> item.getUsuarioInspecao())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            if (usuariosComInspecaoRelevante.isEmpty()) {
            return usuarioService.listarTodos().stream()
                .map(usuario -> usuario.getNome())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            }
            
            return usuariosComInspecaoRelevante;
        } catch (Exception e) {
            logger.warn("Erro ao obter usuários para inspeção: {}", e.getMessage());
            return new ArrayList<>();
        }
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


    private String gerarTituloRelatorio(String status, String tipo, int quantidade) {
        StringBuilder titulo = new StringBuilder();
        
        // define título base com base no status:
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
        
        // adiciona info sobre a quantidade de itens:
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

    private void validarItensPertencemAoStatus(List<MuralDTO.Listagem> itens, String status) throws SmartValidityException {
        if (status == null || status.isEmpty()) return;
        for (MuralDTO.Listagem item : itens) {
            if (!status.equals(item.getStatus())) {
                throw new SmartValidityException("Um ou mais produtos selecionados não pertencem à aba/status informada. Por favor, selecione apenas produtos da aba correta.");
            }
        }
    }

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
                    validarItensPertencemAoStatus(itens, request.getStatus());
                    break;
                case "PAGINA":
                    logger.debug("Gerando relatorio para itens da pagina atual. Filtros: {}", request.getFiltro());
                    itens = buscarComFiltro(request.getFiltro());
                    validarItensPertencemAoStatus(itens, request.getStatus());
                    break;
                case "TODOS":
                    logger.debug("Gerando relatorio para todos os itens. Filtros aplicados: {}", request.getFiltro());
                    MuralDTO.Filtro filtroSemPaginacao = request.getFiltro();
                    filtroSemPaginacao.setPagina(null);
                    filtroSemPaginacao.setLimite(null);
                    itens = buscarComFiltro(filtroSemPaginacao);
                    validarItensPertencemAoStatus(itens, request.getStatus());
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

    public void cancelarSelecao(List<String> ids) { // to do

    }
}