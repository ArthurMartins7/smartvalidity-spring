package br.com.smartvalidity.service;

import br.com.smartvalidity.model.dto.ProdutoDTO;
import org.springframework.stereotype.Service;

@Service
public class BaixaValidataService {

    public ProdutoDTO getBaixaEstoque(final ProdutoDTO produtoDTO) {

        System.out.println("Produto: " + produtoDTO);

        return produtoDTO;
    }
}
