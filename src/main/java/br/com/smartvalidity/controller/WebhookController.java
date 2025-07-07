package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.EstoqueDTO;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.service.BaixaValidataService;
import br.com.smartvalidity.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/webhook")
public class WebhookController {

    @Autowired
    private BaixaValidataService baixaValidataService;

    @Autowired
    private WebhookService webhookService;

    @PostMapping
    public ProdutoDTO getBaixaEstoque(@RequestBody final ProdutoDTO produtoDTO) {

        return baixaValidataService.getBaixaEstoque(produtoDTO);
    }

    /*
    @PostMapping("/baixa-validata")
    public ResponseEntity<List<Object>> getItensVendidos1(@RequestBody final List<Object> itensVendidos) {
        List<Object> response = webhookService.getProdutosVendidos1(itensVendidos);
        return ResponseEntity.ok(response);
    }
     */

    @PostMapping("/baixa-validata")
    public ResponseEntity<List<EstoqueDTO>> getItensVendidos(@RequestBody final List<EstoqueDTO> itensVendidos) throws SmartValidityException {
        List<EstoqueDTO> response = webhookService.getProdutosVendidos(itensVendidos);
        return ResponseEntity.ok(response);
    }

}
