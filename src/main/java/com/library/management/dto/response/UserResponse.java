package com.library.management.dto.response;

import com.library.management.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO for user information")
public class UserResponse {
    
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;
    
    @Schema(description = "Moodle ID of the user", example = "user001")
    private String moodleId;
    
    @Schema(description = "Name of the user", example = "John Doe")
    private String name;
    
    @Schema(description = "Email of the user", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Role of the user", example = "USER")
    private User.UserRole role;
    
    @Schema(description = "Date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Address of the user", example = "456 User Avenue")
    private String address;
    
    @Schema(description = "Number of books currently borrowed", example = "2")
    private Integer currentBorrowCount;
    
    @Schema(description = "Total number of books borrowed historically", example = "15")
    private Integer totalBorrowCount;
    
    @Schema(description = "Number of overdue books", example = "0")
    private Integer overdueCount;
    
    @Schema(description = "Total number of books borrowed (for statistics)", example = "25")
    private Long totalBorrows;
    
    @Schema(description = "Number of currently active borrows (for statistics)", example = "3")
    private Long activeBorrows;
    
    @Schema(description = "Number of overdue borrows (for statistics)", example = "1")
    private Long overdueBorrows;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

