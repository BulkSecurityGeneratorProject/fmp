package com.atibusinessgroup.fmp.domain.atpco;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "atpco_record_3_cat_007")
public class AtpcoRecord3Cat07 {
	@Id
    private String id;

	@Field("rules_type")
    private String rulestype;

	@Field("record_batch")
    private String recordbatch;
    
	@Field("record_sequence")
    private String recordsequence;

	@Field("rec_type")
    private String recType;

	@Field("action")
    private String action;

	@Field("cat_no")
    private String catNno;

	@Field("tbl_no")
    private String tblNo;
	
	@Field("return")
    private String return_;

	@Field("time_of_day")
    private String timeOfDay;

	@Field("max_stay")
    private String maxStay;

	@Field("unit")
    private String unit;
	
	@Field("tkt_iss")
    private String tktIss;

	@Field("from_geo_tbl_no_995")
    private String fromGeoTblNo995;

	@Field("to_geo_tbl_no_995")
    private String toGeoTblNo995;
    
	@Field("waiver")
	private String waiver;

	@Field("date_tbl_no_994")
    private String dateTblNo994;

	@Field("text_tbl_no_996")
    private String textTblNo996;

	@Field("unavail")
    private String unavail;

	@Field("max_stay_date")
    private Object maxStayDate;

	@Field("el")
    private String el;

	@Field("waiver_date")
    private Object waiverDate;

	@Field("waiver_el")
    private String waiverEl;

	@Field("waiver_period")
    private String waiverPperiod;

	@Field("waiver_unit")
    private String waiverUnit;

	@Field("reserved_1")
    private String reserved1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRulestype() {
		return rulestype;
	}

	public void setRulestype(String rulestype) {
		this.rulestype = rulestype;
	}

	public String getRecordbatch() {
		return recordbatch;
	}

	public void setRecordbatch(String recordbatch) {
		this.recordbatch = recordbatch;
	}

	public String getRecordsequence() {
		return recordsequence;
	}

