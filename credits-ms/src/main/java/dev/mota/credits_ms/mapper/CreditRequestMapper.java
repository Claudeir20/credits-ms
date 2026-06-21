package dev.mota.credits_ms.mapper;

import dev.mota.credits_ms.dto.CreditRequestDTO;
import dev.mota.credits_ms.dto.CreditRequestResponseDTO;
import dev.mota.credits_ms.model.CreditRequest;

import dev.mota.credits_ms.vo.Cpf;
import dev.mota.credits_ms.vo.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class CreditRequestMapper {

    public CreditRequestResponseDTO toResponse(CreditRequest entity) {
        return new CreditRequestResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCpf().toString(),
                entity.getIncome().value(),
                entity.getValueRequest(),
                entity.getTermMonths(),
                entity.getStatus().name(),
                entity.getCorrelationId(),
                entity.getCreatedAt()
        );
    }

    public CreditRequest toEntity(CreditRequestDTO dto) {
        return CreditRequest.request(
                new Cpf(dto.cpf()),
                dto.name(),
                new Income(dto.income()),
                dto.valueRequest(),
                dto.termMonths()
        );
    }
}