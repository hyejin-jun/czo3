package org.mbc.czo.function.Salary.Controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.Salary.DTO.SalaryDTO;
import org.mbc.czo.function.Salary.Entity.Salary;
import org.mbc.czo.function.Salary.Repository.SalaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/salaries")
@CrossOrigin(origins = "http://192.168.0.183:3000")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryRepository salaryRepository;

    // 전체 조회
    @GetMapping
    public List<Salary> getAll() {
        return salaryRepository.findAll();
    }

    // 단일 추가
    @PostMapping
    public ResponseEntity<?> addSalary(@RequestBody SalaryDTO dto) {
        try {
            Salary entity = Salary.builder()
                    .sname(dto.getSname())
                    .sposition(dto.getSposition())
                    .sbasic(dto.getSbasic())
                    .sbonus(dto.getSbonus())
                    .stax(dto.getStax())
                    .salary(dto.getSbasic() + dto.getSbonus() - dto.getStax())
                    .month(dto.getMonth())   // ✅ 월 저장
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();

            Salary saved = salaryRepository.save(entity);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("추가 실패: " + e.getMessage());
        }
    }


    // 단일 수정
    @PutMapping("/{sid}")
    public ResponseEntity<?> update(@PathVariable Integer sid, @RequestBody SalaryDTO dto) {
        Salary entity = salaryRepository.findById(sid)
                .orElseThrow(() -> new RuntimeException("해당 sid가 없습니다: " + sid));

        entity.setSname(dto.getSname());
        entity.setSposition(dto.getSposition());
        entity.setSbasic(dto.getSbasic());
        entity.setSbonus(dto.getSbonus());
        entity.setStax(dto.getStax());
        entity.setSalary(dto.getSbasic() + dto.getSbonus() - dto.getStax()); // ✅ 항상 재계산
        entity.setMonth(dto.getMonth()); // ✅ 월 반영
        entity.setActive(dto.getActive() != null ? dto.getActive() : entity.getActive());

        salaryRepository.save(entity);
        return ResponseEntity.ok(entity);
    }

    // 단일 삭제
    @DeleteMapping("/{sid}")
    public ResponseEntity<?> delete(@PathVariable Integer sid) {
        if (!salaryRepository.existsById(sid)) {
            return ResponseEntity.badRequest().body("삭제 실패: 해당 sid가 없습니다: " + sid);
        }
        salaryRepository.deleteById(sid);
        return ResponseEntity.ok("삭제 완료");
    }

    // 월별 조회
    @GetMapping("/month/{month}")
    public List<Salary> getSalariesByMonth(@PathVariable int month) {
        return salaryRepository.findByMonth(month);
    }


}