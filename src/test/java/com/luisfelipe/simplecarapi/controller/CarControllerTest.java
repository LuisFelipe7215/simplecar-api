package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.mapper.CarMapperImpl;
import com.luisfelipe.simplecarapi.service.CarService;
import com.luisfelipe.simplecarapi.utils.CarUtils;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@WebMvcTest(controllers = CarController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({CarService.class, CarMapperImpl.class})
class CarControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CarService service;
    private List<Car> carsList;
    @InjectMocks
    private CarUtils carUtils;
    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    void init() {
        carsList = carUtils.newCarsList();
    }

    @Order(1)
    @Test
    @DisplayName("GET /v1/cars returns all cars when successful")
    void findAll_ReturnsAllCars_WhenSuccessful() throws Exception {
        BDDMockito.given(service.findAll()).willReturn(carsList);

        String response = readResourceFile("car/get-cars-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(2)
    @Test
    @DisplayName("GET /v1/cars returns an empty list when no cars are found")
    void findAll_ReturnsEmptyList_WhenNoCarsAreFound() throws Exception {
        BDDMockito.given(service.findAll()).willReturn(Collections.emptyList());
        String response = readResourceFile("car/get-empty-cars-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(3)
    @Test
    @DisplayName("GET /v1/cars/1 returns a car with the given id")
    void findById_ReturnsCar_WhenSuccessful() throws Exception {
        Car car = carsList.getFirst();
        Long id = car.getId();
        BDDMockito.given(service.findById(id)).willReturn(car);
        String response = readResourceFile("car/get-car-by-id-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(4)
    @Test
    @DisplayName("GET /v1/cars/99 throws NotFoundException 404 when car is not found")
    void findById_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Long id = 99L;
        BDDMockito.given(service.findById(id)).willThrow(new NotFoundException("Car not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.status().reason("Car not found"));
    }

    @Order(5)
    @Test
    @DisplayName("POST /v1/car creates a car")
    void save_CreatesCar_WhenSuccessful() throws Exception {
        Car savedCar = carUtils.newCarToSave();
        BDDMockito.given(service.save(BDDMockito.any())).willReturn(savedCar);

        String request = readResourceFile("car/post-request-car-200.json");
        String response = readResourceFile("car/post-response-car-201.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/cars")
                        .content(request).contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(6)
    @Test
    @DisplayName("PUT /v1/car updates a car when successful")
    void update_UpdateCar_WhenSuccessful() throws Exception {
        Car carToUpdate = carsList.getFirst().withPrice(25000D);
        BDDMockito.willDoNothing().given(service).update(carToUpdate);

        String request = readResourceFile("car/put-request-car-204.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Order(7)
    @Test
    @DisplayName("PUT /v1/car throws NotFoundException when car is not found")
    void update_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Car carToUpdate = carsList.getFirst().withPrice(25000D).withId(99L);
        BDDMockito.willThrow(new NotFoundException("Car not found")).given(service).update(carToUpdate);

        String request = readResourceFile("car/put-request-car-404.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.status().reason("Car not found"));
    }

    @Order(8)
    @Test
    @DisplayName("DELETE v1/car/1 removes a car when successful")
    void delete_RemovesCar_WhenSuccessful() throws Exception {
        Long id = carsList.getFirst().getId();
        BDDMockito.willDoNothing().given(service).deleteById(id);

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Order(9)
    @Test
    @DisplayName("DELETE v1/car/99 throws NotFoundException when car is not found")
    void delete_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Long id = carsList.getFirst().withId(99L).getId();
        BDDMockito.willThrow(new NotFoundException("Car not found")).given(service).deleteById(id);

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.status().reason("Car not found"));
    }

    private String readResourceFile(String fileName) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:%s".formatted(fileName));

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}