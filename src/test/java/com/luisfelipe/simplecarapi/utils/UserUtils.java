package com.luisfelipe.simplecarapi.utils;

import com.luisfelipe.simplecarapi.domain.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserUtils {

    public List<User> newUsersList(){
        User first = User.builder().id(1L).username("gustavo432").password("Golflindo123@").roles("USER").build();
        User second = User.builder().id(1L).username("santanalover").password("Santaninha501?").roles("USER").build();
        User third = User.builder().id(1L).username("mareadordecabeca").password("Dordecabeça404!").roles("USER").build();

        return new ArrayList<>(List.of(first, second, third));
    }
    public User newUserToSave(){
        return User.builder().id(1L).username("julio23").password("Teste123#").roles("USER").build();
    }
}
