package com.example.evolveumrest.repository;

import com.example.evolveumrest.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepo extends JpaRepository<Policy,String> {
}
