package com.authentication.AuthProject.util;

import java.time.LocalDate;
import java.time.Period;

public class AgeCalculator {

    private AgeCalculator() {
    }

    public static int calculate(LocalDate dob) {
        if(dob == null){
            return 0;
        }

        return Period.between(dob,LocalDate.now()).getYears();
    }
}
