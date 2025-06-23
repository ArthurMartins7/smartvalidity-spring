package br.com.smartvalidity.java.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralDTO;
import br.com.smartvalidity.service.ExcelService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ExcelService")
class ExcelServiceTest {

    @InjectMocks
    private ExcelService excelService;

    private List<MuralDTO.Listagem> itensTest;

    @BeforeEach
    void setUp() {
        // Criar itens de teste
        MuralDTO.Listagem item1 = criarItemListagem("1", "Produto Test 1", "hoje");
        MuralDTO.Listagem item2 = criarItemListagem("2", "Produto Test 2", "vencido");
        MuralDTO.Listagem item3 = criarItemListagem("3", "Produto Test 3", "proximo");
        
        itensTest = Arrays.asList(item1, item2, item3);
    }

    private MuralDTO.Listagem criarItemListagem(String id, String descricao, String status) {
        MuralDTO.Listagem item = new MuralDTO.Listagem();
        item.setId(id);
        item.setStatus(status);
        item.setLote("LOTE" + id);
        item.setCategoria("Categoria Test");
        item.setCorredor("Corredor Test");
        item.setFornecedor("Fornecedor Test");
        item.setInspecionado(false);
        item.setDataFabricacao(LocalDateTime.now().minusDays(30));
        item.setDataRecebimento(LocalDateTime.now().minusDays(25));
        item.setDataValidade(LocalDateTime.now().plusDays(5));
        
        // Criar produto
        MuralDTO.Produto produto = new MuralDTO.Produto();
        produto.setId(id);
        produto.setDescricao(descricao);
        produto.setMarca("Marca Test");
        item.setProduto(produto);
        
        return item;
    }

    @Test
    @DisplayName("Deve gerar Excel com sucesso para lista de itens")
    void deveGerarExcelComSucesso() throws SmartValidityException {
        // Given
        String titulo = "Relatório de Teste";

        // When
        byte[] resultado = excelService.gerarExcelMural(itensTest, titulo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Deve gerar Excel para lista vazia")
    void deveGerarExcelParaListaVazia() throws SmartValidityException {
        // Given
        List<MuralDTO.Listagem> listaVazia = Arrays.asList();
        String titulo = "Relatório Vazio";

        // When
        byte[] resultado = excelService.gerarExcelMural(listaVazia, titulo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Deve gerar Excel com item sem produto")
    void deveGerarExcelComItemSemProduto() throws SmartValidityException {
        // Given
        MuralDTO.Listagem itemSemProduto = new MuralDTO.Listagem();
        itemSemProduto.setId("4");
        itemSemProduto.setStatus("hoje");
        itemSemProduto.setLote("LOTE4");
        itemSemProduto.setProduto(null);
        
        List<MuralDTO.Listagem> itensComNulo = Arrays.asList(itemSemProduto);
        String titulo = "Relatório com Item Sem Produto";

        // When
        byte[] resultado = excelService.gerarExcelMural(itensComNulo, titulo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Deve gerar Excel com datas nulas")
    void deveGerarExcelComDatasNulas() throws SmartValidityException {
        // Given
        MuralDTO.Listagem itemComDatasNulas = criarItemListagem("6", "Produto Com Datas Nulas", "hoje");
        itemComDatasNulas.setDataFabricacao(null);
        itemComDatasNulas.setDataRecebimento(null);
        itemComDatasNulas.setDataValidade(null);
        
        List<MuralDTO.Listagem> itensComDatasNulas = Arrays.asList(itemComDatasNulas);
        String titulo = "Relatório com Datas Nulas";

        // When
        byte[] resultado = excelService.gerarExcelMural(itensComDatasNulas, titulo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Deve gerar Excel com item inspecionado")
    void deveGerarExcelComItemInspecionado() throws SmartValidityException {
        // Given
        MuralDTO.Listagem itemInspecionado = criarItemListagem("5", "Produto Inspecionado", "vencido");
        itemInspecionado.setInspecionado(true);
        itemInspecionado.setMotivoInspecao("Avaria/Quebra");
        itemInspecionado.setUsuarioInspecao("usuario@teste.com");
        itemInspecionado.setDataHoraInspecao(LocalDateTime.now());
        
        List<MuralDTO.Listagem> itensInspecionados = Arrays.asList(itemInspecionado);
        String titulo = "Relatório de Itens Inspecionados";

        // When
        byte[] resultado = excelService.gerarExcelMural(itensInspecionados, titulo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }
} 