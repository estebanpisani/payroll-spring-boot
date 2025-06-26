package com.example.demo.payroll;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {

    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/employees")
    public CollectionModel<EntityModel<Employee>> all() {
        List<EntityModel<Employee>> employees = repository.findAll()
                .stream()
                .map(employee -> EntityModel.of(employee,
                        linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                        linkTo(methodOn(EmployeeController.class).all()).withRel("employees")))
                .collect(Collectors.toList());

        return CollectionModel.of(employees,
                linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @PostMapping("/employees")
    public EntityModel<Employee> newEmployee(@RequestBody Employee newEmployee){
        Employee savedEmployee = repository.save(newEmployee);
        return EntityModel.of(savedEmployee,
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"),
                linkTo(methodOn(EmployeeController.class).one(savedEmployee.getId())).withSelfRel());
    }

    @GetMapping("/employees/{id}")
    public EntityModel<Employee> one(@PathVariable Long id){
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return EntityModel.of(employee, //
                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
    }

    @PutMapping("/employees/{id}")
    public EntityModel<Employee> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id){
        Employee updatedEmployee = repository.findById(id).map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                })
                .orElseGet(() -> repository.save(newEmployee));

        return EntityModel.of(updatedEmployee,
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"),
                linkTo(methodOn(EmployeeController.class).one(updatedEmployee.getId())).withSelfRel());
    }

    @DeleteMapping("/employees/{id}")
    public void deleteEmployee(@PathVariable Long id){
        repository.deleteById(id);
    }
}
