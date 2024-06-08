package com.jgz.backend.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jgz.backend.entity.OurUser;
import com.jgz.backend.entity.Product;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqRes {
    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationDate;
    private String expirationTime;
    private String name;
    private String phoneNumber;
    private String email;
    private String role;
    private String password;
    private String identity;
    private String username;
    private List<Product> products;
    private OurUser users;

}
