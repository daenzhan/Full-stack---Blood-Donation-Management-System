package org.example.bloodrequestservice;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long medcenterId;

    private String name;

    private String componentType;

    private String bloodGroup;

    private String rhesusFactor;

    private Integer volume;

    private String comments;
}
