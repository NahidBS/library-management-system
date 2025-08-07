package com.library.management.service;

import com.library.management.entity.Notification;
import com.library.management.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(String message, String recipient){
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    //new book in app notification
    public void notifyNewBook(String recipient, String bookTitle) {
        String message = "A new book titled '" + bookTitle + "' is now available in the library!";
        createNotification(message, recipient);
    }

    public List<Notification> getUnreadNotifications(String recipient) {
        return notificationRepository.findByRecipientAndRead(recipient, false);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

}
