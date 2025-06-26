package br.com.smartvalidity.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String>, JpaSpecificationExecutor<Empresa> {
    Optional<Empresa> findByCnpj(String cnpj);
} 