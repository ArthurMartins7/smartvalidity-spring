package br.com.smartvalidity.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.ItemProduto;

@Repository
public interface ItemProdutoRepository extends JpaRepository<ItemProduto, String>, JpaSpecificationExecutor<ItemProduto> {

    List<ItemProduto> findByProdutoId(String produtoId);

    List<ItemProduto> findByLote(String lote);
    
    /**
     * Buscar todos os itens-produto que ainda não foram inspecionados
     */
    List<ItemProduto> findByInspecionadoFalse();
    
    /**
     * Buscar itens-produto não inspecionados de um produto específico
     */
    List<ItemProduto> findByProdutoIdAndInspecionadoFalse(String produtoId);
}
