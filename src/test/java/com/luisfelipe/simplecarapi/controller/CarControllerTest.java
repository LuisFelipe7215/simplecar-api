package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.config.SecurityConfig;
import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.mapper.CarMapperImpl;
import com.luisfelipe.simplecarapi.service.CarService;
import com.luisfelipe.simplecarapi.utils.CarUtils;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@WebMvcTest(controllers = CarController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({CarService.class, CarMapperImpl.class, FileUtils.class, CarUtils.class, SecurityConfig.class})
@WithMockUser
class CarControllerTest {
    public static final String URL = "/v1/cars";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CarService service;
    private List<Car> carsList;
    @Autowired
    private CarUtils carUtils;
    @Autowired
    private FileUtils fileUtils;

    @BeforeEach
    void init() {
        carsList = carUtils.newCarsList();
    }

    @Order(1)
    @Test
    @DisplayName("GET /v1/cars returns all cars when successful")
    void findAll_ReturnsAllCars_WhenSuccessful() throws Exception {
        BDDMockito.given(service.findAll()).willReturn(carsList);

        String response = fileUtils.readResourceFile("car/get-cars-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(2)
    @Test
    @DisplayName("GET /v1/cars returns an empty list when no cars are found")
    void findAll_ReturnsEmptyList_WhenNoCarsAreFound() throws Exception {
        BDDMockito.given(service.findAll()).willReturn(Collections.emptyList());
        String response = fileUtils.readResourceFile("car/get-empty-cars-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get(URL))
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
        String response = fileUtils.readResourceFile("car/get-car-by-id-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(4)
    @Test
    @DisplayName("GET /v1/cars/1 returns a car with the given id without a list of photos")
    void findById_ReturnsCarWithoutPhotos_WhenSuccessful() throws Exception {
        Car car = carsList.get(2);
        Long id = car.getId();
        BDDMockito.given(service.findById(id)).willReturn(car);
        String response = fileUtils.readResourceFile("car/get-car-by-id-empty-photos-200.json");

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(5)
    @Test
    @DisplayName("GET /v1/cars/99 throws NotFoundException 404 when car is not found")
    void findById_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Long id = 99L;
        BDDMockito.given(service.findById(id)).willThrow(new NotFoundException("Car not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Order(6)
    @Test
    @DisplayName("POST /v1/car creates a car")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void save_CreatesCar_WhenSuccessful() throws Exception {
        Car savedCar = carUtils.newCarToSave();
        BDDMockito.given(service.save(BDDMockito.any())).willReturn(savedCar);

        String request = fileUtils.readResourceFile("car/post-request-car-200.json");
        String response = fileUtils.readResourceFile("car/post-response-car-201.json");

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .content(request).contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(7)
    @Test
    @DisplayName("POST /v1/car returns 403 when user is not admin")
    @WithMockUser
    void save_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        String request = fileUtils.readResourceFile("car/post-request-car-200.json");

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Order(8)
    @Test
    @DisplayName("POST /v1/car returns 400 when body is invalid")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void save_ReturnsBadRequest_WhenBodyIsInvalid() throws Exception {
        String request = fileUtils.readResourceFile("car/post-request-car-400.json");

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .content(request).contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Order(9)
    @Test
    @DisplayName("PUT /v1/car updates a car when successful")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void update_UpdateCar_WhenSuccessful() throws Exception {
        Car carToUpdate = carsList.getFirst().withPrice(new BigDecimal("25000.00"));
        BDDMockito.willDoNothing().given(service).update(carToUpdate);

        String request = fileUtils.readResourceFile("car/put-request-car-204.json");

        mockMvc.perform(MockMvcRequestBuilders.put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Order(10)
    @Test
    @DisplayName("PUT /v1/car throws NotFoundException when car is not found")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void update_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Car carToUpdate = carsList.getFirst().withPrice(new BigDecimal("25000.00")).withId(99L);
        BDDMockito.willThrow(new NotFoundException("Car not found")).given(service).update(carToUpdate);

        String request = fileUtils.readResourceFile("car/put-request-car-404.json");

        mockMvc.perform(MockMvcRequestBuilders.put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Order(11)
    @Test
    @DisplayName("PUT /v1/car returns 400 when body is invalid")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void update_ReturnsBadRequest_WhenBodyIsInvalid() throws Exception {
        String request = fileUtils.readResourceFile("car/put-request-car-400.json");

        mockMvc.perform(MockMvcRequestBuilders.put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Order(12)
    @Test
    @DisplayName("PUT /v1/car returns 403 when user is not admin")
    @WithMockUser
    void update_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        String request = fileUtils.readResourceFile("car/put-request-car-204.json");

        mockMvc.perform(MockMvcRequestBuilders.put(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Order(13)
    @Test
    @DisplayName("DELETE v1/car/1 removes a car and its photos when successful")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void delete_RemovesCar_WhenSuccessful() throws Exception {
        Long id = carsList.getFirst().getId();
        BDDMockito.willDoNothing().given(service).deleteById(id);

        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Order(14)
    @Test
    @DisplayName("DELETE v1/car/99 throws NotFoundException when car is not found")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void delete_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Long id = carsList.getFirst().withId(99L).getId();
        BDDMockito.willThrow(new NotFoundException("Car not found")).given(service).deleteById(id);

        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Order(15)
    @Test
    @DisplayName("DELETE v1/car/1 returns 403 when user is not admin ")
    @WithMockUser
    void delete_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        Long id = carsList.getFirst().getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}