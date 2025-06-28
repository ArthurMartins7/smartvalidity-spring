package br.com.smartvalidity.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ItemProdutoService itemProdutoService;

    public List<Produto> buscarTodos() {
        return produtoRepository.findAll();
    }
    
    /**
     * Busca apenas produtos que possuem itens-produto não inspecionados
     * Para uso em alertas personalizados
     */
    public List<Produto> buscarProdutosComItensNaoInspecionados() {
        return produtoRepository.findProdutosComItensNaoInspecionados();
    }

    /**
     * Busca produtos com itens não inspecionados filtrando por termo de busca
     * Para uso em busca dinâmica de alertas
     */
    public List<Produto> buscarPorTermoComItensNaoInspecionados(String termo, int limite) {
        if (termo == null || termo.trim().isEmpty()) {
            return List.of();
        }
        org.springframework.data.domain.PageRequest pageable = 
            org.springframework.data.domain.PageRequest.of(0, limite);
        return produtoRepository.findProdutosComItensNaoInspecionadosPorTermo(termo.trim(), pageable);
    }

    public Produto buscarPorId(String id) throws SmartValidityException {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Produto não encontrado com o ID: " + id));
    }

    public List<Produto> buscarPorCategoria(String categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId);
    }
    public List<ProdutoDTO> listarTodosDTO() {
        return produtoRepository.findAll().stream().map(produto -> {
            ProdutoDTO dto = new ProdutoDTO();
            dto.setId(UUID.fromString(produto.getId()));
            dto.setCodigoBarras(produto.getCodigoBarras());
            dto.setDescricao(produto.getDescricao());
            dto.setMarca(produto.getMarca());
            dto.setUnidadeMedida(produto.getUnidadeMedida());
            dto.setQuantidade(produto.getQuantidade());

            // Categoria com id e nome (permitir valores nulos)
            if (produto.getCategoria() != null) {
                java.util.Map<String, Object> categoria = new java.util.HashMap<>();
                categoria.put("id", produto.getCategoria().getId());
                categoria.put("nome", produto.getCategoria().getNome());
                dto.setCategoria(categoria);
            }

            // Fornecedores com endereço incluso (permitir valores nulos)
            if (produto.getFornecedores() != null) {
                List<java.util.Map<String, Object>> fornecedores = produto.getFornecedores().stream().map(f -> {
                    java.util.Map<String, Object> enderecoMap = null;
                    if (f.getEndereco() != null) {
                        enderecoMap = new java.util.HashMap<>();
                        enderecoMap.put("id", f.getEndereco().getId());
                        enderecoMap.put("logradouro", f.getEndereco().getLogradouro());
                        enderecoMap.put("numero", f.getEndereco().getNumero());
                        enderecoMap.put("complemento", f.getEndereco().getComplemento());
                        enderecoMap.put("bairro", f.getEndereco().getBairro());
                        enderecoMap.put("cidade", f.getEndereco().getCidade());
                        enderecoMap.put("estado", f.getEndereco().getEstado());
                        enderecoMap.put("pais", f.getEndereco().getPais());
                        enderecoMap.put("cep", f.getEndereco().getCep());
                    }

                    java.util.Map<String, Object> fornMap = new java.util.HashMap<>();
                    fornMap.put("id", f.getId());
                    fornMap.put("nome", f.getNome());
                    fornMap.put("telefone", f.getTelefone());
                    fornMap.put("cnpj", f.getCnpj());
                    fornMap.put("endereco", enderecoMap);
                    return fornMap;
                }).toList();

                dto.setFornecedores(fornecedores);
            }
            return dto;
        }).toList();
    }



    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> pesquisarComSeletor(ProdutoSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            int pageNumber = seletor.getPagina();
            int pageSize = seletor.getLimite();

            PageRequest pagina = PageRequest.of(pageNumber - 1, pageSize);
            return produtoRepository.findAll(seletor, pagina).getContent();
        }
        return produtoRepository.findAll(seletor);
    }

    public int contarPaginas(ProdutoSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            int pageSize = seletor.getLimite();
            PageRequest pagina = PageRequest.of(0, pageSize);

            Page<Produto> paginaResultado = produtoRepository.findAll(seletor, pagina);
            return paginaResultado.getTotalPages();
        }

        long totalRegistros = produtoRepository.count(seletor);
        return totalRegistros > 0 ? 1 : 0;
    }

    public long contarTotalRegistros(ProdutoSeletor seletor) {
        return produtoRepository.count(seletor);
    }


    public Produto atualizar(String id, Produto produtoAtualizado) throws SmartValidityException {
        Produto produto = buscarPorId(id);

        produto.setCodigoBarras(produtoAtualizado.getCodigoBarras());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setMarca(produtoAtualizado.getMarca());
        produto.setUnidadeMedida(produtoAtualizado.getUnidadeMedida());
        produto.setQuantidade(produtoAtualizado.getQuantidade());
        produto.setCategoria(produtoAtualizado.getCategoria());
        produto.setFornecedores(produtoAtualizado.getFornecedores());

        return produtoRepository.save(produto);
    }

    public void excluir(String id) throws SmartValidityException {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }
}
