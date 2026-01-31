package com.asish.demo.service;

import com.asish.demo.entity.Doctor;
import com.asish.demo.repository.DoctorRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepo doctorRepo;

    public DoctorService(DoctorRepo doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(Long doctorId) {
        return doctorRepo.findById(doctorId);
    }
}
