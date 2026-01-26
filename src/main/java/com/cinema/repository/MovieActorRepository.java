package com.cinema.repository;

import com.cinema.model.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {

    List<MovieActor> findByMovie_Id(Long movieId);
}


