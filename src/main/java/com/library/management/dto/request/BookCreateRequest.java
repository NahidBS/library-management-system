package com.library.management.dto.request;

import com.library.management.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for creating a new book")
public class BookCreateRequest {
    
    @NotBlank(message = "Book name is required")
    @Size(max = 255, message = "Book name must be between 1 and 255 characters")
    @Schema(description = "Name of the book", example = "The Great Gatsby")
    private String name;
    
    @Size(max = 500, message = "Short details must not exceed 500 characters")
    @Schema(description = "Short details about the book", example = "A classic American novel")
    private String shortDetails;
    
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author name must be between 1 and 255 characters")
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;
    
    @Schema(description = "Detailed description about the book", example = "A story of decadence and excess in the Jazz Age")
    private String about;
    
    @NotNull(message = "Category ID is required")
    @Schema(description = "ID of the category this book belongs to", example = "1")
    private Long categoryId;
    
    @NotNull(message = "Book format is required")
    @Schema(description = "Format of the book", example = "HARD_COPY")
    private Book.BookFormat format;
    
    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Schema(description = "Total number of copies", example = "5")
    private Integer totalCopies;
    
    @Schema(description = "Number of available copies (defaults to total copies if not provided)", example = "5")
    private Integer availableCopies;
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Schema(description = "ISBN of the book", example = "9780743273565")
    private String isbn;
    
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2100, message = "Publication year must not exceed 2100")
    @Schema(description = "Publication year of the book", example = "1925")
    private Integer publicationYear;

//    private String book_cover;
//    private String book_coverUrl;
//
//    private String pdf_file;
//    private String pdf_fileUrl;
//
//    private String audio_file;
//    private String audio_fileUrl;
    // No files here — files come via MultipartFile in controller

}

