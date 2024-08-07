package com.demo.travellybe.Reservation.domain;

import com.demo.travellybe.member.domain.Member;
import com.demo.travellybe.product.domain.Product;
import com.demo.travellybe.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member buyer;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationTicket> reservationTickets = new ArrayList<>();

    // 예약자명(구매자와 다를 수 있음)
    @Column(nullable = false)
    private String name;

    // 예약자 연락처(구매자와 다를 수 있음)
    @Column(nullable = false)
    private String phone;

    // 예약자 이메일(구매자와 다를 수 있음)
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    // 예약 거절 사유 (REJECTED 상태일 때만 저장)
    @Column(length = 500)
    private String rejectionReason;

    private int totalPrice;
    private int totalTicketCount;

    public static Reservation of(Product product, Member buyer, String name, String phone, String email,
                                 LocalDate date, int totalPrice, int totalTicketCount) {
        Reservation reservation = new Reservation();
        reservation.product = product;
        reservation.buyer = buyer;
        reservation.name = name;
        reservation.phone = phone;
        reservation.email = email;
        reservation.date = date;
        reservation.totalPrice = totalPrice;
        reservation.totalTicketCount = totalTicketCount;
        return reservation;
    }

    public void addReservationTicket(ReservationTicket reservationTicket) {
        reservationTicket.setReservation(this);
    }

    public void setBuyer(Member buyer) {
        this.buyer = buyer;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
