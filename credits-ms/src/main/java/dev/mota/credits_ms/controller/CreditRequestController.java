package dev.mota.credits_ms.controller;

import dev.mota.credits_ms.dto.CreditRequestDTO;
import dev.mota.credits_ms.dto.CreditRequestResponseDTO;
import dev.mota.credits_ms.services.CreditRequestService;
import dev.mota.credits_ms.vo.Cpf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
public class CreditRequestController {

    private final CreditRequestService service;

    @PostMapping
    public ResponseEntity<CreditRequestResponseDTO> request(
            @RequestBody @Valid CreditRequestDTO dto) {

        CreditRequestResponseDTO response = service.requestCredit(dto);

        return new  ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditRequestResponseDTO> findById(@PathVariable UUID id){
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<List<CreditRequestResponseDTO>> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.findByCpf(cpf));
    }

}