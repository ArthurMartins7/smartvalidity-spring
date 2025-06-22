package br.com.smartvalidity.service;

import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    public AlertaResponseDTO create(AlertaRequestDTO dto) {
        Alerta alerta = dto.toEntity();
        alerta = alertaRepository.save(alerta);
        return AlertaResponseDTO.fromEntity(alerta);
    }

    public List<AlertaResponseDTO> findAll() {
        return alertaRepository.findAll().stream()
                .map(AlertaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<AlertaResponseDTO> findById(Integer id) {
        return alertaRepository.findById(id).map(AlertaResponseDTO::fromEntity);
    }

    public Optional<AlertaResponseDTO> update(Integer id, AlertaRequestDTO dto) {
        return alertaRepository.findById(id).map(alerta -> {
            alerta.setTitulo(dto.getTitulo());
            alerta.setDescricao(dto.getDescricao());
            alerta.setDataHoraDisparo(dto.getDataHoraDisparo());
            alerta.setDisparoRecorrente(dto.isDisparoRecorrente());
            alerta.setFrequenciaDisparo(dto.getFrequenciaDisparo());
            // Relacionamentos podem ser atualizados aqui se necessário
            alerta = alertaRepository.save(alerta);
            return AlertaResponseDTO.fromEntity(alerta);
        });
    }

    public void delete(Integer id) {
        alertaRepository.deleteById(id);
    }
} 