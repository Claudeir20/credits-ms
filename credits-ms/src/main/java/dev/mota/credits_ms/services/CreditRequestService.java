package dev.mota.credits_ms.services;

import dev.mota.credits_ms.Event.consumed.CreditApprovedEvent;
import dev.mota.credits_ms.Event.consumed.CreditRejectEvent;
import dev.mota.credits_ms.dto.CreditRequestDTO;
import dev.mota.credits_ms.dto.CreditRequestResponseDTO;
import dev.mota.credits_ms.mapper.CreditRequestMapper;
import dev.mota.credits_ms.model.CreditRequest;
import dev.mota.credits_ms.repository.CreditRepository;
import dev.mota.credits_ms.vo.Cpf;
import dev.mota.credits_ms.vo.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditRequestService {

    private final CreditRepository repository;
    private final CreditRequestMapper mapper;
    private final OutboxService outboxService;

    @Transactional
    @CacheEvict(value = "solicitacoes", key = "#requestDTO.cpf()")    public CreditRequestResponseDTO requestCredit(CreditRequestDTO requestDTO){

        CreditRequest creditRequest = CreditRequest.request(
                new Cpf(requestDTO.cpf()),
                requestDTO.name(),
                new Income(requestDTO.income()),
                requestDTO.valueRequest(),
                requestDTO.termMonths()
        );

        CreditRequest savedRequest = repository.save(creditRequest);

        outboxService.saveEvent(savedRequest);

         return mapper.toResponse(savedRequest);
    }

    @Transactional
    public void approveFromCreditApprovedEvent(CreditApprovedEvent event) {
        CreditRequest creditRequest = repository.findById(event.requestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        creditRequest.approve();

        repository.save(creditRequest);

        evitCache(creditRequest.getCpf().value());
    }

    @CacheEvict(value = "solicitacoes", key = "#cpf")
    public void evitCache(String cpf){

    }

    public void rejectFromCreditRejectedEvent(CreditRejectEvent event){
        CreditRequest creditRequest = repository.findById(event.requestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        creditRequest.reject();

        repository.save(creditRequest);
        evitCache(creditRequest.getCpf().value());
    }

    @Cacheable(value = "solicitacoes_por_id", key = "#id")
    public CreditRequestResponseDTO findById(UUID id){
        CreditRequest creditRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        return mapper.toResponse(creditRequest);
    }

    @Cacheable(value = "solicitacoes" , key = "#cpf")
    public List<CreditRequestResponseDTO> findByCpf(String cpf) {
        return repository.findByCpf(new Cpf(cpf))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
