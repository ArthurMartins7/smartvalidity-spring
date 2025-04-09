package br.com.smartvalidity.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
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
    public ResponseEntity<MuralListagemDTO> marcarInspecionado(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, String> dados) {
        try {
            String motivo = (dados != null) ? dados.get("motivo") : null;
            return ResponseEntity.ok(muralListagemService.marcarInspecionado(id, motivo));
        } catch (SmartValidityException e) {
            return ResponseEntity.notFound().build();
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
    public ResponseEntity<List<MuralListagemDTO>> marcarVariosInspecionados(@RequestBody InspecionarLoteRequest request) {
        try {
            return ResponseEntity.ok(muralListagemService.marcarVariosInspecionados(request.getIds(), request.getMotivo()));
        } catch (SmartValidityException e) {
            return ResponseEntity.badRequest().build();
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
} 