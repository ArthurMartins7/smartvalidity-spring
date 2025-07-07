package br.com.smartvalidity.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.TipoAlerta;


public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioOrderByDataHoraCriacaoDesc(Usuario usuario);

    List<Notificacao> findByUsuarioAndLidaFalseOrderByDataHoraCriacaoDesc(Usuario usuario);

    Long countByUsuarioAndLidaFalse(Usuario usuario);

    Optional<Notificacao> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByAlertaIdAndUsuarioId(Integer alertaId, String usuarioId);

    List<Notificacao> findByAlerta(Alerta alerta);

    @Query("SELECT n FROM Notificacao n WHERE n.usuario = :usuario AND n.alerta.itemProduto.inspecionado = true ORDER BY n.alerta.itemProduto.dataHoraInspecao DESC")
    List<Notificacao> findByUsuarioAndProdutoInspecionado(@Param("usuario") Usuario usuario);

    @Query("SELECT n FROM Notificacao n WHERE n.usuario = :usuario AND n.alerta.tipo = :tipo ORDER BY n.dataHoraCriacao DESC")
    List<Notificacao> findByUsuarioAndAlertaTipoOrderByDataHoraCriacaoDesc(@Param("usuario") Usuario usuario, @Param("tipo") TipoAlerta tipo);

    @Query("UPDATE Notificacao n SET n.lida = true, n.dataHoraLeitura = CURRENT_TIMESTAMP WHERE n.usuario = :usuario AND n.lida = false")
    @Modifying
    @Transactional
    int marcarTodasComoLidasPorUsuario(@Param("usuario") Usuario usuario);

}