package com.luisfelipe.simplecarapi.request;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserPostRequest {
    private String username;
    private String password;
}
