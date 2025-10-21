package org.mbc.czo.function.Salary.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sid;

    private String sname;
    private String sposition;
    private int sbasic;
    private int sbonus;
    private int stax;
    private int salary;

    // ✅ 월별 구분 (1~12)
    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private Boolean active = true;

}