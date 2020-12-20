package com.neirodiaz.prospects.repository;

import com.neirodiaz.prospects.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query(value = "SELECT c FROM Candidate c WHERE c.identification IN :ids")
    List<Candidate> findByIdentifications(@Param("ids") Collection<Long> ids);

    Candidate findByIdentification(Long identification);
}
