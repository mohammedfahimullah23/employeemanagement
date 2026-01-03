package com.example.learningjava.service;

import com.example.learningjava.azure_functions.AzureFunctionClient;
import com.example.learningjava.model.Employee;
import com.example.learningjava.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final AzureFunctionClient azureFunctionClient;
    private final AzureBlobService azureBlobService;

    public EmployeeService(EmployeeRepository repository, AzureFunctionClient azureFunctionClient,
            AzureBlobService azureBlobService) {
        this.repository = repository;
        this.azureFunctionClient = azureFunctionClient;
        this.azureBlobService = azureBlobService;
    }

    public Employee create(Employee emp) {
        return repository.save(emp);
    }

    public Employee createFromAzureFunctions(Employee employee) {
        return this.azureFunctionClient.processEmployee(employee);
    }

    public List<Employee> getAll() {
        return repository.findAll();
    }

    public Employee getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public Employee update(Long id, Employee emp) {
        Employee existing = getById(id);
        existing.setName(emp.getName());
        existing.setEmail(emp.getEmail());
        existing.setDepartment(emp.getDepartment());
        existing.setSalary(emp.getSalary());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Employee updateProfilePicture(Long id, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String blobName = azureBlobService.upload(file);

        try {
            emp.setProfilePictureUrl(blobName);
            return repository.save(emp);

        } catch (Exception ex) {
            azureBlobService.delete(blobName);
            throw ex;
        }
    }

    public String getProfileImageBlobName(Long employeeId) {
        return repository.findProfileImageBlobById(employeeId)
                .orElse(null);
    }
}
