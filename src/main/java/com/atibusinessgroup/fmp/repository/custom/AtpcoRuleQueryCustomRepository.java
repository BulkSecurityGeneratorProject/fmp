package com.atibusinessgroup.fmp.repository.custom;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.atibusinessgroup.fmp.constant.CollectionName;
import com.atibusinessgroup.fmp.domain.atpco.AtpcoRecord2;
import com.atibusinessgroup.fmp.domain.atpco.AtpcoRecord8;
import com.atibusinessgroup.fmp.domain.dto.AtpcoRecord1FareClassInformation;
import com.atibusinessgroup.fmp.domain.dto.AtpcoRecord2GroupByRuleNoCxrTarNo;
import com.atibusinessgroup.fmp.domain.dto.FareClassGroup;
import com.atibusinessgroup.fmp.domain.dto.FareClassQuery;
import com.atibusinessgroup.fmp.domain.dto.FareClassQueryParam;
import com.atibusinessgroup.fmp.domain.dto.Rec8Param;
import com.atibusinessgroup.fmp.domain.dto.RuleQueryParam;
import com.atibusinessgroup.fmp.service.util.DateUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service
public class AtpcoRuleQueryCustomRepository {

	@Autowired
	MongoTemplate mongoTemplate;
	
	
	public Page<AtpcoRecord2GroupByRuleNoCxrTarNo> getQueryByType(RuleQueryParam param, Pageable pageable) {
		
		List<AggregationOperation> aggregationOperations = getAggregationGroupingByType(param, pageable);
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		System.out.println(aggregation);

		List<AtpcoRecord2GroupByRuleNoCxrTarNo> result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_2, AtpcoRecord2GroupByRuleNoCxrTarNo.class).getMappedResults();
		
		int total = 0;
		
		if(result.size() > 0) {
			total = Integer.parseInt(result.get(0).getTotal());
		}
		
		return new PageImpl<>(result, pageable, total);
	}
	
	public List<AtpcoRecord2> getRuleDetails(RuleQueryParam param) {
		
		List<AtpcoRecord2> rec2 = new ArrayList<>();
		
		Date today = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, 0);
		Date twoYearsBefore = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, -2);
		
		String recordId = param.getRuleTarNo()+param.getCxr()+param.getRuleNo();
		Query query = new Query();

		query.addCriteria(Criteria.where("record_id").is(recordId));
		
		Criteria includeDisc1 = Criteria.where("dates_disc").lte(today).gte(twoYearsBefore);
		Criteria includeDisc2 = Criteria.where("dates_disc").is("indef");
		Criteria orInclude1 = new Criteria();
		orInclude1.orOperator(includeDisc1, includeDisc2);
		
		Criteria notIncludeDisc1 = Criteria.where("dates_disc").gte(today);
		Criteria orInclude2 = new Criteria();
		orInclude2.orOperator(notIncludeDisc1, includeDisc2);
		
		if(param.isIncludeDisc()) {
			query.addCriteria(orInclude1);
		} else {
			query.addCriteria(orInclude2);
		}
		
		
		rec2 = mongoTemplate.find(query, AtpcoRecord2.class);
		
		return rec2;
		
	}

	
	public List<AtpcoRecord2> getListRecord2ById(String recordId) {
		List<AtpcoRecord2> result = new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("record_id").is(recordId));
		result = mongoTemplate.find(query, AtpcoRecord2.class);
		return result;
	}
	
	// GET RECORD 8
	public Page<AtpcoRecord8> getListRec8(Rec8Param param, Pageable pageable) {
		
		Date today = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, 0);
		Date twoYearsBefore = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, -2);
		
		SkipOperation skip = new SkipOperation(pageable.getPageNumber() * pageable.getPageSize());
		LimitOperation limit = new LimitOperation(pageable.getPageSize());
		
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject andQuery = new BasicDBObject();

				List<BasicDBObject> and = new ArrayList<>();
				
			
				if (param.getCxr() != null && !param.getCxr().isEmpty()) {
					and.add(new BasicDBObject("cxr_code", param.getCxr()));
				} else {
					and.add(new BasicDBObject("cxr_code", new BasicDBObject("$exists", true)));
				}
				
				if (param.getRuleNo() != null && !param.getRuleNo().isEmpty()) {
					and.add(new BasicDBObject("rule_no", param.getRuleNo()));
				} else {
					and.add(new BasicDBObject("rule_no", new BasicDBObject("$exists", true)));
				}

				if (param.getRuleTarNo() != null && !param.getRuleTarNo().isEmpty()) {
					and.add(new BasicDBObject("tariff", param.getRuleTarNo()));
				} else {
					and.add(new BasicDBObject("tariff", new BasicDBObject("$exists", true)));
				}
				
				if (param.getAccountCode() != null && !param.getAccountCode().isEmpty()) {
					and.add(new BasicDBObject("account_code", param.getAccountCode()));
				} else {
					and.add(new BasicDBObject("account_code", new BasicDBObject("$exists", true)));
				}
				
				
				if(param.isIncludeDisc()) {
					and.add(new BasicDBObject("$or", Arrays.asList(
							new BasicDBObject("dates_disc", new BasicDBObject("$lte", today).append("$gte", twoYearsBefore)), 
							new BasicDBObject("dates_disc", "indef"))));
					
				} else {
					and.add(new BasicDBObject("$or", Arrays.asList(
							new BasicDBObject("dates_disc", new BasicDBObject("$gte", today)), 
							new BasicDBObject("dates_disc", "indef"))));
				}

				if (and.size() > 0) {
					andQuery.append("$and", and);
				}

				match.append("$match", andQuery);

				return match;
			}
		});
		
		
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject lookup = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("from", CollectionName.ATPCO_MASTER_TARIFF);
				query.append("localField", "tariff");
				query.append("foreignField", "tar_no");
				query.append("as", "m_tariff");
				lookup.append("$lookup", query);
				return lookup;
			}
		});
		
