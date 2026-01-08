package com.example.learningjava.repository;

import com.example.learningjava.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("select e.profilePictureUrl from Employee e where e.id = :id")
    Optional<String> findProfileImageBlobById(Long id);

    @Procedure(name = "Employee.getByDepartment")
    List<Employee> getByDepartment(@Param("department") String department);

}
