package com.example.studentinternship.Controller;

import com.example.studentinternship.Entity.StudentEntity;
import com.example.studentinternship.Repository.StudentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Retrieve all students (only accessible to `ROLE_STUDENT` or `COLLEGE_ADMIN`)
    @PreAuthorize("hasAnyAuthority('student', 'college-admin', 'mentor')")
    @GetMapping("/list")
    public List<StudentEntity> listAllStudents() {
        return studentRepository.findAll();
    }

    // Create a new student (only accessible to `ROLE_COLLEGE_ADMIN`)
    @PreAuthorize("hasAuthority('college-admin')")
    @PostMapping("/create")
    public String createStudent(@RequestBody StudentEntity student) {
        StudentEntity savedStudent = studentRepository.save(student);
        return "Student created with ID: " + savedStudent.getId();
    }

    // View student by ID (only accessible to `ROLE_STUDENT`)
    @GetMapping("/view/{id}")
    @PreAuthorize("hasRole('student')")
    public StudentEntity viewStudent(@PathVariable Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
    }

    @GetMapping( "/greet")
    public String greet(){
        return "Hello";
    }
}