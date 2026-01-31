package com.asish.demo.controller;

import com.asish.demo.entity.Slot;
import com.asish.demo.service.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/doctors/{doctorId}/slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    // GET /api/doctors/{doctorId}/slots/{slotNum}
    @GetMapping("/{slotNum}")
    public ResponseEntity<Slot> getSlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotNum) {

        return slotService.getSlotByDoctorAndSlotNum(doctorId, slotNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/doctors/{doctorId}/slots/nearest/normal?from=1
    @GetMapping("/nearest/normal")
    public ResponseEntity<Long> getNearestNormalSlot(
            @PathVariable Long doctorId,
            @RequestParam("from") Long fromSlotNum) {

        return slotService.findNearestNormalSlot(doctorId, fromSlotNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/doctors/{doctorId}/slots/nearest/any?from=1
    @GetMapping("/nearest/any")
    public ResponseEntity<Long> getNearestAvailableSlot(
            @PathVariable Long doctorId,
            @RequestParam("from") Long fromSlotNum) {

        return slotService.findNearestAvailableSlot(doctorId, fromSlotNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/doctors/{doctorId}/tokens/normal/auto?from=1
    @PostMapping("/tokens/normal/auto")
    public ResponseEntity<Long> autoBookNormalToken(
            @PathVariable Long doctorId,
            @RequestParam("from") Long fromSlotNum) {

        return slotService.autoBookNormalToken(doctorId, fromSlotNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(409).build());
    }


    // POST /api/doctors/{doctorId}/tokens/any/auto?from=1
    @PostMapping("/tokens/any/auto")
    public ResponseEntity<Long> autoBookAnyToken(
            @PathVariable Long doctorId,
            @RequestParam("from") Long fromSlotNum) {

        return slotService.autoBookAnyToken(doctorId, fromSlotNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(409).build());
    }


    // POST /api/doctors/{doctorId}/slots/{slotNum}/tokens/normal
    @PostMapping("/{slotNum}/tokens/normal")
    public ResponseEntity<Long> bookNormalTokenForSlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotNum) {

        return slotService.bookNormalTokenForSlot(doctorId, slotNum)
                .map(token -> ResponseEntity.ok(token))
                .orElse(ResponseEntity.status(409).build());
    }

    // POST /api/doctors/{doctorId}/slots/{slotNum}/tokens/any
    @PostMapping("/{slotNum}/tokens/any")
    public ResponseEntity<Long> bookAnyTokenForSlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotNum) {

        return slotService.bookAnyTokenForSlot(doctorId, slotNum)
                .map(token -> ResponseEntity.ok(token))
                .orElse(ResponseEntity.status(409).build());
    }

    // DELETE /api/doctors/{doctorId}/slots/{slotNum}/tokens/{token}
    @DeleteMapping("/{slotNum}/tokens/{token}")
    public ResponseEntity<Void> cancelToken(
            @PathVariable Long doctorId,
            @PathVariable Long slotNum,
            @PathVariable Long token) {

        boolean cancelled =
                slotService.cancelTokenForSlot(doctorId, slotNum, token);

        return cancelled
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

