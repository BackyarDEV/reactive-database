package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(value = Organization.TABLE_NAME, schema = Organization.SCHEMA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization {

    public static final String TABLE_NAME = "ORGANIZATION";
    public static final String SCHEMA = "dev_schema";

    @Id
    private Integer id;
    private String orgName;
}
