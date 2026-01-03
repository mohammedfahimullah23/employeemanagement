package com.example.learningjava.repository;

import com.example.learningjava.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("select e.profilePictureUrl from Employee e where e.id = :id")
    Optional<String> findProfileImageBlobById(Long id);
}
