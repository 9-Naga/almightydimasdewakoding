package com.example.projectbinar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.projectbinar.dto.plafond.PlafondRequest;
import com.example.projectbinar.dto.plafond.PlafondResponse;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.PlafondRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlafondServiceTest {

  @Mock private PlafondRepository plafondRepository;

  @InjectMocks private PlafondService plafondService;

  private Plafond silverPlafond;
  private Plafond goldPlafond;
  private PlafondRequest validRequest;

  @BeforeEach
  void setUp() {
    // Setup test data
    silverPlafond =
        Plafond.builder()
            .id(1L)
            .name("Silver")
            .minAmount(new BigDecimal("1000000"))
            .maxAmount(new BigDecimal("5000000"))
            .interestRate(new BigDecimal("12.00"))
            .tenorMonth(6)
            .isActive(true)
            .build();

    goldPlafond =
        Plafond.builder()
            .id(2L)
            .name("Gold")
            .minAmount(new BigDecimal("5000000"))
            .maxAmount(new BigDecimal("20000000"))
            .interestRate(new BigDecimal("10.00"))
            .tenorMonth(12)
            .isActive(true)
            .build();

    validRequest = new PlafondRequest();
    validRequest.setName("Platinum");
    validRequest.setMinAmount(new BigDecimal("20000000"));
    validRequest.setMaxAmount(new BigDecimal("100000000"));
    validRequest.setInterestRate(new BigDecimal("8.00"));
    validRequest.setTenorMonth(24);
  }

  @Test
  @DisplayName("Should return all active plafonds")
  void getAllActivePlafonds_ShouldReturnActivePlafonds() {
    // Arrange
    when(plafondRepository.findByIsActiveTrue())
        .thenReturn(Arrays.asList(silverPlafond, goldPlafond));

    // Act
    List<PlafondResponse> result = plafondService.getAllActivePlafonds();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Silver", result.get(0).getName());
    assertEquals("Gold", result.get(1).getName());
    verify(plafondRepository, times(1)).findByIsActiveTrue();
  }

  @Test
  @DisplayName("Should return plafond by ID when exists")
  void getPlafondById_WhenExists_ShouldReturnPlafond() {
    // Arrange
    when(plafondRepository.findById(1L)).thenReturn(Optional.of(silverPlafond));

    // Act
    PlafondResponse result = plafondService.getPlafondById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("Silver", result.getName());
    assertEquals(new BigDecimal("1000000"), result.getMinAmount());
    verify(plafondRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when plafond not found")
  void getPlafondById_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(plafondRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> plafondService.getPlafondById(99L));
    verify(plafondRepository, times(1)).findById(99L);
  }

  @Test
  @DisplayName("Should create plafond successfully")
  void createPlafond_WithValidRequest_ShouldCreatePlafond() {
    // Arrange
    Plafond savedPlafond =
        Plafond.builder()
            .id(3L)
            .name("Platinum")
            .minAmount(new BigDecimal("20000000"))
            .maxAmount(new BigDecimal("100000000"))
            .interestRate(new BigDecimal("8.00"))
            .tenorMonth(24)
            .isActive(true)
            .build();

    when(plafondRepository.existsByName("Platinum")).thenReturn(false);
    when(plafondRepository.save(any(Plafond.class))).thenReturn(savedPlafond);

    // Act
    PlafondResponse result = plafondService.createPlafond(validRequest);

    // Assert
    assertNotNull(result);
    assertEquals("Platinum", result.getName());
    assertEquals(new BigDecimal("20000000"), result.getMinAmount());
    verify(plafondRepository, times(1)).existsByName("Platinum");
    verify(plafondRepository, times(1)).save(any(Plafond.class));
  }

  @Test
  @DisplayName("Should throw exception when plafond name already exists")
  void createPlafond_WhenNameExists_ShouldThrowException() {
    // Arrange
    validRequest.setName("Silver");
    when(plafondRepository.existsByName("Silver")).thenReturn(true);

    // Act & Assert
    assertThrows(BadRequestException.class, () -> plafondService.createPlafond(validRequest));
    verify(plafondRepository, times(1)).existsByName("Silver");
    verify(plafondRepository, never()).save(any(Plafond.class));
  }

  @Test
  @DisplayName("Should throw exception when min amount greater than max amount")
  void createPlafond_WhenMinGreaterThanMax_ShouldThrowException() {
    // Arrange
    validRequest.setMinAmount(new BigDecimal("100000000"));
    validRequest.setMaxAmount(new BigDecimal("20000000"));
    when(plafondRepository.existsByName("Platinum")).thenReturn(false);

    // Act & Assert
    assertThrows(BadRequestException.class, () -> plafondService.createPlafond(validRequest));
  }

  @Test
  @DisplayName("Should deactivate plafond successfully")
  void deactivatePlafond_WhenExists_ShouldDeactivate() {
    // Arrange
    when(plafondRepository.findById(1L)).thenReturn(Optional.of(silverPlafond));
    when(plafondRepository.save(any(Plafond.class))).thenReturn(silverPlafond);

    // Act
    plafondService.deactivatePlafond(1L);

    // Assert
    assertFalse(silverPlafond.getIsActive());
    verify(plafondRepository, times(1)).findById(1L);
    verify(plafondRepository, times(1)).save(silverPlafond);
  }

  @Test
  @DisplayName("Should return all plafonds including inactive")
  void getAllPlafonds_ShouldReturnAllPlafonds() {
    // Arrange
    Plafond inactivePlafond =
        Plafond.builder()
            .id(3L)
            .name("Bronze")
            .minAmount(new BigDecimal("500000"))
            .maxAmount(new BigDecimal("1000000"))
            .interestRate(new BigDecimal("15.00"))
            .tenorMonth(3)
            .isActive(false)
            .build();

    when(plafondRepository.findAll())
        .thenReturn(Arrays.asList(silverPlafond, goldPlafond, inactivePlafond));

    // Act
    List<PlafondResponse> result = plafondService.getAllPlafonds();

    // Assert
    assertNotNull(result);
    assertEquals(3, result.size());
    verify(plafondRepository, times(1)).findAll();
  }
}
