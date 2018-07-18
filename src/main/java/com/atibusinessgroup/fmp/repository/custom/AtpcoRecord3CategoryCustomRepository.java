package com.atibusinessgroup.fmp.repository.custom;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.stereotype.Service;

import com.atibusinessgroup.fmp.constant.CollectionName;
import com.atibusinessgroup.fmp.domain.dto.AtpcoRecord3CategoryWithDataTable;
import com.atibusinessgroup.fmp.domain.dto.BaseFareTable;
import com.atibusinessgroup.fmp.domain.dto.DateTable;
import com.atibusinessgroup.fmp.domain.dto.FlightTable;
import com.atibusinessgroup.fmp.domain.dto.SecurityDataTable;
import com.atibusinessgroup.fmp.domain.dto.TextTable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service
public class AtpcoRecord3CategoryCustomRepository {	
	
	@Autowired
    MongoTemplate mongoTemplate;

	public List<AtpcoRecord3CategoryWithDataTable> findAllRecord3ByDataTable(String collectionName, List<String> tableNos) {
		
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject tableNo = new BasicDBObject();
				tableNo.append("tbl_no", new BasicDBObject("$in", tableNos));
				match.append("$match", tableNo);
				return match;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("$project", new BasicDBObject("category", "$$ROOT").append("tbl_no", "$tbl_no"));
				return project;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject projectResult = new BasicDBObject();
				BasicDBObject query = new BasicDBObject();
				query.append("_id", 0);
				projectResult.append("$project", query);
				return projectResult;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		List<AtpcoRecord3CategoryWithDataTable> result = mongoTemplate.aggregate(aggregation, collectionName, AtpcoRecord3CategoryWithDataTable.class).getMappedResults();
		
		return result;
	}
	
	public FlightTable findRecord3FlightTable(String tableNo) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject no = new BasicDBObject();
				no.append("tbl_no", tableNo);
				match.append("$match", no);
				return match;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("$project", new BasicDBObject("_id", 0)
						.append("tbl_no", 1)
						.append("carrier_flight", 1));
				return project;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		FlightTable result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_FLIGHT_TABLE_986, FlightTable.class).getUniqueMappedResult();
		
		return result;
	}
	
	public TextTable findRecord3TextTable(String tableNo) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject no = new BasicDBObject();
				no.append("tbl_no", tableNo);
				match.append("$match", no);
				return match;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject group = new BasicDBObject();
				group.append("$group", new BasicDBObject("_id", "$tbl_no").append("text", new BasicDBObject("$push", "$text")));
				return group;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		TextTable result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_TEXT_TABLE_996, TextTable.class).getUniqueMappedResult();
		
		return result;
	}

	public DateTable findRecord3DateTable(String tableNo) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject no = new BasicDBObject();
				no.append("tbl_no", tableNo);
				match.append("$match", no);
				return match;
			}
		});
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject project = new BasicDBObject();
				project.append("$project", new BasicDBObject("_id", 0)
						.append("tbl_no", 1)
						.append("travel_dates_eff", 1)
						.append("travel_dates_disc", 1)
						.append("ticketing_dates_eff", 1)
						.append("ticketing_dates_disc", 1)
						.append("reservations_dates_eff", 1)
						.append("reservations_dates_disc", 1));
				return project;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		DateTable result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_OVERRIDE_DATE_TABLE_994, DateTable.class).getUniqueMappedResult();
		
		return result;
	}
	
	public BaseFareTable findRecord3BaseFareTable(String tableNo) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject no = new BasicDBObject();
				no.append("tbl_no", tableNo);
				match.append("$match", no);
				return match;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		BaseFareTable result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_BASE_FARE_TABLE_989, BaseFareTable.class).getUniqueMappedResult();
		
		return result;
	}
	
	public List<SecurityDataTable> findRecord3SecurityTable(String tableNo) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		
		aggregationOperations.add(new AggregationOperation() {
			@Override
			public DBObject toDBObject(AggregationOperationContext context) {
				BasicDBObject match = new BasicDBObject();
				BasicDBObject no = new BasicDBObject();
				no.append("tbl_no", tableNo);
				match.append("$match", no);
				return match;
			}
		});
		
		Aggregation aggregation = newAggregation(aggregationOperations);
		
		List<SecurityDataTable> result = mongoTemplate.aggregate(aggregation, CollectionName.ATPCO_RECORD_SECURITY_TABLE_983, SecurityDataTable.class).getMappedResults();
		
		return result;
	}
}
