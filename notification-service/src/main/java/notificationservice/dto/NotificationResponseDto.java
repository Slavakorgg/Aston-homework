package notificationservice.dto;

public class NotificationResponseDto {

    private String message;

    public NotificationResponseDto() {
    }

    public NotificationResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
