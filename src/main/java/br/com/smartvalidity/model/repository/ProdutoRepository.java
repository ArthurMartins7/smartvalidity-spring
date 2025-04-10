package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String>, JpaSpecificationExecutor<Produto> {

    List<Produto> findByCategoriaId(String categoriaId);

}
