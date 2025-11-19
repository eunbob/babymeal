package com.example.devkorproject.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCreateReqDto {

    private String title;
    private String body;
    private String type;
}
