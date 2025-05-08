package br.com.smartvalidity.service;

import br.com.smartvalidity.model.dto.ProdutoDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaixaValidataService {

    public ProdutoDTO getBaixaEstoque(final ProdutoDTO produtoDTO) {

        System.out.println("Produto: " + produtoDTO);

        return produtoDTO;
    }

}
