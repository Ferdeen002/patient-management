package com.pm.patientservice.service;

import billing.BillingResponse;
import com.pm.patientservice.DTO.PatientRequestDTO;
import com.pm.patientservice.DTO.PatientResponseDTO;
import com.pm.patientservice.Exception.EmailAlreadyExistsException;
import com.pm.patientservice.Exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import com.pm.patientservice.service.Mapper.PatientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private  final PatientRepository  patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final PatientMapper patientMapper;
    private final KafkaProducer kafkaProducer;


    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, PatientMapper patientMapper, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.patientMapper = patientMapper;
        this.kafkaProducer = kafkaProducer;
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
        BillingResponse billingAccount = billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
        kafkaProducer.sendMessage(newPatient);
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
