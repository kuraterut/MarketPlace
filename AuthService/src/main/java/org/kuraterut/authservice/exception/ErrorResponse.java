package org.kuraterut.authservice.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ErrorResponse {
    private HttpStatus status;
    private String message;
    private OffsetDateTime timestamp;

    public ErrorResponse(List<String> messages, HttpStatus status, OffsetDateTime timestamp) {
        StringBuilder totalMessage = new StringBuilder();
        for(String message: messages){
            totalMessage.append(message).append("\n");
        }
        this.message = totalMessage.toString();
        this.status = status;
        this.timestamp = timestamp;
    }
}
