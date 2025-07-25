package org.kuraterut.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

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