package com.demo.travellybe.product.dto;

import com.demo.travellybe.product.domain.OperationDay;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OperationDayDto {
    @NotNull
    @Schema(description = "운영 날짜", examples = "2024-05-29")
    private LocalDate date;

    private List<OperationDayHourDto> operationDayHours = new ArrayList<>();

    public OperationDayDto(OperationDay operationDay) {
        this.date = operationDay.getDate();

        this.operationDayHours = operationDay.getOperationHours().stream().map(OperationDayHourDto::new).toList();
    }

    @Builder
    public OperationDayDto(LocalDate date, List<OperationDayHourDto> operationDayHours) {
        this.date = date;
        this.operationDayHours = operationDayHours;
    }
}