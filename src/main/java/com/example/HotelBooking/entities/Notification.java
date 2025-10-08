package com.example.HotelBooking.entities;

import com.example.HotelBooking.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
<<<<<<< HEAD
@Table(name = "notifications")
=======
@Table(name = "payments")
>>>>>>> 7b1b7a7f3b6b596b319e13dcae87273676cd7e59
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    @NotNull(message = "recipient is required")
    private String recipient;

    private String body;

    private String bookingReference;

    @Enumerated(EnumType.STRING)
<<<<<<< HEAD
    private NotificationType type;
=======
    private NotificationType notificationType;
>>>>>>> 7b1b7a7f3b6b596b319e13dcae87273676cd7e59

    private final LocalDateTime createdAt = LocalDateTime.now();

}
