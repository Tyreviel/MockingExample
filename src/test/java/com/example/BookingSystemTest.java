package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for the BookingSystem class.
 * 
 * These tests verify both successful and failed scenarios for all public methods,
 * ensuring proper validation, error handling, and business logic correctness.
 * 
 * Test doubles (mocks) are used for all dependencies:
 * - TimeProvider: Provides controllable time for testing temporal logic
 * - RoomRepository: Simulates data persistence layer
 * - NotificationService: Simulates external notification service
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingSystem Tests")
class BookingSystemTest {

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private NotificationService notificationService;

    private BookingSystem bookingSystem;

    private LocalDateTime currentTime;
    private LocalDateTime futureStartTime;
    private LocalDateTime futureEndTime;

    @BeforeEach
    void setUp() {
        bookingSystem = new BookingSystem(timeProvider, roomRepository, notificationService);
        currentTime = LocalDateTime.of(2026, 2, 4, 10, 0);
        futureStartTime = LocalDateTime.of(2026, 2, 5, 10, 0);
        futureEndTime = LocalDateTime.of(2026, 2, 5, 12, 0);
        
        // Use lenient() to allow stubbing that may not be used in all tests
        lenient().when(timeProvider.getCurrentTime()).thenReturn(currentTime);
    }

    // ========== bookRoom Tests ==========