	public void setRecordsequence(String recordsequence) {
		this.recordsequence = recordsequence;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCatNno() {
		return catNno;
	}

	public void setCatNno(String catNno) {
		this.catNno = catNno;
	}

	public String getTblNo() {
		return tblNo;
	}

	public void setTblNo(String tblNo) {
		this.tblNo = tblNo;
	}

	public String getReturn_() {
		return return_;
	}

	public void setReturn_(String return_) {
		this.return_ = return_;
	}

	public String getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(String timeOfDay) {
		this.timeOfDay = timeOfDay;
	}

	public String getMaxStay() {
		return maxStay;
	}

	public void setMaxStay(String maxStay) {
		this.maxStay = maxStay;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getTktIss() {
		return tktIss;
	}

	public void setTktIss(String tktIss) {
		this.tktIss = tktIss;
	}

	public String getFromGeoTblNo995() {
		return fromGeoTblNo995;
	}

	public void setFromGeoTblNo995(String fromGeoTblNo995) {
		this.fromGeoTblNo995 = fromGeoTblNo995;
	}

	public String getToGeoTblNo995() {
		return toGeoTblNo995;
	}

	public void setToGeoTblNo995(String toGeoTblNo995) {
		this.toGeoTblNo995 = toGeoTblNo995;
	}

	public String getWaiver() {
		return waiver;
	}

	public void setWaiver(String waiver) {
		this.waiver = waiver;
	}

	public String getDateTblNo994() {
		return dateTblNo994;
	}

	public void setDateTblNo994(String dateTblNo994) {
		this.dateTblNo994 = dateTblNo994;
	}

	public String getTextTblNo996() {
		return textTblNo996;
	}

	public void setTextTblNo996(String textTblNo996) {
		this.textTblNo996 = textTblNo996;
	}

	public String getUnavail() {
		return unavail;
	}

	public void setUnavail(String unavail) {
		this.unavail = unavail;
	}

	public Object getMaxStayDate() {
		return maxStayDate;
	}

	public void setMaxStayDate(Object maxStayDate) {
		this.maxStayDate = maxStayDate;
	}

	public String getEl() {
		return el;
	}

	public void setEl(String el) {
		this.el = el;
	}

	public Object getWaiverDate() {
		return waiverDate;
	}

	public void setWaiverDate(Object waiverDate) {
		this.waiverDate = waiverDate;
	}

	public String getWaiverEl() {
		return waiverEl;
	}

	public void setWaiverEl(String waiverEl) {
		this.waiverEl = waiverEl;
	}

	public String getWaiverPperiod() {
		return waiverPperiod;
	}

	public void setWaiverPperiod(String waiverPperiod) {
		this.waiverPperiod = waiverPperiod;
	}

	public String getWaiverUnit() {
		return waiverUnit;
	}

	public void setWaiverUnit(String waiverUnit) {
		this.waiverUnit = waiverUnit;
	}

	public String getReserved1() {
		return reserved1;
	}

	public void setReserved1(String reserved1) {
		this.reserved1 = reserved1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((catNno == null) ? 0 : catNno.hashCode());
		result = prime * result + ((dateTblNo994 == null) ? 0 : dateTblNo994.hashCode());
		result = prime * result + ((el == null) ? 0 : el.hashCode());
		result = prime * result + ((fromGeoTblNo995 == null) ? 0 : fromGeoTblNo995.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((maxStay == null) ? 0 : maxStay.hashCode());
		result = prime * result + ((maxStayDate == null) ? 0 : maxStayDate.hashCode());
		result = prime * result + ((recType == null) ? 0 : recType.hashCode());
		result = prime * result + ((recordbatch == null) ? 0 : recordbatch.hashCode());
		result = prime * result + ((recordsequence == null) ? 0 : recordsequence.hashCode());
		result = prime * result + ((reserved1 == null) ? 0 : reserved1.hashCode());
		result = prime * result + ((return_ == null) ? 0 : return_.hashCode());
		result = prime * result + ((rulestype == null) ? 0 : rulestype.hashCode());
		result = prime * result + ((tblNo == null) ? 0 : tblNo.hashCode());
		result = prime * result + ((textTblNo996 == null) ? 0 : textTblNo996.hashCode());
		result = prime * result + ((timeOfDay == null) ? 0 : timeOfDay.hashCode());
		result = prime * result + ((tktIss == null) ? 0 : tktIss.hashCode());
		result = prime * result + ((toGeoTblNo995 == null) ? 0 : toGeoTblNo995.hashCode());
		result = prime * result + ((unavail == null) ? 0 : unavail.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((waiver == null) ? 0 : waiver.hashCode());
		result = prime * result + ((waiverDate == null) ? 0 : waiverDate.hashCode());
		result = prime * result + ((waiverEl == null) ? 0 : waiverEl.hashCode());
		result = prime * result + ((waiverPperiod == null) ? 0 : waiverPperiod.hashCode());
		result = prime * result + ((waiverUnit == null) ? 0 : waiverUnit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtpcoRecord3Cat07 other = (AtpcoRecord3Cat07) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (catNno == null) {
			if (other.catNno != null)
				return false;
		} else if (!catNno.equals(other.catNno))
			return false;
		if (dateTblNo994 == null) {
			if (other.dateTblNo994 != null)
				return false;
		} else if (!dateTblNo994.equals(other.dateTblNo994))
			return false;
		if (el == null) {
			if (other.el != null)
				return false;
		} else if (!el.equals(other.el))
			return false;
		if (fromGeoTblNo995 == null) {
			if (other.fromGeoTblNo995 != null)
				return false;
		} else if (!fromGeoTblNo995.equals(other.fromGeoTblNo995))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (maxStay == null) {
			if (other.maxStay != null)
				return false;
		} else if (!maxStay.equals(other.maxStay))
			return false;
		if (maxStayDate == null) {
			if (other.maxStayDate != null)
				return false;
		} else if (!maxStayDate.equals(other.maxStayDate))
			return false;
		if (recType == null) {
			if (other.recType != null)
				return false;
		} else if (!recType.equals(other.recType))
			return false;
		if (recordbatch == null) {
			if (other.recordbatch != null)
				return false;
		} else if (!recordbatch.equals(other.recordbatch))
			return false;
		if (recordsequence == null) {
			if (other.recordsequence != null)
				return false;
		} else if (!recordsequence.equals(other.recordsequence))
			return false;
		if (reserved1 == null) {
			if (other.reserved1 != null)
				return false;
		} else if (!reserved1.equals(other.reserved1))
			return false;
		if (return_ == null) {
			if (other.return_ != null)
				return false;
		} else if (!return_.equals(other.return_))
			return false;
		if (rulestype == null) {
			if (other.rulestype != null)
				return false;
		} else if (!rulestype.equals(other.rulestype))
			return false;
		if (tblNo == null) {
			if (other.tblNo != null)
				return false;
		} else if (!tblNo.equals(other.tblNo))
			return false;
		if (textTblNo996 == null) {
			if (other.textTblNo996 != null)
				return false;
		} else if (!textTblNo996.equals(other.textTblNo996))
			return false;
		if (timeOfDay == null) {
			if (other.timeOfDay != null)
				return false;
		} else if (!timeOfDay.equals(other.timeOfDay))
			return false;
		if (tktIss == null) {
			if (other.tktIss != null)
				return false;
		} else if (!tktIss.equals(other.tktIss))
			return false;
		if (toGeoTblNo995 == null) {
			if (other.toGeoTblNo995 != null)
				return false;
		} else if (!toGeoTblNo995.equals(other.toGeoTblNo995))
			return false;
		if (unavail == null) {
			if (other.unavail != null)
				return false;
		} else if (!unavail.equals(other.unavail))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (waiver == null) {
			if (other.waiver != null)
				return false;
		} else if (!waiver.equals(other.waiver))
			return false;
		if (waiverDate == null) {
			if (other.waiverDate != null)
				return false;
		} else if (!waiverDate.equals(other.waiverDate))
			return false;
		if (waiverEl == null) {
			if (other.waiverEl != null)
				return false;
		} else if (!waiverEl.equals(other.waiverEl))
			return false;
		if (waiverPperiod == null) {
			if (other.waiverPperiod != null)
				return false;
		} else if (!waiverPperiod.equals(other.waiverPperiod))
			return false;
		if (waiverUnit == null) {
			if (other.waiverUnit != null)
				return false;
		} else if (!waiverUnit.equals(other.waiverUnit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AtpcoRecord3Cat07 [id=" + id + ", rulestype=" + rulestype + ", recordbatch=" + recordbatch
				+ ", recordsequence=" + recordsequence + ", recType=" + recType + ", action=" + action + ", catNno="
				+ catNno + ", tblNo=" + tblNo + ", return_=" + return_ + ", timeOfDay=" + timeOfDay + ", maxStay="
				+ maxStay + ", unit=" + unit + ", tktIss=" + tktIss + ", fromGeoTblNo995=" + fromGeoTblNo995
				+ ", toGeoTblNo995=" + toGeoTblNo995 + ", waiver=" + waiver + ", dateTblNo994=" + dateTblNo994
				+ ", textTblNo996=" + textTblNo996 + ", unavail=" + unavail + ", maxStayDate=" + maxStayDate + ", el="
				+ el + ", waiverDate=" + waiverDate + ", waiverEl=" + waiverEl + ", waiverPperiod=" + waiverPperiod
				+ ", waiverUnit=" + waiverUnit + ", reserved1=" + reserved1 + "]";
	}

	
}