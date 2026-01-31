package com.asish.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "slots")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long slotNum;

    @Column(nullable = false)
    private Integer nextTokenNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ElementCollection
    @CollectionTable(
            name = "slot_normal_queue",
            joinColumns = @JoinColumn(name = "slot_id")
    )
    @Column(name = "token")
    private List<String> normal = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "slot_priority_queue",
            joinColumns = @JoinColumn(name = "slot_id")
    )
    @Column(name = "token")
    private List<String> priority = new ArrayList<>();
}
