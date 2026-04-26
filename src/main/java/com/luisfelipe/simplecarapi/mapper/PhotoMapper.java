package com.luisfelipe.simplecarapi.mapper;

import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.response.PhotoPostResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    PhotoPostResponse toPhotoPostResponse(Photo photo);
}
