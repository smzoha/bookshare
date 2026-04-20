package com.zedapps.bookshare.dto.api;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@Getter
@Setter
public class ErrorResponseDto {

    private List<String> globalErrors;

    private Map<String, List<String[]>> fieldErrors;

    public ErrorResponseDto() {
        this.globalErrors = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
    }

    public ErrorResponseDto(List<String> globalErrors) {
        this();
        this.globalErrors.addAll(globalErrors);
    }
}
