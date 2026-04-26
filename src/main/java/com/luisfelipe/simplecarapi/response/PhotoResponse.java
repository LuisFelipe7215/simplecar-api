package com.luisfelipe.simplecarapi.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PhotoResponse {
    private Long id;
    private String url;
    private Boolean thumbnail;
}