//		aggregationOperations.add(new AggregationOperation() {
//			
//			@Override
//			public DBObject toDBObject(AggregationOperationContext context) {
//				BasicDBObject match = new BasicDBObject();
//				BasicDBObject query = new BasicDBObject();
//				
//				query.append("rec8", new BasicDBObject("$ne", Arrays.asList()));
//				
//				match.append("$match", query);
//				return match;
//			}
//		});
//		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject unwind = new BasicDBObject();
				
				unwind.append("$unwind", new BasicDBObject("path",  "$m_tariff").append("preserveNullAndEmptyArrays", true));
				
				return unwind;
			}
		});

//		aggregationOperations.add(new AggregationOperation() {
//			
//			@Override
//			public DBObject toDBObject(AggregationOperationContext context) {
//				BasicDBObject match = new BasicDBObject();
//				BasicDBObject query = new BasicDBObject();
//				
//				query.append("m_tariff.type", "FARE BY RULE");
//				query.append("m_tariff.pp", "public");
//				
//				match.append("$match", query);
//				return match;
//			}
//		});
		
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject projection = new BasicDBObject();
				projection.append("$project", 
						new BasicDBObject("cxr_code", "$cxr_code")
						.append("tariff", "$tariff")
						.append("tar_cd", "$m_tariff.tar_cd")
						.append("description", "$m_tariff.description")
						.append("rule_no", "$rule_no")
						.append("cat35", "$cat35")
						.append("prim_pass_type", "$prim_pass_type")
						.append("account_code", "$account_code")
						.append("fare_geo_scope_global", "$fare_geo_scope_global")
						.append("fare_geo_scope_loc_1", "$fare_geo_scope_loc_1")
						.append("fare_geo_scope_loc_2", "$fare_geo_scope_loc_2")
						.append("dates_eff","$dates_eff")
						.append("dates_disc", "$dates_disc")
						);
				return projection;
			}
		});
		
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		System.out.println(aggregation);
		
		aggregationOperations.add(skip);

		aggregationOperations.add(limit);

		Aggregation aggregationPagination = newAggregation(aggregationOperations);

		List<AtpcoRecord8> result = mongoTemplate.aggregate(aggregationPagination, CollectionName.ATPCO_RECORD_8, AtpcoRecord8.class).getMappedResults();
		
		return new PageImpl<>(result, pageable, mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_8, AtpcoRecord8.class).getMappedResults().size());
	
	}
	
	public List<AggregationOperation> getAggregationGroupingByType(RuleQueryParam param, Pageable pageable) {
		
		Date today = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, 0);
		Date twoYearsBefore = DateUtil.convertObjectToDateWithParam(new Date(), 0, 0, -2);
		
		List<String> type = new ArrayList<>();
		if(param.getType() != null && !param.getType().isEmpty()) {
			if(param.getType().equals("1")) {
				type.add("FARE BY RULE");
			} else if(param.getType().equals("2")) {
				type.add("GENERAL RULE");
			} if(param.getType().equals("3")) {
				type.add("FARE RULE");
			}
		} else {
			type.add("FARE BY RULE");
			type.add("GENERAL RULE");
			type.add("FARE RULE");
		}
		
		
