package com.atibusinessgroup.fmp.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A ReviewLevel.
 */
@Document(collection = "review_level")
public class ReviewLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("active_status")
    private Boolean activeStatus;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ReviewLevel name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public ReviewLevel description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActiveStatus() {
        return activeStatus;
    }

    public ReviewLevel activeStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
        return this;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReviewLevel reviewLevel = (ReviewLevel) o;
        if (reviewLevel.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), reviewLevel.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ReviewLevel{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", activeStatus='" + isActiveStatus() + "'" +
            "}";
    }
}
