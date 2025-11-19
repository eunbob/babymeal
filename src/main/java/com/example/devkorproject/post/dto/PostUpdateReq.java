package com.example.devkorproject.post.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Data
@RequiredArgsConstructor
public class PostUpdateReq {
    private Long postId;
    private Long comments;
    private Long likes;
    private String title;
    private String body;
    private List<String> filePaths;
    private Long scrap;
    private String type;
    private Boolean isliked;
}
