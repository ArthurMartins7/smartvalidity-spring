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
import br.com.smartvalidity.model.dto.MuralDTO;
import br.com.smartvalidity.service.MuralService;
import lombok.Data;

@RestController
@RequestMapping("/mural")
@CrossOrigin(origins = "*")
public class MuralController {

    @Autowired
    private MuralService muralService;

    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<MuralDTO.Listagem>> getProximosVencer() {
        return ResponseEntity.ok(muralService.getProximosVencer());
    }

    @GetMapping("/vencem-hoje")
    public ResponseEntity<List<MuralDTO.Listagem>> getVencemHoje() {
        return ResponseEntity.ok(muralService.getVencemHoje());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<MuralDTO.Listagem>> getVencidos() {
        return ResponseEntity.ok(muralService.getVencidos());
    }

    @GetMapping("/motivos-inspecao")
    public ResponseEntity<List<String>> getMotivosInspecaoValidos() {
        return ResponseEntity.ok(muralService.getMotivosInspecaoValidos());
    }

    @PutMapping("/inspecionar/{id}")
    public ResponseEntity<?> marcarInspecionado(
            @PathVariable String id,
            @RequestBody MuralDTO.InspecaoRequest request) {
        try {
            MuralDTO.Listagem itemAtualizado = muralService.marcarInspecionado(
                id,
                request.getMotivo(),
                request.getMotivoCustomizado(),
                request.getUsuarioInspecao()
            );
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
    
    @PutMapping("/inspecionar-lote")
    public ResponseEntity<?> marcarVariosInspecionados(@RequestBody MuralDTO.InspecaoLoteRequest request) {
        try {
            List<MuralDTO.Listagem> itensAtualizados = muralService.marcarVariosInspecionados(
                request.getIds(),
                request.getMotivo(),
                request.getMotivoCustomizado(),
                request.getUsuarioInspecao()
            );
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
    
    @GetMapping("/item/{id}")
    public ResponseEntity<MuralDTO.Listagem> getItemById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(muralService.getItemById(id));
        } catch (SmartValidityException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/filtrar")
    public ResponseEntity<List<MuralDTO.Listagem>> buscarComFiltro(@RequestBody MuralDTO.Filtro filtro) {
        return ResponseEntity.ok(muralService.buscarComFiltro(filtro));
    }
    
    @PostMapping("/contar-paginas")
    public ResponseEntity<Integer> contarPaginas(@RequestBody MuralDTO.Filtro filtro) {
        int totalPaginas = muralService.contarPaginas(filtro);
        return ResponseEntity.ok(totalPaginas);
    }
    
    @PostMapping("/contar-registros")
    public ResponseEntity<Long> contarTotalRegistros(@RequestBody MuralDTO.Filtro filtro) {
        long totalRegistros = muralService.contarTotalRegistros(filtro);
        return ResponseEntity.ok(totalRegistros);
    }
    
    @Data
    public static class FiltroOpcoesResponse {
        private List<String> marcas;
        private List<String> corredores;
        private List<String> categorias;
        private List<String> fornecedores;
        private List<String> lotes;
        private List<String> usuariosInspecao;
    }
    
    @GetMapping("/usuarios-inspecao")
    public ResponseEntity<List<String>> getUsuariosInspecao() {
        List<String> usuarios = muralService.getUsuariosInspecaoDisponiveis();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/filtro-opcoes")
    public ResponseEntity<FiltroOpcoesResponse> getFiltroOpcoes() {
        FiltroOpcoesResponse response = new FiltroOpcoesResponse();
        response.setMarcas(muralService.getMarcasDisponiveis());
        response.setCorredores(muralService.getCorredoresDisponiveis());
        response.setCategorias(muralService.getCategoriasDisponiveis());
        response.setFornecedores(muralService.getFornecedoresDisponiveis());
        response.setLotes(muralService.getLotesDisponiveis());
        response.setUsuariosInspecao(muralService.getUsuariosInspecaoDisponiveis());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/buscar-por-ids")
    public ResponseEntity<List<MuralDTO.Listagem>> buscarPorIds(@RequestBody List<String> ids) {
        try {
            List<MuralDTO.Listagem> itens = muralService.buscarPorIds(ids);
            return ResponseEntity.ok(itens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/relatorio")
    public ResponseEntity<?> gerarRelatorio(@RequestBody MuralDTO.RelatorioRequest request) {
        try {
            byte[] relatorio = muralService.gerarRelatorioExcel(request);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=relatorio-mural.xlsx")
                .body(relatorio);
        } catch (SmartValidityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro ao gerar relatório");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/cancelar-selecao")
    public ResponseEntity<?> cancelarSelecao(@RequestBody List<String> ids) {
        muralService.cancelarSelecao(ids);
        return ResponseEntity.ok().build();
    }
}