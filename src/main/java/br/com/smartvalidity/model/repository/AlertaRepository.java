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
     * Verificar se já existe um alerta ativo para um item-produto específico e tipo
     */
    boolean existsByItemProdutoAndTipoAndAtivoTrue(ItemProduto itemProduto, TipoAlerta tipo);
    
    /**
     * Buscar alertas de itens que foram inspecionados e ainda estão ativos
     */
    @Query("SELECT a FROM Alerta a WHERE a.itemProduto.inspecionado = true AND a.ativo = true")
    List<Alerta> findByItemProdutoInspecionadoTrueAndAtivoTrue();
    
    /**
     * Buscar alertas de um usuário específico
     */
    @Query("SELECT a FROM Alerta a JOIN a.usuariosAlerta u WHERE u = :usuario AND a.ativo = true ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findByUsuarioAndAtivoTrue(@Param("usuario") Usuario usuario);
    
    /**
     * Buscar alertas não lidos de um usuário específico
     */
    @Query("SELECT a FROM Alerta a JOIN a.usuariosAlerta u WHERE u = :usuario AND a.ativo = true AND a.lido = false ORDER BY a.dataHoraCriacao DESC")
    List<Alerta> findByUsuarioAndAtivoTrueAndLidoFalse(@Param("usuario") Usuario usuario);
    
    /**
     * Contar alertas não lidos de um usuário específico
     */
    @Query("SELECT COUNT(a) FROM Alerta a JOIN a.usuariosAlerta u WHERE u = :usuario AND a.ativo = true AND a.lido = false")
    Long countByUsuarioAndAtivoTrueAndLidoFalse(@Param("usuario") Usuario usuario);
    
    /**
     * Buscar alertas ainda não ativados cujo horário de disparo já foi alcançado
     */
    List<Alerta> findByAtivoFalseAndDataHoraDisparoLessThanEqual(java.time.LocalDateTime dataHora);

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