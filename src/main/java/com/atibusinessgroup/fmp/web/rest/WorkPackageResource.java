package com.atibusinessgroup.fmp.web.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atibusinessgroup.fmp.domain.Counter;
import com.atibusinessgroup.fmp.domain.Priority;
import com.atibusinessgroup.fmp.domain.TariffNumber;
import com.atibusinessgroup.fmp.domain.User;
import com.atibusinessgroup.fmp.domain.WorkPackage;
import com.atibusinessgroup.fmp.domain.WorkPackage.Attachment;
import com.atibusinessgroup.fmp.domain.WorkPackage.Comment;
import com.atibusinessgroup.fmp.domain.WorkPackage.FilingInstruction;
import com.atibusinessgroup.fmp.domain.WorkPackage.ImportFares;
import com.atibusinessgroup.fmp.domain.WorkPackage.MarketRules;
import com.atibusinessgroup.fmp.domain.WorkPackage.WorkPackageFareSheet;
import com.atibusinessgroup.fmp.domain.WorkPackageFare;
import com.atibusinessgroup.fmp.domain.WorkPackageHistory;
import com.atibusinessgroup.fmp.domain.WorkPackageHistoryData;
import com.atibusinessgroup.fmp.domain.enumeration.Status;
import com.atibusinessgroup.fmp.repository.ContractFMPRepository;
import com.atibusinessgroup.fmp.repository.ContractFareFMPRepository;
import com.atibusinessgroup.fmp.repository.CounterRepository;
import com.atibusinessgroup.fmp.repository.FormRepository;
import com.atibusinessgroup.fmp.repository.PriorityRepository;
import com.atibusinessgroup.fmp.repository.UserRepository;
import com.atibusinessgroup.fmp.repository.WorkPackageFareHistoryDataRepository;
import com.atibusinessgroup.fmp.repository.WorkPackageHistoryDataRepository;
import com.atibusinessgroup.fmp.security.SecurityUtils;
import com.atibusinessgroup.fmp.service.BusinessAreaService;
import com.atibusinessgroup.fmp.service.MailService;
import com.atibusinessgroup.fmp.service.ReviewLevelService;
import com.atibusinessgroup.fmp.service.TargetDistributionService;
import com.atibusinessgroup.fmp.service.UserService;
import com.atibusinessgroup.fmp.service.WorkPackageFareService;
import com.atibusinessgroup.fmp.service.WorkPackageHistoryService;
import com.atibusinessgroup.fmp.service.WorkPackageService;
import com.atibusinessgroup.fmp.web.rest.errors.BadRequestAlertException;
import com.atibusinessgroup.fmp.web.rest.util.HeaderUtil;
import com.atibusinessgroup.fmp.web.rest.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing WorkPackage.
 */
@RestController
@RequestMapping("/api")
public class WorkPackageResource {

    private final Logger log = LoggerFactory.getLogger(WorkPackageResource.class);

    private static final String ENTITY_NAME = "workPackage";

    private final WorkPackageService workPackageService;
    private final WorkPackageFareService workPackageFareService;
    private final TargetDistributionService targetDistributionService;
    private final BusinessAreaService businessAreaService;
    private final ReviewLevelService reviewLevelService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final WorkPackageHistoryService workPackageHistoryService;
    private final FormRepository formRepository;
    private final ContractFMPRepository contractFMPRepository;
    private final ContractFareFMPRepository contractFareFMPRepository;
    private final WorkPackageHistoryDataRepository workPackageHistoryDataRepository;
    private final WorkPackageFareHistoryDataRepository workPackageFareHistoryDataRepository;
    private final CounterRepository counterRepository;
    private final PriorityRepository priorityRepository;
    private final MailService mailService;
    public WorkPackageResource(WorkPackageService workPackageService, WorkPackageFareService workPackageFareService, TargetDistributionService targetDistributionService, BusinessAreaService businessAreaService, ReviewLevelService reviewLevelService, UserService userService, UserRepository userRepository, WorkPackageHistoryService workPackageHistoryService,
    		ContractFMPRepository contractFMPRepository, FormRepository formRepository, WorkPackageHistoryDataRepository workPackageHistoryDataRepository,
    		WorkPackageFareHistoryDataRepository workPackageFareHistoryDataRepository, ContractFareFMPRepository contractFareFMPRepository, CounterRepository counterRepository, PriorityRepository priorityRepository, MailService mailService) {
        this.workPackageService = workPackageService;
        this.workPackageFareService = workPackageFareService;
        this.targetDistributionService = targetDistributionService;
        this.businessAreaService = businessAreaService;
        this.reviewLevelService = reviewLevelService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.workPackageHistoryService = workPackageHistoryService;  
        this.contractFMPRepository = contractFMPRepository;
        this.formRepository = formRepository;
        this.workPackageHistoryDataRepository = workPackageHistoryDataRepository;
        this.workPackageFareHistoryDataRepository = workPackageFareHistoryDataRepository;
        this.contractFareFMPRepository = contractFareFMPRepository;
        this.counterRepository = counterRepository;
        this.priorityRepository = priorityRepository;
        this.mailService = mailService;
    }

