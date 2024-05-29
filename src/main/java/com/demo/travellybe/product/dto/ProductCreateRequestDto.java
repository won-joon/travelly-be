package com.demo.travellybe.product.dto;

import com.demo.travellybe.product.domain.TicketPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductCreateRequestDto {
    @NotNull
    @Schema(description = "상품명", example = "여행 상품 A")
    private String name;

    @NotNull
    @Schema(description = "가격", example = "7000")
    private int price;

    @NotNull
    @Schema(description = "12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 38:쇼핑, 39:음식점", example = "25")
    private String type;

    @NotNull
    @Schema(description = "상품 설명", example = "여행 상품 A는 여행 상품 중 가장 인기 있는 상품입니다.")
    private String description;

    @Schema(description = "상품 이미지 URL", example = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png")
    private String imageUrl;

    @NotNull
    @Schema(description = "주소", example = "서울특별시 강남구 역삼동")
    private String address;
    
    @Schema(description = "상세 주소", example = "강남빌딩 1층")
    private String detailAddress;

    @NotNull
    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "홈페이지", example = "https://www.google.com")
    private String homepage;

    @NotNull
    @Schema(description = "1:서울, 2:인천, 3:대전, 4:대구, 5:광주, 6:부산, 7:울산, 8:세종, " +
            "9:경기, 10:강원, 11:충북, 12:충남, 13:경북, 14:경남, 15:전북, 16:전남, " +
            "17:제주", example = "11")
    private String cityCode;

    @NotNull
    @Schema(description = "상품 수량", example = "100")
    private int ticketCount;

    @Schema(description = "연령별 티켓 가격")
    private TicketPrice ticketPrice;

    @NotNull
    @Schema(description = "운영 요일 및 시간")
    private List<OperationDayDto> operationDays;
}