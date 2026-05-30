package notificationservice.controller;

import notificationservice.dto.NotificationRequestDto;
import notificationservice.dto.NotificationResponseDto;
import notificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDto> sendNotification(@RequestBody NotificationRequestDto request) {
        NotificationResponseDto response = notificationService.send(request.getOperation(), request.getEmail());
        return ResponseEntity.ok(response);
    }
}
