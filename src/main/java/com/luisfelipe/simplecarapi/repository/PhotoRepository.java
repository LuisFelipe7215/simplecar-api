package com.luisfelipe.simplecarapi.repository;

import com.luisfelipe.simplecarapi.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
