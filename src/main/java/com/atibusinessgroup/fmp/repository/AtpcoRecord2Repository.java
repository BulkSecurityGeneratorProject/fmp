package com.atibusinessgroup.fmp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.atibusinessgroup.fmp.domain.atpco.AtpcoRecord2;

/**
 * Spring Data MongoDB repository for the AtpcoRecord2 entity.
 */
@Repository
public interface AtpcoRecord2Repository extends MongoRepository<AtpcoRecord2, String> {

	List<AtpcoRecord2> findAllByRuleTariffNoAndCarrierCodeAndRuleNo(String tariffNo, String carrierCode, String ruleNo);
	
}
