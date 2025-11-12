package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository repository;

    public List<Car> findAll(){
        return repository.findAll();
    }

    public Car findById(Long id){
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Car not found"));
    }

    public Car save(Car car){
        return repository.save(car);
    }

    public void update(Car carToUpdate){
        assertCarExists(carToUpdate.getId());
        repository.save(carToUpdate);
    }

    public void deleteById(Long id){
        Car carToDelete = findById(id);
        repository.delete(carToDelete);
    }

    private void assertCarExists(Long id){
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Car not found"));
    }
}