    /**
     * Tests that bookRoom rejects null parameters.
     * 
     * This parametrized test covers all three null parameter scenarios:
     * - null roomId
     * - null startTime  
     * - null endTime
     * 
     * Expected: IllegalArgumentException with appropriate error message
     */
    @ParameterizedTest(name = "bookRoom should throw IllegalArgumentException when {0} is null")
    @MethodSource("nullParameterProvider")
    @DisplayName("bookRoom should throw IllegalArgumentException for null parameters")
    void bookRoom_shouldThrowException_whenParameterIsNull(String parameterName, String roomId, 
                                                           LocalDateTime startTime, LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    static Stream<Arguments> nullParameterProvider() {
        LocalDateTime validStart = LocalDateTime.of(2026, 2, 5, 10, 0);
        LocalDateTime validEnd = LocalDateTime.of(2026, 2, 5, 12, 0);
        return Stream.of(
                Arguments.of("roomId", null, validStart, validEnd),
                Arguments.of("startTime", "room1", null, validEnd),
                Arguments.of("endTime", "room1", validStart, null)
        );
    }

    /**
     * Tests that bookRoom rejects start times in the past.
     * 
     * Scenario: Attempting to book a room with a start time before the current time
     * Expected: IllegalArgumentException indicating past bookings are not allowed
     */
    @Test
    @DisplayName("bookRoom should throw IllegalArgumentException when startTime is in the past")
    void bookRoom_shouldThrowException_whenStartTimeIsInPast() {
        LocalDateTime pastTime = currentTime.minusHours(1);
        
        assertThatThrownBy(() -> bookingSystem.bookRoom("room1", pastTime, futureEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kan inte boka tid i dåtid");
    }

    /**
     * Tests that bookRoom rejects invalid time ranges where endTime is before startTime.
     * 
     * This parametrized test covers invalid time range scenario:
     * - endTime before startTime
     * 
     * Note: Equal times are allowed (zero-duration bookings are permitted)
     * 
     * Expected: IllegalArgumentException indicating invalid time range
     * Note: Exception is thrown before repository lookup, so no mocking needed
     */
    @ParameterizedTest(name = "bookRoom should throw IllegalArgumentException when endTime {0} startTime")
    @MethodSource("invalidTimeRangeProvider")
    @DisplayName("bookRoom should throw IllegalArgumentException for invalid time ranges")
    void bookRoom_shouldThrowException_whenTimeRangeIsInvalid(String description, 
                                                             LocalDateTime startTime, 
                                                             LocalDateTime endTime) {
        // Time range validation happens before repository lookup, so no mocking needed
        assertThatThrownBy(() -> bookingSystem.bookRoom("room1", startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");
    }

    static Stream<Arguments> invalidTimeRangeProvider() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 5, 10, 0);
        return Stream.of(
                Arguments.of("is before", start, start.minusHours(1))
        );
    }

    /**
     * Tests failed scenario: Room does not exist in repository.
     * 
     * Scenario: Attempting to book a room that doesn't exist
     * Expected: IllegalArgumentException indicating room doesn't exist
     */
    @Test
    @DisplayName("bookRoom should throw IllegalArgumentException when room does not exist")
    void bookRoom_shouldThrowException_whenRoomDoesNotExist() {
        when(roomRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> bookingSystem.bookRoom("nonexistent", futureStartTime, futureEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rummet existerar inte");
    }

    /**
     * Tests failed scenario: Room is not available due to existing booking.
     * 
     * Scenario: Room exists but has an overlapping booking
     * Expected: Returns false, no booking created, no notification sent
     */
    @Test
    @DisplayName("bookRoom should return false when room is not available")
    void bookRoom_shouldReturnFalse_whenRoomIsNotAvailable() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        room.addBooking(new Booking("booking1", "room1", futureStartTime, futureEndTime));
        
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        
        boolean result = bookingSystem.bookRoom("room1", futureStartTime, futureEndTime);
        
        assertThat(result).isFalse();
        verify(roomRepository, never()).save(any());
        verify(notificationService, never()).sendBookingConfirmation(any(Booking.class));
    }

    /**
     * Tests successful scenario: Room booking succeeds.
     * 
     * Scenario: Room exists and is available for the requested time slot
     * Expected: Returns true, booking created, room saved, notification sent
     */
    @Test
    @DisplayName("bookRoom should successfully book room when room is available")
    void bookRoom_shouldReturnTrue_whenRoomIsAvailable() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        
        boolean result = bookingSystem.bookRoom("room1", futureStartTime, futureEndTime);
        
        assertThat(result).isTrue();
        // Verify that a booking was added - room should no longer be available for that time slot
        assertThat(room.isAvailable(futureStartTime, futureEndTime)).isFalse();
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    /**
     * Tests successful scenario: Booking succeeds even when notification fails.
     * 
     * Scenario: Room booking succeeds but notification service throws exception
     * Expected: Returns true, booking created, system continues despite notification failure
     * 
     * This tests the resilience of the system - bookings should not fail due to
     * external notification service issues.
     */
    @Test
    @DisplayName("bookRoom should succeed even when notification service throws exception")
    void bookRoom_shouldSucceed_whenNotificationThrowsException() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        doThrow(new NotificationException("Notification failed")).when(notificationService)
                .sendBookingConfirmation(any(Booking.class));
        
        boolean result = bookingSystem.bookRoom("room1", futureStartTime, futureEndTime);
        
        assertThat(result).isTrue();
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    /**
     * Tests edge case: Booking with zero duration.
     *
     * Scenario: Start time equals end time (boundary condition)
     * Expected: Booking succeeds (only past times are rejected)
     */
    @Test
    void bookRoom_shouldAllowZeroDurationBooking() {
        Room room = new Room("room1", "Test Room");
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom("room1", futureStartTime, futureStartTime);

        assertThat(result).isTrue();
    }


    /**
     * Tests edge case: Booking at exactly the current time.
     * 
     * Scenario: Start time equals current time (boundary condition)
     * Expected: Booking succeeds (only past times are rejected)
     */

    @Test
    @DisplayName("bookRoom should allow booking when startTime equals currentTime")
    void bookRoom_shouldAllowBooking_whenStartTimeEqualsCurrentTime() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        LocalDateTime startAtCurrentTime = currentTime;
        LocalDateTime endAtCurrentTime = currentTime.plusHours(2);
        
        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        
        boolean result = bookingSystem.bookRoom("room1", startAtCurrentTime, endAtCurrentTime);
        
        assertThat(result).isTrue();
        verify(roomRepository).save(room);
    }

    // ========== getAvailableRooms Tests ==========

    /**
     * Tests that getAvailableRooms rejects null parameters.
     * 
     * This parametrized test covers both null parameter scenarios:
     * - null startTime
     * - null endTime
     * 
     * Expected: IllegalArgumentException with appropriate error message
     */
    @ParameterizedTest(name = "getAvailableRooms should throw IllegalArgumentException when {0} is null")
    @MethodSource("nullTimeParameterProvider")
    @DisplayName("getAvailableRooms should throw IllegalArgumentException for null parameters")
    void getAvailableRooms_shouldThrowException_whenParameterIsNull(String parameterName,
                                                                    LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Måste ange både start- och sluttid");
    }

    static Stream<Arguments> nullTimeParameterProvider() {
        LocalDateTime validStart = LocalDateTime.of(2026, 2, 5, 10, 0);
        LocalDateTime validEnd = LocalDateTime.of(2026, 2, 5, 12, 0);
        return Stream.of(
                Arguments.of("startTime", null, validEnd),
                Arguments.of("endTime", validStart, null)
        );
    }

    /**
     * Tests that getAvailableRooms rejects invalid time ranges.
     * 
     * Scenario: endTime is before startTime
     * Note: Equal times are allowed (zero-duration time slots are permitted)
     * Expected: IllegalArgumentException indicating invalid time range
     */
    @ParameterizedTest(name = "getAvailableRooms should throw IllegalArgumentException when endTime {0} startTime")
    @MethodSource("invalidTimeRangeProvider")
    @DisplayName("getAvailableRooms should throw IllegalArgumentException for invalid time ranges")
    void getAvailableRooms_shouldThrowException_whenTimeRangeIsInvalid(String description,
                                                                         LocalDateTime startTime,
                                                                         LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");
    }

    /**
     * Tests successful scenario: Empty result when no rooms exist.
     * 
     * Scenario: Repository returns empty list
     * Expected: Returns empty list
     */
    @Test
    @DisplayName("getAvailableRooms should return empty list when no rooms exist")
    void getAvailableRooms_shouldReturnEmptyList_whenNoRoomsExist() {
        when(roomRepository.findAll()).thenReturn(new ArrayList<>());
        
        List<Room> result = bookingSystem.getAvailableRooms(futureStartTime, futureEndTime);
        
        assertThat(result).isEmpty();
    }

    /**
     * Tests successful scenario: Filters out unavailable rooms correctly.
     * 
     * Scenario: Some rooms have overlapping bookings, some are available
     * Expected: Returns only available rooms, excludes rooms with conflicts
     */
    @Test
    @DisplayName("getAvailableRooms should return only available rooms")
    void getAvailableRooms_shouldReturnOnlyAvailableRooms() {
        Room availableRoom1 = new Room("room1", "Available Room 1");
        Room availableRoom2 = new Room("room2", "Available Room 2");
        Room unavailableRoom = new Room("room3", "Unavailable Room");
        
        // Add a booking that overlaps with the requested time
        unavailableRoom.addBooking(new Booking("booking1", "room3", futureStartTime, futureEndTime));
        
        List<Room> allRooms = List.of(availableRoom1, availableRoom2, unavailableRoom);
        when(roomRepository.findAll()).thenReturn(allRooms);
        
        List<Room> result = bookingSystem.getAvailableRooms(futureStartTime, futureEndTime);
        
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(availableRoom1, availableRoom2);
        assertThat(result).doesNotContain(unavailableRoom);
    }

    /**
     * Tests successful scenario: All rooms available.
     * 
     * Scenario: All rooms in repository are available for requested time
     * Expected: Returns all rooms
     */
    @Test
    @DisplayName("getAvailableRooms should return all rooms when all are available")
    void getAvailableRooms_shouldReturnAllRooms_whenAllAreAvailable() {
        Room room1 = new Room("room1", "Room 1");
        Room room2 = new Room("room2", "Room 2");
        Room room3 = new Room("room3", "Room 3");
        
        List<Room> allRooms = List.of(room1, room2, room3);
        when(roomRepository.findAll()).thenReturn(allRooms);
        
        List<Room> result = bookingSystem.getAvailableRooms(futureStartTime, futureEndTime);
        
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(room1, room2, room3);
    }

    /**
     * Tests successful scenario: Handles partial overlaps correctly.
     * 
     * Scenario: Room has booking that partially overlaps with requested time
     * Expected: Room is excluded from results (any overlap makes room unavailable)
     */
    @Test
    @DisplayName("getAvailableRooms should filter out rooms with overlapping bookings")
    void getAvailableRooms_shouldFilterOutRooms_withOverlappingBookings() {
        Room room1 = new Room("room1", "Room 1");
        Room room2 = new Room("room2", "Room 2");
        
        // Add booking that overlaps partially
        LocalDateTime overlapStart = futureStartTime.minusHours(1);
        LocalDateTime overlapEnd = futureStartTime.plusHours(1);
        room2.addBooking(new Booking("booking1", "room2", overlapStart, overlapEnd));
        
        List<Room> allRooms = List.of(room1, room2);
        when(roomRepository.findAll()).thenReturn(allRooms);
        
        List<Room> result = bookingSystem.getAvailableRooms(futureStartTime, futureEndTime);
        
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(room1);
    }

    // ========== cancelBooking Tests ==========

    /**
     * Tests that cancelBooking rejects null bookingId.
     * 
     * Scenario: Attempting to cancel with null booking ID
     * Expected: IllegalArgumentException indicating booking ID cannot be null
     */
    @Test
    @DisplayName("cancelBooking should throw IllegalArgumentException when bookingId is null")
    void cancelBooking_shouldThrowException_whenBookingIdIsNull() {
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Boknings-id kan inte vara null");
    }

    /**
     * Tests failed scenario: Booking does not exist.
     * 
     * Scenario: Booking ID doesn't match any booking in any room
     * Expected: Returns false, no changes made, no notification sent
     */
    @Test
    @DisplayName("cancelBooking should return false when booking does not exist")
    void cancelBooking_shouldReturnFalse_whenBookingDoesNotExist() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        
        when(roomRepository.findAll()).thenReturn(List.of(room));
        
        boolean result = bookingSystem.cancelBooking("nonexistent");
        
        assertThat(result).isFalse();
        verify(roomRepository, never()).save(any());
        verify(notificationService, never()).sendCancellationConfirmation(any(Booking.class));
    }

    /**
     * Tests failed scenario: Cannot cancel booking that has already started.
     * 
     * Scenario: Booking's start time is before current time (booking in progress or completed)
     * Expected: IllegalStateException indicating cancellation not allowed
     */
    @Test
    @DisplayName("cancelBooking should throw IllegalStateException when booking has already started")
    void cancelBooking_shouldThrowException_whenBookingHasStarted() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        LocalDateTime pastStartTime = currentTime.minusHours(2);
        LocalDateTime pastEndTime = currentTime.minusHours(1);
        Booking pastBooking = new Booking("booking1", "room1", pastStartTime, pastEndTime);
        room.addBooking(pastBooking);
        
        when(roomRepository.findAll()).thenReturn(List.of(room));
        
        assertThatThrownBy(() -> bookingSystem.cancelBooking("booking1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
        
        verify(roomRepository, never()).save(any());
        verify(notificationService, never()).sendCancellationConfirmation(any(Booking.class));
    }

    /**
     * Tests edge case: Cancellation allowed when start time equals current time.
     * 
     * Scenario: Booking starts exactly at current time (boundary condition)
     * Expected: Cancellation succeeds (only past bookings are rejected)
     */
    @Test
    @DisplayName("cancelBooking should succeed when booking startTime equals currentTime")
    void cancelBooking_shouldSucceed_whenBookingStartTimeEqualsCurrentTime() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        Booking booking = new Booking("booking1", "room1", currentTime, futureEndTime);
        room.addBooking(booking);
        
        when(roomRepository.findAll()).thenReturn(List.of(room));
        
        // The code only throws if startTime is BEFORE currentTime, not equal
        boolean result = bookingSystem.cancelBooking("booking1");
        
        assertThat(result).isTrue();
        assertThat(room.hasBooking("booking1")).isFalse();
        verify(roomRepository).save(room);
    }

    /**
     * Tests successful scenario: Cancellation succeeds for future booking.
     * 
     * Scenario: Booking exists and is in the future
     * Expected: Returns true, booking removed, room saved, notification sent
     */
    @Test
    @DisplayName("cancelBooking should successfully cancel future booking")
    void cancelBooking_shouldReturnTrue_whenCancellationSucceeds() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        Booking booking = new Booking("booking1", "room1", futureStartTime, futureEndTime);
        room.addBooking(booking);
        
        when(roomRepository.findAll()).thenReturn(List.of(room));
        
        boolean result = bookingSystem.cancelBooking("booking1");
        
        assertThat(result).isTrue();
        assertThat(room.hasBooking("booking1")).isFalse();
        verify(roomRepository).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }

    /**
     * Tests successful scenario: Cancellation succeeds even when notification fails.
     * 
     * Scenario: Cancellation succeeds but notification service throws exception
     * Expected: Returns true, booking cancelled, system continues despite notification failure
     * 
     * This tests the resilience of the system - cancellations should not fail due to
     * external notification service issues.
     */
    @Test
    @DisplayName("cancelBooking should succeed even when notification service throws exception")
    void cancelBooking_shouldSucceed_whenNotificationThrowsException() throws NotificationException {
        Room room = new Room("room1", "Test Room");
        Booking booking = new Booking("booking1", "room1", futureStartTime, futureEndTime);
        room.addBooking(booking);
        
        when(roomRepository.findAll()).thenReturn(List.of(room));
        doThrow(new NotificationException("Notification failed")).when(notificationService)
                .sendCancellationConfirmation(any(Booking.class));
        
        boolean result = bookingSystem.cancelBooking("booking1");
        
        assertThat(result).isTrue();
        assertThat(room.hasBooking("booking1")).isFalse();
        verify(roomRepository).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }

    /**
     * Tests successful scenario: Finds correct booking when multiple rooms exist.
     * 
     * Scenario: Multiple rooms with bookings, cancelling specific booking
     * Expected: Only the specified booking is cancelled, other bookings remain unchanged
     */
    @Test
    @DisplayName("cancelBooking should find booking in correct room when multiple rooms exist")
    void cancelBooking_shouldFindBookingInCorrectRoom_whenMultipleRoomsExist() throws NotificationException {
        Room room1 = new Room("room1", "Room 1");
        Room room2 = new Room("room2", "Room 2");
        Booking booking1 = new Booking("booking1", "room1", futureStartTime, futureEndTime);
        Booking booking2 = new Booking("booking2", "room2", futureStartTime, futureEndTime);
        room1.addBooking(booking1);
        room2.addBooking(booking2);
        
        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
        
        boolean result = bookingSystem.cancelBooking("booking1");
        
        assertThat(result).isTrue();
        assertThat(room1.hasBooking("booking1")).isFalse();
        assertThat(room2.hasBooking("booking2")).isTrue(); // Should not be cancelled
        verify(roomRepository).save(room1);
        verify(notificationService).sendCancellationConfirmation(booking1);
    }
}
