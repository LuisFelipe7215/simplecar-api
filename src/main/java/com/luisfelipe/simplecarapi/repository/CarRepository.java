package com.luisfelipe.simplecarapi.repository;

import com.luisfelipe.simplecarapi.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
