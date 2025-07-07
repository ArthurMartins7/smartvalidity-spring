package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.seletor.UsuarioSeletor;
import br.com.smartvalidity.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(path = "/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Buscar usuários com seletor")
    @PostMapping("/filtro")
    public List<Usuario> buscarComSeletor(@RequestBody UsuarioSeletor mensagemSeletor) throws SmartValidityException {
        return this.usuarioService.buscarComSeletor(mensagemSeletor);
    }

    @GetMapping
    public List<Usuario> buscarTodos() throws SmartValidityException {
        return this.usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable String id) throws SmartValidityException {
        Usuario usuario = this.usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> alterar(@PathVariable String id, @RequestBody Usuario usuarioDTO) throws SmartValidityException {
        return ResponseEntity.ok(this.usuarioService.alterar(id ,usuarioDTO));
    }

    @Operation(summary = "Atualizar perfil do usuário autenticado")
    @PutMapping("/me")
    public ResponseEntity<Usuario> atualizarPerfil(@RequestBody Usuario usuarioDTO) throws SmartValidityException {
        // Buscar o usuário autenticado e atualizar apenas os campos permitidos
        Usuario usuarioAtualizado = this.usuarioService.atualizarPerfilUsuario(usuarioDTO);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) throws SmartValidityException {
        this.usuarioService.excluir(id);
        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "Convidar novo usuário (envia senha provisória por e-mail)")
    @PostMapping("/convidar")
    @ResponseStatus(code = org.springframework.http.HttpStatus.CREATED)
    public Usuario convidarUsuario(@RequestBody Usuario usuario) throws SmartValidityException {
        return usuarioService.convidarUsuario(usuario);
    }

    @GetMapping("/pendentes")
    public List<Usuario> listarPendentes() throws SmartValidityException {
        return usuarioService.listarPendentes();
    }

    @GetMapping("/ativos")
    public List<Usuario> listarAtivos() throws SmartValidityException {
        return usuarioService.listarAtivos();
    }

    @PostMapping("/{id}/reenviar-convite")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void reenviarConvite(@PathVariable String id) throws SmartValidityException {
        usuarioService.reenviarConvite(id);
    }
}
