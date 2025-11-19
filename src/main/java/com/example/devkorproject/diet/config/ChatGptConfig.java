package com.example.devkorproject.diet.config;

public final class ChatGptConfig {

    private ChatGptConfig() {}
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String CHAT_MODEL = "gpt-3.5-turbo";
    public static final Integer MAX_TOKEN = 2000;
    public static final Boolean STREAM = false;
    public static final String ROLE = "user";
    public static final Double TEMPERATURE = 0.0;
    public static final String MEDIA_TYPE = "application/json; charset=UTF-8";
    public static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
}
