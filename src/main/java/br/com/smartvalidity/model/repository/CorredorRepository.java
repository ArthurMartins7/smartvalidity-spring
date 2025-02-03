package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.Corredor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorredorRepository extends JpaRepository<Corredor, Integer> {
}
