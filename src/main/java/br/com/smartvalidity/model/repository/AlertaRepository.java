package br.com.smartvalidity.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.TipoAlerta;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
    

    boolean existsByItemProdutoAndTipoAndExcluidoFalse(ItemProduto itemProduto, TipoAlerta tipo);
    
    @Query("SELECT a FROM Alerta a WHERE a.itemProduto.inspecionado = true AND a.excluido = false")
    List<Alerta> findByItemProdutoInspecionadoTrueAndExcluidoFalse();
    
    @Query("SELECT a FROM Alerta a JOIN a.usuariosAlerta u WHERE u = :usuario AND a.excluido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findByUsuarioAndExcluidoFalse(@Param("usuario") Usuario usuario);

    @Query("SELECT a FROM Alerta a WHERE a.id = :id AND a.excluido = false")
    java.util.Optional<Alerta> findByIdAndExcluidoFalse(@Param("id") Integer id);

    @Query("SELECT a FROM Alerta a WHERE a.excluido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findAllNotDeleted();

    @Query("SELECT a FROM Alerta a WHERE a.itemProduto = :itemProduto AND a.excluido = false")
    List<Alerta> findByItemProdutoAndExcluidoFalse(@Param("itemProduto") ItemProduto itemProduto);

    @Query("SELECT a FROM Alerta a WHERE a.itemProduto = :itemProduto AND a.excluido = false")
    Optional<Alerta> findFirstByItemProdutoAndExcluidoFalse(@Param("itemProduto") ItemProduto itemProduto);

    @Query("SELECT a FROM Alerta a WHERE a.itemProduto.inspecionado = true AND a.excluido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findAlertasJaResolvidos();

    List<Alerta> findByTipoAndExcluidoFalse(TipoAlerta tipo);

    // Métodos para alertas recorrentes removidos - alertas personalizados são mais simples

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alerta_usuario WHERE id_alerta = :alertaId", nativeQuery = true)
    void deleteUsuariosAlerta(@Param("alertaId") Integer alertaId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alerta_produto WHERE id_alerta = :alertaId", nativeQuery = true)
    void deleteProdutosAlerta(@Param("alertaId") Integer alertaId);
}