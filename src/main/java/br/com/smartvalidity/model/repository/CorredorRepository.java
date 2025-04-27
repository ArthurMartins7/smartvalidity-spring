package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorredorRepository extends JpaRepository<Corredor, Integer>, JpaSpecificationExecutor<Corredor> {

//    @Query("SELECT c FROM Corredor c LEFT JOIN FETCH c.categorias")
//    List<Corredor> findAllWithCategorias();

    @EntityGraph(attributePaths = {"categorias"})
    @Query("SELECT c FROM Corredor c")
    List<Corredor> findAllWithCategorias();

}
