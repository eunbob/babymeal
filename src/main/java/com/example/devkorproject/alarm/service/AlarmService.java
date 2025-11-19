package com.example.devkorproject.alarm.service;

import com.example.devkorproject.alarm.dto.AlarmResDto;
import com.example.devkorproject.alarm.dto.FCMMessageDto;
import com.example.devkorproject.alarm.entity.AlarmEntity;
import com.example.devkorproject.alarm.repository.AlarmRepository;
import com.example.devkorproject.common.constants.ErrorCode;
import com.example.devkorproject.common.exception.GeneralException;
import com.example.devkorproject.customer.entity.CustomerEntity;
import com.example.devkorproject.customer.exception.CustomerDoesNotExistException;
import com.example.devkorproject.customer.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class AlarmService {

    private static final Logger log = LoggerFactory.getLogger(AlarmService.class);
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final AlarmRepository alarmRepository;


    @Autowired
    public AlarmService(ObjectMapper objectMapper, CustomerRepository customerRepository, AlarmRepository alarmRepository){
        this.objectMapper = objectMapper;
        this.customerRepository = customerRepository;
        this.alarmRepository = alarmRepository;
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";
        // firebase로 부터 access token을 가져온다.
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public String makeMessage(
            String targetToken, String title, String body
    ) throws JsonProcessingException {

        FCMMessageDto fcmMessage = FCMMessageDto.builder()
                .message(FCMMessageDto.Message.builder()
                        .token(targetToken)
                        .notification(FCMMessageDto.Notification.builder()
                                        .title(title)
                                        .body(body)
                                        .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    @Value("${api.url}")
    private String firbaseAlarmSendApiUrl;

    public void sendMessageTo(
            String targetToken, String title, String body
    ) throws IOException{

        String message = makeMessage(targetToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(firbaseAlarmSendApiUrl)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer "+getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        if (log.isInfoEnabled()) {
            ResponseBody responseBody = response.body();
            log.info(responseBody != null ? responseBody.string() : "");
        }
    }

    public List<AlarmResDto> getRecentAlarm(Long customerId){
        Optional<CustomerEntity> opCustomerEntity = customerRepository.findCustomerEntityByCustomerId(customerId);
        if(opCustomerEntity.isEmpty())
            throw new CustomerDoesNotExistException();

        List<AlarmEntity> alarmEntities;
        alarmEntities = alarmRepository.findTop20ByCustomer_CustomerIdOrderByDateDesc(customerId);
        if(alarmEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return alarmEntities.stream().map(alarm -> {

            if(alarm.getCustomer().getCustomerName().isEmpty())
                throw new GeneralException(ErrorCode.CUSTOMER_NAME_DOES_NOT_EXIST);

            return new AlarmResDto(
                    alarm.getBody(),
                    alarm.getDate(),
                    alarm.getPost().getPostId()
            );
        }).toList();
    }
}
