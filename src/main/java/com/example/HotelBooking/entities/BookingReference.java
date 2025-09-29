package com.example.HotelBooking.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
//@Table(name = "bookingReference")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String referenceNo;

}
