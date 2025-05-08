package br.com.smartvalidity.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebhookService {

    public List<Object> getProdutosVendidos(final List<Object> itensVendidos) {
        System.out.println("Itens vendidos: ");
        for (Object item : itensVendidos) {
            System.out.println(item);
        }
        return itensVendidos;
    }

}


