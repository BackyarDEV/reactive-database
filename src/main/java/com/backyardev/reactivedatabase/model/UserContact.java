package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = UserContact.TABLE_NAME, schema = UserContact.SCHEMA)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = { "user" })
public class UserContact {

    public static final String TABLE_NAME = "USER_CONTACT";
    public static final String SCHEMA = "dev_schema";

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "contact_type")
    private String contactType;
    @Column(name = "contact_value")
    private String contactValue;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
