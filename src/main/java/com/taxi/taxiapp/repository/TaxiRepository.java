package com.taxi.taxiapp.repository;

import com.taxi.taxiapp.data.Taxi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiRepository extends CrudRepository<Taxi, String> {
}
