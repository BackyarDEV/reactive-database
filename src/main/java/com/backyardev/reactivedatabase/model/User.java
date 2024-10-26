package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@Table(value = User.TABLE_NAME, schema = User.SCHEMA)
@JsonIgnoreProperties(value = { "password" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    public static final String TABLE_NAME = "USERS";
    public static final String SCHEMA = "dev_schema";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Id
    private Integer id;
    private String username;
    private String password;
    private String role;
    private Integer organizationId;

    public void setPassword(String password) {
        this.password = encoder.encode(password);
    }
}
