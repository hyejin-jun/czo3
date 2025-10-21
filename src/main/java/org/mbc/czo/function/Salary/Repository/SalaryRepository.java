package org.mbc.czo.function.Salary.Repository;

import org.mbc.czo.function.Salary.Entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryRepository extends JpaRepository<Salary, Integer> {


    List<Salary> findByMonth(int month);
}
