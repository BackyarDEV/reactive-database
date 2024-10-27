package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = User.TABLE_NAME, schema = User.SCHEMA)
@JsonIgnoreProperties(value = { "password", "organization", "contacts" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    public static final String TABLE_NAME = "USERS";
    public static final String SCHEMA = "dev_schema";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String role;
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserContact> contacts = new ArrayList<>();

    public void setPassword(String password) {
        this.password = encoder.encode(password);
    }
}
