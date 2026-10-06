package com.kinal.loan.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalClients {

    private final RestClient userClient;
    private final RestClient bookClient;

    public ExternalClients(@Value("${application.user-service.url}") String userServiceUrl,
                           @Value("${application.book-service.url}") String bookServiceUrl) {
        this.userClient = RestClient.builder().baseUrl(userServiceUrl).build();
        this.bookClient = RestClient.builder().baseUrl(bookServiceUrl).build();
    }

    public void sancionarUsuario(Long usuarioId) {
        userClient.put()
                .uri("/api/v1/usuarios/internal/{id}/sancionar", usuarioId)
                .retrieve()
                .toBodilessEntity();
    }

    public void reducirStock(Long libroId) {
        bookClient.put()
                .uri("/api/v1/libros/internal/{id}/reducir-stock", libroId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("El libro no tiene stock disponible o no existe");
                })
                .toBodilessEntity();
    }

    public void aumentarStock(Long libroId) {
        bookClient.put()
                .uri("/api/v1/libros/internal/{id}/aumentar-stock", libroId)
                .retrieve()
                .toBodilessEntity();
    }
}
