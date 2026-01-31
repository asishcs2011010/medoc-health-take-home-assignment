# OPD Token Allocation Engine (Spring Boot)

## Overview

This project implements a backend service for **OPD token allocation** in a hospital setting.  
Doctors operate in **fixed time slots**, and each slot has a limited capacity. Tokens are issued from different sources such as online booking, walk-ins, paid priority patients, follow-ups, and emergencies.

The system focuses on:

- Enforcing strict per-slot capacity limits
- Handling priority vs normal patients
- Supporting elastic allocation by moving load to future slots
- Managing real-world scenarios like cancellations and emergency arrivals

The project is built using **Java + Spring Boot + JPA**.

---

## Key Design Decisions

### Slot-based Scheduling

- Each doctor has multiple numbered slots (e.g., Slot 1 = 9–10 AM)
- Each slot has:
    - A hard maximum capacity
    - A reserved priority capacity
    - Remaining capacity for normal patients

All limits are configurable via application properties.

---

### Token Source Handling & Prioritization

Although tokens come from different sources, the system internally uses **two queues**:

| Token Source     | Internal Queue |
|------------------|----------------|
| Emergency        | Priority       |
| Paid priority    | Priority       |
| Follow-up        | Priority       |
| Walk-in          | Normal         |
| Online booking   | Normal         |

This approach keeps the system simple while still respecting patient urgency.

---

## Token Allocation Algorithm

### Capacity Rules

- Total tokens in a slot never exceed the hard capacity
- Priority tokens are limited by a reserved quota
- Normal tokens use remaining capacity

---

### Normal Token Allocation

Used for online bookings and walk-ins.

**Logic:**

1. Start from the requested slot
2. Scan future slots in increasing order
3. Allocate to the first slot with available normal capacity
4. If no slot is available, return failure

This allows natural spillover to future slots.

---

### Priority / Emergency Token Allocation

Used for paid, follow-up, and emergency patients.

**Logic:**

1. Search for the nearest slot with available total capacity
2. Allocate to priority queue if quota allows
3. Otherwise allocate to normal queue
4. Hard capacity is never exceeded

**Policy note:**  
Emergency patients bypass normal quota but do **not** bypass hard slot limits.

---

## Handling Real-World Scenarios

### Cancellations

- Tokens can be cancelled from either queue
- Cancellation immediately frees capacity
- No automatic waitlist promotion is done

---

### No-shows

- Treated the same as cancellations

---

### Delays

- Handled indirectly by shifting new bookings to future slots
- Existing tokens are not rescheduled

---

### Emergencies

- Admitted as priority tokens when capacity allows
- Redirected to future slots if the current slot is full

---

## Failure Handling

| Scenario                     | Behavior                      |
|------------------------------|-------------------------------|
| Slot not found               | Error returned                |
| Slot capacity full           | 409 Conflict                  |
| No future slot available     | Allocation fails              |
| Concurrent bookings          | Handled using transactions    |

All write operations are **transactional** to ensure consistency.

---

## API Design

### Doctor APIs

- `GET /api/doctors`
- `GET /api/doctors/{doctorId}`

---

### Slot APIs

- `GET /api/doctors/{doctorId}/slots/{slotNum}`
- `GET /api/doctors/{doctorId}/slots/nearest/normal`
- `GET /api/doctors/{doctorId}/slots/nearest/any`

---

### Token APIs

- `POST /api/doctors/{doctorId}/tokens/normal/auto`
- `POST /api/doctors/{doctorId}/tokens/any/auto`
- `POST /api/doctors/{doctorId}/slots/{slotNum}/tokens/normal`
- `POST /api/doctors/{doctorId}/slots/{slotNum}/tokens/any`
- `DELETE /api/doctors/{doctorId}/slots/{slotNum}/tokens/{token}`

---

## Data Model

### Slot Entity

- Slot number
- Doctor reference
- Next token number
- Normal token list
- Priority token list

Queues are stored using `@ElementCollection` to keep the model simple and efficient.

---

## OPD Day Simulation (Example)

### Setup

- 3 doctors
- Multiple slots per doctor
- Fixed slot capacity with reserved priority quota

---

### Typical Day

- Online bookings fill normal slots
- Walk-ins spill into later slots
- Paid and follow-up patients use priority quota
- Emergency patients are accommodated within capacity
- Cancellations free up slots dynamically

This demonstrates elastic behavior without overbooking.

---

## Trade-offs & Limitations

- Token sources are grouped into priority and normal
- No automatic waitlist promotion
- No overbooking beyond hard capacity
- No rescheduling of existing tokens

These trade-offs were made to keep the system **clear, safe, and easy to reason about**.

---

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- H2 / MySQL (configurable)

---

## How to Run the Project

```bash
git clone <your-repo-url>
cd opd-token-allocation
mvn clean install
mvn spring-boot:run
