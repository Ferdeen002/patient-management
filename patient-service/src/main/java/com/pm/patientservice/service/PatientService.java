package com.pm.patientservice.service;

import com.pm.patientservice.DTO.PatientRequestDTO;
import com.pm.patientservice.DTO.PatientResponseDTO;
import com.pm.patientservice.Exception.EmailAlreadyExistsException;
import com.pm.patientservice.Exception.PatientNotFoundException;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import com.pm.patientservice.service.Mapper.PatientMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private  final PatientRepository  patientRepository;

    private final PatientMapper patientMapper;


    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<PatientResponseDTO> findAll() {
        List<Patient> patientList  = patientRepository.findAll();

        return patientList.stream().map(patientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists --" +
                    patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(patientMapper.toModel(patientRequestDTO));

        return patientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID uuid , PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(uuid).orElseThrow(() ->
                new PatientNotFoundException("Patient not found with Id : " + uuid));
        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail() , uuid)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists --" +
                    patientRequestDTO.getEmail());
        }
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);
        return patientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID uuid) {
        patientRepository.deleteById(uuid);
    }
}
