package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = Organization.TABLE_NAME, schema = Organization.SCHEMA)
@JsonIgnoreProperties(value = { "users"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization {

    public static final String TABLE_NAME = "ORGANIZATION";
    public static final String SCHEMA = "dev_schema";

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "org_name")
    private String orgName;
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();
}
