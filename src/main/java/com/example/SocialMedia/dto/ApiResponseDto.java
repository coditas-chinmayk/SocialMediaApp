package com.example.SocialMedia.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public ApiResponseDto(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

}