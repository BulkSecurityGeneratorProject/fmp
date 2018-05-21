package com.atibusinessgroup.fmp.repository;

import com.atibusinessgroup.fmp.domain.Agent;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Agent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AgentRepository extends MongoRepository<Agent, String> {
	
	Optional<Agent> findOneByIataCode(String iata_code);
}
