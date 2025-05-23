package com.example.dto.io;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class SectorIO implements Serializable {

    /** */
    private static final long serialVersionUID = 1495674084604184012L;

    private String code;

    private String sectorName;
}
