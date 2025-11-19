package com.example.devkorproject.auth.service;

import com.example.devkorproject.auth.dto.RegisterReq;
import com.example.devkorproject.auth.dto.RegisterRes;
import com.example.devkorproject.auth.jwt.JwtUtil;
import com.example.devkorproject.common.constants.ErrorCode;
import com.example.devkorproject.common.exception.GeneralException;
import com.example.devkorproject.customer.entity.CustomerEntity;
import com.example.devkorproject.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AuthService {
    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    @Autowired
    public AuthService(CustomerRepository customerRepository,JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    public Boolean checkCustomerName(String customerName){
        Optional<CustomerEntity> customer=customerRepository.findCustomerEntityByCustomerName(customerName);
        return customer.isEmpty();
    }

    public RegisterRes register(RegisterReq registerReq){
        //비밀번호 해싱
        Optional<CustomerEntity> customer=customerRepository.findCustomerEntityByCustomerName(registerReq.getCustomerName());
        if(customer.isPresent())
            throw new GeneralException(ErrorCode.CUSTOMER_EXIST);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword= encoder.encode(registerReq.getPassword());
        if(encodedPassword.isEmpty())
            throw new GeneralException(ErrorCode.BLANK_PASSWORD);
        // 기본값 보정: 누락된 필드에 안전한 기본값 적용
        String resolvedRank = (registerReq.getRank() == null || registerReq.getRank().isBlank()) ? "USER" : registerReq.getRank();
        Long resolvedMyPosts = (registerReq.getMyPosts() == null) ? 0L : registerReq.getMyPosts();
        Long resolvedMyLikes = (registerReq.getMyLikes() == null) ? 0L : registerReq.getMyLikes();
        Long resolvedMyComments = (registerReq.getMyComments() == null) ? 0L : registerReq.getMyComments();
        CustomerEntity customerEntity=CustomerEntity.builder()
                    .customerName(registerReq.getCustomerName())
                    .email(registerReq.getEmail())
                    .password(encodedPassword)
                    .authority("ROLE_USER")
                    .rank(resolvedRank)
                    .myPosts(resolvedMyPosts)
                    .myLikes(resolvedMyLikes)
                    .myComments(resolvedMyComments)
                    .build();
        customerRepository.save(customerEntity);
        String accessToken= jwtUtil.createToken(customerEntity.getCustomerId());
        return new RegisterRes(accessToken);
    }
    public Boolean checkEmail(String email){
        Optional<CustomerEntity> customer=customerRepository.findCustomerEntityByEmail(email);
        return customer.isEmpty();
    }

}
