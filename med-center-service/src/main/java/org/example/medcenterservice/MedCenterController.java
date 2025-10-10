package org.example.medcenterservice;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medcenters")
public class MedCenterController {
    private final MedCenterService medCenterService;

    public MedCenterController(MedCenterService medCenterService) {
        this.medCenterService = medCenterService;
    }

    @PostMapping
    public MedCenter create_med_center(@RequestBody MedCenter med_center) {
        return medCenterService.create(med_center);
    }

    @GetMapping
    public List<MedCenter> get_all_med_centers() {
        return medCenterService.get_all();
    }

    @GetMapping("/{med_center_id}")
    public MedCenter get_med_center_by_id(@PathVariable Long med_center_id) {
        return medCenterService.get_by_id(med_center_id);
    }

    @PutMapping("/{med_center_id}")
    public MedCenter update_med_center(@PathVariable Long med_center_id, @RequestBody MedCenter m_c) {
        return medCenterService.update(med_center_id, m_c);
    }

    @DeleteMapping("/{med_center_id}")
    public void delete_med_center(@PathVariable Long med_center_id) {
        medCenterService.delete(med_center_id);
    }
}