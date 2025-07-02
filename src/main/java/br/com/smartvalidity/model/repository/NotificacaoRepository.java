package br.com.smartvalidity.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Usuario;


public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /**
     * Buscar todas as notificações de um usuário (ordenadas por data de criação)
     */
    List<Notificacao> findByUsuarioOrderByDataHoraCriacaoDesc(Usuario usuario);

    /**
     * Buscar apenas notificações não lidas de um usuário
     */
    List<Notificacao> findByUsuarioAndLidaFalseOrderByDataHoraCriacaoDesc(Usuario usuario);

    /**
     * Contar notificações não lidas de um usuário
     */
    Long countByUsuarioAndLidaFalse(Usuario usuario);

    /**
     * Buscar notificação específica de um usuário
     */
    Optional<Notificacao> findByIdAndUsuario(Long id, Usuario usuario);

    /**
     * Verificar se já existe notificação para um alerta e usuário específicos
     */
    boolean existsByAlertaIdAndUsuarioId(Integer alertaId, String usuarioId);

    /**
     * Marcar todas as notificações de um usuário como lidas
     */
    @Query("UPDATE Notificacao n SET n.lida = true, n.dataHoraLeitura = CURRENT_TIMESTAMP WHERE n.usuario = :usuario AND n.lida = false")
    @Modifying
    @Transactional
    int marcarTodasComoLidasPorUsuario(@Param("usuario") Usuario usuario);


} 