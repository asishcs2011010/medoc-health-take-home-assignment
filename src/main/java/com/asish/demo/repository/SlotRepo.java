package com.asish.demo.repository;

import com.asish.demo.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlotRepo extends JpaRepository<Slot, Long> {

    // Get a specific slot for a doctor
    Optional<Slot> findByDoctorIdAndSlotNum(Long doctorId, Long slotNum);

    // Get slots for a doctor starting from a slot number (ordered)
    List<Slot> findByDoctorIdAndSlotNumGreaterThanEqualOrderBySlotNumAsc(
            Long doctorId,
            Long slotNum
    );
}
