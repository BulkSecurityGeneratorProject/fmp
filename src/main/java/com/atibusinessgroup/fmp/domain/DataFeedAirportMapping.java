package com.atibusinessgroup.fmp.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A RMDataFeeds.
 */
@Document(collection = "data_feed_airport_maping")
public class DataFeedAirportMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Field("city_code")
	private String cityCode;

	@Field("airport_code")
	private String airportCode;

	@Field("country_code")
	private String countryCode;

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAirportCode() {
		return airportCode;
	}

	public void setAirportCode(String airportCode) {
		this.airportCode = airportCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	// jhipster-needle-entity-add-field - JHipster will add fields here, do not
	// remove
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	// jhipster-needle-entity-add-getters-setters - JHipster will add getters and
	// setters here, do not remove

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DataFeedAirportMapping rMDataFeeds = (DataFeedAirportMapping) o;
		if (rMDataFeeds.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), rMDataFeeds.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "RMDataFeeds [id=" + id + ", cityCode=" + cityCode + ", airportCode=" + airportCode + ", countryCode="
				+ countryCode + "]";
	}

}
