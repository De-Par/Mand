package com.messenger.mand.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class User {

    private String name;
    private String email;
    private String id;
    private String avatar;
    private String status;
    private String dateCreation;
    private String searchName;
    private String phone;
    private String dateBirth;
    private String sex;
    private String aboutMe;
}