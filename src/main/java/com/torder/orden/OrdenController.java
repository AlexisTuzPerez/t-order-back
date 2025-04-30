package com.torder.orden;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {
    private final OrdenRepository ordenRepository;

    @Autowired
    public OrdenController(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @GetMapping
    public Iterable<Orden> getAllOrders() {
        return ordenRepository.findAll();
    }
}
