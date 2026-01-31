package com.asish.demo.service;

import com.asish.demo.config.AppConfig;
import com.asish.demo.entity.Slot;
import com.asish.demo.repository.SlotRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SlotService {

    private final SlotRepo slotRepo;
    private final AppConfig appConfig;

    public SlotService(SlotRepo slotRepo, AppConfig appConfig) {
        this.slotRepo = slotRepo;
        this.appConfig = appConfig;
    }

    // Fetch a slot by doctor + slot number
    public Optional<Slot> getSlotByDoctorAndSlotNum(Long doctorId, Long slotNum) {
        return slotRepo.findByDoctorIdAndSlotNum(doctorId, slotNum);
    }

    // Nearest slot where NORMAL queue has capacity
    public Optional<Long> findNearestNormalSlot(Long doctorId, Long fromSlotNum) {

        int maxCapacity = appConfig.getMaxSlotCapacity();
        int priorityCapacity = appConfig.getPriorityMaxSlots();
        int maxNormalCapacity = maxCapacity - priorityCapacity;

        List<Slot> slots =
                slotRepo.findByDoctorIdAndSlotNumGreaterThanEqualOrderBySlotNumAsc(
                        doctorId, fromSlotNum
                );

        for (Slot slot : slots) {
            if (slot.getNormal().size() < maxNormalCapacity) {
                return Optional.of(slot.getSlotNum());
            }
        }

        return Optional.empty();
    }

    // Nearest slot where TOTAL (normal + priority) has capacity
    public Optional<Long> findNearestAvailableSlot(Long doctorId, Long fromSlotNum) {

        int maxCapacity = appConfig.getMaxSlotCapacity();

        List<Slot> slots =
                slotRepo.findByDoctorIdAndSlotNumGreaterThanEqualOrderBySlotNumAsc(
                        doctorId, fromSlotNum
                );

        for (Slot slot : slots) {
            int totalSize = slot.getNormal().size() + slot.getPriority().size();
            if (totalSize < maxCapacity) {
                return Optional.of(slot.getSlotNum());
            }
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<Long> autoBookNormalToken(Long doctorId, Long fromSlotNum) {

        int maxCapacity = appConfig.getMaxSlotCapacity();
        int priorityCapacity = appConfig.getPriorityMaxSlots();
        int maxNormalCapacity = maxCapacity - priorityCapacity;

        List<Slot> slots =
                slotRepo.findByDoctorIdAndSlotNumGreaterThanEqualOrderBySlotNumAsc(
                        doctorId, fromSlotNum
                );

        for (Slot slot : slots) {

            // NORMAL capacity check only
            if (slot.getNormal().size() >= maxNormalCapacity) {
                continue;
            }

            long token = generateToken(slot);
            slot.getNormal().add(String.valueOf(token));

            slotRepo.save(slot);
            return Optional.of(token);
        }

        return Optional.empty(); // no eligible slot
    }


    @Transactional
    public Optional<Long> autoBookAnyToken(Long doctorId, Long fromSlotNum) {

        int maxCapacity = appConfig.getMaxSlotCapacity();
        int priorityCapacity = appConfig.getPriorityMaxSlots();

        List<Slot> slots =
                slotRepo.findByDoctorIdAndSlotNumGreaterThanEqualOrderBySlotNumAsc(
                        doctorId, fromSlotNum
                );

        for (Slot slot : slots) {
            int totalSize = slot.getNormal().size() + slot.getPriority().size();
            if (totalSize >= maxCapacity) {
                continue;
            }

            long token = generateToken(slot);

            if (slot.getPriority().size() < priorityCapacity) {
                slot.getPriority().add(String.valueOf(token));
            } else {
                slot.getNormal().add(String.valueOf(token));
            }

            slotRepo.save(slot);
            return Optional.of(token);
        }

        return Optional.empty(); // no eligible slot
    }


    @Transactional
    public Optional<Long> bookNormalTokenForSlot(Long doctorId, Long slotNum) {

        Slot slot = slotRepo.findByDoctorIdAndSlotNum(doctorId, slotNum)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        int maxCapacity = appConfig.getMaxSlotCapacity();
        int priorityCapacity = appConfig.getPriorityMaxSlots();
        int maxNormalCapacity = maxCapacity - priorityCapacity;

        if (slot.getNormal().size() >= maxNormalCapacity) {
            return Optional.empty();
        }

        long token = generateToken(slot);
        slot.getNormal().add(String.valueOf(token));

        slotRepo.save(slot);
        return Optional.of(token);
    }

    @Transactional
    public Optional<Long> bookAnyTokenForSlot(Long doctorId, Long slotNum) {

        Slot slot = slotRepo.findByDoctorIdAndSlotNum(doctorId, slotNum)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        int maxCapacity = appConfig.getMaxSlotCapacity();
        int priorityCapacity = appConfig.getPriorityMaxSlots();

        int totalSize = slot.getNormal().size() + slot.getPriority().size();
        if (totalSize >= maxCapacity) {
            return Optional.empty();
        }

        long token = generateToken(slot);

        if (slot.getPriority().size() < priorityCapacity) {
            slot.getPriority().add(String.valueOf(token));
        } else {
            slot.getNormal().add(String.valueOf(token));
        }

        slotRepo.save(slot);
        return Optional.of(token);
    }

    @Transactional
    public boolean cancelTokenForSlot(Long doctorId, Long slotNum, Long token) {

        Slot slot = slotRepo.findByDoctorIdAndSlotNum(doctorId, slotNum)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        String tokenStr = String.valueOf(token);

        if (slot.getPriority().remove(tokenStr) || slot.getNormal().remove(tokenStr)) {
            slotRepo.save(slot);
            return true;
        }

        return false;
    }

    // 🔹 helper
    private long generateToken(Slot slot) {
        int next = slot.getNextTokenNumber();
        slot.setNextTokenNumber(next + 1);
        return slot.getSlotNum() * 100 + next;
    }
}

