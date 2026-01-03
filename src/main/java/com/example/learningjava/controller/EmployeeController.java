package com.example.learningjava.controller;

import com.example.learningjava.model.Employee;
import com.example.learningjava.service.EmployeeService;
import com.example.learningjava.service.ProfileImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    private final EmployeeService service;
    private final ProfileImageService profileImageService;

    public EmployeeController(EmployeeService service, ProfileImageService profileImageService) {
        this.service = service;
        this.profileImageService = profileImageService;
    }

    @PostMapping
    public Employee create(@RequestBody Employee emp) {
        return service.create(emp);
    }

    @PostMapping("/functions/create")
    public Employee createEmployeeViaFunctions(@RequestBody Employee emp) {
        return service.createFromAzureFunctions(emp);
    }

    @PostMapping("/{id}/profile-picture")
    public Employee uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return service.updateProfilePicture(id, file);
    }

    @GetMapping("/dummy")
    public List<String> test() {
        return List.of("hiiiissss" + redirectUri);
    }

    @GetMapping
    public List<Employee> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Employee getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/profile-image-url")
    public String getProfileImageUrl(@PathVariable Long id) {
        String blobName = service.getProfileImageBlobName(id);
        return profileImageService.generateReadSasUrl(blobName);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee emp) {
        return service.update(id, emp);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
