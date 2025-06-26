package br.com.smartvalidity.auth;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void verificarPerfilAcesso() throws SmartValidityException {
        Usuario usuarioAutenticado = authenticationService.getUsuarioAutenticado();

        if (usuarioAutenticado.getPerfilAcesso() == PerfilAcesso.OPERADOR) {
            throw new SmartValidityException("Usuário sem permissão de acesso!");
        }
    }

    public void verifiarCredenciaisUsuario(String idUsuarioURL) throws SmartValidityException {
        Usuario usuarioURL = usuarioRepository.findById(idUsuarioURL).orElseThrow(
                () -> new SmartValidityException("Usuário não encontrado"));

        Usuario usuarioAutenticado = authenticationService.getUsuarioAutenticado();

        if (usuarioAutenticado.getId() != usuarioURL.getId()) {
            throw new SmartValidityException("Somente o portador da conta pode executar essa ação!");
        }
    }
}
