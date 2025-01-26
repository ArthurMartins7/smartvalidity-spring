package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarTodos() throws SmartValidityException {
        return usuarioService.listarTodos();
    }
}
