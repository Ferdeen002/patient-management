package com.pm.patientservice.service;

import com.pm.patientservice.DTO.PatientDTO;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import com.pm.patientservice.service.Mapper.PatientMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private  final PatientRepository  patientRepository;

    private final PatientMapper patientMapper;


    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<PatientDTO> findAll() {
        List<Patient> patientList  = patientRepository.findAll();

        List<PatientDTO> patientDTOList = patientMapper.toDTO(patientList)

    }
}
