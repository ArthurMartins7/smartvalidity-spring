package br.com.smartvalidity.model.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Busca produtos com itens não inspecionados filtrando por termo de busca
     */
    @Query("SELECT DISTINCT p FROM Produto p " +
           "JOIN p.itensProduto ip " + 
           "WHERE ip.inspecionado = false " +
           "AND (LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
           "ORDER BY p.descricao")
    List<Produto> findProdutosComItensNaoInspecionadosPorTermo(@Param("termo") String termo, Pageable pageable);

}
