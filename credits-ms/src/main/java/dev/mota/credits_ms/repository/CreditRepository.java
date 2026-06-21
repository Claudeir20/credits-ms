package dev.mota.credits_ms.repository;

import dev.mota.credits_ms.model.CreditRequest;
import dev.mota.credits_ms.vo.Cpf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditRepository extends JpaRepository<CreditRequest, UUID> {

    List<CreditRequest> findByCpf(Cpf cpf);

}
