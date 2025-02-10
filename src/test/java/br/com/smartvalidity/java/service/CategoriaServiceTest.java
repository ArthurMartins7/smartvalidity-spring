package br.com.smartvalidity.java.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.repository.CategoriaRepository;
import br.com.smartvalidity.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    public void setUp() {
        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Bebidas");
    }

    @Test
    @DisplayName("Deve listar todas as categorias com sucesso")
    public void testListarTodasCategorias_Sucesso() {
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoria));

        List<Categoria> categorias = categoriaService.listarTodas();

        assertThat(categorias).isNotEmpty();
        assertThat(categorias.size()).isEqualTo(1);
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar uma categoria por ID com sucesso")
    public void testBuscarPorId_Sucesso() throws SmartValidityException {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.buscarPorId(1);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Bebidas");
        verify(categoriaRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar uma categoria inexistente")
    public void testBuscarPorId_Falha() {
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.buscarPorId(999))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Deve salvar uma nova categoria com sucesso")
    public void testSalvarCategoria_Sucesso() {
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.salvar(categoria);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Bebidas");
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    @DisplayName("Deve atualizar uma categoria com sucesso")
    public void testAtualizarCategoria_Sucesso() throws SmartValidityException {
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome("Alimentos");

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(novaCategoria);

        Categoria resultado = categoriaService.atualizar(1, novaCategoria);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Alimentos");
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Deve excluir uma categoria com sucesso")
    public void testExcluirCategoria_Sucesso() throws SmartValidityException {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        doNothing().when(categoriaRepository).delete(categoria);

        categoriaService.excluir(1);

        verify(categoriaRepository, times(1)).delete(categoria);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir uma categoria inexistente")
    public void testExcluirCategoria_Falha() {
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.excluir(999))
                .isInstanceOf(SmartValidityException.class)
                .hasMessageContaining("Categoria não encontrada");
    }
}
