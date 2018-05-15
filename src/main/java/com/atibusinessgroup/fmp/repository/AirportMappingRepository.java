package com.atibusinessgroup.fmp.repository;

import com.atibusinessgroup.fmp.domain.AirportMapping;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the AirportMapping entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AirportMappingRepository extends MongoRepository<AirportMapping, String> {

	AirportMapping findOneByCityCode(String cityCode);

}
