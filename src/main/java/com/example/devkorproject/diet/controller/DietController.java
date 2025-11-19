package com.example.devkorproject.diet.controller;
import com.example.devkorproject.auth.jwt.JwtUtil;
import com.example.devkorproject.common.constants.ErrorCode;
import com.example.devkorproject.common.exception.GeneralException;
import com.example.devkorproject.diet.dto.*;
import com.example.devkorproject.common.dto.HttpDataResponse;

import com.example.devkorproject.diet.service.DietService;
import lombok.*;

import org.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/diet")
@RestController
public class DietController {
    private final DietService dietService;
    private final JwtUtil jwtUtil;
    @PostMapping("/simple")
    public HttpDataResponse<SimpleResDto[]> askQuestion(@RequestHeader("Authorization") String authHeader, @RequestParam Long babyId ,@RequestBody SimpleReqDto simpleRequestDto) throws JSONException {
        String token=authHeader.substring(7);
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        return HttpDataResponse.of(dietService.askQuestion(customerId, babyId, simpleRequestDto));
    }

    @PostMapping("/detail")
    public HttpDataResponse<DietResDto> getDetailDiet(@RequestHeader("authorization") String token,@RequestParam Long simpleDietId, @RequestBody DetailReqDto detailReqDto) throws JSONException {
        return HttpDataResponse.of(dietService.getDetailDiet(simpleDietId, detailReqDto));
    }


    @PostMapping("/fridge")
    public HttpDataResponse<List<FridgeSimpleDietResDto>> getFridgeSimpleDiet(@RequestHeader("Authorization") String authHeader, @RequestParam Long babyId) throws JSONException {
        String token=authHeader.substring(7);
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        return HttpDataResponse.of(dietService.getFridgeSimpleDiet(customerId, babyId));
    }

    @PostMapping("fridge/detail")
    public HttpDataResponse<DietResDto> getFridgeDetailDiet(@RequestHeader("authorization") String token,@RequestParam Long simpleDietId) throws JSONException {
        return HttpDataResponse.of(dietService.getFridgeDetailDiet(simpleDietId));
    }

    @PutMapping("/press")
    public HttpDataResponse<PressDto> pressHeart (@RequestHeader("authorization") String authHeader,@RequestParam Long simpleDietId){
        return HttpDataResponse.of(dietService.pressHeart(simpleDietId));
    }

    @GetMapping("/heart")
    public HttpDataResponse<List<HeartDietResDto>> getHeartDiet(@RequestHeader("Authorization") String authHeader){
        String token=authHeader.substring(7);
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        return HttpDataResponse.of(dietService.getHeartDiet(customerId));
    }

    @GetMapping("/heart/view")
    public HttpDataResponse<DietResDto> getHeartDietView(@RequestHeader("authorization") String authHeader,@RequestParam Long simpleDietId) throws JSONException {
        return HttpDataResponse.of(dietService.getHeartDietView(simpleDietId));
    }

}



