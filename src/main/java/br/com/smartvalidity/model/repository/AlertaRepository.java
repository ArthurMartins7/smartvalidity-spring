package br.com.smartvalidity.model.repository;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.TipoAlerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Integer>, JpaSpecificationExecutor<Alerta> {

    List<Alerta> findByAtivoTrue();

    List<Alerta> findByTipoAndAtivoTrue(TipoAlerta tipo);

    @Query("SELECT a FROM Alerta a WHERE a.dataHoraDisparo <= :dataHora AND a.ativo = true")
    List<Alerta> findAlertasParaDisparar(@Param("dataHora") LocalDateTime dataHora);

    List<Alerta> findByRecorrenteAndAtivoTrue(Boolean recorrente);

    @Query("SELECT a FROM Alerta a WHERE a.usuarioCriador.id = :usuarioId AND a.ativo = true")
    List<Alerta> findByUsuarioCriadorId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT a FROM Alerta a WHERE a.tipo IN (:tipos) AND a.ativo = true AND " +
           "(a.dataEnvio IS NULL OR DATE(a.dataEnvio) < CURRENT_DATE)")
    List<Alerta> findAlertasVencimentoParaEnviar(@Param("tipos") List<TipoAlerta> tipos);
=======
import br.com.smartvalidity.model.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
>>>>>>> ac60f2e9298f0c29c567180cb212ef149affd74d
} 