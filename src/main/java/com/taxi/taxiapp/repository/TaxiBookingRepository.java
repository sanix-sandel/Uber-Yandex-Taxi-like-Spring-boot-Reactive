package com.taxi.taxiapp.repository;

import com.taxi.taxiapp.data.TaxiBooking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiBookingRepository extends CrudRepository<TaxiBooking, String> {
}
