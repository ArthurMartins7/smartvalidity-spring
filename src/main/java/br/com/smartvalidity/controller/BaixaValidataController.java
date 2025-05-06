package br.com.smartvalidity.controller;

import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.service.BaixaValidataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("baixa-validata")
public class BaixaValidataController {

    @Autowired
    private BaixaValidataService baixaValidataService;

    @GetMapping
    public ProdutoDTO getBaixaEstoque(final ProdutoDTO produtoDTO) {
        return baixaValidataService.getBaixaEstoque(produtoDTO);
    }
}
