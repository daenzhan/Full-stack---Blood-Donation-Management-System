package org.example.medcenterservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MedCenterService {
    private final MedCenterRepository medCenterRepository;
    public MedCenterService(MedCenterRepository medCenterRepository) {
        this.medCenterRepository = medCenterRepository;
    }

    public MedCenter save(MedCenter m_c) {
        return medCenterRepository.save(m_c);
    }

    public List<MedCenter> get_all() {
        return medCenterRepository.findAll();
    }

    public MedCenter get_by_id(Long med_center_id) {
        return medCenterRepository.getById(med_center_id);
    }

    public MedCenter update(Long med_center_id, MedCenter new_m_c) {
        MedCenter m_c = medCenterRepository.getById(med_center_id);
        m_c.setName(new_m_c.getName());
        m_c.setLocation(new_m_c.getLocation());
        m_c.setPhone(new_m_c.getPhone());
        m_c.setSpecialization(new_m_c.getSpecialization());
        m_c.setUser_id(new_m_c.getUser_id());
        m_c.setLicense_file(new_m_c.getLicense_file());
        return medCenterRepository.save(m_c);
    }

    public void delete(Long id) {
        medCenterRepository.deleteById(id);
    }

    public List<MedCenter> searchByName(String name) {
        return medCenterRepository.findByNameContainingIgnoreCase(name);
    }

    public MedCenter completeProfile(Long userId, String name, String location, String phone,
                                     String specialization, String directorName, String email,
                                     String licenseFilePath) {
        if (medCenterRepository.existsByUserId(userId)) {
            throw new RuntimeException("Medical center profile already exists");
        }

        MedCenter profile = new MedCenter();
        profile.setUser_id(userId);
        profile.setName(name);
        profile.setLocation(location);
        profile.setPhone(phone);
        profile.setSpecialization(specialization);
        profile.setDirectorName(directorName);
        profile.setEmail(email);
        profile.setLicense_file(licenseFilePath);

        return medCenterRepository.save(profile);
    }

    public MedCenter getProfileByUserId(Long userId) {
        return medCenterRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Medical center profile not found"));
    }

    public boolean existsByUserId(Long userId) {
        return medCenterRepository.existsByUserId(userId);
    }
}