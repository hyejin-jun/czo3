package org.mbc.czo.function.Salary.Service;


import org.mbc.czo.function.Salary.DTO.SalaryDTO;
import org.mbc.czo.function.Salary.Entity.Salary;
import org.mbc.czo.function.Salary.Repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SalaryService {

    private final SalaryRepository repository;

    public SalaryService(SalaryRepository repository) {
        this.repository = repository;
    }

    public List<Salary> saveAll(List<Salary> salaries) {
        return repository.saveAll(salaries);
    }

    public List<Salary> findAll() {
        return repository.findAll();
    }
}

