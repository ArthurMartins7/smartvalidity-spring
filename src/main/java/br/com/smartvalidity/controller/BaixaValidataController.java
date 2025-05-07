package br.com.smartvalidity.controller;

import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.service.BaixaValidataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/baixa-validata")
public class BaixaValidataController {

    @Autowired
    private BaixaValidataService baixaValidataService;

    @PostMapping
    public ProdutoDTO getBaixaEstoque(@RequestBody final ProdutoDTO produtoDTO) {

        return baixaValidataService.getBaixaEstoque(produtoDTO);
    }

}
