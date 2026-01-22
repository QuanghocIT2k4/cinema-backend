package com.cinema.service;

import com.cinema.model.dto.request.BookingRequest;
import com.cinema.model.dto.response.BookingResponse;
import com.cinema.model.entity.*;
import com.cinema.model.enums.BookingStatus;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.*;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý logic Booking (đặt vé, list, chi tiết, confirm, cancel)
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingRefreshmentRepository bookingRefreshmentRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final RefreshmentRepository refreshmentRepository;
    private final UserRepository userRepository;

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Không xác định được user hiện tại");
        }
        return userDetails;
    }

    private void checkAdminRole() {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails.getUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Chỉ Admin mới có quyền thực hiện thao tác này");
        }
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        Long userId = currentUser.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id: " + userId));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime không tồn tại với id: " + request.getShowtimeId()));

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể đặt vé cho suất chiếu đã qua");
        }

        // Load seats
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new RuntimeException("Một hoặc nhiều ghế không tồn tại");
        }

        // Kiểm tra ghế có thuộc cùng phòng với showtime không
        Long showtimeRoomId = showtime.getRoom().getId();
        boolean invalidSeatRoom = seats.stream()
                .anyMatch(seat -> !seat.getRoom().getId().equals(showtimeRoomId));
        if (invalidSeatRoom) {
            throw new RuntimeException("Ghế không thuộc phòng chiếu của suất chiếu này");
        }

        // Kiểm tra ghế đã được đặt chưa
        List<Long> seatIds = seats.stream().map(Seat::getId).toList();
        List<Ticket> existingTickets = ticketRepository.findBySeatIdInAndBooking_Showtime_IdAndBooking_StatusNot(
                seatIds, showtime.getId(), BookingStatus.CANCELLED);
        if (!existingTickets.isEmpty()) {
            String occupied = existingTickets.stream()
                    .map(t -> t.getSeat().getSeatNumber())
                    .distinct()
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Các ghế đã được đặt: " + occupied);
        }

        // Tính tổng tiền vé
        BigDecimal ticketPrice = showtime.getPrice();
        BigDecimal totalTicketPrice = ticketPrice.multiply(BigDecimal.valueOf(seats.size()));

        // Xử lý refreshments (nếu có)
        List<BookingRequest.RefreshmentOrder> refreshmentOrders =
                request.getRefreshments() != null ? request.getRefreshments() : List.of();

        List<Refreshment> refreshments = new ArrayList<>();
        if (!refreshmentOrders.isEmpty()) {
            List<Long> refreshmentIds = refreshmentOrders.stream()
                    .map(BookingRequest.RefreshmentOrder::getRefreshmentId)
                    .toList();
            refreshments = refreshmentRepository.findAllById(refreshmentIds);
            if (refreshments.size() != refreshmentIds.size()) {
                throw new RuntimeException("Một hoặc nhiều đồ ăn/đồ uống không tồn tại");
            }
            boolean hasInactive = refreshments.stream().anyMatch(r -> !Boolean.TRUE.equals(r.getIsCurrent()));
            if (hasInactive) {
                throw new RuntimeException("Có đồ ăn/đồ uống không còn bán");
            }
        }

        BigDecimal totalRefreshmentPrice = BigDecimal.ZERO;
        List<BookingRefreshment> bookingRefreshments = new ArrayList<>();
        for (BookingRequest.RefreshmentOrder order : refreshmentOrders) {
            Refreshment refreshment = refreshments.stream()
                    .filter(r -> r.getId().equals(order.getRefreshmentId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Refreshment không tồn tại với id: " + order.getRefreshmentId()));
            BigDecimal itemTotal = refreshment.getPrice()
                    .multiply(BigDecimal.valueOf(order.getQuantity()));
            totalRefreshmentPrice = totalRefreshmentPrice.add(itemTotal);

            BookingRefreshment br = new BookingRefreshment();
            br.setRefreshment(refreshment);
            br.setQuantity(order.getQuantity());
            br.setTotalPrice(itemTotal);
            bookingRefreshments.add(br);
        }

        BigDecimal totalPrice = totalTicketPrice.add(totalRefreshmentPrice);

        // Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingCode(generateBookingCode());

        Booking savedBooking = bookingRepository.save(booking);

        // Tạo tickets
        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(savedBooking);
            ticket.setSeat(seat);
            ticket.setPrice(ticketPrice);
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

        // Gắn booking vào bookingRefreshments và lưu
        for (BookingRefreshment br : bookingRefreshments) {
            br.setBooking(savedBooking);
        }
        bookingRefreshmentRepository.saveAll(bookingRefreshments);

        savedBooking.setTickets(tickets);
        savedBooking.setBookingRefreshments(bookingRefreshments);

        return convertToResponse(savedBooking);
    }

    public Page<BookingResponse> getBookings(BookingStatus status, int page, int size) {
        CustomUserDetails currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        if (currentUser.getUser().getRole() == UserRole.ADMIN) {
            Page<Booking> bookingPage;
            if (status != null) {
                bookingPage = bookingRepository.findByStatus(status, pageable);
            } else {
                bookingPage = bookingRepository.findAll(pageable);
            }
            return bookingPage.map(this::convertToResponse);
        } else {
            Long userId = currentUser.getUser().getId();
            Page<Booking> bookingPage;
            if (status != null) {
                bookingPage = bookingRepository.findByUserIdAndStatus(userId, status, pageable);
            } else {
                bookingPage = bookingRepository.findByUserId(userId, pageable);
            }
            return bookingPage.map(this::convertToResponse);
        }
    }

    public BookingResponse getBookingById(Long id) {
        CustomUserDetails currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với id: " + id));

        boolean isAdmin = currentUser.getUser().getRole() == UserRole.ADMIN;
        boolean isOwner = booking.getUser().getId().equals(currentUser.getUser().getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền xem booking này");
        }

        return convertToResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long id) {
        checkAdminRole();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với id: " + id));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Booking đã được thanh toán");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking đã bị hủy");
        }

        booking.setStatus(BookingStatus.PAID);
        booking.setPaymentTime(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        CustomUserDetails currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với id: " + id));

        boolean isAdmin = currentUser.getUser().getRole() == UserRole.ADMIN;
        boolean isOwner = booking.getUser().getId().equals(currentUser.getUser().getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền hủy booking này");
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Không thể hủy booking đã thanh toán");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking đã bị hủy");
        }

        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể hủy booking đã qua thời gian chiếu");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    public List<BookingResponse.TicketSummary> getBookedSeatsForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime không tồn tại với id: " + showtimeId));

        List<Ticket> tickets = ticketRepository
                .findByBooking_Showtime_IdAndBooking_StatusNot(showtime.getId(), BookingStatus.CANCELLED);

        return tickets.stream()
                .map(ticket -> {
                    BookingResponse.TicketSummary dto = new BookingResponse.TicketSummary();
                    dto.setId(ticket.getId());
                    dto.setSeatId(ticket.getSeat().getId());
                    dto.setSeatNumber(ticket.getSeat().getSeatNumber());
                    dto.setRow(ticket.getSeat().getRow());
                    dto.setCol(ticket.getSeat().getCol());
                    dto.setPrice(ticket.getPrice());
                    return dto;
                })
                .toList();
    }

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.setId(booking.getId());
        res.setBookingCode(booking.getBookingCode());
        res.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        res.setShowtimeId(booking.getShowtime() != null ? booking.getShowtime().getId() : null);
        res.setStatus(booking.getStatus());
        res.setTotalPrice(booking.getTotalPrice());
        res.setPaymentTime(booking.getPaymentTime());
        res.setCreatedAt(booking.getCreatedAt());
        res.setUpdatedAt(booking.getUpdatedAt());

        // User summary
        if (booking.getUser() != null) {
            BookingResponse.UserSummary userSummary = new BookingResponse.UserSummary();
            userSummary.setId(booking.getUser().getId());
            userSummary.setEmail(booking.getUser().getEmail());
            userSummary.setFullName(booking.getUser().getFullName());
            res.setUser(userSummary);
        }

        // Showtime summary
        if (booking.getShowtime() != null) {
            Showtime showtime = booking.getShowtime();
            BookingResponse.ShowtimeSummary s = new BookingResponse.ShowtimeSummary();
            s.setId(showtime.getId());
            if (showtime.getMovie() != null) {
                s.setMovieId(showtime.getMovie().getId());
                s.setMovieTitle(showtime.getMovie().getTitle());
            }
            if (showtime.getRoom() != null) {
                s.setRoomId(showtime.getRoom().getId());
                s.setRoomNumber(showtime.getRoom().getRoomNumber());
                if (showtime.getRoom().getCinema() != null) {
                    s.setCinemaId(showtime.getRoom().getCinema().getId());
                    s.setCinemaName(showtime.getRoom().getCinema().getName());
                }
            }
            s.setStartTime(showtime.getStartTime());
            s.setEndTime(showtime.getEndTime());
            s.setPrice(showtime.getPrice());
            res.setShowtime(s);
        }

        // Tickets
        if (booking.getTickets() != null) {
            List<BookingResponse.TicketSummary> ticketDtos = booking.getTickets().stream()
                    .map(ticket -> {
                        BookingResponse.TicketSummary dto = new BookingResponse.TicketSummary();
                        dto.setId(ticket.getId());
                        if (ticket.getSeat() != null) {
                            dto.setSeatId(ticket.getSeat().getId());
                            dto.setSeatNumber(ticket.getSeat().getSeatNumber());
                            dto.setRow(ticket.getSeat().getRow());
                            dto.setCol(ticket.getSeat().getCol());
                        }
                        dto.setPrice(ticket.getPrice());
                        return dto;
                    })
                    .toList();
            res.setTickets(ticketDtos);
        }

        // Refreshments
        if (booking.getBookingRefreshments() != null) {
            List<BookingResponse.BookingRefreshmentSummary> refreshmentDtos =
                    booking.getBookingRefreshments().stream()
                            .map(br -> {
                                BookingResponse.BookingRefreshmentSummary dto =
                                        new BookingResponse.BookingRefreshmentSummary();
                                dto.setId(br.getId());
                                if (br.getRefreshment() != null) {
                                    dto.setRefreshmentId(br.getRefreshment().getId());
                                    dto.setName(br.getRefreshment().getName());
                                    dto.setPicture(br.getRefreshment().getPicture());
                                    dto.setUnitPrice(br.getRefreshment().getPrice());
                                }
                                dto.setQuantity(br.getQuantity());
                                dto.setTotalPrice(br.getTotalPrice());
                                return dto;
                            })
                            .toList();
            res.setRefreshments(refreshmentDtos);
        }

        return res;
    }

    private String generateBookingCode() {
        // BK + 10 ký tự từ UUID
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "BK" + random;
    }
}








