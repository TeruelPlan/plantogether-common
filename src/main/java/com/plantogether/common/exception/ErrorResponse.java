package com.plantogether.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
