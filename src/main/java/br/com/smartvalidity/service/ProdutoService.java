package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> buscarTodos() {
        return produtoRepository.findAll();
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

            // Categoria com id e nome
            if (produto.getCategoria() != null) {
                dto.setCategoria(Map.of(
                        "id", produto.getCategoria().getId(),
                        "nome", produto.getCategoria().getNome()
                ));
            }

            // Fornecedores com endereço incluso
            if (produto.getFornecedores() != null) {
                List<Map<String, Object>> fornecedores = produto.getFornecedores().stream().map(f -> {
                    Map<String, Object> endereco = null;
                    if (f.getEndereco() != null) {
                        endereco = Map.of(
                                "id", f.getEndereco().getId(),
                                "logradouro", f.getEndereco().getLogradouro(),
                                "numero", f.getEndereco().getNumero(),
                                "complemento", f.getEndereco().getComplemento(),
                                "bairro", f.getEndereco().getBairro(),
                                "cidade", f.getEndereco().getCidade(),
                                "estado", f.getEndereco().getEstado(),
                                "pais", f.getEndereco().getPais(),
                                "cep", f.getEndereco().getCep()
                        );
                    }

                    return Map.of(
                            "id", f.getId(),
                            "nome", f.getNome(),
                            "telefone", f.getTelefone(),
                            "cnpj", f.getCnpj(),
                            "endereco", endereco
                    );
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
