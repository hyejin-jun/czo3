package org.mbc.czo.function.Salary.DTO;


import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDTO {

    private Integer sid;
    private String sname;
    private String sposition;
    private int sbasic;
    private int sbonus;
    private int stax;
    private int salary;
    private int month;     // ✅ 월 추가
    private Boolean active;


}