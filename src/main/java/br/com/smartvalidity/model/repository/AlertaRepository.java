package br.com.smartvalidity.model.repository;

import br.com.smartvalidity.model.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
} 