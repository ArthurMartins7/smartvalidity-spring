package br.com.smartvalidity.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.smartvalidity.model.entity.Empresa;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.enums.StatusUsuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findBySenha(String senha);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsUsuarioByPerfilAcesso(PerfilAcesso perfilAcesso);

    List<Usuario> findByEmpresaAndStatus(Empresa empresa, StatusUsuario status);

    Optional<Usuario> findFirstByPerfilAcesso(PerfilAcesso perfilAcesso);

}
