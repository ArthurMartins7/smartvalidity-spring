package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer>, JpaSpecificationExecutor<Categoria> {
}
