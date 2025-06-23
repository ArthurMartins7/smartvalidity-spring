package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.repository.EnderecoRepository;
import br.com.smartvalidity.service.EnderecoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EnderecoService")
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @InjectMocks
    private EnderecoService enderecoService;

    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setId(1);
        endereco.setCep("12345678");
        endereco.setLogradouro("Rua Test");
        endereco.setNumero("123");
        endereco.setComplemento("Sala 1");
        endereco.setBairro("Bairro Test");
        endereco.setCidade("Cidade Test");
        endereco.setEstado("SP");
        endereco.setPais("Brasil");
    }

    @Test
    @DisplayName("Deve salvar endereço com sucesso")
    void deveSalvarEnderecoComSucesso() {
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);

        Endereco resultado = enderecoService.salvar(endereco);

        assertNotNull(resultado);
        assertEquals(endereco.getId(), resultado.getId());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("Deve listar todos os endereços")
    void deveListarTodosOsEnderecos() {
        List<Endereco> enderecos = Arrays.asList(endereco);
        when(enderecoRepository.findAll()).thenReturn(enderecos);

        List<Endereco> resultado = enderecoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(enderecoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar endereço por ID")
    void deveBuscarEnderecoPorId() throws SmartValidityException {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        Endereco resultado = enderecoService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals(endereco.getId(), resultado.getId());
        verify(enderecoRepository).findById(1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando endereço não encontrado")
    void deveLancarExcecaoQuandoEnderecoNaoEncontrado() {
        when(enderecoRepository.findById(999)).thenReturn(Optional.empty());

        SmartValidityException exception = assertThrows(SmartValidityException.class, () -> {
            enderecoService.buscarPorId(999);
        });

        assertTrue(exception.getMessage().contains("Endereço não encontrada com o ID: 999"));
    }

    @Test
    @DisplayName("Deve atualizar endereço")
    void deveAtualizarEndereco() throws SmartValidityException {
        Endereco enderecoAtualizado = new Endereco();
        enderecoAtualizado.setCep("87654321");
        enderecoAtualizado.setLogradouro("Nova Rua");

        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);

        Endereco resultado = enderecoService.atualizar(1, enderecoAtualizado);

        assertNotNull(resultado);
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("Deve excluir endereço")
    void deveExcluirEndereco() throws SmartValidityException {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        enderecoService.excluir(1);

        verify(enderecoRepository).delete(endereco);
    }
} 