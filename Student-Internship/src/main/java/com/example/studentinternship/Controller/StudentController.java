package com.example.studentinternship.Controller;

import com.example.studentinternship.Entity.StudentEntity;
import com.example.studentinternship.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}) // both frontends
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public List<StudentEntity> getAll() {
        return studentRepository.findAll();
    }

    @GetMapping("/greet")
    public String greet() {
        return "Hello World";
    }


    @PostMapping
    public StudentEntity create(@RequestBody StudentEntity student) {
        return studentRepository.save(student);
    }
}

