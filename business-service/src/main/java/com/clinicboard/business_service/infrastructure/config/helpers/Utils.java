package com.clinicboard.business_service.infrastructure.config.helpers;

import java.util.ArrayList;
import java.util.List;

import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;

public class Utils {

    public static void validateEmailFormat(String email) {
        if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$")) {
            throw new BusinessException("O formato do e-mail é inválido");
        }
    }

    public static void validatePhoneNumberFormat(String phone) {
        if (!phone.matches("^\\+[0-9]{1,3}\\s[0-9]{1,15}$")) {
            throw new BusinessException("O formato do número de telefone é inválido");
        }
    }

    public static void validateZipCodeFormat(String zipCode) {
        if (!zipCode.matches("^\\d{5}-\\d{3}$")) {
            throw new BusinessException("O formato do CEP é inválido");
        }
    }

    public static List<String> generateAllHours() {
        List<String> hours = new ArrayList<>();
        for (int i = 8; i <= 21; i++) {
            String hour = String.format("%02d:00", i);
            hours.add(hour);
        }
        return hours;
    }

}
