package com.jtk.ps.api.repository;

import com.jtk.ps.api.model.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    Optional<Lecturer> findById(Integer id);

    Optional<Lecturer> findByAccountId(Integer accountId);
}
