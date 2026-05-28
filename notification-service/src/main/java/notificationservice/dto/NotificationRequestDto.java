package notificationservice.dto;

public class NotificationRequestDto {

    private UserOperation operation;
    private String email;

    public NotificationRequestDto() {
    }

    public NotificationRequestDto(UserOperation operation, String email) {
        this.operation = operation;
        this.email = email;
    }

    public UserOperation getOperation() {
        return operation;
    }

    public void setOperation(UserOperation operation) {
        this.operation = operation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
