package br.com.smartvalidity.model.repository;

import java.util.List;

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
    
    /**
     * Verificar se já existe um alerta não excluído para um item-produto específico e tipo
     */
    boolean existsByItemProdutoAndTipoAndExcluidoFalse(ItemProduto itemProduto, TipoAlerta tipo);
    
    /**
     * Buscar alertas de itens que foram inspecionados e ainda não estão excluídos
     */
    @Query("SELECT a FROM Alerta a WHERE a.itemProduto.inspecionado = true AND a.excluido = false")
    List<Alerta> findByItemProdutoInspecionadoTrueAndExcluidoFalse();
    
    /**
     * Buscar alertas de um usuário específico (não excluídos)
     */
    @Query("SELECT a FROM Alerta a JOIN a.usuariosAlerta u WHERE u = :usuario AND a.excluido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findByUsuarioAndExcluidoFalse(@Param("usuario") Usuario usuario);

    /**
     * Buscar alerta por ID apenas se não estiver excluído
     */
    @Query("SELECT a FROM Alerta a WHERE a.id = :id AND a.excluido = false")
    java.util.Optional<Alerta> findByIdAndExcluidoFalse(@Param("id") Integer id);

    /**
     * Buscar todos os alertas não excluídos
     */
    @Query("SELECT a FROM Alerta a WHERE a.excluido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findAllNotDeleted();

    /*
     * Excluir registros das tabelas de junção para evitar violação de FK ao remover um alerta
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alerta_usuario WHERE id_alerta = :alertaId", nativeQuery = true)
    void deleteUsuariosAlerta(@Param("alertaId") Integer alertaId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alerta_produto WHERE id_alerta = :alertaId", nativeQuery = true)
    void deleteProdutosAlerta(@Param("alertaId") Integer alertaId);
} 