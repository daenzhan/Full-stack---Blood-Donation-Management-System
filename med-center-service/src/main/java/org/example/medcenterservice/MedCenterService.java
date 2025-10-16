package org.example.medcenterservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedCenterService {
    private final MedCenterRepository medCenterRepository;
    private final RestTemplate restTemplate;

    @Value("${yandex.maps.api.key:92fb74a1-4389-4f41-8801-8e9d93e4548c}")
    private String yandexApiKey;

    public MedCenterService(MedCenterRepository medCenterRepository) {
        this.medCenterRepository = medCenterRepository;
        this.restTemplate = new RestTemplate();
    }

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ
    public MedCenter save(MedCenter m_c) {
        // Автоматическое геокодирование при сохранении
        if (m_c.getLatitude() == null && m_c.getLocation() != null) {
            Map<String, Double> coordinates = geocodeAddress(m_c.getLocation());
            if (coordinates != null) {
                m_c.setLatitude(coordinates.get("latitude"));
                m_c.setLongitude(coordinates.get("longitude"));
            }
        }
        return medCenterRepository.save(m_c);
    }

    public List<MedCenter> get_all() {
        return medCenterRepository.findAll();
    }

    public MedCenter get_by_id(Long med_center_id) {
        return medCenterRepository.findById(med_center_id).orElse(null);
    }

    public MedCenter update(Long med_center_id, MedCenter new_m_c) {
        MedCenter m_c = medCenterRepository.findById(med_center_id).orElse(null);
        if (m_c != null) {
            m_c.setName(new_m_c.getName());
            m_c.setLocation(new_m_c.getLocation());
            m_c.setPhone(new_m_c.getPhone());
            m_c.setSpecialization(new_m_c.getSpecialization());
            m_c.setUser_id(new_m_c.getUser_id());
            m_c.setLicense_file(new_m_c.getLicense_file());

            // Обновляем координаты если адрес изменился
            if (!m_c.getLocation().equals(new_m_c.getLocation())) {
                Map<String, Double> coordinates = geocodeAddress(new_m_c.getLocation());
                if (coordinates != null) {
                    m_c.setLatitude(coordinates.get("latitude"));
                    m_c.setLongitude(coordinates.get("longitude"));
                }
            }

            return medCenterRepository.save(m_c);
        }
        return null;
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

        // Автоматическое геокодирование
        Map<String, Double> coordinates = geocodeAddress(location);

        MedCenter profile = new MedCenter();
        profile.setUser_id(userId);
        profile.setName(name);
        profile.setLocation(location);
        profile.setPhone(phone);
        profile.setSpecialization(specialization);
        profile.setDirectorName(directorName);
        profile.setEmail(email);
        profile.setLicense_file(licenseFilePath);

        if (coordinates != null) {
            profile.setLatitude(coordinates.get("latitude"));
            profile.setLongitude(coordinates.get("longitude"));
        }

        return medCenterRepository.save(profile);
    }

    public MedCenter getProfileByUserId(Long userId) {
        return medCenterRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Medical center profile not found"));
    }

    public boolean existsByUserId(Long userId) {
        return medCenterRepository.existsByUserId(userId);
    }

    // МЕТОДЫ ДЛЯ YANDEX MAPS
    public Map<String, Double> geocodeAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://geocode-maps.yandex.ru/1.x/?format=json&apikey=" +
                    yandexApiKey + "&geocode=" + encodedAddress;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = response.getBody();

            Map<String, Object> responseMap = (Map<String, Object>) responseBody.get("response");
            Map<String, Object> geoObjectCollection = (Map<String, Object>) responseMap.get("GeoObjectCollection");
            List<Map<String, Object>> featureMember = (List<Map<String, Object>>) geoObjectCollection.get("featureMember");

            if (featureMember != null && !featureMember.isEmpty()) {
                Map<String, Object> firstFeature = featureMember.get(0);
                Map<String, Object> geoObject = (Map<String, Object>) firstFeature.get("GeoObject");
                Map<String, Object> point = (Map<String, Object>) geoObject.get("Point");
                String pos = (String) point.get("pos");

                String[] coords = pos.split(" ");
                double longitude = Double.parseDouble(coords[0]);
                double latitude = Double.parseDouble(coords[1]);

                Map<String, Double> coordinates = new HashMap<>();
                coordinates.put("latitude", latitude);
                coordinates.put("longitude", longitude);

                return coordinates;
            }
        } catch (Exception e) {
            System.err.println("Geocoding error for address: " + address + " - " + e.getMessage());
        }
        return null;
    }

    public List<MedCenter> findNearbyCenters(Double userLat, Double userLon, Double radiusKm) {
        List<MedCenter> allCenters = medCenterRepository.findAll();

        return allCenters.stream()
                .filter(center -> center.getLatitude() != null && center.getLongitude() != null)
                .map(center -> {
                    double distance = calculateDistance(userLat, userLon,
                            center.getLatitude(), center.getLongitude());
                    center.setDistance(distance);
                    return center;
                })
                .filter(center -> center.getDistance() <= radiusKm)
                .sorted(Comparator.comparingDouble(MedCenter::getDistance))
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public MedCenter updateCenterCoordinates(Long centerId) {
        MedCenter center = get_by_id(centerId);
        if (center != null) {
            Map<String, Double> coordinates = geocodeAddress(center.getLocation());
            if (coordinates != null) {
                center.setLatitude(coordinates.get("latitude"));
                center.setLongitude(coordinates.get("longitude"));
                return medCenterRepository.save(center);
            }
        }
        return null;
    }

    public List<MedCenter> getAllCentersWithCoordinates() {
        return medCenterRepository.findAll().stream()
                .filter(center -> center.getLatitude() != null && center.getLongitude() != null)
                .collect(Collectors.toList());
    }
}