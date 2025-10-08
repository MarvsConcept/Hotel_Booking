package com.example.HotelBooking.repositories;

import com.example.HotelBooking.entities.BookingReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

<<<<<<< HEAD
public interface BookingReferenceRepository extends JpaRepository<BookingReference, Long> {
=======
public interface BookingReferenceRepository extends JpaRepository<BookingReferenceRepository, Long> {
>>>>>>> 7b1b7a7f3b6b596b319e13dcae87273676cd7e59

    Optional<BookingReference> findByReferenceNo(String referenceNo);
}
