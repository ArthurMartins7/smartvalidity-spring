package br.com.smartvalidity.controller;

import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> create(@RequestBody AlertaRequestDTO dto) {
        return ResponseEntity.ok(alertaService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> findAll() {
        return ResponseEntity.ok(alertaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> findById(@PathVariable Integer id) {
        return alertaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> update(@PathVariable Integer id, @RequestBody AlertaRequestDTO dto) {
        return alertaService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        alertaService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 