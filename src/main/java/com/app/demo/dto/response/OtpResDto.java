package com.app.demo.dto.response;

import com.app.demo.enums.OtpStatus;
import lombok.Getter;

@Getter
public class OtpResDto {
    private final OtpStatus status;

    public OtpResDto(OtpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return status.getMessage();
    }

    public boolean isValid() {
        return status == OtpStatus.VALID;
    }
}
