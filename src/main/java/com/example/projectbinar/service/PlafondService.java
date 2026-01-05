package com.example.projectbinar.service;

import com.example.projectbinar.dto.plafond.PlafondRequest;
import com.example.projectbinar.dto.plafond.PlafondResponse;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.PlafondRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlafondService {

  private final PlafondRepository plafondRepository;

  public PlafondService(PlafondRepository plafondRepository) {
    this.plafondRepository = plafondRepository;
  }

  public List<PlafondResponse> getAllActivePlafonds() {
    return plafondRepository.findByIsActiveTrue().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<PlafondResponse> getAllPlafonds() {
    return plafondRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public PlafondResponse getPlafondById(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
    return mapToResponse(plafond);
  }

  public Plafond getPlafondEntityById(Long id) {
    return plafondRepository
        .findByIdAndIsActiveTrue(id)
        .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
  }

  @Transactional
  public PlafondResponse createPlafond(PlafondRequest request) {
    if (plafondRepository.existsByName(request.getName())) {
      throw new BadRequestException("Plafond with name '" + request.getName() + "' already exists");
    }

    if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
      throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
    }

    Plafond plafond =
        Plafond.builder()
            .name(request.getName())
            .minAmount(request.getMinAmount())
            .maxAmount(request.getMaxAmount())
            .interestRate(request.getInterestRate())
            .tenorMonth(request.getTenorMonth())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();

    Plafond savedPlafond = plafondRepository.save(plafond);
    return mapToResponse(savedPlafond);
  }

  @Transactional
  public PlafondResponse updatePlafond(Long id, PlafondRequest request) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));

    // Check if new name conflicts with existing
    if (!plafond.getName().equals(request.getName())
        && plafondRepository.existsByName(request.getName())) {
      throw new BadRequestException("Plafond with name '" + request.getName() + "' already exists");
    }

    if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
      throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
    }

    plafond.setName(request.getName());
    plafond.setMinAmount(request.getMinAmount());
    plafond.setMaxAmount(request.getMaxAmount());
    plafond.setInterestRate(request.getInterestRate());
    plafond.setTenorMonth(request.getTenorMonth());
    if (request.getIsActive() != null) {
      plafond.setIsActive(request.getIsActive());
    }

    Plafond savedPlafond = plafondRepository.save(plafond);
    return mapToResponse(savedPlafond);
  }

  @Transactional
  public void deactivatePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
    plafond.setIsActive(false);
    plafondRepository.save(plafond);
  }

  private PlafondResponse mapToResponse(Plafond plafond) {
    return PlafondResponse.builder()
        .id(plafond.getId())
        .name(plafond.getName())
        .minAmount(plafond.getMinAmount())
        .maxAmount(plafond.getMaxAmount())
        .interestRate(plafond.getInterestRate())
        .tenorMonth(plafond.getTenorMonth())
        .isActive(plafond.getIsActive())
        .build();
  }
}
