package com.demo.travellybe.product.domain;

import com.demo.travellybe.product.dto.OperationDayDto;
import com.demo.travellybe.product.dto.OperationDayHourDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
public class OperationDay {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_day_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "operationDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationHour> operationHours = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate date;

    public static OperationDay of(OperationDayDto operationDayDto, Product product) {
        OperationDay operationDay = new OperationDay();
        operationDay.date = operationDayDto.getDate();
        operationDay.product = product;

        if (operationDayDto.getOperationDayHours() == null) {
            OperationDayHourDto operationDayHourDto = OperationDayHourDto.builder()
                    .startTime(LocalTime.of(0, 1))
                    .endTime(LocalTime.of(23, 59))
                    .build();
            operationDay.operationHours.add(OperationHour.of(operationDayHourDto, operationDay));
        } else {
            operationDay.operationHours = operationDayDto.getOperationDayHours().stream().map(operationDayHourDto ->
                    OperationHour.of(operationDayHourDto, operationDay))
                    .collect(Collectors.toList());
        }
        return operationDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationDay that = (OperationDay) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}