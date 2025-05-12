package br.com.smartvalidity.service;

import br.com.smartvalidity.model.dto.EstoqueDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebhookService {

    @Autowired
    private ItemProdutoService itemProdutoService;

    public List<Object> getProdutosVendidos1(final List<Object> itensVendidos) {
        System.out.println("Itens vendidos: ");
        for (Object item : itensVendidos) {
            System.out.println(item);
        }

        //this.itemProdutoService.buscarPorLote("ff");


        return itensVendidos;
    }

    public List<EstoqueDTO> getProdutosVendidos(final List<EstoqueDTO> itensVendidos) {
        System.out.println("Itens vendidos: ");
        for (EstoqueDTO item : itensVendidos) {
            System.out.println(item);
        }

        //this.itemProdutoService.buscarPorLote("ff");


        return itensVendidos;
    }

}


