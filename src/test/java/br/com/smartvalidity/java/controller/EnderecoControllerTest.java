package br.com.smartvalidity.java.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.smartvalidity.controller.EnderecoController;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.service.EnderecoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EnderecoController")
class EnderecoControllerTest {

    @Mock
    private EnderecoService enderecoService;

    @InjectMocks
    private EnderecoController enderecoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(enderecoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve salvar endereco com sucesso")
    void deveSalvarEnderecoComSucesso() throws Exception {
        // Given
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Test");
        endereco.setNumero("123");
        endereco.setCep("12345678");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setPais("Brasil");

        Endereco enderecoSalvo = new Endereco();
        enderecoSalvo.setId(1);
        enderecoSalvo.setLogradouro("Rua Test");
        enderecoSalvo.setNumero("123");
        enderecoSalvo.setCep("12345678");
        enderecoSalvo.setBairro("Centro");
        enderecoSalvo.setCidade("São Paulo");
        enderecoSalvo.setEstado("SP");
        enderecoSalvo.setPais("Brasil");

        when(enderecoService.salvar(any(Endereco.class))).thenReturn(enderecoSalvo);

        // When & Then
        mockMvc.perform(post("/endereco")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endereco)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.logradouro").value("Rua Test"));

        verify(enderecoService).salvar(any(Endereco.class));
    }

    @Test
    @DisplayName("Deve listar todos os enderecos")
    void deveListarTodosEnderecos() throws Exception {
        // Given
        Endereco endereco1 = new Endereco();
        endereco1.setId(1);
        endereco1.setLogradouro("Rua 1");

        Endereco endereco2 = new Endereco();
        endereco2.setId(2);
        endereco2.setLogradouro("Rua 2");

        List<Endereco> enderecos = Arrays.asList(endereco1, endereco2);
        when(enderecoService.listarTodos()).thenReturn(enderecos);

        // When & Then
        mockMvc.perform(get("/endereco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(enderecoService).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar endereco por ID")
    void deveBuscarEnderecoPorId() throws Exception {
        // Given
        Endereco endereco = new Endereco();
        endereco.setId(1);
        endereco.setLogradouro("Rua Test");

        when(enderecoService.buscarPorId(1)).thenReturn(endereco);

        // When & Then
        mockMvc.perform(get("/endereco/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.logradouro").value("Rua Test"));

        verify(enderecoService).buscarPorId(1);
    }

    @Test
    @DisplayName("Deve atualizar endereco com sucesso")
    void deveAtualizarEnderecoComSucesso() throws Exception {
        // Given
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Atualizada");
        endereco.setNumero("456");
        endereco.setCep("87654321");
        endereco.setBairro("Vila Nova");
        endereco.setCidade("Rio de Janeiro");
        endereco.setEstado("RJ");
        endereco.setPais("Brasil");

        Endereco enderecoAtualizado = new Endereco();
        enderecoAtualizado.setId(1);
        enderecoAtualizado.setLogradouro("Rua Atualizada");
        enderecoAtualizado.setNumero("456");
        enderecoAtualizado.setCep("87654321");
        enderecoAtualizado.setBairro("Vila Nova");
        enderecoAtualizado.setCidade("Rio de Janeiro");
        enderecoAtualizado.setEstado("RJ");
        enderecoAtualizado.setPais("Brasil");

        when(enderecoService.atualizar(eq(1), any(Endereco.class))).thenReturn(enderecoAtualizado);

        // When & Then
        mockMvc.perform(put("/endereco/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endereco)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logradouro").value("Rua Atualizada"));

        verify(enderecoService).atualizar(eq(1), any(Endereco.class));
    }

    @Test
    @DisplayName("Deve excluir endereco com sucesso")
    void deveExcluirEnderecoComSucesso() throws Exception {
        // Given
        doNothing().when(enderecoService).excluir(1);

        // When & Then
        mockMvc.perform(delete("/endereco/1"))
                .andExpect(status().isNoContent());

        verify(enderecoService).excluir(1);
    }
} 