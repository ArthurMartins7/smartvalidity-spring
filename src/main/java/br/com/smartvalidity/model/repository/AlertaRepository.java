package br.com.smartvalidity.model.repository;

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

    // Buscar alertas ativos
    List<Alerta> findByAtivoTrue();

    // Buscar alertas por tipo
    List<Alerta> findByTipoAndAtivoTrue(TipoAlerta tipo);

    // Buscar alertas para disparar (data/hora <= agora e ativo)
    @Query("SELECT a FROM Alerta a WHERE a.dataHoraDisparo <= :dataHora AND a.ativo = true")
    List<Alerta> findAlertasParaDisparar(@Param("dataHora") LocalDateTime dataHora);

    // Buscar alertas recorrentes ativos
    List<Alerta> findByRecorrenteAndAtivoTrue(Boolean recorrente);

    // Buscar alertas de um usuário criador
    @Query("SELECT a FROM Alerta a WHERE a.usuarioCriador.id = :usuarioId AND a.ativo = true")
    List<Alerta> findByUsuarioCriadorId(@Param("usuarioId") Integer usuarioId);

    // Buscar alertas automáticos de vencimento que ainda não foram enviados hoje
    @Query("SELECT a FROM Alerta a WHERE a.tipo IN (:tipos) AND a.ativo = true AND " +
           "(a.dataEnvio IS NULL OR DATE(a.dataEnvio) < CURRENT_DATE)")
    List<Alerta> findAlertasVencimentoParaEnviar(@Param("tipos") List<TipoAlerta> tipos);
} 