package com.example.devkorproject.diet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
//chatGPT에 요청할 DTO Format
public class GptReqDto{
    @JsonProperty
    private String model;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    @JsonProperty
    private Double temperature;
    @JsonProperty
    private Boolean stream;
    @JsonProperty
    private List<ChatGptMessage> messages;

    @Builder
    public GptReqDto(String model, Integer maxTokens, Double temperature,
                          Boolean stream, List<ChatGptMessage> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.stream = stream;
        this.messages = messages;
    }
}