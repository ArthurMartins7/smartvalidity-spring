package br.com.smartvalidity.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralFiltroDTO;
import br.com.smartvalidity.model.dto.MuralListagemDTO;
import br.com.smartvalidity.service.MuralListagemService;
import lombok.Data;

/**
 * Controller para o mural de listagem de produtos
 * Disponibiliza endpoints para consulta de produtos por validade
 */
@RestController
@RequestMapping("/mural")
@CrossOrigin(origins = "*")
public class MuralListagemController {

    @Autowired
    private MuralListagemService muralListagemService;

    /**
     * Endpoint para buscar os produtos próximos a vencer (até 15 dias)
     * @return Lista de produtos próximos a vencer
     */
    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<MuralListagemDTO>> getProximosVencer() {
        return ResponseEntity.ok(muralListagemService.getProximosVencer());
    }

    /**
     * Endpoint para buscar os produtos que vencem hoje
     * @return Lista de produtos que vencem hoje
     */
    @GetMapping("/vencem-hoje")
    public ResponseEntity<List<MuralListagemDTO>> getVencemHoje() {
        return ResponseEntity.ok(muralListagemService.getVencemHoje());
    }

    /**
     * Endpoint para buscar os produtos já vencidos
     * @return Lista de produtos vencidos
     */
    @GetMapping("/vencidos")
    public ResponseEntity<List<MuralListagemDTO>> getVencidos() {
        return ResponseEntity.ok(muralListagemService.getVencidos());
    }
    
    /**
     * Endpoint para marcar um item como inspecionado
     * @param id ID do item a ser marcado
     * @param dados Dados contendo o motivo da inspeção
     * @return Item atualizado
     */
    @PutMapping("/inspecionar/{id}")
    public ResponseEntity<?> marcarInspecionado(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, String> dados) {
        try {
            // Validação do motivo da inspeção
            String motivo = (dados != null) ? dados.get("motivo") : null;
            
            if (motivo == null || motivo.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "O motivo da inspeção é obrigatório");
                errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            MuralListagemDTO itemAtualizado = muralListagemService.marcarInspecionado(id, motivo);
            return ResponseEntity.ok(itemAtualizado);
        } catch (SmartValidityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ocorreu um erro ao processar a solicitação");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Classe para representar a requisição com IDs e motivo
     */
    @Data // Usa Lombok para gerar getters, setters, toString, equals e hashCode
    public static class InspecionarLoteRequest {
        private List<String> ids;
        private String motivo;
    }
    
    /**
     * Endpoint para marcar vários itens como inspecionados
     * @param request Request contendo IDs dos itens e motivo da inspeção
     * @return Lista de itens atualizados
     */
    @PutMapping("/inspecionar-lote")
    public ResponseEntity<?> marcarVariosInspecionados(@RequestBody InspecionarLoteRequest request) {
        try {
            // Validação dos IDs
            if (request.getIds() == null || request.getIds().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nenhum item selecionado para inspeção");
                errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Validação do motivo
            if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "O motivo da inspeção é obrigatório");
                errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            List<MuralListagemDTO> itensAtualizados = muralListagemService.marcarVariosInspecionados(
                    request.getIds(), request.getMotivo());
            return ResponseEntity.ok(itensAtualizados);
        } catch (SmartValidityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ocorreu um erro ao processar a solicitação");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para buscar detalhes de um item específico
     * @param id ID do item a ser buscado
     * @return Detalhes do item
     */
    @GetMapping("/item/{id}")
    public ResponseEntity<MuralListagemDTO> getItemById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(muralListagemService.getItemById(id));
        } catch (SmartValidityException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Endpoint para buscar itens com filtros
     * @param filtro Parâmetros de filtro
     * @return Lista de itens filtrados
     */
    @PostMapping("/filtrar")
    public ResponseEntity<List<MuralListagemDTO>> buscarComFiltro(@RequestBody MuralFiltroDTO filtro) {
        return ResponseEntity.ok(muralListagemService.buscarComFiltro(filtro));
    }
    
    /**
     * Classe para representar as opções de filtro
     */
    @Data
    public static class FiltroOpcoesResponse {
        private List<String> marcas;
        private List<String> corredores;
        private List<String> categorias;
        private List<String> fornecedores;
        private List<String> lotes;
    }
    
    /**
     * Endpoint para obter todas as opções disponíveis para filtros
     * @return Opções de filtro
     */
    @GetMapping("/filtro-opcoes")
    public ResponseEntity<FiltroOpcoesResponse> getFiltroOpcoes() {
        FiltroOpcoesResponse opcoes = new FiltroOpcoesResponse();
        opcoes.setMarcas(muralListagemService.getMarcasDisponiveis());
        opcoes.setCorredores(muralListagemService.getCorredoresDisponiveis());
        opcoes.setCategorias(muralListagemService.getCategoriasDisponiveis());
        opcoes.setFornecedores(muralListagemService.getFornecedoresDisponiveis());
        opcoes.setLotes(muralListagemService.getLotesDisponiveis());
        return ResponseEntity.ok(opcoes);
    }
} 