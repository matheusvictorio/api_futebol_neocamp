package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.entities.Address;
import com.neocamp.api_futebol.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class ViaCepService {
    public Address findByCep(String cep) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        Address address = restTemplate.getForObject(url, Address.class);

        if (address.getCep() == null) {
            throw new NotFoundException("CEP não encontrado");
        }
        return address;
    }
}
