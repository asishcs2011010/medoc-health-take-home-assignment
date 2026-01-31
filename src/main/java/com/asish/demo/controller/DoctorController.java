package com.asish.demo.controller;

import com.asish.demo.entity.Doctor;
import com.asish.demo.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // GET /api/doctors
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    // GET /api/doctors/{doctorId}
    @GetMapping("/{doctorId}")
    public ResponseEntity<Doctor> getDoctorById(
            @PathVariable Long doctorId) {

        return doctorService.getDoctorById(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