//		final List<String> typeFinal = type;

		List<AggregationOperation> aggregationOperations = new ArrayList<>();

		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject andQuery = new BasicDBObject();

				List<BasicDBObject> and = new ArrayList<>();

				if (param.getCxr() != null && !param.getCxr().isEmpty()) {
					and.add(new BasicDBObject("cxr_code", param.getCxr()));
				} else {
					and.add(new BasicDBObject("cxr_code", new BasicDBObject("$exists", true)));
				}
				
				if (param.getRuleNo() != null && !param.getRuleNo().isEmpty()) {
					and.add(new BasicDBObject("rule_no", param.getRuleNo()));
				} else {
					and.add(new BasicDBObject("rule_no", new BasicDBObject("$exists", true)));
				}

				if (param.getRuleTarNo() != null && !param.getRuleTarNo().isEmpty()) {
					and.add(new BasicDBObject("rule_tar_no", param.getRuleTarNo()));
				} else {
					and.add(new BasicDBObject("rule_tar_no", new BasicDBObject("$exists", true)));
				}
				
				if (param.getCatNo() != null && !param.getCatNo().isEmpty()) {
					and.add(new BasicDBObject("cat_no", param.getCatNo()));
				} else {
					and.add(new BasicDBObject("cat_no", new BasicDBObject("$exists", true)));
				}
				
				
				if(param.isIncludeDisc()) {
					and.add(new BasicDBObject("$or",
							Arrays.asList(
									new BasicDBObject("dates_disc", new BasicDBObject("$gte", twoYearsBefore)),
									new BasicDBObject("dates_disc", "indef")))
							);
					
				} else {
					and.add(new BasicDBObject("$or", Arrays.asList(
							new BasicDBObject("dates_disc", new BasicDBObject("$gte", today)), 
							new BasicDBObject("dates_disc", "indef"))));
				}

				
				if (and.size() > 0) {
					andQuery.append("$and", and);
				}

				match.append("$match", andQuery);

				return match;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject group = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("_id", 
						new BasicDBObject("cxr", "$cxr_code")
						.append("ruleNo", "$rule_no")
						.append("tarNo", "$rule_tar_no"));
				group.append("$group", query);
				return group;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject unwind = new BasicDBObject();
				unwind.append("$unwind", "$_id"); 	
				return unwind;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject lookup = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("from", CollectionName.ATPCO_MASTER_TARIFF);
				query.append("let", new BasicDBObject("tarNo", "$_id.tarNo"));
				query.append("pipeline", Arrays.asList(
						new BasicDBObject("$match", 
								new BasicDBObject("$expr", 
										new BasicDBObject("$and", Arrays.asList(
												new BasicDBObject("$eq", Arrays.asList("$tar_no", "$$tarNo")), 
												new BasicDBObject("$in", Arrays.asList("$type", type)),
												new BasicDBObject("$eq", Arrays.asList("$pp", "public"))
												)
											)
										)
									)
						));
				query.append("as", "m_tariff");
				lookup.append("$lookup", query);
				return lookup;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject unwind = new BasicDBObject("$unwind", new BasicDBObject("path", "$m_tariff").append("preserveNullAndEmptyArrays", true));
				return unwind;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject group = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("_id", 
						new BasicDBObject("cxr", "$_id.cxr")
						.append("ruleNo", "$_id.ruleNo")
						.append("tarNo", "$_id.tarNo")
						.append("type", "$m_tariff.type"));
				group.append("$group", query);
				return group;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				
				query.append("tmp", "$_id");
				project.append("$project", query);
				return project;
			}
		});
		
		
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject group = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("_id", null);
				query.append("total", new BasicDBObject("$sum", 1));
				query.append("data", new BasicDBObject("$addToSet", "$tmp"));
				group.append("$group", query);
				return group;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				
				query.append("_id", "null");
				query.append("total", "$total");
				query.append("data", new BasicDBObject("$slice", Arrays.asList("$data", pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize())));
				project.append("$project", query);
				return project;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject unwind = new BasicDBObject();
				unwind.append("$unwind", "$data"); 	
				return unwind;
			}
		});
		
		
		aggregationOperations.add(new AggregationOperation() {
			
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				
				query.append("_id", 0);
				query.append("total", "$total");
				query.append("cxr", "$data.cxr");
				query.append("tarNo", "$data.tarNo");
				query.append("ruleNo", "$data.ruleNo");
				query.append("type", "$data.type");
				project.append("$project", query);
				return project;
			}
		});


		return aggregationOperations;
	}	
	
	
	public Page<FareClassGroup> getListFareClasses(FareClassQueryParam param, Pageable pageable) {
		List<AggregationOperation> aggregationOperations = getFareClassAggregation(param);
		Aggregation aggregation = newAggregation(aggregationOperations);
		System.out.println("aggregation "+aggregation);
		SkipOperation skip = new SkipOperation(pageable.getPageNumber() * pageable.getPageSize());
		aggregationOperations.add(skip);
		LimitOperation limit = new LimitOperation(pageable.getPageSize());
		aggregationOperations.add(limit);
		Aggregation aggregationPagination = newAggregation(aggregationOperations);
		
		List<FareClassGroup> result = mongoTemplate.aggregate(aggregationPagination, FareClassGroup.class, FareClassGroup.class).getMappedResults();
		long allResultCount = mongoTemplate.aggregate(aggregation, FareClassGroup.class, FareClassGroup.class).getMappedResults().size();
		
		return new PageImpl<>(result, pageable, allResultCount);
	}
	
	public List<FareClassQuery> getFareClassGroups(FareClassGroup param) {
		List<AggregationOperation> aggregationOperations = getFareClassGroupAggregation(param);
		Aggregation aggregation = newAggregation(aggregationOperations);
		List<FareClassQuery> result = mongoTemplate.aggregate(aggregation, FareClassQuery.class, FareClassQuery.class).getMappedResults();
		for (FareClassQuery fareClassQuery : result) {
			for (AtpcoRecord1FareClassInformation fci:fareClassQuery.getFareClassInformation()) {
				for (String rbd:fci.getRbd()) {
					if (rbd != null && !rbd.trim().isEmpty() && !fareClassQuery.getBkcd().contains(rbd.trim())) {
						fareClassQuery.getBkcd().add(rbd.trim());
					}
				}
				if (fci.getPassengerType() != null && !fci.getPassengerType().trim().isEmpty() && !fareClassQuery.getPaxType().contains(fci.getPassengerType().trim())) {
					fareClassQuery.getPaxType().add(fci.getPassengerType().trim());
				}
			}
		}
		return result;
	}
	
	private List<AggregationOperation> getFareClassAggregation(FareClassQueryParam param) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				
				if (param.getCxr() != null && !param.getCxr().isEmpty()) {
					match.append("cxr_code", param.getCxr());
				}
				if (param.getRuleNo() != null && !param.getRuleNo().isEmpty()) {
					match.append("rule_no", param.getRuleNo());
				}
				if (param.getTarNo() != null && !param.getTarNo().isEmpty()) {
					match.append("rule_tar_no", param.getTarNo());
				}
				if (param.getFareClass() != null && !param.getFareClass().isEmpty()) {
					match.append("fare_class", param.getFareClass());
				}
				if (param.getFareType() != null && !param.getFareType().isEmpty()) {
					match.append("fare_type", param.getFareType());
				}
				
				return new BasicDBObject("$match", match);
			}
		});
		
		if (param.getPsgrType() != null && !param.getPsgrType().isEmpty()) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					return new BasicDBObject("$unwind", "$fare_class_information");
				}
			});
		}
		
		if (param.getBookingClass() != null && !param.getBookingClass().isEmpty()) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					return new BasicDBObject("$unwind", "$fare_class_information.rbd");
				}
			});
		}
		
		if ((param.getPsgrType() != null && !param.getPsgrType().isEmpty()) || (param.getBookingClass() != null && !param.getBookingClass().isEmpty())) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					BasicDBObject match = new BasicDBObject();
					if (param.getPsgrType() != null && !param.getPsgrType().isEmpty()) {
						match.append("fare_class_information.psgr_type", param.getPsgrType());
					}
					if (param.getBookingClass() != null && !param.getBookingClass().isEmpty()) {
						match.append("fare_class_information.rbd", new BasicDBObject("$regex", param.getBookingClass()).append("$options", "i"));
					}
					
					return new BasicDBObject("$match", match);
				}
			});
		}
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject group = new BasicDBObject();
				BasicDBObject idGroup = new BasicDBObject();
				idGroup.append("cxr_code", "$cxr_code");
				idGroup.append("tar_no", "$rule_tar_no");
				idGroup.append("rule_no", "$rule_no");
				
				if (param.getFareClass() != null && !param.getFareClass().isEmpty()) {
					idGroup.append("fare_class", "$fare_class");
				}
				if (param.getFareType() != null && !param.getFareType().isEmpty()) {
					idGroup.append("fare_type", "$fare_type");
				}
				
				group.append("_id", idGroup);
				
				return new BasicDBObject("$group", group);
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("_id", 0);
				project.append("cxr_code", "$_id.cxr_code");
				project.append("tarNo", "$_id.tar_no");
				project.append("rule_no", "$_id.rule_no");
				
				return new BasicDBObject("$project", project);
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject lookup = new BasicDBObject();
				lookup.append("from", "master_tariff");
				lookup.append("let", new BasicDBObject("tar_no", "$tarNo"));
				lookup.append("pipeline", Arrays.asList(
						new BasicDBObject("$match", 
								new BasicDBObject("$expr", 
										new BasicDBObject("$and", 
												Arrays.asList(
														new BasicDBObject("$eq", Arrays.asList("$tar_no", "$$tar_no")), 
														new BasicDBObject("$eq", Arrays.asList("$type", "FARE")),
														new BasicDBObject("$eq", Arrays.asList("$pp", "public"))
												)
										)
								)
						)
				));
				lookup.append("as", "master_tariff");
				
				return new BasicDBObject("$lookup", lookup);
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				return new BasicDBObject("$unwind", new BasicDBObject("path", "$master_tariff").append("preserveNullAndEmptyArrays", true));
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("_id", 0);
				project.append("cxr_code", "$cxr_code");
				project.append("tar_no", "$tarNo");
				project.append("tar_cd", "$master_tariff.tar_cd");
				project.append("description", "$master_tariff.description");
				project.append("rule_no", "$rule_no");
				
				return new BasicDBObject("$project", project);
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				return new BasicDBObject("$sort", new BasicDBObject("tar_no", 1));
			}
		});
		
		return aggregationOperations;
	}
	
	private List<AggregationOperation> getFareClassGroupAggregation(FareClassGroup param) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				match.append("cxr_code", param.getCxr());
				match.append("rule_no", param.getRuleNo());
				match.append("rule_tar_no", param.getTarNo());
				if (param.getFareClass() != null && !param.getFareClass().isEmpty()) {
					match.append("fare_class", param.getFareClass());
				}
				if (param.getFareType() != null && !param.getFareType().isEmpty()) {
					match.append("fare_type", param.getFareType());
				}
				
				return new BasicDBObject("$match", match);
			}
		});
		
		if (param.getPsgrType() != null && !param.getPsgrType().isEmpty()) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					return new BasicDBObject("$unwind", "$fare_class_information");
				}
			});
		}
		
		if (param.getBookingClass() != null && !param.getBookingClass().isEmpty()) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					return new BasicDBObject("$unwind", "$fare_class_information.rbd");
				}
			});
		}
		
		if ((param.getPsgrType() != null && !param.getPsgrType().isEmpty()) || (param.getBookingClass() != null && !param.getBookingClass().isEmpty())) {
			aggregationOperations.add(new AggregationOperation() {
				@Override
				public DBObject toDBObject(AggregationOperationContext context) {
					BasicDBObject match = new BasicDBObject();
					if (param.getPsgrType() != null && !param.getPsgrType().isEmpty()) {
						match.append("fare_class_information.psgr_type", param.getPsgrType());
					}
					if (param.getBookingClass() != null && !param.getBookingClass().isEmpty()) {
						match.append("fare_class_information.rbd", new BasicDBObject("$regex", param.getBookingClass()).append("$options", "i"));
					}
					
					return new BasicDBObject("$match", match);
				}
			});
		}
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject lookup = new BasicDBObject();
				lookup.append("from", "master_tariff");
				lookup.append("let", new BasicDBObject("tar_no", "$rule_tar_no"));
				lookup.append("pipeline", Arrays.asList(
						new BasicDBObject("$match", 
								new BasicDBObject("$expr", 
										new BasicDBObject("$and", 
												Arrays.asList(
														new BasicDBObject("$eq", Arrays.asList("$tar_no", "$$tar_no")), 
														new BasicDBObject("$eq", Arrays.asList("$type", "FARE")),
														new BasicDBObject("$eq", Arrays.asList("$pp", "public"))
												)
										)
								)
						)
				));
				lookup.append("as", "master_tariff");
				
				return new BasicDBObject("$lookup", lookup);
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				return new BasicDBObject("$unwind", new BasicDBObject("path", "$master_tariff").append("preserveNullAndEmptyArrays", true));
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("cxr_code", "$cxr_code");
				project.append("tar_no", "$rule_tar_no");
				project.append("tar_cd", "$master_tariff.tar_cd");
				project.append("description", "$master_tariff.description");
				project.append("rule_no", "$rule_no");
				project.append("fare_class", "$fare_class");
				project.append("seq_no", "$seq_no");
				project.append("geo_type_1", "$geo_type_1");
				project.append("geo_loc_1", "$geo_loc_1");
				project.append("geo_type_2", "$geo_type_2");
				project.append("geo_loc_2", "$geo_loc_2");
				project.append("ft_nt", "$ft_nt");
				project.append("rtg_no", "$rtg_no");
				project.append("season_type", "$season_type");
				project.append("day_of_week_type", "$day_of_week_type");
				project.append("fare_type", "$fare_type");
				project.append("owrt", "$owrt");
				project.append("normal_special", "$prc_cat");
				project.append("display_type", "$dis_cat");
				project.append("dates_eff", "$dates_eff");
				project.append("dates_disc", "$dates_disc");
				project.append("fare_class_information", "$fare_class_information");
				
				return new BasicDBObject("$project", project);
			}
		});
		
		return aggregationOperations;
	}
	
	public List<String> getFareClassText(FareClassQuery param) {
		List<String> fareClassTextList = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		
		String datesEffFormat = "";
		if(param.getDatesEff() != null && !param.getDatesEff().equals("indef")) {
			try {
				Date dt = sdf2.parse(param.getDatesEff());
				datesEffFormat = sdf.format(dt);
			} catch (Exception e) {
				datesEffFormat = param.getDatesEff();
				e.printStackTrace();
			}
		} else {
			datesEffFormat = param.getDatesEff();
		}
		
		String datesDiscFormat = "";
		if(param.getDatesDisc() != null && !param.getDatesDisc().equals("indef")) {
			try {
				Date dt = sdf2.parse(param.getDatesDisc());
				datesDiscFormat = sdf.format(dt);
			} catch (Exception e) {
				datesDiscFormat = param.getDatesDisc();
				e.printStackTrace();
			}
		} else {
			datesDiscFormat = param.getDatesDisc();
		}
		
		fareClassTextList.add("CXR: "+param.getCxr().toUpperCase()+" "+"RULE: "+param.getRuleNo().toUpperCase()+" "+"TARIFF: "+param.getTarCd().toUpperCase()+" - "+param.getDescription().toUpperCase());
		fareClassTextList.add("EXPLANATION");
		fareClassTextList.add("------------------------------------------------------");
		fareClassTextList.add("");
		fareClassTextList.add("SEQUENCE: "+param.getSeqNo().toUpperCase()+" "+"EFF: "+datesEffFormat+" "+"DISC: "+datesDiscFormat);
		fareClassTextList.add(param.getFareClass().toUpperCase());
		fareClassTextList.add("");
		fareClassTextList.add("");
		fareClassTextList.add("CONSTRUCTION DATA");
		fareClassTextList.add("");
		fareClassTextList.add("");
		fareClassTextList.add("END OF TEXT");
		
		return fareClassTextList;
	}

	public List<String> getFareClassConstructionDetails(FareClassQuery param) {
		List<String> fareClassContructionDetailsList = new ArrayList<>();
		fareClassContructionDetailsList.add("Carrier: "+param.getCxr().toUpperCase()+" "+"Tariff No: "+param.getTarNo().toUpperCase()+" "+"Tariff Code: "+param.getTarCd().toUpperCase()+" "+"Rule No: "+param.getRuleNo().toUpperCase());
		
		return fareClassContructionDetailsList;
	}
}
