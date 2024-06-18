package com.demo.travellybe.Reservation.service;

import com.demo.travellybe.Reservation.domain.Reservation;
import com.demo.travellybe.Reservation.domain.ReservationStatus;
import com.demo.travellybe.Reservation.domain.ReservationTicket;
import com.demo.travellybe.Reservation.dto.*;
import com.demo.travellybe.Reservation.repository.ReservationRepository;
import com.demo.travellybe.exception.CustomException;
import com.demo.travellybe.exception.ErrorCode;
import com.demo.travellybe.member.domain.Member;
import com.demo.travellybe.member.domain.MemberRepository;
import com.demo.travellybe.product.domain.OperationDay;
import com.demo.travellybe.product.domain.OperationHour;
import com.demo.travellybe.product.domain.Product;
import com.demo.travellybe.product.domain.Ticket;
import com.demo.travellybe.product.repository.ProductRepository;
import com.demo.travellybe.product.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final TicketRepository ticketRepository;

    @Override
    public ReservationResponseDto createReservation(Long memberId, Long productId, ReservationCreateDto reservationCreateDto) {
        Product product = findProductById(productId);
        Member buyer = findMemberById(memberId);
        Member seller = product.getMember();

        List<Long> ticketIds = reservationCreateDto.getTicketDtos().stream()
                .map(ReservationTicketDto::getTicketId)
                .collect(Collectors.toList());
        Map<Long, Ticket> tickets = ticketRepository.findAllById(ticketIds).stream()
                .collect(Collectors.toMap(Ticket::getId, Function.identity()));

        // 상품 수량이 부족하면 PRODUCT_NOT_ENOUGH_TICKET_QUANTITY 에러 발생
        int totalPrice = getTotalPrice(reservationCreateDto, tickets);
        if (product.getQuantity() < getTotalQuantity(reservationCreateDto))
            throw new CustomException(ErrorCode.PRODUCT_NOT_ENOUGH_TICKET_QUANTITY);
        // 구매자의 포인트가 부족하면 MEMBER_NOT_ENOUGH_POINT 에러 발생
        int totalQuantity = getTotalQuantity(reservationCreateDto);
        if (buyer.getPoint() < totalPrice)
            throw new CustomException(ErrorCode.MEMBER_NOT_ENOUGH_POINT);

        Reservation reservation = Reservation.of(product, buyer, reservationCreateDto.getName(), reservationCreateDto.getPhone(),
                reservationCreateDto.getEmail(), reservationCreateDto.getDate(),
                reservationCreateDto.getStartTime(), reservationCreateDto.getEndTime(), totalPrice, totalQuantity);

        for (ReservationTicketDto ticketDto : reservationCreateDto.getTicketDtos()) {
            Ticket ticket = tickets.get(ticketDto.getTicketId());
            if (ticket == null) throw new CustomException(ErrorCode.TICKET_NOT_FOUND);

            ReservationTicket reservationTicket = ReservationTicket.of(ticket, ticketDto.getQuantity());
            reservation.addReservationTicket(reservationTicket);
            ticket.addReservationTicket(reservationTicket);

        }
        Reservation saved = reservationRepository.save(reservation);
        product.addReservation(reservation);
        buyer.addReservation(reservation);

        // 상품 수량 차감, 구매자 포인트 차감, 판매자 포인트 증가
        product.setQuantity(product.getQuantity() - reservation.getTotalTicketCount());
        buyer.setPoint(buyer.getPoint() - reservation.getTotalPrice());
        seller.setPoint(seller.getPoint() + reservation.getTotalPrice());

        return new ReservationResponseDto(saved);
    }

    @Override
    public ReservationResponseDto updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(status);
        return new ReservationResponseDto(reservation);
    }

    private int getTotalPrice(ReservationCreateDto reservationCreateDto, Map<Long, Ticket> tickets) {
        return reservationCreateDto.getTicketDtos().stream()
                .mapToInt(ticketDto -> {
                    Ticket ticket = tickets.get(ticketDto.getTicketId());
                    if (ticket == null) throw new CustomException(ErrorCode.TICKET_NOT_FOUND);
                    return ticket.getPrice() * ticketDto.getQuantity();
                })
                .sum();
    }

    private int getTotalQuantity(ReservationCreateDto reservationCreateDto) {
        return reservationCreateDto.getTicketDtos().stream()
                .mapToInt(ReservationTicketDto::getQuantity)
                .sum();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public ReservationResponseDto getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        return new ReservationResponseDto(reservation);
    }

    @Override
    public void checkProductOwner(Long productId, Long memberId) {
        Product product = findProductById(productId);
        if (product.getMember().getId().equals(memberId)) throw new CustomException(ErrorCode.RESERVATION_SELF_PRODUCT);
    }

    @Override
    public void checkOperationDateTime(Long productId, ReservationCreateDto reservationCreateDto) {
        Product product = findProductById(productId);
        // 예약 시간이 없을 경우 하루 중 최대 시간으로 설정
        if (reservationCreateDto.getStartTime() == null && reservationCreateDto.getEndTime() == null) {
            reservationCreateDto.setStartTime(LocalTime.of(0, 1));
            reservationCreateDto.setEndTime(LocalTime.of(23, 59));
        }

        Optional<OperationDay> day = product.getOperationDays().stream()
                .filter(operationDay -> operationDay.getDate().equals(reservationCreateDto.getDate()))
                .findAny();
        if (day.isEmpty()) throw new CustomException(ErrorCode.PRODUCT_NOT_AVAILABLE_OPERATION_DAY);

        List<OperationHour> hours = day.get().getOperationHours();
        if (hours.stream().noneMatch(hour -> hour.getStartTime().equals(reservationCreateDto.getStartTime())
                && hour.getEndTime().equals(reservationCreateDto.getEndTime()))) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_AVAILABLE_OPERATION_DAY);
        }
    }

    @Override
    public List<ReservationResponseDto> getReservationsByMemberId(Long memberId) {
        return reservationRepository.findByBuyerId(memberId).stream()
                .map(ReservationResponseDto::new)
                .toList();
    }

    @Override
    public MyReservationResponseDto getReservationsByProductId(Long memberId, Long productId) {

        // 상품 검색
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 예약 검색
        List<Reservation> reservations = reservationRepository.findByBuyerId(memberId);

        return new MyReservationResponseDto(product, reservations);
    }

    @Override
    public List<PendingReservationsPerProductDto> getProductsByMemberId(Long sellerId) {
        List<Product> productList = productRepository.findAllByMemberId(sellerId);

        List<PendingReservationsPerProductDto> responseList = new ArrayList<>();
        for (Product product : productList) {
            List<Reservation> reservationList = product.getReservations();
            long pendingReservationCount = reservationList.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                    .count();

            PendingReservationsPerProductDto dto = PendingReservationsPerProductDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(reservationList.getFirst().getTotalPrice())
                    .date(reservationList.getFirst().getDate())
                    .startTime(reservationList.getFirst().getStartTime())
                    .endTime(reservationList.getFirst().getEndTime())
                    .reservationCount(reservationList.size())
                    .pendingReservationCount((int) pendingReservationCount)
                    .build();
            responseList.add(dto);
        }
        return responseList;
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(ReservationStatus.CANCELED);

        refund(reservation);
    }

    @Override
    public void rejectReservation(Long id, String rejectReason) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setRejectionReason(rejectReason);

        refund(reservation);
    }

    // 환불 처리
    private void refund(Reservation reservation) {
        Member buyer = reservation.getBuyer();
        Product product = reservation.getProduct();
        Member seller = product.getMember();

        buyer.setPoint(buyer.getPoint() + reservation.getTotalPrice());
        seller.setPoint(seller.getPoint() - reservation.getTotalPrice());
        product.setQuantity(product.getQuantity() + reservation.getTotalTicketCount());
    }
}
