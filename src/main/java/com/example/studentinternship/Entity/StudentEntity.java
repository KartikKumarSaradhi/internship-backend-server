package com.example.studentinternship.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class StudentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rollNumber;
    private String name;
    private String semester;
    private String email;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;


}