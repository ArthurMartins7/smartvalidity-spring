package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.ItemProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemProdutoRepository extends JpaRepository<ItemProduto, String>, JpaSpecificationExecutor<ItemProduto> {

    List<ItemProduto> findByProdutoId(String produtoId);
}
