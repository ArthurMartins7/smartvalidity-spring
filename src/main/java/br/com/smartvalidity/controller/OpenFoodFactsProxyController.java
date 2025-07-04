package br.com.smartvalidity.controller;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Endpoint de "pass-through" para a API pública do OpenFoodFacts.
 * O navegador chama este controller (que já está liberado por CORS via {@link br.com.smartvalidity.auth.SecurityConfig}).
 * O backend faz a requisição HTTPS para o OpenFoodFacts e devolve o JSON bruto,
 * resolvendo problemas de CORS no frontend.
 */
@RestController
@RequestMapping("/public/openfoodfacts")
@CrossOrigin(origins = "http://localhost:4200")
public class OpenFoodFactsProxyController {

    private final WebClient web = WebClient.builder()
            .baseUrl("https://world.openfoodfacts.org")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    /**
     * Retorna informações resumidas do produto. A lista de *fields* pode ser alterada no frontend sem mudar o backend.
     * Exemplo de chamada: GET /public/openfoodfacts/product/7894900701517
     */
    @GetMapping("/product/{barcode}")
    public ResponseEntity<String> getProduct(@PathVariable String barcode) {
        // Campos essenciais – adicione ou remova conforme necessidade
        String fields = "code,product_name,brands,quantity,image_front_small_url";
        String v2Path = "/api/v2/product/" + barcode + "?fields=" + fields;

        Mono<String> callV2 = web.get().uri(v2Path)
                .retrieve()
                .bodyToMono(String.class);

        try {
            String body = callV2.block(Duration.ofSeconds(10));
            return ResponseEntity.ok(body);
        } catch (WebClientResponseException e) {
            // se não encontrado na v2, tenta v0
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String v0Path = "/api/v0/product/" + barcode + ".json";
                try {
                    String body = web.get().uri(v0Path)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block(Duration.ofSeconds(10));
                    return ResponseEntity.ok(body);
                } catch (Exception second) {
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                            .body("Falha ao contatar OpenFoodFacts v0: " + second.getMessage());
                }
            }
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body("Falha ao contatar OpenFoodFacts: " + ex.getMessage());
        }
    }
} 