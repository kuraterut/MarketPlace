package org.kuraterut.orderservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private HttpStatus status;
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
