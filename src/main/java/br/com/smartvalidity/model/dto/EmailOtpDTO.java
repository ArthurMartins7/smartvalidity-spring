package br.com.smartvalidity.model.dto;

import lombok.Data;

@Data
public class EmailOtpDTO {
    private String email;
    private String otp;
} 