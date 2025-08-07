package com.pm.patientservice.Controller;

import com.pm.patientservice.DTO.PatientRequestDTO;
import com.pm.patientservice.DTO.PatientResponseDTO;
import com.pm.patientservice.DTO.Validators.CreatePatientValidationGroup;
import com.pm.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient" , description = "API for managing Patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get Patients.")
    public ResponseEntity<List<PatientResponseDTO>> toDTO() {
        return ResponseEntity.ok().body(patientService.findAll());
    }

    @PostMapping
    @Operation(summary = "Add a new Patient.")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class , CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        return ResponseEntity.ok().body(patientService.createPatient(patientRequestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a patient record.")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
                                                            @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient record.")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
