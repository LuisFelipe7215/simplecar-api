package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository repository;
    private final FileStorageService fileStorageService;

    public List<Car> findAll(){
        return repository.findAll();
    }

    public Car findById(Long id){
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Car not found"));
    }

    public Car save(Car car){
        return repository.save(car);
    }

    public void update(Car carToUpdate){
        Car existingCar = findById(carToUpdate.getId());
        existingCar.setType(carToUpdate.getType());
        existingCar.setBrand(carToUpdate.getBrand());
        existingCar.setModel(carToUpdate.getModel());
        existingCar.setYear(carToUpdate.getYear());
        existingCar.setPrice(carToUpdate.getPrice());
        repository.save(existingCar);
    }

    @Transactional
    public void deleteById(Long id){
        Car carToDelete = findById(id);

        List<String> photoFileNames = carToDelete.getPhotos().stream()
                .map(Photo::getFileName)
                .toList();

        repository.delete(carToDelete);

        photoFileNames.forEach(fileStorageService::deleteFile);
    }
}
