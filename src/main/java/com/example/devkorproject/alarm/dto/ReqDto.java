package com.example.devkorproject.alarm.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqDto {
    private Long customerId;
    private String title;
    private String body;
}
