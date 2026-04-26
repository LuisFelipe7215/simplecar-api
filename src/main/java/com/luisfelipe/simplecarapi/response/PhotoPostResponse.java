package com.luisfelipe.simplecarapi.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PhotoPostResponse {
    private Long id;
    private String fileName;
    private Boolean thumbnail;
}
