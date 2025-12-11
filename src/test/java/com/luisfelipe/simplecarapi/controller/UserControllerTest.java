package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.config.PasswordEncoderConfig;
import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.exception.UsernameAlreadyExistsException;
import com.luisfelipe.simplecarapi.mapper.PasswordEncoderMapper;
import com.luisfelipe.simplecarapi.mapper.UserMapperImpl;
import com.luisfelipe.simplecarapi.service.UserService;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import com.luisfelipe.simplecarapi.utils.UserUtils;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = UserController.class)
@Import({UserMapperImpl.class, PasswordEncoderMapper.class, PasswordEncoderConfig.class, UserUtils.class, FileUtils.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService service;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private FileUtils fileUtils;


    @Order(1)
    @Test
    @DisplayName("POST /v1/users 201 creates a new user when successful")
    @WithMockUser
    void save_CreatesUser_WhenSuccessful() throws Exception {
        User savedUser = userUtils.newUserToSave();
        BDDMockito.given(service.save(BDDMockito.any())).willReturn(savedUser);

        String request = fileUtils.readResourceFile("user/post-request-user-200.json");
        String response = fileUtils.readResourceFile("user/post-response-user-201.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users")
                        .content(request)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Order(2)
    @Test
    @DisplayName("POST /v1/users throws UsernameAlreadyExistsException 409 when username already exists.")
    @WithMockUser
    void save_ThrowsUsernameAlreadyExistsException_WhenUsernameExists() throws Exception {
        BDDMockito.given(service.save(BDDMockito.any()))
                .willThrow(new UsernameAlreadyExistsException("Username is already being used."));

        String request = fileUtils.readResourceFile("user/post-request-user-409.json");
        String expectedResponse = fileUtils.readResourceFile("user/post-response-user-409.json");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/users")
                        .content(request)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andReturn().getResponse().getContentAsString();

        JsonAssertions.assertThatJson(response)
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }
}