package br.com.smartvalidity.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String>, JpaSpecificationExecutor<Produto> {

    List<Produto> findByCategoriaId(String categoriaId);
    
    /**
     * Busca produtos que possuem pelo menos um item-produto não inspecionado
     */
    @Query("SELECT DISTINCT p FROM Produto p " +
           "JOIN p.itensProduto ip " +
           "WHERE ip.inspecionado = false")
    List<Produto> findProdutosComItensNaoInspecionados();

}
