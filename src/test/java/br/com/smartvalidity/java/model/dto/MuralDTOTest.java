package br.com.smartvalidity.java.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.smartvalidity.model.dto.MuralDTO;

public class MuralDTOTest {

    @Test
    public void deveTestarListagemDTO() {
        // Given
        LocalDateTime agora = LocalDateTime.now();
        MuralDTO.Produto produto = MuralDTO.Produto.builder()
                .id("1")
                .nome("Produto Teste")
                .descricao("Descrição teste")
                .codigoBarras("123456789")
                .marca("Marca Teste")
                .unidadeMedida("UN")
                .build();

        // When
        MuralDTO.Listagem listagem = MuralDTO.Listagem.builder()
                .id("item-1")
                .itemProduto("Item Produto")
                .produto(produto)
                .categoria("Categoria Teste")
                .corredor("Corredor A")
                .fornecedor("Fornecedor Teste")
                .dataValidade(agora)
                .dataFabricacao(agora.minusDays(30))
                .dataRecebimento(agora.minusDays(20))
                .lote("LOTE123")
                .precoVenda(15.50)
                .status("proximo")
                .inspecionado(false)
                .motivoInspecao(null)
                .usuarioInspecao(null)
                .dataHoraInspecao(null)
                .build();

        // Then
        assertEquals("item-1", listagem.getId());
        assertEquals("Item Produto", listagem.getItemProduto());
        assertEquals(produto, listagem.getProduto());
        assertEquals("Categoria Teste", listagem.getCategoria());
        assertEquals("Corredor A", listagem.getCorredor());
        assertEquals("Fornecedor Teste", listagem.getFornecedor());
        assertEquals("LOTE123", listagem.getLote());
        assertEquals(15.50, listagem.getPrecoVenda());
        assertEquals("proximo", listagem.getStatus());
        assertFalse(listagem.getInspecionado());
    }

    @Test
    public void deveTestarProdutoDTO() {
        // When
        MuralDTO.Produto produto = MuralDTO.Produto.builder()
                .id("prod-1")
                .nome("Produto")
                .descricao("Descrição")
                .codigoBarras("987654321")
                .marca("Marca")
                .unidadeMedida("KG")
                .build();

        // Then
        assertEquals("prod-1", produto.getId());
        assertEquals("Produto", produto.getNome());
        assertEquals("Descrição", produto.getDescricao());
        assertEquals("987654321", produto.getCodigoBarras());
        assertEquals("Marca", produto.getMarca());
        assertEquals("KG", produto.getUnidadeMedida());
    }

    @Test
    public void deveValidarTemPaginacao() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();

        // When/Then - Sem paginação
        assertFalse(filtro.temPaginacao());

        // When/Then - Com limite mas sem página
        filtro.setLimite(10);
        assertFalse(filtro.temPaginacao());

        // When/Then - Com página mas sem limite
        filtro.setLimite(null);
        filtro.setPagina(1);
        assertFalse(filtro.temPaginacao());

        // When/Then - Com limite zero
        filtro.setLimite(0);
        filtro.setPagina(1);
        assertFalse(filtro.temPaginacao());