    /**
     * POST  /work-packages : Create a new workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages")
    @Timed
    public ResponseEntity<WorkPackage> createWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to save WorkPackage : {}", workPackage);
        if (workPackage.getId() != null) {
            throw new BadRequestAlertException("A new workPackage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        
        Optional<User> user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get());        
        workPackage.setReviewLevel(user.get().getReviewLevels().get(0));
        
        if(workPackage.isSpecifiedFares()) {
        	workPackage.setFilingDetails(true);
        }
        else {
        	
        }
        if(workPackage.isAddon()) {
        	
        }
        else {
        	workPackage.getAddonFareSheet().clear();
        }
        if(workPackage.isMarketFares()) {
        	
        } else {
        	workPackage.getMarketFareSheet().clear();
        }
        if(workPackage.isDiscount()) {
        	
        } else {
        	workPackage.getDiscountFareSheet().clear();
        }
        
        workPackage.setStatus(Status.NEW);
        
        WorkPackage result = workPackageService.save(workPackage);
        
//        WorkPackageHistory history = new WorkPackageHistory();
//        history.setWorkPackage(new ObjectId(result.getId()));
//        history.setType("CREATE");
//        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
//        workPackageHistoryService.save(history);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * POST  /work-packages/reuse : Reuse a new workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/reuse")
    @Timed
    public ResponseEntity<WorkPackage> reuseWorkPackage(@RequestBody WorkPackage wp) throws URISyntaxException {
        log.debug("REST request to save reuse WorkPackage : {}", wp);
        
//        WorkPackage wp = workPackageService.findOne(workPackage.getId());
        wp.setReuseFrom(wp.getWpid());
        wp.setId(null);
        wp.setWpid(null);
        
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        wp.setReviewLevel(user.getReviewLevels().get(0));
        wp.setComment(null);
        wp.setInterofficeComment(null);
        wp.setFilingInstructionData(null);     
        wp.setCreatedBy(null);
        wp.setCreatedDate(null);
        wp.setLastModifiedBy(null);
        wp.setLastModifiedDate(null);
        wp.setFilingInstruction(false);
//        if(!wp.getReuseReplaceConfig().isAttachment()) {
//        	wp.setAttachment(false);
//        	wp.getAttachmentData().clear();
//        }
        for(WorkPackageFareSheet wps : wp.getFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getAddonFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getMarketFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getWaiverFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getDiscountFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        WorkPackage result = workPackageService.save(wp);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/replace : Reuse a new workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/replace")
    @Timed
    public ResponseEntity<WorkPackage> replaceWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to save reuse WorkPackage : {}", workPackage);
               
        WorkPackage wp = workPackageService.findOne(workPackage.getId());
        wp.setReplaceFrom(wp.getWpid());
        wp.setId(null);
        wp.setWpid(null);
        
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        wp.setReviewLevel(user.getReviewLevels().get(0));
        wp.setCreatedBy(null);
        wp.setCreatedDate(null);
        wp.setLastModifiedBy(null);
        wp.setLastModifiedDate(null);
//        if(!workPackage.getReuseReplaceConfig().isAttachment()) {
//        	wp.setAttachment(false);
//        	wp.getAttachmentData().clear();
//        }
        for(WorkPackageFareSheet wps : wp.getFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getAddonFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getMarketFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getWaiverFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        for(WorkPackageFareSheet wps : wp.getDiscountFareSheet()) {
        	for(WorkPackageFare fare : wps.getFares()) {
        		fare.setStatus("PENDING");
        	}
        }
        WorkPackage result = workPackageService.save(wp);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    

    /**
     * This method for the type of data in the cell, extracts the data and
     * returns it as a string.
     */
    public static String getCellValueAsString(Cell cell) {
        String strCellValue = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                strCellValue = cell.toString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "dd/MM/yyyy");
                    strCellValue = dateFormat.format(cell.getDateCellValue());
                } else {
                    Double value = cell.getNumericCellValue();
                    Long longValue = value.longValue();
                    strCellValue = new String(longValue.toString());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                strCellValue = new String(new Boolean(
                        cell.getBooleanCellValue()).toString());
                break;
            case Cell.CELL_TYPE_BLANK:
                strCellValue = "";
                break;
            }
        }
        return strCellValue;
    }
    
    
    /**
     * POST  /work-packages/import-fares : Import a new fares workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/import-fares")
    @Timed
    public ResponseEntity<WorkPackage> importFaresWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to save importFaresWorkPackage : {}", workPackage);

        try {
            ImportFares importData = workPackage.getImportFares();
			InputStream input = new ByteArrayInputStream(importData.getFile());
			Workbook workbook = new XSSFWorkbook(input);
			Sheet datatypeSheet = workbook.getSheetAt(0);	
			
			datatypeSheet.getRow(1).getCell(1).getStringCellValue();
			
			Iterator<Row> iterator = datatypeSheet.iterator();
			iterator.next();
			

            List<WorkPackageFare> workPackageFares = new ArrayList<WorkPackageFare>();
			
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                WorkPackageFare wpFare = new WorkPackageFare();
                TariffNumber tariffNumber = new TariffNumber();
                wpFare.setTariffNumber(tariffNumber);
                for(int cell=1;cell<=30;cell++) {
                    Cell currentCell = currentRow.getCell(cell);

                    String value = "";
                    try {
                    		value = getCellValueAsString(currentCell);
                    }catch(Exception e) {
                    	
                    }
//                    System.out.println("CELL "+cell+" : "+value);
                    if(cell == 1) {
                    		wpFare.setStatus(value);
                    }
                    else if(cell == 2) {
	                		wpFare.setCarrier(value);
	                }
                    else if(cell == 3) {
	                		wpFare.setAction(value);
	                }
                    else if(cell == 4) {
                    	tariffNumber.setTarNo(value);
	                }
                    else if(cell == 5) {
                    	tariffNumber.setTarCd(value);
	                }
                    else if(cell == 6) {
                    	tariffNumber.setGlobal(value);
	                }
                    else if(cell == 7) {
	                		wpFare.setOrigin(value);
	                }
                    else if(cell == 8) {
	                		wpFare.setDestination(value);
	                }
                    else if(cell == 9) {
                			wpFare.setFareBasis(value);
                    }
                    else if(cell == 10) {
	            			wpFare.setBookingClass(value);
	                }
                    else if(cell == 11) {
	            			wpFare.setCabin(value);
	                }
                    else if(cell == 12) {
            				wpFare.setTypeOfJourney(value);	
                    }
                    else if(cell == 13) {
	        				wpFare.setFootnote1(value);	
	                }
                    else if(cell == 14) {
	        				wpFare.setRtgno(value);	
	                }
                    else if(cell == 15) {
	        				wpFare.setRuleno(value);	
	                }
                    else if(cell == 16) {
	        				wpFare.setCurrency(value);	
	                }
                    else if(cell == 17) {
	        				wpFare.setAmount(value);	
	                }
                    else if(cell == 18) {
	        				wpFare.setAif(value);	
	                }
                    else if(cell == 19) {
	                    	wpFare.setTravelStart(ZonedDateTime.now());
	                }
                    else if(cell == 20) {
                    		wpFare.setTravelEnd(ZonedDateTime.now());
	                }
                    else if(cell == 21) {
                    		wpFare.setSaleStart(ZonedDateTime.now());
	                }
                    else if(cell == 22) {
                    		wpFare.setSaleEnd(ZonedDateTime.now());
	                }
                    else if(cell == 23) {
                    		wpFare.setComment(value);
	                }
                    else if(cell == 24) {
                    		wpFare.setTravelComplete(ZonedDateTime.now());
	                }
                    else if(cell == 25) {
                    		wpFare.setTravelCompleteIndicator(value);
	                }
                    else if(cell == 26) {
                    		wpFare.setRatesheetComment(value);
	                }
                }
                workPackageFares.add(wpFare);
            }			
            
//            workPackage = workPackageService.findOne(workPackage.getId());
//            workPackage.getFareSheet().get(0).getFares().addAll(workPackageFares);
            workPackage.getFareSheet().get(0).getFares().addAll(workPackageFares);
            workPackage = workPackageService.save(workPackage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return getWorkPackage(workPackage.getId());
    }
    
    /**
     * POST  /work-packages/import-fares-market : Import a new market fares workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/import-fares-market")
    @Timed
    public ResponseEntity<WorkPackage> importFaresMarketWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to save importFaresMarketWorkPackage : {}", workPackage);

        try {
            ImportFares importData = workPackage.getImportFares();
			InputStream input = new ByteArrayInputStream(importData.getFile());
			Workbook workbook = new XSSFWorkbook(input);
			Sheet datatypeSheet = workbook.getSheetAt(0);	
			
			String status = datatypeSheet.getRow(1).getCell(0).getStringCellValue();
			
			Iterator<Row> iterator = datatypeSheet.iterator();
			iterator.next();
			

            List<WorkPackageFare> workPackageFares = new ArrayList<WorkPackageFare>();
			
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                WorkPackageFare wpFare = new WorkPackageFare();
                for(int cell=0;cell<30;cell++) {
                    Cell currentCell = currentRow.getCell(cell);

                    String value = "";
                    try {
                    		value = getCellValueAsString(currentCell);
                    }catch(Exception e) {
                    	
                    }
                    if(cell == 0) {
                    		wpFare.setStatus(value);
                    }
                    else if(cell == 1) {
	                		wpFare.setCarrier(value);
	                }
                    else if(cell == 2) {
	                		wpFare.setAction(value);
	                }
                    else if(cell == 3) {
	                		wpFare.setOrigin(value);
	                }
                    else if(cell == 4) {
	                		wpFare.setDestination(value);
	                }
                    else if(cell == 5) {
	                		wpFare.setFareBasis(value);
	                }
                    else if(cell == 6) {
	                		wpFare.setBookingClass(value);
	                }
                    else if(cell == 7) {
	                		wpFare.setSsn(value);
	                }
                    else if(cell == 8) {
                			wpFare.setCabin(value);
                    }
                    else if(cell == 9) {
	            			wpFare.setTypeOfJourney(value);
	                }
                    else if(cell == 10) {
	            			wpFare.setRtgno(value);
	                }
                    else if(cell == 11) {
            				wpFare.setRuleno(value);	
                    }
                    else if(cell == 12) {
	        				wpFare.setCurrency(value);	
	                }
                    else if(cell == 13) {
	        				wpFare.setAmount(value);	
	                }
                    else if(cell == 14) {
//	        				wpFare.s(value);	
	                }
                    else if(cell == 15) {
//	        				wpFare.setCurrency(value);	
	                }
                    else if(cell == 16) {
//	        				wpFare.setAmount(value);	
	                }
                    else if(cell == 17) {
                    		wpFare.setTravelStart(ZonedDateTime.now());
	                }
                    else if(cell == 18) {
                    		wpFare.setTravelEnd(ZonedDateTime.now());
	                }
                    else if(cell == 19) {
                    		wpFare.setSaleStart(ZonedDateTime.now());
	                }
                    else if(cell == 20) {
                    		wpFare.setSaleEnd(ZonedDateTime.now());
	                }
                    else if(cell == 21) {
                    		wpFare.setComment(value);
	                }
                    else if(cell == 22) {
                    		wpFare.setTravelComplete(ZonedDateTime.now());
	                }
                    else if(cell == 23) {
                    		wpFare.setTravelCompleteIndicator(value);
	                }
                    else if(cell == 24) {
                    		wpFare.setRatesheetComment(value);
	                }
                    else if(cell == 25) {
                    		wpFare.setDealCode(value);
	                }
                }
                workPackageFares.add(wpFare);
            }			
            
            workPackage.getMarketFareSheet().get(0).getFares().addAll(workPackageFares);
            workPackage = workPackageService.save(workPackage);
            
//            for(WorkPackageFare wpf : workPackageFares) {
//            		wpf.setWorkPackage(new ObjectId(workPackage.getId()));
//            		wpf.setFareType("MARKET");
//            		WorkPackageFare fare = workPackageFareService.save(wpf);
//            		workPackage.getMarketFares().add(fare);
//            }
            
//            workPackage = workPackageService.save(workPackage);

//			WorkPackageFare
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return getWorkPackage(workPackage.getId());
    }
   
    /**
     * POST  /work-packages/import-fares-discount : Import a new discount fares workPackage.
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/import-fares-discount")
    @Timed
    public ResponseEntity<WorkPackage> importFaresDiscountWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to save importFaresDiscountWorkPackage : {}", workPackage);

        try {
            ImportFares importData = workPackage.getImportFares();
			InputStream input = new ByteArrayInputStream(importData.getFile());
			Workbook workbook = new XSSFWorkbook(input);
			Sheet datatypeSheet = workbook.getSheetAt(0);	
			
			String status = datatypeSheet.getRow(1).getCell(0).getStringCellValue();
			
			Iterator<Row> iterator = datatypeSheet.iterator();
			iterator.next();
			
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                WorkPackageFare wpFare = new WorkPackageFare();
                
                for(int cell=0;cell<30;cell++) {
                    Cell currentCell = currentRow.getCell(cell);

                    String value = "";
                    try {
                    		value = getCellValueAsString(currentCell);
                    }catch(Exception e) {
                    	
                    }
                    if(cell == 0) {
                    	wpFare.setStatus(value);
                    }
                    else if(cell == 1) {
                		wpFare.setTarcd(value);
                    }
                    else if(cell == 2) {
	                		wpFare.setLoc1Type(value);
	                }
                    else if(cell == 3) {
	                		wpFare.setLoc1(value);
	                }
                    else if(cell == 4) {
	                		wpFare.setLoc2Type(value);
	                }
                    else if(cell == 5) {
	                		wpFare.setLoc2(value);
	                }
                    else if(cell == 6) {
	                		wpFare.setFareBasis(value);
	                }
                    else if(cell == 7) {
	                		wpFare.setBaseRuleno(value);
	                }
                    else if(cell == 8) {
                		wpFare.setTarcd(value);
                    }
                    else if(cell == 9) {
	                		wpFare.setCalcType(value);
	                }
                    else if(cell == 10) {
                			wpFare.setPercentBaseFare(value);
                    }
                    else if(cell == 11) {
                    		wpFare.setCurrency(value);	
	                }
                    else if(cell == 12) {
	        				wpFare.setAmount(value);	
	                }
                    else if(cell == 13) {
	        				wpFare.setPassengerType(value);	
	                }
                    else if(cell == 14) {
	        				wpFare.setFareType(value);	
	                }
                    else if(cell == 15) {
                    		wpFare.setTicketCode(value);
	                }
                    else if(cell == 16) {
                			wpFare.setTicketDesignator(value);
	                }
                    else if(cell == 17) {
                			wpFare.setTypeOfJourney(value);
	                }
                    else if(cell == 18) {
                    		wpFare.setGlobal(value);
	                }
                    else if(cell == 19) {
                    		wpFare.setRtgno(value);
	                }
                    else if(cell == 20) {
                    		wpFare.setRtgnoTarno(value);
	                }
                    else if(cell == 21) {
                    		wpFare.setNewFareBasis(value);
	                }
                    else if(cell == 22) {
                    		wpFare.setNewTypeOfJourney(value);
	                }
                    else if(cell == 23) {
                    		wpFare.setNewBookingCode(value);
	                }
                    else if(cell == 24) {
                		wpFare.setTravelStart(ZonedDateTime.now());
                    }
                    else if(cell == 25) {
                		wpFare.setTravelEnd(ZonedDateTime.now());
                    }
                    else if(cell == 26) {
                		wpFare.setSaleStart(ZonedDateTime.now());
                    }
                    else if(cell == 27) {
                		wpFare.setSaleEnd(ZonedDateTime.now());
                    }
                    else if(cell == 28) {
                		wpFare.setComment(value);
                    }
                    else if(cell == 29) {
                    		wpFare.setTravelComplete(ZonedDateTime.now());
	                }
                    else if(cell == 30) {
                    		wpFare.setTravelCompleteIndicator(value);
	                }
                }
                
                workPackage.getDiscountFareSheet().get(0).getFares().add(wpFare);
            }			
            
            workPackage = workPackageService.save(workPackage);
            
//            for(WorkPackageFare wpf : workPackageFares) {
//            		wpf.setWorkPackage(new ObjectId(workPackage.getId()));
//            		wpf.setFareType("DISCOUNT");
//            		WorkPackageFare fare = workPackageFareService.save(wpf);
//            		workPackage.getMarketFares().add(fare);
//            }
            

//			WorkPackageFare
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return getWorkPackage(workPackage.getId());
    }
   

    /**
     * POST  /work-packages/export-fares : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/export-fares")
    @Timed
    public ResponseEntity<Attachment> exportFaresWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
    	log.debug("REST request to save exportFares : {}", workPackage);

        XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Workorder Fare");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;

        cell = row.createCell(1);
        cell.setCellValue("Status");
        cell = row.createCell(2);
        cell.setCellValue("Carrier");
        cell = row.createCell(3);
        cell.setCellValue("Action");
        cell = row.createCell(4);
        cell.setCellValue("Tar No");
        cell = row.createCell(5);
        cell.setCellValue("Tar Cd");
        cell = row.createCell(6);
        cell.setCellValue("Global");
        cell = row.createCell(7);
        cell.setCellValue("Origin");
        cell = row.createCell(8);
        cell.setCellValue("Dest");
        cell = row.createCell(9);
        cell.setCellValue("Fare Cls");
        cell = row.createCell(10);
        cell.setCellValue("Bkg Cls");
        cell = row.createCell(11);
        cell.setCellValue("Cabin");
        cell = row.createCell(12);
        cell.setCellValue("OW/RT");
        cell = row.createCell(13);
        cell.setCellValue("Ftnt");
        cell = row.createCell(14);
        cell.setCellValue("Rtg No");
        cell = row.createCell(15);
        cell.setCellValue("Rule No");
        cell = row.createCell(16);
        cell.setCellValue("Curr");
        cell = row.createCell(17);
        cell.setCellValue("Base Amt");
        cell = row.createCell(18);
        cell.setCellValue("Target AIF");        
        cell = row.createCell(19);
        cell.setCellValue("Travel Start");
        cell = row.createCell(20);
        cell.setCellValue("Travel End");
        cell = row.createCell(21);
        cell.setCellValue("Sales Start");
        cell = row.createCell(22);
        cell.setCellValue("Sales End");        
        cell = row.createCell(23);
        cell.setCellValue("Comment");
        cell = row.createCell(24);
        cell.setCellValue("Travel Complete");
        cell = row.createCell(25);
        cell.setCellValue("Travel Complete Indicator");
        cell = row.createCell(26);
        cell.setCellValue("Ratesheet Comment");
//        cell.setCellValue("Deal Code");
//        cell = row.createCell(31);
        
//        List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackage(workPackage.getId());
        WorkPackage wp = workPackageService.findOne(workPackage.getId());
        List<WorkPackageFare> fares = wp.getFareSheet().get(0).getFares();
        for(int i=0; i<fares.size(); i++) {
        		XSSFRow rows = spreadsheet.createRow(i+2);
            cell = rows.createCell(1);
            cell.setCellValue(fares.get(i).getStatus());
            cell = rows.createCell(2);
            cell.setCellValue(fares.get(i).getCarrier());
            cell = rows.createCell(3);
            cell.setCellValue(fares.get(i).getAction());
            cell = rows.createCell(4);
            if(fares.get(i).getTariffNumber() != null) {
            	cell.setCellValue(fares.get(i).getTariffNumber().getTarNo());
            }
            cell = rows.createCell(5);
            if(fares.get(i).getTariffNumber() != null) {
            	cell.setCellValue(fares.get(i).getTariffNumber().getTarCd());
            }
            cell = rows.createCell(6);
            if(fares.get(i).getTariffNumber() != null) {
            	cell.setCellValue(fares.get(i).getTariffNumber().getGlobal());
            }
            cell = rows.createCell(7);
            cell.setCellValue(fares.get(i).getOrigin());
            cell = rows.createCell(8);
            cell.setCellValue(fares.get(i).getDestination());
            cell = rows.createCell(9);
            cell.setCellValue(fares.get(i).getFareBasis());
            cell = rows.createCell(10);
            cell.setCellValue(fares.get(i).getBookingClass());
            cell = rows.createCell(11);
            cell.setCellValue(fares.get(i).getCabin());
            cell = rows.createCell(12);
            cell.setCellValue(fares.get(i).getTypeOfJourney());
            cell = rows.createCell(13);
            cell.setCellValue(fares.get(i).getFootnote1());
            cell = rows.createCell(14);
            cell.setCellValue(fares.get(i).getRtgno());
            cell = rows.createCell(15);
            cell.setCellValue(fares.get(i).getRuleno());
            cell = rows.createCell(16);
            cell.setCellValue(fares.get(i).getCurrency());
            cell = rows.createCell(17);
            cell.setCellValue(fares.get(i).getAmount());
            cell = rows.createCell(18);
            cell.setCellValue(fares.get(i).getAif());
            cell = rows.createCell(19);
            cell.setCellValue(fares.get(i).getTravelStart().toString());
            cell = rows.createCell(20);
            cell.setCellValue(fares.get(i).getTravelEnd().toString());
            cell = rows.createCell(21);
            cell.setCellValue(fares.get(i).getSaleStart().toString());
            cell = rows.createCell(22);
            cell.setCellValue(fares.get(i).getSaleEnd().toString());
            cell = rows.createCell(23);
            cell.setCellValue(fares.get(i).getComment());
            cell = rows.createCell(24);
            cell.setCellValue(fares.get(i).getTravelComplete().toString());
            cell = rows.createCell(25);
            cell.setCellValue(fares.get(i).getTravelCompleteIndicator());
            cell = rows.createCell(26);
            cell.setCellValue(fares.get(i).getRatesheetComment());
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    

    /**
     * POST  /work-packages/export-fares-market : Export work package fares market
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/export-fares-market")
    @Timed
    public ResponseEntity<Attachment> exportFaresMarketWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
    	log.debug("REST request to save exportFaresMarket : {}", workPackage);

        XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Workorder Fare Market");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;

        cell = row.createCell(1);
        cell.setCellValue("Status");
        cell = row.createCell(2);
        cell.setCellValue("Carrier");
        cell = row.createCell(3);
        cell.setCellValue("Action");
        cell = row.createCell(4);
        cell.setCellValue("Origin");
        cell = row.createCell(5);
        cell.setCellValue("Destination");
        cell = row.createCell(6);
        cell.setCellValue("Fare Cls");
        cell = row.createCell(7);
        cell.setCellValue("Bkg Cls");
        cell = row.createCell(8);
        cell.setCellValue("SSN");
        cell = row.createCell(9);
        cell.setCellValue("Cabin");
        cell = row.createCell(10);
        cell.setCellValue("OW/RT");
        cell = row.createCell(11);
        cell.setCellValue("Rtg No");
        cell = row.createCell(12);
        cell.setCellValue("Rule Id");
        cell = row.createCell(13);
        cell.setCellValue("Currency");
        cell = row.createCell(14);
        cell.setCellValue("Amount");
        cell = row.createCell(15);
        cell.setCellValue("Prev Base Amt");
        cell = row.createCell(16);
        cell.setCellValue("Prev Base Amt Diff");
        cell = row.createCell(17);
        cell.setCellValue("% Prev Base Amt Diff");
        cell = row.createCell(18);
        cell.setCellValue("Travel Start");
        cell = row.createCell(19);
        cell.setCellValue("Travel End");
        cell = row.createCell(20);
        cell.setCellValue("Sales Start");
        cell = row.createCell(21);
        cell.setCellValue("Sales End");
        cell = row.createCell(22);
        cell.setCellValue("Comment");
        cell = row.createCell(23);
        cell.setCellValue("Travel Complete");
        cell = row.createCell(24);
        cell.setCellValue("Travel Complete Indicator");
        cell = row.createCell(25);
        cell.setCellValue("Ratesheet Comment");
        cell = row.createCell(26);
        cell.setCellValue("Deal Code");
        cell = row.createCell(27);
        
        List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "MARKET");
        for(int i=0; i<fares.size(); i++) {
        		XSSFRow rows = spreadsheet.createRow(i+2);
            cell = rows.createCell(1);
            cell.setCellValue(fares.get(i).getStatus());
            cell = rows.createCell(2);
            cell.setCellValue(fares.get(i).getCarrier());
            cell = rows.createCell(3);
            cell.setCellValue(fares.get(i).getAction());
            cell = rows.createCell(4);
            cell.setCellValue(fares.get(i).getOrigin());
            cell = rows.createCell(5);
            cell.setCellValue(fares.get(i).getDestination());
            cell = rows.createCell(6);
            cell.setCellValue(fares.get(i).getFareBasis());
            cell = rows.createCell(7);
            cell.setCellValue(fares.get(i).getBookingClass());
            cell = rows.createCell(8);
            cell.setCellValue(fares.get(i).getSsn());
            cell = rows.createCell(9);
            cell.setCellValue(fares.get(i).getCabin());
            cell = rows.createCell(10);
            cell.setCellValue(fares.get(i).getTypeOfJourney());
            cell = rows.createCell(11);
            cell.setCellValue(fares.get(i).getRtgno());
            cell = rows.createCell(12);
            cell.setCellValue(fares.get(i).getRuleno());
            cell = rows.createCell(13);
            cell.setCellValue(fares.get(i).getCurrency());
            cell = rows.createCell(14);
            cell.setCellValue(fares.get(i).getAmount());
            cell = rows.createCell(16);
            cell.setCellValue("");
            cell = rows.createCell(17);
            cell.setCellValue("");
            cell = rows.createCell(18);
            cell.setCellValue("");

            
            cell.setCellValue(fares.get(i).getTravelStart().toString());
            cell = rows.createCell(19);
            cell.setCellValue(fares.get(i).getTravelEnd().toString());
            cell = rows.createCell(20);
            cell.setCellValue(fares.get(i).getSaleStart().toString());
            cell = rows.createCell(21);
            cell.setCellValue(fares.get(i).getSaleEnd().toString());
            cell = rows.createCell(22);
            cell.setCellValue(fares.get(i).getComment());
            cell = rows.createCell(23);
            cell.setCellValue(fares.get(i).getTravelComplete().toString());
            cell = rows.createCell(24);
            cell.setCellValue(fares.get(i).getTravelCompleteIndicator());
            cell = rows.createCell(25);
            cell.setCellValue(fares.get(i).getRatesheetComment());
            cell = rows.createCell(26);
            cell.setCellValue(fares.get(i).getDealCode());
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    

    /**
     * POST  /work-packages/export-fares-discount : Export work package fares discount
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/export-fares-discount")
    @Timed
    public ResponseEntity<Attachment> exportFaresDiscountWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
    	log.debug("REST request to save exportFaresDiscountWorkPackage : {}", workPackage);

        XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Workorder Fare Discount");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;

        cell = row.createCell(1);
        cell.setCellValue("Status");
        cell = row.createCell(2);
        cell.setCellValue("FBR Tariff Code");
        cell = row.createCell(3);
        cell.setCellValue("Loc1 Type");
        cell = row.createCell(4);
        cell.setCellValue("Loc1");
        cell = row.createCell(5);
        cell.setCellValue("Loc2 Type");
        cell = row.createCell(6);
        cell.setCellValue("Loc2");
        cell = row.createCell(7);
        cell.setCellValue("Base Fare Cls");
        cell = row.createCell(8);
        cell.setCellValue("Base Rule No");
        cell = row.createCell(9);
        cell.setCellValue("Base Tariff Code");
        cell = row.createCell(10);
        cell.setCellValue("Calc Type");
        cell = row.createCell(11);
        cell.setCellValue("% Of Base Fare");
        cell = row.createCell(12);
        cell.setCellValue("Curr");
        cell = row.createCell(13);
        cell.setCellValue("Specified Amount");
        cell = row.createCell(14);
        cell.setCellValue("Pax Type");
        cell = row.createCell(15);
        cell.setCellValue("Fare Type");
        cell = row.createCell(16);
        cell.setCellValue("Tkt Code");
        cell = row.createCell(17);
        cell.setCellValue("Tkt Des");
        cell = row.createCell(18);
        cell.setCellValue("OW/RT");
        cell = row.createCell(19);
        cell.setCellValue("Global");
        cell = row.createCell(20);
        cell.setCellValue("Rtg No");
        cell = row.createCell(21);
        cell.setCellValue("Rtg No Tarno");
        cell = row.createCell(22);
        cell.setCellValue("New Farecls");
        cell = row.createCell(23);
        cell.setCellValue("New OW/RT");
        cell = row.createCell(24);
        cell.setCellValue("New Bkg Cd");
        cell = row.createCell(25);
        cell.setCellValue("Travel Start");
        cell = row.createCell(26);
        cell.setCellValue("Travel End");
        cell = row.createCell(27);
        cell.setCellValue("Sale Start");
        cell = row.createCell(28);
        cell.setCellValue("Sale End");
        cell = row.createCell(29);
        cell.setCellValue("Comment");
        cell = row.createCell(30);
        cell.setCellValue("Travel Complete");
        cell = row.createCell(31);
        cell.setCellValue("Travel Complete Indicator");
        
        WorkPackage wp = workPackageService.findOne(workPackage.getId());
        
        List<WorkPackageFare> fares = wp.getDiscountFareSheet().get(0).getFares();
        
        for(int i=0; i<fares.size(); i++) {
        		XSSFRow rows = spreadsheet.createRow(i+2);
            cell = rows.createCell(1);
            cell.setCellValue(fares.get(i).getStatus());
            cell = rows.createCell(2);
            cell.setCellValue(fares.get(i).getTarcd());
            cell = rows.createCell(3);
            cell.setCellValue(fares.get(i).getLoc1Type());
            cell = rows.createCell(4);
            cell.setCellValue(fares.get(i).getLoc1());
            cell = rows.createCell(5);
            cell.setCellValue(fares.get(i).getLoc2Type());
            cell = rows.createCell(6);
            cell.setCellValue(fares.get(i).getLoc2());
            cell = rows.createCell(7);
            cell.setCellValue(fares.get(i).getFareBasis());
            cell = rows.createCell(8);
            cell.setCellValue(fares.get(i).getBaseRuleno());
            cell = rows.createCell(9);
            cell.setCellValue(fares.get(i).getTarcd());
            cell = rows.createCell(10);
            cell.setCellValue(fares.get(i).getCalcType());
            cell = rows.createCell(11);
            cell.setCellValue(fares.get(i).getPercentBaseFare());
            cell = rows.createCell(12);
            cell.setCellValue(fares.get(i).getCurrency());
            cell = rows.createCell(13);
            cell.setCellValue(fares.get(i).getAmount());
            cell = rows.createCell(14);
            cell.setCellValue(fares.get(i).getPassengerType());
            cell = rows.createCell(15);
            cell.setCellValue(fares.get(i).getFareType());
            cell = rows.createCell(16);
            cell.setCellValue(fares.get(i).getTicketCode());
            cell = rows.createCell(17);
            cell.setCellValue(fares.get(i).getTicketDesignator());
            cell = rows.createCell(18);
            cell.setCellValue(fares.get(i).getTypeOfJourney());
            cell = rows.createCell(19);
            cell.setCellValue(fares.get(i).getGlobal());
            cell = rows.createCell(20);
            cell.setCellValue(fares.get(i).getRtgno());
            cell = rows.createCell(21);
            cell.setCellValue(fares.get(i).getRtgnoTarno());
            cell = rows.createCell(22);
            cell.setCellValue(fares.get(i).getNewFareBasis());
            cell = rows.createCell(23);
            cell.setCellValue(fares.get(i).getNewTypeOfJourney());
            cell = rows.createCell(24);
            cell.setCellValue(fares.get(i).getNewBookingCode());
            cell = rows.createCell(25);
            cell.setCellValue(fares.get(i).getTravelStart().toString());
            cell = rows.createCell(26);
            cell.setCellValue(fares.get(i).getTravelEnd().toString());
            cell = rows.createCell(27);
            cell.setCellValue(fares.get(i).getSaleStart().toString());
            cell = rows.createCell(28);
            cell.setCellValue(fares.get(i).getSaleEnd().toString());
            cell = rows.createCell(29);
            cell.setCellValue(fares.get(i).getComment());
            cell = rows.createCell(30);
            cell.setCellValue(fares.get(i).getTravelComplete().toString());
            cell = rows.createCell(31);
            cell.setCellValue(fares.get(i).getTravelCompleteIndicator());
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    public static class WorkPackageExportOption{
    	public String outputTo;
    	public boolean gridLines;
    	public boolean columnHeaders;
    	public boolean onlySelectedRows;
		public String getOutputTo() {
			return outputTo;
		}
		public void setOutputTo(String outputTo) {
			this.outputTo = outputTo;
		}
		public boolean isGridLines() {
			return gridLines;
		}
		public void setGridLines(boolean gridLines) {
			this.gridLines = gridLines;
		}
		public boolean isColumnHeaders() {
			return columnHeaders;
		}
		public void setColumnHeaders(boolean columnHeaders) {
			this.columnHeaders = columnHeaders;
		}
		public boolean isOnlySelectedRows() {
			return onlySelectedRows;
		}
		public void setOnlySelectedRows(boolean onlySelectedRows) {
			this.onlySelectedRows = onlySelectedRows;
		}
    	
    	
    }
    /**
     * POST  /work-packages/export-fares : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/exportQueue")
    @Timed
    public ResponseEntity<Attachment> exportQueueWorkPackage(@RequestBody WorkPackageExportOption workPackageExportOption) throws URISyntaxException {
    	log.debug("REST request to export work package : {}", workPackageExportOption);

        XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Workorder Queue");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;

        cell = row.createCell(1);
        cell.setCellValue("Status");
        cell = row.createCell(2);
        cell.setCellValue("Carrier");
        cell = row.createCell(3);
        cell.setCellValue("Action");
        cell = row.createCell(4);
        cell.setCellValue("Tar No");
        cell = row.createCell(5);
        cell.setCellValue("Tar Cd");
        cell = row.createCell(6);
        cell.setCellValue("Global");
        cell = row.createCell(7);
        cell.setCellValue("Origin");
        cell = row.createCell(8);
        cell.setCellValue("Dest");
        cell = row.createCell(9);
        cell.setCellValue("Fare Cls");
        cell = row.createCell(10);
        cell.setCellValue("Bkg Cls");
        cell = row.createCell(11);
        cell.setCellValue("Cabin");
        cell = row.createCell(12);
        cell.setCellValue("OW/RT");
        cell = row.createCell(13);
        cell.setCellValue("Ftnt");
        cell = row.createCell(14);
        cell.setCellValue("Rtg No");
        cell = row.createCell(15);
        cell.setCellValue("Rule No");
        cell = row.createCell(16);
        cell.setCellValue("Curr");
        cell = row.createCell(17);
        cell.setCellValue("Base Amt");
        cell = row.createCell(18);
        cell.setCellValue("Target AIF");        
        cell = row.createCell(19);
        cell.setCellValue("Travel Start");
        cell = row.createCell(20);
        cell.setCellValue("Travel End");
        cell = row.createCell(21);
        cell.setCellValue("Sales Start");
        cell = row.createCell(22);
        cell.setCellValue("Sales End");        
        cell = row.createCell(23);
        cell.setCellValue("Comment");
        cell = row.createCell(24);
        cell.setCellValue("Travel Complete");
        cell = row.createCell(25);
        cell.setCellValue("Travel Complete Indicator");
        cell = row.createCell(26);
        cell.setCellValue("Ratesheet Comment");
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    
    /**
     * POST  /work-packages/download-market-rules : Download Market Rules Template
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/download-market-rules")
    @Timed
    public ResponseEntity<Attachment> downloadMarketRules(@RequestBody WorkPackage workPackage) throws URISyntaxException {
    	log.debug("REST request to download Market Rules : {}", workPackage);

    	String namaFile = "MarketRules.xlsx";
//    	String filename = "/Users/billy/Downloads/" + namaFile;
    	String filename = "/home/development/xml/" + namaFile;
    	byte[] templateFile = null;
    	if (new File(filename).exists()) {            
            try {
                templateFile = Files.readAllBytes(Paths.get(filename));
            } catch (IOException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("contract", "Error occured when retrieving template file, please try again later.", "Fail Template File")).body(null);
            }
        }
	    Attachment att = new Attachment();
	    att.setFile(templateFile);
	    return ResponseEntity.ok()
	        .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
	        .body(att);
    }
    
    /**
     * PUT  /work-packages : Updates an existing workPackage.
     *
     * @param workPackage the workPackage to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated workPackage,
     * or with status 400 (Bad Request) if the workPackage is not valid,
     * or with status 500 (Internal Server Error) if the workPackage couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/work-packages")
    @Timed
    public ResponseEntity<WorkPackage> updateWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to update WorkPackage : {}", workPackage);
        
        boolean isWorkPackageNew = false;
        
        if (workPackage.getId() == null) {
            return createWorkPackage(workPackage);
        }
        
    	if(workPackage.getWpid() == null) {
    		isWorkPackageNew = true;
    		
    		DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
    		DateFormat dfFull = new SimpleDateFormat("yyyy"); // Just the year, with 4 digits
    		
    		Counter c = counterRepository.findOneByIdAndYear("workpackageId", dfFull.format(Calendar.getInstance().getTime()));
    		
    		NumberFormat nf = new DecimalFormat("00000");
        	c.setSequenceValue(c.getSequenceValue()+1);
        	c = counterRepository.save(c);
        	
    		String year = df.format(Calendar.getInstance().getTime());
    		workPackage.setWpid(year+nf.format(c.getSequenceValue()+1));
        }
    	
//	    workPackage = workPackageService.save(workPackage);
//	        
//        if(isWorkPackageNew) {
//        	WorkPackageHistory history = new WorkPackageHistory();
//            history.setWorkPackage(new ObjectId(workPackage.getId()));
//            history.setType("CREATE");
//            history.setUsername(SecurityUtils.getCurrentUserLogin().get());
//            workPackageHistoryService.save(history);
//        }

//		List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), null);
//		for(WorkPackageFare fare : fares) {
//			workPackageFareService.delete(fare.getId());
//		}
		
		/*
		// Regular Fares
		for(WorkPackageFareSheet sheet : workPackage.getFareSheet()) {
			if(sheet.getFares() != null && sheet.getFares().size() > 0) {	    		
	    		for(WorkPackageFare wpf : workPackage.getFares()) {
	    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
	    			workPackageFareService.save(wpf);
	    		}
	    	}
		}
    	
    	
    	// Addon Fares
    	List<WorkPackageFare> addonFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "ADDON");
		for(WorkPackageFare fare : addonFares) {
			workPackageFareService.delete(fare.getId());
		}
    	if(workPackage.getAddonFares() != null && workPackage.getAddonFares().size() > 0) {
    		for(WorkPackageFare wpf : workPackage.getAddonFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("ADDON");
    			workPackageFareService.save(wpf);
    		}
    	}
    	
    	//Market Fares
    	List<WorkPackageFare> marketFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "MARKET");
		for(WorkPackageFare fare : marketFares) {
			workPackageFareService.delete(fare.getId());
		} 
    	if(workPackage.getMarketFares() != null && workPackage.getMarketFares().size() > 0) {
    		for(WorkPackageFare wpf : workPackage.getMarketFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("MARKET");
    			workPackageFareService.save(wpf);
    		}
    	}
    	
    	//Discount Fares
    	List<WorkPackageFare> discountFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "DISCOUNT");
		for(WorkPackageFare fare : discountFares) {
			workPackageFareService.delete(fare.getId());
		}  
    	if(workPackage.getDiscountFares() != null && workPackage.getDiscountFares().size() > 0) {
    		for(WorkPackageFare wpf : workPackage.getDiscountFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("DISCOUNT");
    			workPackageFareService.save(wpf);
    		}
    	}
    	
    	if(workPackage.getMarketRulesData() != null) {
    		for(MarketRules marketRules : workPackage.getMarketRulesData()) {
    			log.debug("REST request to SAVE WorkPackage MARKET RULES DATA : {}", workPackage.getMarketRulesData());
	    	    
    			try {
    				InputStream input = new ByteArrayInputStream(marketRules.getFile());
	    			Workbook workbook = new XSSFWorkbook(input);
				Sheet datatypeSheet = workbook.getSheetAt(0);	
				
				String ruleApplication = datatypeSheet.getRow(2).getCell(1).getStringCellValue();
				String eligibility = datatypeSheet.getRow(3).getCell(1).getStringCellValue();
				String dayTime = datatypeSheet.getRow(4).getCell(1).getStringCellValue();
				String season = datatypeSheet.getRow(5).getCell(1).getStringCellValue();
				String flightApps = datatypeSheet.getRow(6).getCell(1).getStringCellValue();
				String advancePurchase = datatypeSheet.getRow(7).getCell(1).getStringCellValue();
				String minStay = datatypeSheet.getRow(8).getCell(1).getStringCellValue();
				String maxStay = datatypeSheet.getRow(9).getCell(1).getStringCellValue();
				String stopover = datatypeSheet.getRow(10).getCell(1).getStringCellValue();
				String transfer = datatypeSheet.getRow(11).getCell(1).getStringCellValue();
				String combination = datatypeSheet.getRow(12).getCell(1).getStringCellValue();
				String blackout = datatypeSheet.getRow(13).getCell(1).getStringCellValue();
				String surcharge = datatypeSheet.getRow(14).getCell(1).getStringCellValue();
				String accompanied = datatypeSheet.getRow(15).getCell(1).getStringCellValue();
				String travel = datatypeSheet.getRow(16).getCell(1).getStringCellValue();
				String sales = datatypeSheet.getRow(17).getCell(1).getStringCellValue();
				String penalties = datatypeSheet.getRow(18).getCell(1).getStringCellValue();
				String ticketEndorsement = datatypeSheet.getRow(19).getCell(1).getStringCellValue();
				String child = datatypeSheet.getRow(20).getCell(1).getStringCellValue();
				String tourCode = datatypeSheet.getRow(21).getCell(1).getStringCellValue();
				
				FareRule fareRule = new FareRule();
				fareRule.setRuleApp(ruleApplication);
				fareRule.setEligibility(eligibility);
				fareRule.setDayTime(dayTime);
				fareRule.setSeasonality(season);
				fareRule.setFlightApp(flightApps);
				fareRule.setAdvBkg(advancePurchase);
				fareRule.setMinStay(minStay);
				fareRule.setMaxStay(maxStay);
				fareRule.setStopover(stopover);
				fareRule.setTransfers(transfer);
				fareRule.setCombination(combination);
				fareRule.setBlackouts(blackout);
				fareRule.setSurcharges(surcharge);
				fareRule.setAccompaniedTravel(accompanied);
				fareRule.setTravelRestriction(travel);
				fareRule.setSalesRestriction(sales);
				fareRule.setPenalties(penalties);
				fareRule.setTktEndorse(ticketEndorsement);
				fareRule.setChildDisc(child);
				fareRule.setTours(tourCode);
				marketRules.setFareRule(fareRule);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		}
    	}
    	*/
    	
    	if(workPackage.getComment() != null) {
	    	for(Comment comments : workPackage.getComment()) {
	    		if(comments.getUsername() == null && comments.getCreatedTime() == null) {
	    			comments.setUsername(SecurityUtils.getCurrentUserLogin().get());
	    			comments.setCreatedTime(ZonedDateTime.now());
	    		}
	    	}
    	}
    	
    	if(workPackage.getInterofficeComment() != null) {
	    	for(Comment comments : workPackage.getInterofficeComment()) {
	    		if(comments.getUsername() == null && comments.getCreatedTime() == null) {
	    			comments.setUsername(SecurityUtils.getCurrentUserLogin().get());
	    			comments.setCreatedTime(ZonedDateTime.now());
	    		}
	    	}
    	}
    	
    	if(workPackage.getAttachmentData() != null) {
    		for(Attachment attachment : workPackage.getAttachmentData()) {
    			if(attachment.getUsername() == null && attachment.getCreatedTime() == null) {
    				attachment.setUsername(SecurityUtils.getCurrentUserLogin().get());
    				attachment.setCreatedTime(ZonedDateTime.now());
    			}
    		}
    	}
    	
    	if(workPackage.getFilingInstructionData() != null) {
    		for(FilingInstruction filingInstruction : workPackage.getFilingInstructionData()) {    			
    			if(filingInstruction.getUsername() == null && filingInstruction.getCreatedTime() == null) {
    				filingInstruction.setUsername(SecurityUtils.getCurrentUserLogin().get());
    				filingInstruction.setCreatedTime(ZonedDateTime.now());
    			}
    		}
    	}
    	
    	if(workPackage.getMarketRulesData() != null) {
    		for(MarketRules marketRules : workPackage.getMarketRulesData()) {
    			if(marketRules.getUsername() == null && marketRules.getCreatedTime() == null) {
    				marketRules.setUsername(SecurityUtils.getCurrentUserLogin().get());
    				marketRules.setCreatedTime(ZonedDateTime.now());
    			}
    		}
    	}
    	
    	if(workPackage.getSaleDate() != null && (workPackage.getStatus() != Status.DISTRIBUTED) && workPackage.getTargetDistribution().contentEquals("ATPCO")) {
	    	Sort sort = new Sort(Direction.ASC, "priority");
	    	List<Priority> priorities = priorityRepository.findAll(sort);
	    	
	    	boolean found = false;
	    	
	    	for(Priority p : priorities) {
	    		if(p.getType().contentEquals("DAYS")) {
	    			long val = zonedDateTimeDifference(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS), workPackage.getSaleDate().truncatedTo(ChronoUnit.DAYS), ChronoUnit.DAYS);
	    			long value = p.getValue();

	    			if(val <= value) {    				
	    				workPackage.setPriority(p.getName());
	    				found = true;
	    				break;
	    			}
	    		}
	    	}
	    	
	    	if(!found) {
		    	Sort sortDesc = new Sort(Direction.DESC, "priority");
		    	List<Priority> prioritiesDesc = priorityRepository.findAll(sortDesc);
		    	workPackage.setPriority(prioritiesDesc.get(0).getName());
	    	}
    	}
    	
    	List<WorkPackageFare> allFares = new ArrayList<>();
    	if(workPackage.getFareSheet().size() > 0) {
    		for(WorkPackageFareSheet sheet : workPackage.getFareSheet()) {
    			if(sheet.getFares().size() > 0) {
    				for(WorkPackageFare fares : sheet.getFares()) {
    					if(fares.getSaleStart() != null) {
    						allFares.add(fares);
    						continue;
    					}
    				}
    			}
    		}
    	}
    	if(workPackage.getAddonFareSheet().size() > 0) {
    		for(WorkPackageFareSheet sheet : workPackage.getAddonFareSheet()) {
    			if(sheet.getFares().size() > 0) {
    				for(WorkPackageFare fares : sheet.getFares()) {
    					if(fares.getSaleStart() != null) {
    						allFares.add(fares);
    						continue;
    					}
    				}
    			}
    		}
    	}
    	if(workPackage.getDiscountFareSheet().size() > 0) {
    		for(WorkPackageFareSheet sheet : workPackage.getDiscountFareSheet()) {
    			if(sheet.getFares().size() > 0) {
    				for(WorkPackageFare fares : sheet.getFares()) {
    					if(fares.getSaleStart() != null) {
    						allFares.add(fares);
    						continue;
    					}
    				}
    			}
    		}
    	}
    	if(workPackage.getMarketFareSheet().size() > 0) {
    		for(WorkPackageFareSheet sheet : workPackage.getMarketFareSheet()) {
    			if(sheet.getFares().size() > 0) {
    				for(WorkPackageFare fares : sheet.getFares()) {
    					if(fares.getSaleStart() != null) {
    						allFares.add(fares);
    						continue;
    					}
    				}
    			}
    		}
    	}
    	if(workPackage.getWaiverFareSheet().size() > 0) {
    		for(WorkPackageFareSheet sheet : workPackage.getWaiverFareSheet()) {
    			if(sheet.getFares().size() > 0) {
    				for(WorkPackageFare fares : sheet.getFares()) {
    					if(fares.getSaleStart() != null) {
    						allFares.add(fares);
    						continue;
    					}
    				}
    			}
    		}
    	}
    	
    	if(allFares.size() > 0) {
	    	Collections.sort(allFares, new WorkPackageFare.WorkPackageFareComparator());  
	    	workPackage.setSaleDate(allFares.get(0).getSaleStart());
    	}
    	
    	workPackage.setValidation(null);
    	workPackage = workPackageService.save(workPackage);
	    
    	if(workPackage.isValidate()) {
    		WorkPackage.Validation validation = new WorkPackage.Validation();
    		
    		List<WorkPackage.Validation.Tab> tabs = new ArrayList<WorkPackage.Validation.Tab>();
	    		WorkPackage.Validation.Tab tab1 = new WorkPackage.Validation.Tab();
		    		tab1.setName("Fare");
		    		List<WorkPackage.Validation.Tab.Error> errors = new ArrayList<>();
		    		
		    			//List Error
			    		WorkPackage.Validation.Tab.Error err1 = new WorkPackage.Validation.Tab.Error();
			    		err1.setMessage("ERROR 1");
			    		errors.add(err1);
			    		
		    		tab1.setError(errors);
		    		
		    		List<WorkPackage.Validation.Tab.Error> warnings = new ArrayList<>();
		    		
			    		//List Warning
			    		WorkPackage.Validation.Tab.Error warn1 = new WorkPackage.Validation.Tab.Error();
			    		warn1.setMessage("WARNING 1");
			    		warnings.add(err1);
			    		
		    		tab1.setWarning(warnings);
	    		tabs.add(tab1);
    		validation.setTab(tabs);
    		workPackage.setValidation(validation);
    	}
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(workPackage));    	
    }
    
    static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit){
        return unit.between(d1, d2);
    }

    /**
     * GET  /work-packages : get all the workPackages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of workPackages in body
     */
    @GetMapping("/work-packages")
    @Timed
    public ResponseEntity<List<WorkPackage>> getAllWorkPackages(WorkPackageFilter filter, Pageable pageable) {
        log.debug("REST request to get a page of WorkPackages {}", filter);
//        Page<WorkPackage> page = workPackageService.findAllByOrderByLastModifiedDate(pageable);       
        Page<WorkPackage> page = workPackageService.findCustom(filter, pageable);       
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/work-packages");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    public static class WorkPackageFilter{
    	public ReviewLevel reviewLevel;
    	public Status status;
    	public DistributionType distributionType;
    	public Type type;
    	public String approvalReference;
    	
    	public static class ReviewLevel{
    		public boolean ho;
    		public boolean lso;
    		public boolean distribution;
    		public boolean routeManagement;
    		
    		public ReviewLevel() {
				// TODO Auto-generated constructor stub
			}
    		
			public boolean isHo() {
				return ho;
			}
			public void setHo(boolean ho) {
				this.ho = ho;
			}
			public boolean isLso() {
				return lso;
			}
			public void setLso(boolean lso) {
				this.lso = lso;
			}
			public boolean isDistribution() {
				return distribution;
			}
			public void setDistribution(boolean distribution) {
				this.distribution = distribution;
			}
			public boolean isRouteManagement() {
				return routeManagement;
			}
			public void setRouteManagement(boolean routeManagement) {
				this.routeManagement = routeManagement;
			}
			@Override
			public String toString() {
				return "ReviewLevel [ho=" + ho + ", lso=" + lso + ", distribution=" + distribution
						+ ", routeManagement=" + routeManagement + "]";
			}
    	}

    	public static class Status{
    		public boolean newStatus;
    		public boolean distributed;
    		public boolean reviewing;
    		public boolean readyToRelease;
    		public boolean pending;
    		public boolean completed;
    		public boolean withdrawn;
        	public boolean replace;
        	public boolean reuse;
        	public boolean referred;
        	
			public boolean isReferred() {
				return referred;
			}
			public void setReferred(boolean referred) {
				this.referred = referred;
			}
			public boolean isWithdrawn() {
				return withdrawn;
			}
			public void setWithdrawn(boolean withdrawn) {
				this.withdrawn = withdrawn;
			}
			public boolean isNewStatus() {
				return newStatus;
			}
			public void setNewStatus(boolean newStatus) {
				this.newStatus = newStatus;
			}
			public boolean isDistributed() {
				return distributed;
			}
			public void setDistributed(boolean distributed) {
				this.distributed = distributed;
			}
			public boolean isReviewing() {
				return reviewing;
			}
			public void setReviewing(boolean reviewing) {
				this.reviewing = reviewing;
			}
			public boolean isReadyToRelease() {
				return readyToRelease;
			}
			public void setReadyToRelease(boolean readyToRelease) {
				this.readyToRelease = readyToRelease;
			}
			public boolean isPending() {
				return pending;
			}
			public void setPending(boolean pending) {
				this.pending = pending;
			}
			public boolean isCompleted() {
				return completed;
			}
			public void setCompleted(boolean completed) {
				this.completed = completed;
			}
			public boolean isReplace() {
				return replace;
			}
			public void setReplace(boolean replace) {
				this.replace = replace;
			}
			public boolean isReuse() {
				return reuse;
			}
			public void setReuse(boolean reuse) {
				this.reuse = reuse;
			}
    		
    		
    	}
    	
    	public static class DistributionType{
    		public boolean atpco;
    		public boolean market;
    		public boolean waiver;
			public boolean isAtpco() {
				return atpco;
			}
			public void setAtpco(boolean atpco) {
				this.atpco = atpco;
			}
			public boolean isMarket() {
				return market;
			}
			public void setMarket(boolean market) {
				this.market = market;
			}
			public boolean isWaiver() {
				return waiver;
			}
			public void setWaiver(boolean waiver) {
				this.waiver = waiver;
			}
    	}
    
    	public static class Type{
    		public boolean regular;
    		public boolean discount;
    		public boolean waiver;
			public boolean isRegular() {
				return regular;
			}
			public void setRegular(boolean regular) {
				this.regular = regular;
			}
			public boolean isDiscount() {
				return discount;
			}
			public void setDiscount(boolean discount) {
				this.discount = discount;
			}
			public boolean isWaiver() {
				return waiver;
			}
			public void setWaiver(boolean waiver) {
				this.waiver = waiver;
			}
    	}
    	
		public ReviewLevel getReviewLevel() {
			return reviewLevel;
		}

		public void setReviewLevel(ReviewLevel reviewLevel) {
			this.reviewLevel = reviewLevel;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public DistributionType getDistributionType() {
			return distributionType;
		}

		public void setDistributionType(DistributionType distributionType) {
			this.distributionType = distributionType;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		
		public String getApprovalReference() {
			return approvalReference;
		}

		public void setApprovalReference(String approvalReference) {
			this.approvalReference = approvalReference;
		}

		@Override
		public String toString() {
			return "WorkPackageFilter [reviewLevel=" + reviewLevel + "]";
		}
    }
    
    /**
     * GET  /work-packages/:id : get the "id" workPackage.
     *
     * @param id the id of the workPackage to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the workPackage, or with status 404 (Not Found)
     */
    @GetMapping("/work-packages/{id}")
    @Timed
    public ResponseEntity<WorkPackage> getWorkPackage(@PathVariable String id) {
        log.debug("REST request to get WorkPackage : {}", id);
        WorkPackage workPackage = workPackageService.findOne(id);
        if(workPackage.getStatus() == Status.PENDING || workPackage.getStatus() == Status.REFERRED) {
        	workPackage.setStatus(Status.REVIEWING);
        	workPackageService.save(workPackage);
        }
        
        workPackage.setLocked(true);
        workPackage.setLockedBy(SecurityUtils.getCurrentUserLogin().get());
        workPackage.setLockedSince(ZonedDateTime.now());
        workPackage = workPackageService.save(workPackage);
//        List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), null);
//        log.debug("REST request to set WorkPackageFARES : {}", fares.size());
//        workPackage.setFares(fares);
//       
//        List<WorkPackageFare> addonFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "ADDON");
//        log.debug("REST request to set WorkPackageFARES Addon : {}", addonFares.size());
//        workPackage.setAddonFares(addonFares);
//        
//        List<WorkPackageFare> marketFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "MARKET");
//        log.debug("REST request to set WorkPackageFARES Market: {}", marketFares.size());
//        workPackage.setMarketFares(marketFares);
//        
//        List<WorkPackageFare> discountFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "DISCOUNT");
//        log.debug("REST request to set WorkPackageFARES Discount: {}", discountFares.size());
//        workPackage.setDiscountFares(discountFares);
        
//        if(workPackage.getVersion() == null) {
//        	workPackage.setVersion("current");
//        }
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(workPackage));
    }
    
    /**
     * GET  /work-packages/history/:id : get the "id" workPackage.
     *
     * @param id the id of the workPackage to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the workPackage, or with status 404 (Not Found)
     */
    @GetMapping("/work-packages/history/{id}")
    @Timed
    public ResponseEntity<List<WorkPackageHistory>> getWorkPackageHistory(@PathVariable String id) {
        log.debug("REST request to get WorkPackage : {}", id);
        WorkPackage workPackage = workPackageService.findOne(id);
        
        List<WorkPackageHistory> history = workPackageHistoryService.findAllByWorkPackage(new ObjectId(workPackage.getId()));
        
//        List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackage(workPackage.getId());
//        log.debug("REST request to get WorkPackageFARES : {}", fares.size());
//        workPackage.setFares(fares);
//       
//        List<WorkPackageFare> addonFares = workPackageFareService.findAllByWorkPackageAndFareType(workPackage.getId(), "ADDON");
//        log.debug("REST request to get WorkPackageFARES : {}", fares.size());
//        workPackage.setFares(fares);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(history));
    }

    /**
     * DELETE  /work-packages/:id : delete the "id" workPackage.
     *
     * @param id the id of the workPackage to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/work-packages/{id}")
    @Timed
    public ResponseEntity<Void> deleteWorkPackage(@PathVariable String id) {
        log.debug("REST request to delete WorkPackage : {}", id);
        
        List<WorkPackageFare> fares = workPackageFareService.findAllByWorkPackage(id);
        for(WorkPackageFare wp : fares){
        		workPackageFareService.delete(wp.getId());
        }
        workPackageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }
    
    /**
     * POST  /work-packages/passup : Passup
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/passup")
    @Timed
    public ResponseEntity<WorkPackage> passupWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to passup WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
                        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        String reviewLevel = result.getReviewLevel();
        if(reviewLevel.contentEquals("LSO")) {
    		result.setReviewLevel("HO");
    		result.setStatus(Status.PENDING);
    		result.setLocked(false);
        }
//        else if(reviewLevel.contentEquals("LSO2")) {
//    		result.setReviewLevel("HO1");
//    		result.setStatus(Status.PENDING);
//        }
//        else if(reviewLevel.contentEquals("HO1")) {
//    		result.setReviewLevel("HO2");
//    		result.setStatus(Status.PENDING);
//        }
//        else if(reviewLevel.contentEquals("HO2")) {
//        		//cannot passup
//        }
        
        workPackageService.save(result);
        
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("PASSUP");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/unlock : unlock
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/unlock")
    @Timed
    public ResponseEntity<WorkPackage> unlockWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to withdraw WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setLocked(false);
        workPackageService.save(result);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }
    
    /**
     * POST  /work-packages/withdraw : withdraw
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/withdraw")
    @Timed
    public ResponseEntity<WorkPackage> withdrawWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to withdraw WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setStatus(Status.WITHDRAWN);
        workPackageService.save(result);
        /*
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
                        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        String reviewLevel = result.getReviewLevel();
        if(reviewLevel.contentEquals("LSO")) {
    		result.setReviewLevel("HO");
    		result.setStatus(Status.PENDING);
        }
//        else if(reviewLevel.contentEquals("LSO2")) {
//    		result.setReviewLevel("HO1");
//    		result.setStatus(Status.PENDING);
//        }
//        else if(reviewLevel.contentEquals("HO1")) {
//    		result.setReviewLevel("HO2");
//    		result.setStatus(Status.PENDING);
//        }
//        else if(reviewLevel.contentEquals("HO2")) {
//        		//cannot passup
//        }
        
        workPackageService.save(result);
        
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("PASSUP");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        */
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    private void saveHistoryData(WorkPackage workPackage) {
		// TODO Auto-generated method stub
    		String woId = workPackage.getId();        
        long count = workPackageHistoryDataRepository.countByWorkPackage(new ObjectId(woId));
        count = count+1;
        WorkPackageHistoryData wphd = new WorkPackageHistoryData(workPackage, String.valueOf(count));        
        wphd.setWorkPackage(new ObjectId(woId));       
        log.debug("REST request to save WorkPackage History Data : {}", wphd);
        workPackageHistoryDataRepository.save(wphd);

//	    	if(workPackage.getFares() != null && workPackage.getFares().size() > 0) {	    		
//	    		List<WorkPackageFare> fares = workPackageFareHistoryDataRepository.findAllByWorkPackageAndFareType(new ObjectId(wphd.getId()), null);
//	    		for(WorkPackageFare fare : fares) {
//	    			workPackageFareHistoryDataRepository.delete(fare.getId());
//	    		}
//	    		
//	    		for(WorkPackageFare wpf : workPackage.getFares()) {
//	    			WorkPackageFareHistoryData fareHistory = new WorkPackageFareHistoryData(wpf, wphd.getVersion());
//	    			fareHistory.setWorkPackage(new ObjectId(wphd.getId()));
//	    			workPackageFareHistoryDataRepository.save(fareHistory);
//	    		}
//	    	}
    
    	/*
    	
    	if(workPackage.getAddonFares() != null && workPackage.getAddonFares().size() > 0) {
    		List<WorkPackageFare> fares = workPackageFareHistoryRepository.findAllByWorkPackageAndFareType(workPackage.getId(), "ADDON");
    		for(WorkPackageFare fare : fares) {
    			workPackageFareHistoryRepository.delete(fare.getId());
    		}
    		
    		for(WorkPackageFare wpf : workPackage.getAddonFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("ADDON");
    			workPackageFareHistoryRepository.save(wpf);
    		}
    	}
    	
    	if(workPackage.getMarketFares() != null && workPackage.getMarketFares().size() > 0) {
    		List<WorkPackageFare> fares = workPackageFareHistoryRepository.findAllByWorkPackageAndFareType(workPackage.getId(), "MARKET");
    		for(WorkPackageFare fare : fares) {
    			workPackageFareHistoryRepository.delete(fare.getId());
    		}  
    		for(WorkPackageFare wpf : workPackage.getMarketFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("MARKET");
    			workPackageFareHistoryRepository.save(wpf);
    		}
    	}
    	
    	if(workPackage.getDiscountFares() != null && workPackage.getDiscountFares().size() > 0) {
    		System.out.println("DISCOUNT FARES SAVE");
    		List<WorkPackageFare> fares = workPackageFareHistoryRepository.findAllByWorkPackageAndFareType(workPackage.getId(), "DISCOUNT");
    		for(WorkPackageFare fare : fares) {
    			workPackageFareHistoryRepository.delete(fare.getId());
    		}       
    		for(WorkPackageFare wpf : workPackage.getDiscountFares()) {
    			wpf.setWorkPackage(new ObjectId(workPackage.getId()));
    			wpf.setFareType("DISCOUNT");
    			workPackageFareHistoryRepository.save(wpf);
    		}
    	}
    	*/
	}

	/**
     * POST  /work-packages/passdown : Passdown
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/passdown")
    @Timed
    public ResponseEntity<WorkPackage> passdownWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to passdown WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        String reviewLevel = result.getReviewLevel();
        if(reviewLevel.contentEquals("HO")) {
    		result.setReviewLevel("LSO");
    		result.setStatus(Status.PENDING);
    		result.setLocked(false);
        }
        workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("PASSDOWN");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/passsideway : Passsideway
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/passsideway")
    @Timed
    public ResponseEntity<WorkPackage> passsidewayWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to passsideway WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        String reviewLevel = result.getReviewLevel();
        
        if(result.getSidewayReviewLevel() == null) {
    		result.setSidewayReviewLevel(reviewLevel);
    		result.setReviewLevel("ROUTE_MANAGEMENT");
        }
        else {
     		result.setReviewLevel(result.getSidewayReviewLevel());
    		result.setSidewayReviewLevel(null);
        }
		result.setLocked(false);
        result.setStatus(Status.PENDING);
        workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("PASSSIDEWAY");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/approve : Approve
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/approve")
    @Timed
    public ResponseEntity<WorkPackage> approveWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to approve WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        String reviewLevel = result.getReviewLevel();
        
        if(reviewLevel.contentEquals("HO")) {
    		result.setDistributionReviewLevel(reviewLevel);
    		result.setReviewLevel("DISTRIBUTION");
    		result.setLocked(false);
    		result.setStatus(Status.PENDING);        		
	    }
        workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("APPROVE");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        String[] emailData = null;
        if(workPackage.getApproveConfig().getEmail() != null && workPackage.getApproveConfig().getEmail().size() > 0) {
	        emailData = new String[workPackage.getApproveConfig().getEmail().size()];
	        for (int i=0;i<workPackage.getApproveConfig().getEmail().size();i++) {
	        	emailData[i] = workPackage.getApproveConfig().getEmail().get(i);
	        }
        }
        
        String[] emailDataCc = null;
        if(workPackage.getApproveConfig().getCcEmail() != null && workPackage.getApproveConfig().getCcEmail().size() > 0) {
	        emailDataCc = new String[workPackage.getApproveConfig().getCcEmail().size()];        
	        for (int i=0;i<workPackage.getApproveConfig().getCcEmail().size();i++) {
	        	emailDataCc[i] = workPackage.getApproveConfig().getCcEmail().get(i);
	        }
        }
        User u = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        
        String content = "<h2>Inter Office Comment</h2>";
        content += "<br/></br>";
        content += "<table>";
        	content += "<thead>";        
	        content += "<tr>";
	        	content += 	"<th>Message</th>";
	        	content += 	"<th>Username</th>";
	        	content += 	"<th>Date</th>";
	        content += "</tr>";
	        content += "</thead>";  
	        
	        content += "<tbody>";  
	        	if(workPackage.getInterofficeComment() != null) {
		        	for(Comment c : workPackage.getInterofficeComment()) {
				        content += "<tr>";
				        	content += 	"<td>"+c.getComment()+"</td>";
				        	content += 	"<td>"+c.getUsername()+"</td>";
				        	content += 	"<td>"+c.getCreatedTime().toString()+"</td>";
				        content += "</tr>";
		        	}
	        	}
	        	else {
	        		content += "<tr>";
		        		content += 	"<td colspan='3'>No Interoffice Comment</td>";
		        	content += "</tr>";
	        	}
        	content += "</tbody>";  
        content += "</table>";
        
        List<Attachment> sendAttachments = new ArrayList<>();
        List<Attachment> attachments = workPackage.getAttachmentData();
        for (Attachment attachment : attachments) {
        	try {
            	if(attachment.getInOnly().equals(true)) {
            		sendAttachments.add(attachment);
            	}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
        if(!sendAttachments.isEmpty()) {
        	log.debug("SEND EMAIL WITH ATTACHMENT");
        	mailService.sendEmailWithAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true, sendAttachments);
        }
        else {
        	log.debug("SEND EMAIL WITHOUT ATTACHMENT");
        	mailService.sendEmailWithoutAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true);
        }
//        if(workPackage.getApproveConfig().attachment) {
//        	log.debug("SEND EMAIL WITH ATTACHMENT");
//        	mailService.sendEmailWithAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true, sendAttachments);
//        }
//        else {
//        	log.debug("SEND EMAIL WITHOUT ATTACHMENT");
//        	mailService.sendEmailWithoutAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true);
//        }
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/resend-approve : Approve
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/resend-approve")
    @Timed
    public ResponseEntity<WorkPackage> resendApproveWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to resend approve WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setLocked(false);
        workPackageService.save(result);
        
        String[] emailData = null;
        if(workPackage.getApproveConfig().getEmail() != null && workPackage.getApproveConfig().getEmail().size() > 0) {
	        emailData = new String[workPackage.getApproveConfig().getEmail().size()];
	        for (int i=0;i<workPackage.getApproveConfig().getEmail().size();i++) {
	        	emailData[i] = workPackage.getApproveConfig().getEmail().get(i);
	        	log.debug("cek : "+emailData[i]);
	        }
        }
        
        String[] emailDataCc = null;
        if(workPackage.getApproveConfig().getCcEmail() != null && workPackage.getApproveConfig().getCcEmail().size() > 0) {
	        emailDataCc = new String[workPackage.getApproveConfig().getCcEmail().size()];        
	        for (int i=0;i<workPackage.getApproveConfig().getCcEmail().size();i++) {
	        	emailDataCc[i] = workPackage.getApproveConfig().getCcEmail().get(i);
	        }
        }
        User u = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        
        String content = "<h2>Inter Office Comment</h2>";
        content += "<br/></br>";
        content += "<table>";
        	content += "<thead>";        
	        content += "<tr>";
	        	content += 	"<th>Message</th>";
	        	content += 	"<th>Username</th>";
	        	content += 	"<th>Date</th>";
	        content += "</tr>";
	        content += "</thead>";  
	        
	        content += "<tbody>";  
	        	if(workPackage.getInterofficeComment() != null) {
		        	for(Comment c : workPackage.getInterofficeComment()) {
				        content += "<tr>";
				        	content += 	"<td>"+c.getComment()+"</td>";
				        	content += 	"<td>"+c.getUsername()+"</td>";
				        	content += 	"<td>"+c.getCreatedTime().toString()+"</td>";
				        content += "</tr>";
		        	}
	        	}
	        	else {
	        		content += "<tr>";
		        		content += 	"<td colspan='3'>No Interoffice Comment</td>";
		        	content += "</tr>";
	        	}
        	content += "</tbody>";  
        content += "</table>";
        
        List<Attachment> sendAttachments = new ArrayList<>();
        List<Attachment> attachments = workPackage.getAttachmentData();
        for (Attachment attachment : attachments) {
        	try {
        		if(attachment.getInOnly().equals(true)) {
            		sendAttachments.add(attachment);
            	}
			} catch (Exception e) {	}        	
		}
        if(!sendAttachments.isEmpty()) {
        	log.debug("SEND EMAIL WITH ATTACHMENT");
        	mailService.sendEmailWithAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true, sendAttachments);
        }
        else {
        	log.debug("SEND EMAIL WITHOUT ATTACHMENT");
        	mailService.sendEmailWithoutAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true);
        }
        
//        if(workPackage.getApproveConfig().attachment) {
//        	log.debug("SEND EMAIL WITH ATTACHMENT");
//        	mailService.sendEmailWithAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true, workPackage.getAttachmentData());
//        }
//        else {
//        	log.debug("SEND EMAIL WITHOUT ATTACHMENT");
//        	mailService.sendEmailWithoutAttachment(u.getEmail(), emailData, emailDataCc, "Approve", content, true, true);
//        }
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/referback : referback
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/referback")
    @Timed
    public ResponseEntity<WorkPackage> referbackWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to referback WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());

        result.setReviewLevel(result.getDistributionReviewLevel());
        result.setDistributionReviewLevel(null);
        result.setStatus(Status.REFERRED);
		result.setLocked(false);
        workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("REFERBACK");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/createbatch : createbatch
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/createbatch")
    @Timed
    public ResponseEntity<WorkPackage> createbatchWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to createbatch WorkPackage : {}", workPackage);
        /*
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
                
        StringBuilder batchBuilder = new StringBuilder();
        
        batchBuilder.append("W\t\t\tNN");
        batchBuilder.append("\n");        
        try {
        batchBuilder.append(workPackage.getFilingDetail().getEmail());
        }catch(Exception e) {}
        
        batchBuilder.append("\n");        
        batchBuilder.append("B  N  "+workPackage.getSpecifiedFaresName());
        batchBuilder.append("\n");        
        for(WorkPackageFare fare : workPackage.getFares()) {
	    		batchBuilder.append("F");
	    		batchBuilder.append(fare.getCarrier());
	    		batchBuilder.append(fare.getTarno());
	    		
	    		try {
		    		if(fare.getAction().contentEquals("New")) {
		    			batchBuilder.append("N");
		    		}else {
		    			batchBuilder.append(" ");
		    		}
	    		}catch(Exception e) {
	    			batchBuilder.append(" ");
	    		}
	    		batchBuilder.append(fare.getOrigin());
	    		batchBuilder.append(fare.getDestination());
	    		batchBuilder.append(fare.getFareBasis());
	    		batchBuilder.append("  ");
	    		if(fare.getTypeOfJourney().contentEquals("One Way") || fare.getTypeOfJourney().contentEquals("One Way Only") ) {
	    			batchBuilder.append("1");            			
	    		}
	    		if(fare.getTypeOfJourney().contentEquals("Return") || fare.getTypeOfJourney().contentEquals("Round Trip")) {
	    			batchBuilder.append("2");            			
	    		}
	
	    		if(fare.getRtgno() == null) {
	    			batchBuilder.append("0000");
	    		}
	    		else {
	    			batchBuilder.append(fare.getRtgno());
	    		}
	    		batchBuilder.append("  ");
	    		batchBuilder.append(fare.getCurrency());
	    		batchBuilder.append(fare.getAmount());
	    		
	    		batchBuilder.append("\n");           		
        }
        
        if(workPackage.getAddonFares() != null && workPackage.getAddonFares().size() > 0) {
        	
        }
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setStatus(Status.READY_TO_RELEASE);
        workPackage = workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("CREATEBATCH");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        result.setBatchString(batchBuilder.toString());
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
*/
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    
    /**
     * POST  /work-packages/revisebatch : revisebatch
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/revisebatch")
    @Timed
    public ResponseEntity<WorkPackage> revisebatchWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to revisebatch WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setStatus(Status.PENDING);
        workPackage = workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("REVISEBATCH");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        
        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/completebatch : completebatch
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/completebatch")
    @Timed
    public ResponseEntity<WorkPackage> completebatchWorkPackage(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to completebatch WorkPackage : {}", workPackage);
        if (workPackage.getId() == null) {
            throw new BadRequestAlertException("A workPackage should have an ID", ENTITY_NAME, "idexists");
        }
        
        WorkPackage result = workPackageService.findOne(workPackage.getId());
        result.setStatus(Status.DISTRIBUTED);
        workPackage = workPackageService.save(result);
        
        WorkPackageHistory history = new WorkPackageHistory();
        history.setWorkPackage(new ObjectId(result.getId()));
        history.setType("COMPLETEBATCH");
        history.setUsername(SecurityUtils.getCurrentUserLogin().get());
        workPackageHistoryService.save(history);
        

        saveHistoryData(workPackage);
        
        //updateWorkPackage(workPackage);
        
        return ResponseEntity.created(new URI("/api/work-packages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * POST  /work-packages/changeVersion : Create a new workPackage.
     *
     * @param workPackage the workPackage to change
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/work-packages/changeVersion")
    @Timed
    public ResponseEntity<WorkPackage> changeVersion(@RequestBody WorkPackage workPackage) throws URISyntaxException {
        log.debug("REST request to change version of WorkPackage : {}", workPackage);
        /*
        if(workPackage.getVersion().contentEquals("current")) {
        	updateWorkPackage(workPackage);
        }
        
        if(workPackage.getChangeType() != null && !workPackage.getChangeType().contentEquals("")) {
        	String version = "current";
        	String currentVersion = workPackage.getVersion();
    		
        	if(workPackage.getChangeType().contentEquals("previous")) {
                if(currentVersion.contentEquals("current")) {
        			version = workPackageHistoryDataRepository.countByWorkPackage(new ObjectId(workPackage.getId()))+"";
                }
                else if(Long.parseLong(currentVersion) > 1){                	                	
                	version = (Long.parseLong(currentVersion) - 1)+"";
                }
        	}
        	else if(workPackage.getChangeType().contentEquals("next")) {
        	    if(!currentVersion.contentEquals("current")) {
	        	    	if(Long.parseLong(currentVersion) != workPackageHistoryDataRepository.countByWorkPackage(new ObjectId(workPackage.getId()))) {
	        	    		version = (Long.parseLong(currentVersion) + 1) + "";
	        	    	}
	        	    	else {
	        	    		version = "current";
	        	    	}
            }
        	    else {
        	    		version = "1";
        	    }
        	}
        	
        	if(version.contentEquals("current")) {        		
        		workPackage = getWorkPackage(workPackage.getId()).getBody();
        	}
        	else {
        		workPackage = workPackageHistoryDataRepository.findByWorkPackageAndVersion(new ObjectId(workPackage.getId()), version);        	
        		
        		//get fare
        		List<WorkPackageFare> fares = workPackageFareHistoryDataRepository.findAllByWorkPackageAndFareType(new ObjectId(workPackage.getId()), null);
        		workPackage.setFares(fares);
        	}
        }
        */
        return ResponseEntity.created(new URI("/api/work-packages/" + workPackage.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, workPackage.getId().toString()))
            .body(workPackage);
    }

    public static class WorkPackageRateSheet {
    	private WorkPackage wp;
    	private String ruleText;
    	private String index;
    	private String[] header;
    	
		public WorkPackage getWp() {
			return wp;
		}
		public void setWp(WorkPackage wp) {
			this.wp = wp;
		}
		public String getRuleText() {
			return ruleText;
		}
		public void setRuleText(String ruleText) {
			this.ruleText = ruleText;
		}
		public String getIndex() {
			return index;
		}
		public void setIndex(String index) {
			this.index = index;
		}
		public String[] getHeader() {
			return header;
		}
		public void setHeader(String[] header) {
			this.header = header;
		}
		
    }
	/**
     * POST  /work-packages/export-fares : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackage(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFares : {}{}", wprs.getWp(), wprs.getRuleText());
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        Integer count = header.length;
        PdfPTable table;
        Document document;
        if(count < 25) {
        	document = new Document(PageSize.A4,30,30,60,0);
    	}else {
    		document = new Document(PageSize.A4.rotate());
    		document.setMargins(60, 30, 30, 30);
    	}
    	
    	PdfWriter.getInstance(document, output);    	
    	InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png");
    	byte[] imageByte = IOUtils.toByteArray(inputStream);    	
    	document.open();
    	Font font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
    	Image image = Image.getInstance(imageByte); 
    	
    	image.scaleToFit(PageSize.A4.getWidth()/4, PageSize.A4.getHeight()/4);
    	image.setAlignment(Image.MIDDLE);
    	String[] content = {"WORK ID : ","TITLE FARE SHEET : ","DESCRIPTION OF GA FARES : ","NOTES RATESHEET : ", "FARES : ", "RULE TEXT : "};
    	document.add(image); 
    	document.add(new Chunk(" "));

        Paragraph p1 = new Paragraph();
        Paragraph p2 = new Paragraph();
        Paragraph p3 = new Paragraph();
        Paragraph p4 = new Paragraph();
        Paragraph p5 = new Paragraph();
        Paragraph p6 = new Paragraph();

        p1.setFont(font);               
        p1.add(content[0]+" "+ workPackage.getWpid());        
    	document.add(p1);
    	document.add(new Chunk(" "));
        p2.setFont(font);               
        p2.add(content[1]+" "+workPackage.getName());
        document.add(p2);
    	document.add(new Chunk(" "));

        p3.setFont(font);               
        p3.add(content[2]+" "+workPackage.getFareSheet().get(idx).getSpecifiedFaresName());
    	document.add(p3);
    	document.add(new Chunk(" "));

        p4.setFont(font);               
        p4.add(content[3]+" "+workPackage.getRatesheetComment());
    	document.add(p4);
    	document.add(new Chunk(" "));

        p5.setFont(font);               
        p5.add(content[4]);
    	document.add(p5);
    	document.add(new Chunk(" "));

    	table = new PdfPTable(count);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		for(int l=0; l<header.length ;l++) {
			table.addCell(header[l]);
		}
		table.setHeaderRows(1);
        
		if(workPackage.getTargetDistribution().contentEquals("ATPCO")) {
			PdfPCell[] cells = table.getRow(0).getCells(); 
        	for (int j=0;j<cells.length;j++){
        			cells[j].setBackgroundColor(BaseColor.GRAY);
        	}
        	log.debug("cek : "+workPackage.getFareSheet().size());
        	for(int l=0; l<workPackage.getFareSheet().get(idx).getFares().size();l++) {
        		for (int i=0;i<header.length;i++){
            		if(header[i].contentEquals("Status")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getStatus());
            		}else if(header[i].contentEquals("Carrier")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getCarrier());
            		}else if(header[i].contentEquals("Action")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getAction());
            		}else if(header[i].contentEquals("Tar No")) {
            			try {
            				table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Tar Cd")) {
            			try {
            				table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Global")) {
            			try {
            				table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Origin")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getOrigin());
            		}else if(header[i].contentEquals("Destination")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getDestination());
            		}else if(header[i].contentEquals("Fare Class")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getFareBasis());
            		}else if(header[i].contentEquals("Booking Class")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getBookingClass());
            		}else if(header[i].contentEquals("Cabin")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getCabin());
            		}else if(header[i].contentEquals("OW/RT")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
            		}else if(header[i].contentEquals("Footnote")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getFootnote1());
            		}else if(header[i].contentEquals("Routing No")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getRtgno());
            		}else if(header[i].contentEquals("Rule No")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getRuleno());
            		}else if(header[i].contentEquals("Currency")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getCurrency());
            		}else if(header[i].contentEquals("Base Amt")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getAmount());
            		}else if(header[i].contentEquals("Amt Different")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("% Amt Different")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("YQYR")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getYqyr());
            		}else if(header[i].contentEquals("Cat 12")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getCat12());
            		}else if(header[i].contentEquals("TFC")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTfc());
            		}else if(header[i].contentEquals("Target AIF")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getAif());
            		}else if(header[i].contentEquals("Itinerary")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getItinerary());
            		}else if(header[i].contentEquals("Override Indicator")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getOverrideIndicator().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel Start")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel End")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales Start")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales End")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("EffDt")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getEffDt());
            		}else if(header[i].contentEquals("Comment")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getComment());
            		}else if(header[i].contentEquals("Travel Complete")) {
            			try {
                			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel Complete Indicator")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
            		}else if(header[i].contentEquals("RateSheet Comment")) {
            			table.addCell(workPackage.getFareSheet().get(idx).getFares().get(l).getRatesheetComment());
            		}
            		else {
            			table.addCell("-");
            		}
            	}        		
        	}
        	
          document.add(table);
		}else if(workPackage.getTargetDistribution().contentEquals("MARKET")){
			PdfPCell[] cells = table.getRow(0).getCells(); 
        	for (int j=0;j<cells.length;j++){
        			cells[j].setBackgroundColor(BaseColor.GRAY);
        	}
        	log.debug("cek : "+workPackage.getMarketFareSheet().size());
        	for(int l=0; l<workPackage.getMarketFareSheet().get(idx).getFares().size();l++) {
        		for (int i=0;i<header.length;i++){
            		if(header[i].contentEquals("Status")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getStatus());
            		}else if(header[i].contentEquals("Carrier")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCarrier());
            		}else if(header[i].contentEquals("Action")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAction());
            		}else if(header[i].contentEquals("Tar No")) {
            			try {
            				table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Tar Cd")) {
            			try {
            				table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Global")) {
            			try {
            				table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}            			
            		}else if(header[i].contentEquals("Origin")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOrigin());
            		}else if(header[i].contentEquals("Destination")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getDestination());
            		}else if(header[i].contentEquals("Fare Class")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFareBasis());
            		}else if(header[i].contentEquals("Booking Class")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBookingClass());
            		}else if(header[i].contentEquals("Cabin")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCabin());
            		}else if(header[i].contentEquals("OW/RT")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
            		}else if(header[i].contentEquals("Footnote")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFootnote1());
            		}else if(header[i].contentEquals("Routing No")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRtgno());
            		}else if(header[i].contentEquals("Rule No")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRuleno());
            		}else if(header[i].contentEquals("Currency")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCurrency());
            		}else if(header[i].contentEquals("Base Amt")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAmount());
            		}else if(header[i].contentEquals("Amt Different")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("% Amt Different")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("YQYR")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getYqyr());
            		}else if(header[i].contentEquals("Cat 12")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCat12());
            		}else if(header[i].contentEquals("TFC")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTfc());
            		}else if(header[i].contentEquals("Target AIF")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAif());
            		}else if(header[i].contentEquals("Itinerary")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getItinerary());
            		}else if(header[i].contentEquals("Override Indicator")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOverrideIndicator().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel Start")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel End")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales Start")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales End")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("EffDt")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getEffDt());
            		}else if(header[i].contentEquals("Comment")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getComment());
            		}else if(header[i].contentEquals("Travel Complete")) {
            			try {
                			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel Complete Indicator")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
            		}else if(header[i].contentEquals("RateSheet Comment")) {
            			table.addCell(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRatesheetComment());
            		}
            		else {
            			table.addCell("-");
            		}
            	}        		
        	}
        	
          document.add(table);
		}
	  
        
	
        p6.setFont(font);               
        p6.add(content[5]+" "+ruleText);
    	document.add(p6);
    	document.add(new Chunk(" "));

    	document.close();
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-excel : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet-excel")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageExcel(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFaresExcel : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Rate Sheet");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;
        
        cell = row.createCell(0);
        cell.setCellValue("WORK ID :");
        cell = row.createCell(1);
        cell.setCellValue(workPackage.getWpid());
        
        XSSFRow row2 = spreadsheet.createRow(2);
        XSSFCell cell2;
        cell2 = row2.createCell(0);
        cell2.setCellValue("TITLE FARE SHEET :");
        cell2 = row2.createCell(1);
        cell2.setCellValue(workPackage.getName());
        
        if(workPackage.getTargetDistribution().contentEquals("ATPCO")) {
        	 XSSFRow row3 = spreadsheet.createRow(3);
             XSSFCell cell3;
             cell3 = row3.createCell(0);
             cell3.setCellValue("DESCRIPTION OF GA FARES :");
             cell3 = row3.createCell(1);
             cell3.setCellValue(workPackage.getFareSheet().get(idx).getSpecifiedFaresName());
        }else if(workPackage.getTargetDistribution().contentEquals("MARKET")) {
        	 XSSFRow row3 = spreadsheet.createRow(3);
             XSSFCell cell3;
             cell3 = row3.createCell(0);
             cell3.setCellValue("DESCRIPTION OF GA FARES :");
             cell3 = row3.createCell(1);
             cell3.setCellValue(workPackage.getFareSheet().get(idx).getMarketFaresName());
        }       
        
        XSSFRow row4 = spreadsheet.createRow(4);
        XSSFCell cell4;
        cell4 = row4.createCell(0);
        cell4.setCellValue("NOTES RATESHEET : ");
        cell4 = row4.createCell(1);
        cell4.setCellValue(workPackage.getRatesheetComment());
        
        XSSFRow row5 = spreadsheet.createRow(5);
        XSSFCell cell5;
        for(int l=0; l<header.length ;l++) {
        	cell5 = row5.createCell(l);
			cell5.setCellValue(header[l]);
		}
        
        if(workPackage.getTargetDistribution().contentEquals("ATPCO")) {
        	for(int l=0; l<workPackage.getFareSheet().get(idx).getFares().size();l++) {
            	XSSFRow rows = spreadsheet.createRow(l+6);
        		for (int i=0;i<header.length;i++){
        			cell = rows.createCell(i);
        			
        			if(header[i].contentEquals("Status")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getStatus());
        			} else if(header[i].contentEquals("Carrier")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getCarrier());
        			} else if(header[i].contentEquals("Action")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getAction());
        			} else if(header[i].contentEquals("Tar No")) {
        				try {
        					cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}    				
        			} else if(header[i].contentEquals("Tar Cd")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Global")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Origin")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getOrigin());
        			} else if(header[i].contentEquals("Destination")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getDestination());
        			} else if(header[i].contentEquals("Fare Class")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getFareBasis());
        			} else if(header[i].contentEquals("Booking Class")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getBookingClass());
        			} else if(header[i].contentEquals("Cabin")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getCabin());
        			} else if(header[i].contentEquals("OW/RT")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
        			} else if(header[i].contentEquals("Footnote")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getFootnote1());
        			} else if(header[i].contentEquals("Routing No")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getRtgno());
        			} else if(header[i].contentEquals("Rule No")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getRuleno());
        			} else if(header[i].contentEquals("Currency")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getCurrency());
        			} else if(header[i].contentEquals("Base Amt")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getAmount());
        			} else if(header[i].contentEquals("Amt Different")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        			} else if(header[i].contentEquals("% Amt Different")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        			} else if(header[i].contentEquals("YQYR")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getYqyr());
        			} else if(header[i].contentEquals("Cat 12")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getCat12());
        			} else if(header[i].contentEquals("Taxes")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTotalTax());
        			} else if(header[i].contentEquals("TFC")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTfc());
        			} else if(header[i].contentEquals("Target AIF")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getAif());
        			} else if(header[i].contentEquals("Itinerary")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getItinerary());
        			} else if(header[i].contentEquals("Override Indicator")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getOverrideIndicator());
        			} else if(header[i].contentEquals("Travel Start")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelStart().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Travel End")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Sales Start")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleStart().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Sales End")) {
        				try {
            				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("EffDt")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getEffDt());
        			} else if(header[i].contentEquals("Comment")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getComment());
        			} else if(header[i].contentEquals("Travel Complete")) {
        				try {
        					cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());
    					} catch (Exception e) {
    						cell.setCellValue("-");
    					}
        				
        			} else if(header[i].contentEquals("Travel Complete Indicator")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
        			} else if(header[i].contentEquals("RateSheet Comment")) {
        				cell.setCellValue(workPackage.getFareSheet().get(idx).getFares().get(l).getRatesheetComment());
        			} 
        		}
            }
            
            
            XSSFRow rowRuleText = spreadsheet.createRow(workPackage.getFareSheet().get(idx).getFares().size()+7);
            XSSFCell cellRuleText;
            cellRuleText = rowRuleText.createCell(0);
            cellRuleText.setCellValue("RULE TEXT : ");
            cellRuleText = rowRuleText.createCell(1);
            cellRuleText.setCellValue(ruleText);
        }else if(workPackage.getTargetDistribution().contentEquals("MARKET")) {
        	for(int l=0; l<workPackage.getMarketFareSheet().get(idx).getFares().size();l++) {
            	XSSFRow rows = spreadsheet.createRow(l+6);
        		for (int i=0;i<header.length;i++){
        			cell = rows.createCell(i);
        			
        			if(header[i].contentEquals("Status")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getStatus());
        			} else if(header[i].contentEquals("Carrier")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCarrier());
        			} else if(header[i].contentEquals("Action")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAction());
        			} else if(header[i].contentEquals("Tar No")) {
        				try {
        					cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}    				
        			} else if(header[i].contentEquals("Tar Cd")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Global")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Origin")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOrigin());
        			} else if(header[i].contentEquals("Destination")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getDestination());
        			} else if(header[i].contentEquals("Fare Class")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFareBasis());
        			} else if(header[i].contentEquals("Booking Class")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBookingClass());
        			} else if(header[i].contentEquals("Cabin")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCabin());
        			} else if(header[i].contentEquals("OW/RT")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
        			} else if(header[i].contentEquals("Footnote")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFootnote1());
        			} else if(header[i].contentEquals("Routing No")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRtgno());
        			} else if(header[i].contentEquals("Rule No")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRuleno());
        			} else if(header[i].contentEquals("Currency")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCurrency());
        			} else if(header[i].contentEquals("Base Amt")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAmount());
        			} else if(header[i].contentEquals("Amt Different")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        			} else if(header[i].contentEquals("% Amt Different")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        			} else if(header[i].contentEquals("YQYR")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getYqyr());
        			} else if(header[i].contentEquals("Cat 12")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCat12());
        			} else if(header[i].contentEquals("Taxes")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTotalTax());
        			} else if(header[i].contentEquals("TFC")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTfc());
        			} else if(header[i].contentEquals("Target AIF")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAif());
        			} else if(header[i].contentEquals("Itinerary")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getItinerary());
        			} else if(header[i].contentEquals("Override Indicator")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOverrideIndicator());
        			} else if(header[i].contentEquals("Travel Start")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelStart().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Travel End")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Sales Start")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleStart().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("Sales End")) {
        				try {
            				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());						
    					} catch (Exception e) {
    						// TODO: handle exception
    						cell.setCellValue("-");
    					}
        			} else if(header[i].contentEquals("EffDt")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getEffDt());
        			} else if(header[i].contentEquals("Comment")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getComment());
        			} else if(header[i].contentEquals("Travel Complete")) {
        				try {
        					cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());
    					} catch (Exception e) {
    						cell.setCellValue("-");
    					}
        				
        			} else if(header[i].contentEquals("Travel Complete Indicator")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
        			} else if(header[i].contentEquals("RateSheet Comment")) {
        				cell.setCellValue(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRatesheetComment());
        			} 
        		}
            }
            
            
            XSSFRow rowRuleText = spreadsheet.createRow(workPackage.getMarketFareSheet().get(idx).getFares().size()+7);
            XSSFCell cellRuleText;
            cellRuleText = rowRuleText.createCell(0);
            cellRuleText.setCellValue("RULE TEXT : ");
            cellRuleText = rowRuleText.createCell(1);
            cellRuleText.setCellValue(ruleText);
        }
        
        
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
        
    /**
     * POST  /work-packages/export-fares-discount : Export work package fares-discount
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet-discount")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageDiscount(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFares-discount : {}{}", wprs.getWp(), wprs.getRuleText());
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        Integer count = header.length;
        PdfPTable table;
        Document document;
        if(count < 25) {
        	document = new Document(PageSize.A4,30,30,60,0);
    	}else {
    		document = new Document(PageSize.A4.rotate());
    		document.setMargins(60, 30, 30, 30);
    	}
    	
    	PdfWriter.getInstance(document, output);
    	InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png");
    	byte[] imageByte = IOUtils.toByteArray(inputStream);    	
    	document.open();
    	Font font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
    	Image image = Image.getInstance(imageByte); 
    	image.scaleToFit(PageSize.A4.getWidth()/4, PageSize.A4.getHeight()/4);
    	image.setAlignment(Image.MIDDLE);
    	String[] content = {"WORK ID : ","TITLE FARE SHEET : ", "FARE NAME : ","NOTES RATESHEET : ", "FARES : ", "RULE TEXT : "};
    	document.add(image); 
    	document.add(new Chunk(" "));

        Paragraph p1 = new Paragraph();
        Paragraph p2 = new Paragraph();
        Paragraph p3 = new Paragraph();
        Paragraph p4 = new Paragraph();
        Paragraph p5 = new Paragraph();
        Paragraph p6 = new Paragraph();

        p1.setFont(font);               
        p1.add(content[0]+" "+ workPackage.getWpid());        
    	document.add(p1);
    	document.add(new Chunk(" "));
        p2.setFont(font);               
        p2.add(content[1]+" "+workPackage.getName());
        document.add(p2);
    	document.add(new Chunk(" "));

        p3.setFont(font);               
        p3.add(content[2]+" "+workPackage.getDiscountFareSheet().get(idx).getDiscountFaresName());
    	document.add(p3);
    	document.add(new Chunk(" "));

        p4.setFont(font);               
        p4.add(content[3]+" "+workPackage.getRatesheetComment());
    	document.add(p4);
    	document.add(new Chunk(" "));

        p5.setFont(font);               
        p5.add(content[4]);
    	document.add(p5);
    	document.add(new Chunk(" "));

    	table = new PdfPTable(count);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		for(int l=0; l<header.length ;l++) {
			table.addCell(header[l]);
		}
		table.setHeaderRows(1);
        
		
		PdfPCell[] cells = table.getRow(0).getCells(); 
    	for (int j=0;j<cells.length;j++){
    			cells[j].setBackgroundColor(BaseColor.GRAY);
    	}
        	log.debug("cek discount : "+workPackage.getDiscountFareSheet().size());
        	for(int l=0; l<workPackage.getDiscountFareSheet().get(idx).getFares().size();l++) {
        		for (int i=0;i<header.length;i++){
            		if(header[i].contentEquals("Status")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getStatus());
            		}else if(header[i].contentEquals("FBR Tariff Code")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTarcd());
            		}else if(header[i].contentEquals("Loc 1 Type")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1Type());       			
            		}else if(header[i].contentEquals("Loc 1")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1());
            		}else if(header[i].contentEquals("Loc 2 Type")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2Type());
            		}else if(header[i].contentEquals("Loc 2")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2());
            		}else if(header[i].contentEquals("Base FareCls")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseFareBasis());
            		}else if(header[i].contentEquals("Base Rule No")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("Base Tariff Code")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseTarcd());
            		}else if(header[i].contentEquals("Calc Type")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getCalcType());
            		}else if(header[i].contentEquals("% of Base Fare")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPercentBaseFare());
            		}else if(header[i].contentEquals("Currency")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRuleno());
            		}else if(header[i].contentEquals("Specified Amount")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getDiscountSpecifiedAmount());
            		}else if(header[i].contentEquals("PAX Type")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPassengerType());
            		}else if(header[i].contentEquals("Fare Type")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getFareType());
            		}else if(header[i].contentEquals("Tkt Code")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketCode());
            		}else if(header[i].contentEquals("Tkt Des")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketDesignator());
            		}else if(header[i].contentEquals("Base Fare OW/RT")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
            		}else if(header[i].contentEquals("Global")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getGlobal());
            		}else if(header[i].contentEquals("Rtg No")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgno());
            		}else if(header[i].contentEquals("Rtg No Tarno")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgnoTarno());
            		}else if(header[i].contentEquals("New FareCls")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewFareBasis());
            		}else if(header[i].contentEquals("New OW/RT")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewTypeOfJourney());
            		}else if(header[i].contentEquals("New BkgCd")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewBookingCode());
            		}else if(header[i].contentEquals("Travel Start")) {
            			try {
                			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel End")) {
            			try {
                			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales Start")) {
            			try {
                			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Sales End")) {
            			try {
                			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Comment")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getComment());
            		}else if(header[i].contentEquals("Travel Complete")) {
            			try {
                			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.addCell("-");
						}
            		}else if(header[i].contentEquals("Travel Complete Indicator")) {
            			table.addCell(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
            		}
            		else {
            			table.addCell("-");
            		}
            	}        		
        	}
        	
          document.add(table);  
        
	
        p6.setFont(font);               
        p6.add(content[5]+" "+ruleText);
    	document.add(p6);
    	document.add(new Chunk(" "));

    	document.close();
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-excel : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet-excel-discount")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageExcelDiscount(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFaresExcel : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Rate Sheet");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;
        
        cell = row.createCell(0);
        cell.setCellValue("WORK ID :");
        cell = row.createCell(1);
        cell.setCellValue(workPackage.getWpid());
        
        XSSFRow row2 = spreadsheet.createRow(2);
        XSSFCell cell2;
        cell2 = row2.createCell(0);
        cell2.setCellValue("TITLE FARE SHEET :");
        cell2 = row2.createCell(1);
        cell2.setCellValue(workPackage.getName());
        
		 XSSFRow row3 = spreadsheet.createRow(3);
	     XSSFCell cell3;
	     cell3 = row3.createCell(0);
	     cell3.setCellValue("FARE NAME :");
	     cell3 = row3.createCell(1);
	     cell3.setCellValue(workPackage.getDiscountFareSheet().get(idx).getDiscountFaresName());
          
        
        XSSFRow row4 = spreadsheet.createRow(4);
        XSSFCell cell4;
        cell4 = row4.createCell(0);
        cell4.setCellValue("NOTES RATESHEET : ");
        cell4 = row4.createCell(1);
        cell4.setCellValue(workPackage.getRatesheetComment());
        
        XSSFRow row5 = spreadsheet.createRow(5);
        XSSFCell cell5;
        for(int l=0; l<header.length ;l++) {
        	cell5 = row5.createCell(l);
			cell5.setCellValue(header[l]);
		}
        
    
    	for(int l=0; l<workPackage.getDiscountFareSheet().get(idx).getFares().size();l++) {
        	XSSFRow rows = spreadsheet.createRow(l+6);
    		for (int i=0;i<header.length;i++){
    			cell = rows.createCell(i);
    	
    			if(header[i].contentEquals("Status")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getStatus());
        		}else if(header[i].contentEquals("FBR Tariff Code")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTarcd());
        		}else if(header[i].contentEquals("Loc 1 Type")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1Type());       			
        		}else if(header[i].contentEquals("Loc 1")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1());
        		}else if(header[i].contentEquals("Loc 2 Type")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2Type());
        		}else if(header[i].contentEquals("Loc 2")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2());
        		}else if(header[i].contentEquals("Base FareCls")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseFareBasis());
        		}else if(header[i].contentEquals("Base Rule No")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        		}else if(header[i].contentEquals("Base Tariff Code")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseTarcd());
        		}else if(header[i].contentEquals("Calc Type")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getCalcType());
        		}else if(header[i].contentEquals("% of Base Fare")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPercentBaseFare());
        		}else if(header[i].contentEquals("Currency")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRuleno());
        		}else if(header[i].contentEquals("Specified Amount")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getDiscountSpecifiedAmount());
        		}else if(header[i].contentEquals("PAX Type")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPassengerType());
        		}else if(header[i].contentEquals("Fare Type")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getFareType());
        		}else if(header[i].contentEquals("Tkt Code")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketCode());
        		}else if(header[i].contentEquals("Tkt Des")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketDesignator());
        		}else if(header[i].contentEquals("Base Fare OW/RT")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
        		}else if(header[i].contentEquals("Global")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getGlobal());
        		}else if(header[i].contentEquals("Rtg No")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgno());
        		}else if(header[i].contentEquals("Rtg No Tarno")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgnoTarno());
        		}else if(header[i].contentEquals("New FareCls")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewFareBasis());
        		}else if(header[i].contentEquals("New OW/RT")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewTypeOfJourney());
        		}else if(header[i].contentEquals("New BkgCd")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewBookingCode());
        		}else if(header[i].contentEquals("Travel Start")) {
        			try {
            			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						cell.setCellValue("-");
					}
        		}else if(header[i].contentEquals("Travel End")) {
        			try {
            			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
						
					} catch (Exception e) {
						// TODO: handle exception
						cell.setCellValue("-");
					}
        		}else if(header[i].contentEquals("Sales Start")) {
        			try {
            			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
						
					} catch (Exception e) {
						// TODO: handle exception
						cell.setCellValue("-");
					}
        		}else if(header[i].contentEquals("Sales End")) {
        			try {
            			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						cell.setCellValue("-");
					}
        		}else if(header[i].contentEquals("Comment")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getComment());
        		}else if(header[i].contentEquals("Travel Complete")) {
        			try {
            			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						cell.setCellValue("-");
					}
        		}else if(header[i].contentEquals("Travel Complete Indicator")) {
        			cell.setCellValue(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
        		}
    			
    			
    			
    		}
        }
        
        
        XSSFRow rowRuleText = spreadsheet.createRow(workPackage.getDiscountFareSheet().get(idx).getFares().size()+7);
        XSSFCell cellRuleText;
        cellRuleText = rowRuleText.createCell(0);
        cellRuleText.setCellValue("RULE TEXT : ");
        cellRuleText = rowRuleText.createCell(1);
        cellRuleText.setCellValue(ruleText);
        
        
        
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
        
    /**
     * POST  /work-packages/export-fares-discount : Export work package fares-discount
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet-waiver")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageWaiver(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFares-discount : {}{}", wprs.getWp(), wprs.getRuleText());
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        Integer count = header.length;
        PdfPTable table;
        Document document;
        if(count < 25) {
        	document = new Document(PageSize.A4,30,30,60,0);
    	}else {
    		document = new Document(PageSize.A4.rotate());
    		document.setMargins(60, 30, 30, 30);
    	}
             
    	PdfWriter.getInstance(document, output);    	
    	InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png");
    	byte[] imageByte = IOUtils.toByteArray(inputStream);    	
    	document.open();
    	Font font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
    	Image image = Image.getInstance(imageByte); 
    	image.scaleToFit(PageSize.A4.getWidth()/4, PageSize.A4.getHeight()/4);
    	image.setAlignment(Image.MIDDLE);
    	String[] content = {"WORK ID : ","TITLE FARE SHEET : ", "FARE NAME : ","NOTES RATESHEET : ", "FARES : ", "RULE TEXT : "};
    	document.add(image); 
    	document.add(new Chunk(" "));

        Paragraph p1 = new Paragraph();
        Paragraph p2 = new Paragraph();
        Paragraph p3 = new Paragraph();
        Paragraph p4 = new Paragraph();
        Paragraph p5 = new Paragraph();
        Paragraph p6 = new Paragraph();

        p1.setFont(font);               
        p1.add(content[0]+" "+ workPackage.getWpid());        
    	document.add(p1);
    	document.add(new Chunk(" "));
        p2.setFont(font);               
        p2.add(content[1]+" "+workPackage.getName());
        document.add(p2);
    	document.add(new Chunk(" "));

        p3.setFont(font);               
        p3.add(content[2]+" "+workPackage.getWaiverFareSheet().get(idx).getWaiverFaresName());
    	document.add(p3);
    	document.add(new Chunk(" "));

        p4.setFont(font);               
        p4.add(content[3]+" "+workPackage.getRatesheetComment());
    	document.add(p4);
    	document.add(new Chunk(" "));

        p5.setFont(font);               
        p5.add(content[4]);
    	document.add(p5);
    	document.add(new Chunk(" "));

    	table = new PdfPTable(count);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		for(int l=0; l<header.length ;l++) {
			table.addCell(header[l]);
		}
		table.setHeaderRows(1);
        
		
		PdfPCell[] cells = table.getRow(0).getCells(); 
    	for (int j=0;j<cells.length;j++){
    			cells[j].setBackgroundColor(BaseColor.GRAY);
    	}
        	log.debug("cek discount : "+workPackage.getDiscountFareSheet().size());
        	for(int l=0; l<workPackage.getWaiverFareSheet().get(idx).getFares().size();l++) {
        		for (int i=0;i<header.length;i++){
            		if(header[i].contentEquals("Type")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverType());
            		}else if(header[i].contentEquals("Full/Partial")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFullPartial());
            		}else if(header[i].contentEquals("PNR")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPnr());       			
            		}else if(header[i].contentEquals("Tkt From")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktFrom());
            		}else if(header[i].contentEquals("Tkt To")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktTo());
            		}else if(header[i].contentEquals("Ori")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOri());
            		}else if(header[i].contentEquals("Dest")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverDest());
            		}else if(header[i].contentEquals("Original Itinerary")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalItinerary());
            		}else if(header[i].contentEquals("New Itinerary")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewItinerary());
            		}else if(header[i].contentEquals("Original Basic Fare")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalBasicFare());
            		}else if(header[i].contentEquals("New Basic Fare")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewBasicFare());
            		}else if(header[i].contentEquals("Approved Fares")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedFare());
            		}else if(header[i].contentEquals("Fare Lost")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFareLost());
            		}else if(header[i].contentEquals("Calculated PN")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCalculatedPn());
            		}else if(header[i].contentEquals("Original PN")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalPn());
            		}else if(header[i].contentEquals("Approved PN")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedPn());
            		}else if(header[i].contentEquals("Penalty Lost %")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostPercent());
            		}else if(header[i].contentEquals("Penalty Lost Amount")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostAmount());
            		}else if(header[i].contentEquals("Currency")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCurrency());
            		}else if(header[i].contentEquals("Total Pax")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalPax());
            		}else if(header[i].contentEquals("Total Lost")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalLost());
            		}else if(header[i].contentEquals("Approver")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprover());
            		}else if(header[i].contentEquals("Remark")) {
            			table.addCell(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverRemark());
            		}
            		else {
            			table.addCell("-");
            		}
            	}        		
        	}
        	
          document.add(table);  
        
	
        p6.setFont(font);               
        p6.add(content[5]+" "+ruleText);
    	document.add(p6);
    	document.add(new Chunk(" "));

    	document.close();
        
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-excel : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     */
    @PostMapping("/work-packages/export-ratesheet-excel-waiver")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageExcelWaiver(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException {
    	log.debug("REST request to save exportFaresExcel : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet spreadsheet = workbook.createSheet("Rate Sheet");
        
        XSSFRow row = spreadsheet.createRow(1);
        XSSFCell cell;
        
        cell = row.createCell(0);
        cell.setCellValue("WORK ID :");
        cell = row.createCell(1);
        cell.setCellValue(workPackage.getWpid());
        
        XSSFRow row2 = spreadsheet.createRow(2);
        XSSFCell cell2;
        cell2 = row2.createCell(0);
        cell2.setCellValue("TITLE FARE SHEET :");
        cell2 = row2.createCell(1);
        cell2.setCellValue(workPackage.getName());
        
		 XSSFRow row3 = spreadsheet.createRow(3);
	     XSSFCell cell3;
	     cell3 = row3.createCell(0);
	     cell3.setCellValue("FARE NAME :");
	     cell3 = row3.createCell(1);
	     cell3.setCellValue(workPackage.getWaiverFareSheet().get(idx).getDiscountFaresName());
          
        
        XSSFRow row4 = spreadsheet.createRow(4);
        XSSFCell cell4;
        cell4 = row4.createCell(0);
        cell4.setCellValue("NOTES RATESHEET : ");
        cell4 = row4.createCell(1);
        cell4.setCellValue(workPackage.getRatesheetComment());
        
        XSSFRow row5 = spreadsheet.createRow(5);
        XSSFCell cell5;
        for(int l=0; l<header.length ;l++) {
        	cell5 = row5.createCell(l);
			cell5.setCellValue(header[l]);
		}
        
    
    	for(int l=0; l<workPackage.getWaiverFareSheet().get(idx).getFares().size();l++) {
        	XSSFRow rows = spreadsheet.createRow(l+6);
    		for (int i=0;i<header.length;i++){
    			cell = rows.createCell(i);
    	
    			if(header[i].contentEquals("Type")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverType());
        		}else if(header[i].contentEquals("Full/Partial")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFullPartial());
        		}else if(header[i].contentEquals("PNR")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPnr());       			
        		}else if(header[i].contentEquals("Tkt From")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktFrom());
        		}else if(header[i].contentEquals("Tkt To")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktTo());
        		}else if(header[i].contentEquals("Ori")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOri());
        		}else if(header[i].contentEquals("Dest")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverDest());
        		}else if(header[i].contentEquals("Original Itinerary")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalItinerary());
        		}else if(header[i].contentEquals("New Itinerary")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewItinerary());
        		}else if(header[i].contentEquals("Original Basic Fare")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalBasicFare());
        		}else if(header[i].contentEquals("New Basic Fare")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewBasicFare());
        		}else if(header[i].contentEquals("Approved Fares")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedFare());
        		}else if(header[i].contentEquals("Fare Lost")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFareLost());
        		}else if(header[i].contentEquals("Calculated PN")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCalculatedPn());
        		}else if(header[i].contentEquals("Original PN")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalPn());
        		}else if(header[i].contentEquals("Approved PN")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedPn());
        		}else if(header[i].contentEquals("Penalty Lost %")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostPercent());
        		}else if(header[i].contentEquals("Penalty Lost Amount")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostAmount());
        		}else if(header[i].contentEquals("Currency")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCurrency());
        		}else if(header[i].contentEquals("Total Pax")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalPax());
        		}else if(header[i].contentEquals("Total Lost")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalLost());
        		}else if(header[i].contentEquals("Approver")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprover());
        		}else if(header[i].contentEquals("Remark")) {
        			cell.setCellValue(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverRemark());
        		}  			
    			    			
    		}
        }
        
        
        XSSFRow rowRuleText = spreadsheet.createRow(workPackage.getWaiverFareSheet().get(idx).getFares().size()+7);
        XSSFCell cellRuleText;
        cellRuleText = rowRuleText.createCell(0);
        cellRuleText.setCellValue("RULE TEXT : ");
        cellRuleText = rowRuleText.createCell(1);
        cellRuleText.setCellValue(ruleText);
        
        
        
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
			workbook.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Attachment att = new Attachment();
        att.setFile(output.toByteArray());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
            .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-word : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException 
     */
    @PostMapping("/work-packages/export-ratesheet-word")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageWord(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
    	log.debug("REST request to save exportFaresWord : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        XWPFDocument document = new XWPFDocument();  
        
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png"); 	
    	
        XWPFParagraph img_header = document.createParagraph();
        img_header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphFiveRunOne = img_header.createRun();
        paragraphFiveRunOne.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, "logo_ga.png", Units.toEMU(50), Units.toEMU(50));
        
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun title = paragraph.createRun();
        title.setText("Work ID : "+workPackage.getWpid());
        title.addBreak();
        
        XWPFRun name = paragraph.createRun();
        name.setText("Title Fare Sheet : "+workPackage.getName());
        name.addBreak();
        
        XWPFRun specname = paragraph.createRun();
        specname.setText("Specified Name : "+workPackage.getFareSheet().get(idx).getSpecifiedFaresName());
        specname.addBreak();
        
        XWPFRun comment = paragraph.createRun();
        comment.setText("Ratesheet Comment : "+workPackage.getRatesheetComment());
        comment.addBreak();
        
        XWPFTable table = document.createTable();
        
        for(int l=0; l<header.length ;l++) {
        	XWPFTableRow header_title = table.getRow(0);
        	if(l==0) {
        		header_title.getCell(0).setText(header[0]);
        	}else {
        		header_title.addNewTableCell().setText(header[l]);
        	}
        	table.getRow(0).getCell(l).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
		}
        
        if(workPackage.getTargetDistribution().contentEquals("ATPCO")) {
        	for(int l=0; l<workPackage.getFareSheet().get(idx).getFares().size();l++) {  
    			XWPFTableRow row = table.createRow(); 
        		for (int i=0;i<header.length;i++){
        			if(header[i].contentEquals("Status")) {
        				table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getStatus());
            		}else if(header[i].contentEquals("Carrier")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getCarrier());
            		}else if(header[i].contentEquals("Action")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getAction());
            		}else if(header[i].contentEquals("Tar No")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}            			
            		}else if(header[i].contentEquals("Tar Cd")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}            			
            		}else if(header[i].contentEquals("Global")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}            			
            		}else if(header[i].contentEquals("Origin")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getOrigin());
            		}else if(header[i].contentEquals("Destination")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getDestination());
            		}else if(header[i].contentEquals("Fare Class")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getFareBasis());
            		}else if(header[i].contentEquals("Booking Class")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getBookingClass());
            		}else if(header[i].contentEquals("Cabin")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getCabin());
            		}else if(header[i].contentEquals("OW/RT")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
            		}else if(header[i].contentEquals("Footnote")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getFootnote1());
            		}else if(header[i].contentEquals("Routing No")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getRtgno());
            		}else if(header[i].contentEquals("Rule No")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getRuleno());
            		}else if(header[i].contentEquals("Currency")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getCurrency());
            		}else if(header[i].contentEquals("Base Amt")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getAmount());
            		}else if(header[i].contentEquals("Amt Different")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("% Amt Different")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("YQYR")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getYqyr());
            		}else if(header[i].contentEquals("Cat 12")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getCat12());
            		}else if(header[i].contentEquals("TFC")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTfc());
            		}else if(header[i].contentEquals("Target AIF")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getAif());
            		}else if(header[i].contentEquals("Itinerary")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getItinerary());
            		}else if(header[i].contentEquals("Override Indicator")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getOverrideIndicator().toString());							
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("Travel Start")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("Travel End")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
    						
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("Sales Start")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
    						
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("Sales End")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("EffDt")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getEffDt());
            		}else if(header[i].contentEquals("Comment")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getComment());
            		}else if(header[i].contentEquals("Travel Complete")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
    					} catch (Exception e) {
    						// TODO: handle exception
    						table.getRow(l+1).getCell(i).setText("-");
    					}
            		}else if(header[i].contentEquals("Travel Complete Indicator")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
            		}else if(header[i].contentEquals("RateSheet Comment")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getFareSheet().get(idx).getFares().get(l).getRatesheetComment());
            		}
    				table.getRow(l+1).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
        		}
            }
        }else if(workPackage.getTargetDistribution().contentEquals("MARKET")){
        	XWPFTableRow row = table.createRow(); 
        	for(int l=0; l<workPackage.getMarketFareSheet().get(idx).getFares().size();l++) {
        		for (int i=0;i<header.length;i++){
            		if(header[i].contentEquals("Status")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getStatus());
            		}else if(header[i].contentEquals("Carrier")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCarrier());
            		}else if(header[i].contentEquals("Action")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAction());
            		}else if(header[i].contentEquals("Tar No")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarNo());
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}            			
            		}else if(header[i].contentEquals("Tar Cd")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getTarCd());
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}            			
            		}else if(header[i].contentEquals("Global")) {
            			try {
            				table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTariffNumber().getGlobal());
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}            			
            		}else if(header[i].contentEquals("Origin")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOrigin());
            		}else if(header[i].contentEquals("Destination")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getDestination());
            		}else if(header[i].contentEquals("Fare Class")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFareBasis());
            		}else if(header[i].contentEquals("Booking Class")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBookingClass());
            		}else if(header[i].contentEquals("Cabin")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCabin());
            		}else if(header[i].contentEquals("OW/RT")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
            		}else if(header[i].contentEquals("Footnote")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getFootnote1());
            		}else if(header[i].contentEquals("Routing No")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRtgno());
            		}else if(header[i].contentEquals("Rule No")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRuleno());
            		}else if(header[i].contentEquals("Currency")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCurrency());
            		}else if(header[i].contentEquals("Base Amt")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAmount());
            		}else if(header[i].contentEquals("Amt Different")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("% Amt Different")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
            		}else if(header[i].contentEquals("YQYR")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getYqyr());
            		}else if(header[i].contentEquals("Cat 12")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getCat12());
            		}else if(header[i].contentEquals("TFC")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTfc());
            		}else if(header[i].contentEquals("Target AIF")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getAif());
            		}else if(header[i].contentEquals("Itinerary")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getItinerary());
            		}else if(header[i].contentEquals("Override Indicator")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getOverrideIndicator().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("Travel Start")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("Travel End")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("Sales Start")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("Sales End")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("EffDt")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getEffDt());
            		}else if(header[i].contentEquals("Comment")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getComment());
            		}else if(header[i].contentEquals("Travel Complete")) {
            			try {
                			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
						} catch (Exception e) {
							// TODO: handle exception
							table.getRow(l+1).getCell(i).setText("-");
						}
            		}else if(header[i].contentEquals("Travel Complete Indicator")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
            		}else if(header[i].contentEquals("RateSheet Comment")) {
            			table.getRow(l+1).getCell(i).setText(workPackage.getMarketFareSheet().get(idx).getFares().get(l).getRatesheetComment());
            		}
            		else {
            			table.getRow(l+1).getCell(i).setText("-");
            		}
            	}        		
        	}
        }
        

   	 XWPFRun ruletext = paragraph.createRun();
   	 ruletext.setText("Rule Text : "+ruleText);
   	 ruletext.addBreak();
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
        	document.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	document.close();
    	
    	Attachment att = new Attachment();
    	att.setFile(output.toByteArray());
    	return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
                .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-discount-word : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException 
     */
    @PostMapping("/work-packages/export-ratesheet-word-discount")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageWordDiscount(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
    	log.debug("REST request to save exportFaresWord : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        XWPFDocument document = new XWPFDocument();   
        
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png"); 	
    	
        XWPFParagraph img_header = document.createParagraph();
        img_header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphFiveRunOne = img_header.createRun();
        paragraphFiveRunOne.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, "logo_ga.png", Units.toEMU(50), Units.toEMU(50));    
        
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun title = paragraph.createRun();
        title.setText("Work ID : "+workPackage.getWpid());
        title.addBreak();
        
        XWPFRun name = paragraph.createRun();
        name.setText("Title Fare Sheet : "+workPackage.getName());
        name.addBreak();
        
        XWPFRun specname = paragraph.createRun();
        specname.setText("Specified Name : "+workPackage.getDiscountFareSheet().get(idx).getDiscountFaresName());
        specname.addBreak();
        
        XWPFRun comment = paragraph.createRun();
        comment.setText("Ratesheet Comment : "+workPackage.getRatesheetComment());
        comment.addBreak();
        
        XWPFTable table = document.createTable();
        
        for(int l=0; l<header.length ;l++) {
        	XWPFTableRow header_title = table.getRow(0);
        	if(l==0) {
        		header_title.getCell(0).setText(header[0]);
        	}else {
        		header_title.addNewTableCell().setText(header[l]);
        	}
        	table.getRow(0).getCell(l).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
		}
        

    	for(int l=0; l<workPackage.getDiscountFareSheet().get(idx).getFares().size();l++) {  
			XWPFTableRow row = table.createRow(); 
    		for (int i=0;i<header.length;i++){
    			if(header[i].contentEquals("Status")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getStatus());
        		}else if(header[i].contentEquals("FBR Tariff Code")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTarcd());
        		}else if(header[i].contentEquals("Loc 1 Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1Type());       			
        		}else if(header[i].contentEquals("Loc 1")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc1());
        		}else if(header[i].contentEquals("Loc 2 Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2Type());
        		}else if(header[i].contentEquals("Loc 2")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getLoc2());
        		}else if(header[i].contentEquals("Base FareCls")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseFareBasis());
        		}else if(header[i].contentEquals("Base Rule No")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseRuleNo());
        		}else if(header[i].contentEquals("Base Tariff Code")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getBaseTarcd());
        		}else if(header[i].contentEquals("Calc Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getCalcType());
        		}else if(header[i].contentEquals("% of Base Fare")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPercentBaseFare());
        		}else if(header[i].contentEquals("Currency")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRuleno());
        		}else if(header[i].contentEquals("Specified Amount")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getDiscountSpecifiedAmount());
        		}else if(header[i].contentEquals("PAX Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getPassengerType());
        		}else if(header[i].contentEquals("Fare Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getFareType());
        		}else if(header[i].contentEquals("Tkt Code")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketCode());
        		}else if(header[i].contentEquals("Tkt Des")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTicketDesignator());
        		}else if(header[i].contentEquals("Base Fare OW/RT")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTypeOfJourney());
        		}else if(header[i].contentEquals("Global")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getGlobal());
        		}else if(header[i].contentEquals("Rtg No")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgno());
        		}else if(header[i].contentEquals("Rtg No Tarno")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getRtgnoTarno());
        		}else if(header[i].contentEquals("New FareCls")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewFareBasis());
        		}else if(header[i].contentEquals("New OW/RT")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewTypeOfJourney());
        		}else if(header[i].contentEquals("New BkgCd")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getNewBookingCode());
        		}else if(header[i].contentEquals("Travel Start")) {
        			try {
            			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelStart().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						table.getRow(l+1).getCell(i).setText("-");
					}
        		}else if(header[i].contentEquals("Travel End")) {
        			try {
            			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelEnd().toString());
						
					} catch (Exception e) {
						// TODO: handle exception
						table.getRow(l+1).getCell(i).setText("-");
					}
        		}else if(header[i].contentEquals("Sales Start")) {
        			try {
            			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleStart().toString());
						
					} catch (Exception e) {
						// TODO: handle exception
						table.getRow(l+1).getCell(i).setText("-");
					}
        		}else if(header[i].contentEquals("Sales End")) {
        			try {
            			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getSaleEnd().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						table.getRow(l+1).getCell(i).setText("-");
					}
        		}else if(header[i].contentEquals("Comment")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getComment());
        		}else if(header[i].contentEquals("Travel Complete")) {
        			try {
            			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelComplete().toString());							
					} catch (Exception e) {
						// TODO: handle exception
						table.getRow(l+1).getCell(i).setText("-");
					}
        		}else if(header[i].contentEquals("Travel Complete Indicator")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getDiscountFareSheet().get(idx).getFares().get(l).getTravelCompleteIndicator());
        		}
				table.getRow(l+1).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
    		}
        }
        
    	 XWPFRun ruletext = paragraph.createRun();
    	 ruletext.setText("Rule Text : "+ruleText);
    	 ruletext.addBreak();
    	 
    	 
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
        	document.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	document.close();
    	
    	Attachment att = new Attachment();
    	att.setFile(output.toByteArray());
    	return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
                .body(att);
    }
    
    /**
     * POST  /work-packages/export-ratesheet-waiver-word : Export work package fares
     *
     * @param workPackage the workPackage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new workPackage, or with status 400 (Bad Request) if the workPackage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws DocumentException 
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException 
     */
    @PostMapping("/work-packages/export-ratesheet-word-waiver")
    @Timed
    public ResponseEntity<Attachment> exportRateSheetWorkPackageWordWaiver(@RequestBody WorkPackageRateSheet wprs) throws URISyntaxException, MalformedURLException, IOException, DocumentException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
    	log.debug("REST request to save exportFaresWord : {}{}", wprs.getWp(), wprs.getRuleText());
    	
    	WorkPackage workPackage = wprs.getWp();
        String ruleText = wprs.getRuleText();
        int idx = Integer.parseInt(wprs.getIndex());
        String[] header = wprs.getHeader();
        
        XWPFDocument document = new XWPFDocument();       
                       
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/logo_ga.png"); 	
    	
        XWPFParagraph img_header = document.createParagraph();
        img_header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphFiveRunOne = img_header.createRun();
        paragraphFiveRunOne.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, "logo_ga.png", Units.toEMU(50), Units.toEMU(50));
        
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun title = paragraph.createRun();
        title.setText("Work ID : "+workPackage.getWpid());
        title.addBreak();
        
        XWPFRun name = paragraph.createRun();
        name.setText("Title Fare Sheet : "+workPackage.getName());
        name.addBreak();
        
        XWPFRun specname = paragraph.createRun();
        specname.setText("Specified Name : "+workPackage.getWaiverFareSheet().get(idx).getDiscountFaresName());
        specname.addBreak();
        
        XWPFRun comment = paragraph.createRun();
        comment.setText("Ratesheet Comment : "+workPackage.getRatesheetComment());
        comment.addBreak();
        
        XWPFTable table = document.createTable();
        
        for(int l=0; l<header.length ;l++) {
        	XWPFTableRow header_title = table.getRow(0);
        	if(l==0) {
        		header_title.getCell(0).setText(header[0]);
        	}else {
        		header_title.addNewTableCell().setText(header[l]);
        	}
        	table.getRow(0).getCell(l).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
		}
        

    	for(int l=0; l<workPackage.getWaiverFareSheet().get(idx).getFares().size();l++) {  
			XWPFTableRow row = table.createRow(); 
    		for (int i=0;i<header.length;i++){
    			if(header[i].contentEquals("Type")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverType());
        		}else if(header[i].contentEquals("Full/Partial")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFullPartial());
        		}else if(header[i].contentEquals("PNR")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPnr());       			
        		}else if(header[i].contentEquals("Tkt From")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktFrom());
        		}else if(header[i].contentEquals("Tkt To")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTktTo());
        		}else if(header[i].contentEquals("Ori")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOri());
        		}else if(header[i].contentEquals("Dest")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverDest());
        		}else if(header[i].contentEquals("Original Itinerary")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalItinerary());
        		}else if(header[i].contentEquals("New Itinerary")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewItinerary());
        		}else if(header[i].contentEquals("Original Basic Fare")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalBasicFare());
        		}else if(header[i].contentEquals("New Basic Fare")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverNewBasicFare());
        		}else if(header[i].contentEquals("Approved Fares")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedFare());
        		}else if(header[i].contentEquals("Fare Lost")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverFareLost());
        		}else if(header[i].contentEquals("Calculated PN")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCalculatedPn());
        		}else if(header[i].contentEquals("Original PN")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverOriginalPn());
        		}else if(header[i].contentEquals("Approved PN")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprovedPn());
        		}else if(header[i].contentEquals("Penalty Lost %")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostPercent());
        		}else if(header[i].contentEquals("Penalty Lost Amount")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverPenaltyLostAmount());
        		}else if(header[i].contentEquals("Currency")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverCurrency());
        		}else if(header[i].contentEquals("Total Pax")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalPax());
        		}else if(header[i].contentEquals("Total Lost")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverTotalLost());
        		}else if(header[i].contentEquals("Approver")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverApprover());
        		}else if(header[i].contentEquals("Remark")) {
        			table.getRow(l+1).getCell(i).setText(workPackage.getWaiverFareSheet().get(idx).getFares().get(l).getWaiverRemark());
        		}  	
				table.getRow(l+1).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
    		}
        }
        
    	 XWPFRun ruletext = paragraph.createRun();
    	 ruletext.setText("Rule Text : "+ruleText);
    	 ruletext.addBreak();
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
        	document.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        document.close();
        
    	Attachment att = new Attachment();
    	att.setFile(output.toByteArray());
    	return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ""))
                .body(att);
    }
  
}
