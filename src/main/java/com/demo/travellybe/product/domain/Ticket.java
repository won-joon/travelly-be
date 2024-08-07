package com.demo.travellybe.product.domain;

import com.demo.travellybe.Reservation.domain.ReservationTicket;
import com.demo.travellybe.product.dto.TicketDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationTicket> reservationTickets = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return price == ticket.price &&
                Objects.equals(name, ticket.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }


    public static Ticket of(TicketDto ticketDto, Product product) {
        Ticket ticket = new Ticket();
        ticket.product = product;
        ticket.name = ticketDto.getName();
        ticket.price = ticketDto.getPrice();

        return ticket;
    }

    public void addReservationTicket(ReservationTicket reservationTicket) {
        reservationTicket.setTicket(this);
    }
}