        // When/Then - Com paginação válida
        filtro.setLimite(10);
        filtro.setPagina(1);
        assertTrue(filtro.temPaginacao());
    }

    @Test
    public void deveObterCorredoresEfetivos() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();

        // When/Then - Sem valores
        assertTrue(filtro.getCorredoresEfetivos().isEmpty());

        // When/Then - Com array preenchido
        filtro.setCorredores(Arrays.asList("Corredor A", "Corredor B"));
        List<String> resultado = filtro.getCorredoresEfetivos();
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("Corredor A"));
        assertTrue(resultado.contains("Corredor B"));

        // When/Then - Com array vazio mas valor único preenchido
        filtro.setCorredores(Arrays.asList());
        filtro.setCorredor("Corredor C");
        resultado = filtro.getCorredoresEfetivos();
        assertEquals(1, resultado.size());
        assertEquals("Corredor C", resultado.get(0));

        // When/Then - Com valor único vazio
        filtro.setCorredores(null);
        filtro.setCorredor("   ");
        assertTrue(filtro.getCorredoresEfetivos().isEmpty());
    }

    @Test
    public void deveObterCategoriasEfetivas() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();

        // When/Then - Array tem prioridade
        filtro.setCategorias(Arrays.asList("Cat A", "Cat B"));
        filtro.setCategoria("Cat C");
        List<String> resultado = filtro.getCategoriasEfetivas();
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("Cat A"));
        assertTrue(resultado.contains("Cat B"));

        // When/Then - Fallback para valor único
        filtro.setCategorias(null);
        resultado = filtro.getCategoriasEfetivas();
        assertEquals(1, resultado.size());
        assertEquals("Cat C", resultado.get(0));
    }

    @Test
    public void deveObterFornecedoresEfetivos() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setFornecedores(Arrays.asList("Forn A"));

        // When/Then
        List<String> resultado = filtro.getFornecedoresEfetivos();
        assertEquals(1, resultado.size());
        assertEquals("Forn A", resultado.get(0));
    }

    @Test
    public void deveObterMarcasEfetivas() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setMarca("Marca Única");

        // When/Then
        List<String> resultado = filtro.getMarcasEfetivas();
        assertEquals(1, resultado.size());
        assertEquals("Marca Única", resultado.get(0));
    }

    @Test
    public void deveObterLotesEfetivos() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setLotes(Arrays.asList("LOTE1", "LOTE2"));

        // When/Then
        List<String> resultado = filtro.getLotesEfetivos();
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("LOTE1"));
        assertTrue(resultado.contains("LOTE2"));
    }

    @Test
    public void deveObterMotivosInspecaoEfetivos() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setMotivoInspecao("Avaria");

        // When/Then
        List<String> resultado = filtro.getMotivosInspecaoEfetivos();
        assertEquals(1, resultado.size());
        assertEquals("Avaria", resultado.get(0));
    }

    @Test
    public void deveObterUsuariosInspecaoEfetivos() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setUsuariosInspecao(Arrays.asList("User1", "User2"));

        // When/Then
        List<String> resultado = filtro.getUsuariosInspecaoEfetivos();
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("User1"));
        assertTrue(resultado.contains("User2"));
    }

    @Test
    public void deveTestarInspecaoRequest() {
        // When
        MuralDTO.InspecaoRequest request = MuralDTO.InspecaoRequest.builder()
                .motivo("Avaria")
                .motivoCustomizado("Produto danificado")
                .usuarioInspecao("usuario1")
                .build();

        // Then
        assertEquals("Avaria", request.getMotivo());
        assertEquals("Produto danificado", request.getMotivoCustomizado());
        assertEquals("usuario1", request.getUsuarioInspecao());
    }

    @Test
    public void deveTestarInspecaoLoteRequest() {
        // Given
        List<String> ids = Arrays.asList("item1", "item2", "item3");

        // When
        MuralDTO.InspecaoLoteRequest request = MuralDTO.InspecaoLoteRequest.builder()
                .ids(ids)
                .motivo("Promoção")
                .motivoCustomizado(null)
                .usuarioInspecao("admin")
                .build();

        // Then
        assertEquals(ids, request.getIds());
        assertEquals("Promoção", request.getMotivo());
        assertNull(request.getMotivoCustomizado());
        assertEquals("admin", request.getUsuarioInspecao());
    }

    @Test
    public void deveTestarRelatorioRequest() {
        // Given
        MuralDTO.Filtro filtro = new MuralDTO.Filtro();
        filtro.setStatus("vencido");
        List<String> ids = Arrays.asList("item1", "item2");

        // When
        MuralDTO.RelatorioRequest request = MuralDTO.RelatorioRequest.builder()
                .tipo("SELECIONADOS")
                .ids(ids)
                .filtro(filtro)
                .status("vencido")
                .build();

        // Then
        assertEquals("SELECIONADOS", request.getTipo());
        assertEquals(ids, request.getIds());
        assertEquals(filtro, request.getFiltro());
        assertEquals("vencido", request.getStatus());
    }

    @Test
    public void deveTestarTipoRelatorioEnum() {
        // When/Then
        assertEquals("SELECIONADOS", MuralDTO.TipoRelatorio.SELECIONADOS.name());
        assertEquals("PAGINA", MuralDTO.TipoRelatorio.PAGINA.name());
        assertEquals("TODOS", MuralDTO.TipoRelatorio.TODOS.name());
        
        // Testa valueOf
        assertEquals(MuralDTO.TipoRelatorio.SELECIONADOS, MuralDTO.TipoRelatorio.valueOf("SELECIONADOS"));
        assertEquals(MuralDTO.TipoRelatorio.PAGINA, MuralDTO.TipoRelatorio.valueOf("PAGINA"));
        assertEquals(MuralDTO.TipoRelatorio.TODOS, MuralDTO.TipoRelatorio.valueOf("TODOS"));
    }
} 