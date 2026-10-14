package edu.dosw.parcial.controller.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private long price;
    private boolean active;
}