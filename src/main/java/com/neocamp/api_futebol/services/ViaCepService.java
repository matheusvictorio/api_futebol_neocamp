package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.entities.Address;
import com.neocamp.api_futebol.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ViaCepService {

    private final RestClient restClient;

    public ViaCepService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }

    public Address findByCep(String cep) {
        Address address = restClient.get()
                .uri("/{cep}/json/", cep)
                .retrieve()
                .body(Address.class);

        if (address == null || address.getCep() == null) {
            throw new NotFoundException("CEP não encontrado");
        }

        return address;
    }
}
