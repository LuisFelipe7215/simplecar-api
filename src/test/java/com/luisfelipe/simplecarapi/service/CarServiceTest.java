package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import com.luisfelipe.simplecarapi.utils.CarUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CarServiceTest {
    @InjectMocks
    private CarService service;
    @Mock
    private CarRepository repository;
    private List<Car> carsList;
    @InjectMocks
    private CarUtils carUtils;

    @BeforeEach
    void init(){
        carsList = carUtils.newCarsList();
    }

    @Order(1)
    @Test
    @DisplayName("FindAll returns all cars when successful")
    void findAll_ReturnsAllCars_WhenSuccessful(){
        BDDMockito.given(repository.findAll()).willReturn(carsList);

        List<Car> cars = service.findAll();

        Assertions.assertThat(cars).isNotEmpty().isNotNull().hasSameElementsAs(carsList);
    }

    @Order(2)
    @Test
    @DisplayName("FindAll returns empty list when no cars are found")
    void findAll_ReturnsEmptyList_WhenNoCarsAreFound(){
        BDDMockito.given(repository.findAll()).willReturn(Collections.emptyList());

        List<Car> cars = service.findAll();

        Assertions.assertThat(cars).isNotNull().isEmpty();
    }

    @Order(3)
    @Test
    @DisplayName("FindById returns a car when successful")
    void findById_ReturnsCar_WhenSuccessful(){
        Car car = carsList.getFirst();
        Long id = car.getId();
        BDDMockito.given(repository.findById(id)).willReturn(Optional.of(car));

        Car carFounded = service.findById(id);

        Assertions.assertThat(carFounded).isNotNull().isEqualTo(car);
        Assertions.assertThat(carFounded.getId()).isEqualTo(id);
    }

    @Order(4)
    @Test
    @DisplayName("FindById throws NotFoundException when car is not found")
    void findById_ThrowsNotFoundException_WhenCarIsNotFound(){
        Long id = 99L;
        BDDMockito.given(repository.findById(id)).willReturn(Optional.empty());

        Assertions.assertThatException().isThrownBy(() -> service.findById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Order(5)
    @Test
    @DisplayName("Save returns a car when successful")
    void save_ReturnsCar_WhenSuccessful(){
        Car carToSave = carUtils.newCarToSave();
        BDDMockito.given(repository.save(carToSave)).willReturn(carToSave);

        Car savedCar = service.save(carToSave);

        Assertions.assertThat(savedCar).isNotNull().isEqualTo(carToSave).hasNoNullFieldsOrProperties();
        Assertions.assertThat(savedCar.getId()).isNotNull();
    }

    @Order(6)
    @Test
    @DisplayName("Update updates a car when successful")
    void update_UpdateCar_WhenSuccessful(){
        Car carToUpdate = carsList.getFirst().withPrice(25000D);
        BDDMockito.given(repository.findById(carToUpdate.getId())).willReturn(Optional.of(carToUpdate));
        BDDMockito.given(repository.save(carToUpdate)).willReturn(carToUpdate);

        Assertions.assertThatNoException().isThrownBy(() -> service.update(carToUpdate));
    }

    @Order(7)
    @Test
    @DisplayName("Update throws NotFoundException when car is not found")
    void update_ThrowsNotFoundException_WhenCarIsNotFound(){
        Car carToUpdate = carsList.getFirst().withPrice(25000D).withId(99L);
        BDDMockito.given(repository.findById(carToUpdate.getId())).willReturn(Optional.empty());

        Assertions.assertThatException().isThrownBy(() -> service.update(carToUpdate))
                .isInstanceOf(NotFoundException.class);
    }

    @Order(8)
    @Test
    @DisplayName("Delete removes a car when successful")
    void Delete_RemovesCar_WhenSuccessful(){
        Car carToDelete = carsList.getFirst();
        BDDMockito.given(repository.findById(carToDelete.getId())).willReturn(Optional.of(carToDelete));
        BDDMockito.willDoNothing().given(repository).delete(carToDelete);

        Assertions.assertThatNoException().isThrownBy(() -> service.deleteById(carToDelete.getId()));
    }

    @Order(9)
    @Test
    @DisplayName("Delete throws NotFoundException when car is not found")
    void delete_ThrowsNotFoundException_WhenCarIsNotFound(){
        Car carToDelete = carsList.getFirst().withId(99L);
        BDDMockito.given(repository.findById(carToDelete.getId())).willReturn(Optional.empty());

        Assertions.assertThatException().isThrownBy(() -> service.deleteById(carToDelete.getId()))
                .isInstanceOf(NotFoundException.class);
    }


    
}