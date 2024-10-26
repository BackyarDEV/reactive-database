package com.backyardev.reactivedatabase.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(value = UserContact.TABLE_NAME, schema = UserContact.SCHEMA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserContact {

    public static final String TABLE_NAME = "USER_CONTACT";
    public static final String SCHEMA = "dev_schema";

    @Id
    private Integer id;
    private String contactType;
    private String contactValue;
    private Integer userId;

    public UserContact updateContactValue(UserContact userContact) {
        this.contactValue = userContact.getContactValue();
        return this;
    }
}
