package com.example.projectbinar.base;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private String message;
  private boolean success;
  private T data;
  private Integer code;
  private Instant timestamp;
}
