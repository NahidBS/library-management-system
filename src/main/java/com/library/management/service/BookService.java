package com.library.management.service;

import com.library.management.dto.request.BookCreateRequest;
import com.library.management.dto.request.BookUpdateRequest;
import com.library.management.dto.response.BookResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.exception.BusinessLogicException;
import com.library.management.exception.ResourceAlreadyExistsException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.BookMapper;
import com.library.management.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CategoryService categoryService;
    private final NotificationService notificationService;

    private final Path rootUploadDir = Paths.get("uploads");

    private String cleanUrlFilename(String url) {
        if (url == null) return null;

        int lastSlash = url.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == url.length() - 1) {
            return url; // malformed URL or no filename
        }

        String path = url.substring(0, lastSlash + 1); // e.g. "/files/covers/"
        String filename = url.substring(lastSlash + 1); // e.g. "1754998492625_GGatsby.jpg"

        int underscoreIndex = filename.indexOf('_');
        if (underscoreIndex == -1) {
            return url; // no prefix to remove
        }

        String cleanedFilename = filename.substring(underscoreIndex + 1); // "GGatsby.jpg"
        return path + cleanedFilename;
    }


    //create book with file
    @Transactional
    public BookResponse createBookWithFiles(BookCreateRequest request, MultipartFile bookCover,
                                            MultipartFile pdfFile, MultipartFile audioFile) {

        if (request.getIsbn() != null && bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new ResourceAlreadyExistsException("Book", "ISBN", request.getIsbn());
        }

        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        // Save files and get URLs
        String bookCoverUrl = null;
        String pdfFileUrl = null;
        String audioFileUrl = null;

        if (bookCover != null && !bookCover.isEmpty()) {
            bookCoverUrl = saveFile(bookCover, "covers");
        }
        if (pdfFile != null && !pdfFile.isEmpty()) {
            pdfFileUrl = saveFile(pdfFile, "pdfs");
        }
        if (audioFile != null && !audioFile.isEmpty()) {
            audioFileUrl = saveFile(audioFile, "audio");
        }

        // Convert DTO to entity
        Book book = bookMapper.toEntity(request, category);

        // Set file URLs directly on entity
        book.setBookCoverUrl(bookCoverUrl);
        book.setPdfFileUrl(pdfFileUrl);
        book.setAudioFileUrl(audioFileUrl);



        // Validate rules and save
        validateBookBusinessRules(book);


//        // Manually set URLs in DTO before mapping to entity
//        request.setBook_coverUrl(bookCoverUrl);
//        request.setPdf_fileUrl(pdfFileUrl);
//        request.setAudio_fileUrl(audioFileUrl);

        Book savedBook = bookRepository.save(book);
        // Clean URLs before returning response
        String cleanBookCoverUrl = cleanUrlFilename(savedBook.getBookCoverUrl());
        String cleanPdfFileUrl = cleanUrlFilename(savedBook.getPdfFileUrl());
        String cleanAudioFileUrl = cleanUrlFilename(savedBook.getAudioFileUrl());

// Create response DTO from entity
        BookResponse response = bookMapper.toResponse(savedBook);

// Override URLs in response DTO with cleaned versions
        response.setBookCoverUrl(cleanBookCoverUrl);
        response.setPdfFileUrl(cleanPdfFileUrl);
        response.setAudioFileUrl(cleanAudioFileUrl);

        return response;

//        return bookMapper.toResponse(savedBook);
    }
    private String saveFile(MultipartFile file, String folder) {
        try {
            Path uploadPath = rootUploadDir.resolve(folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL/path for client usage
            return "/files/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    private void validateBookBusinessRules(Book book) {
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new BusinessLogicException("Available copies cannot exceed total copies");
        }
        if (book.getTotalCopies() <= 0) {
            throw new BusinessLogicException("Total copies must be greater than 0");
        }
        if (book.getAvailableCopies() < 0) {
            throw new BusinessLogicException("Available copies cannot be negative");
        }
    }








    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Long categoryId, Boolean available, Pageable pageable) {
        Page<Book> books;
        
        if (categoryId != null && available != null) {
            if (available) {
                books = bookRepository.findByCategoryIdAndAvailableCopiesGreaterThan(categoryId, 0, pageable);
            } else {
                books = bookRepository.findByCategoryId(categoryId, pageable);
            }
        } else if (categoryId != null) {
            books = bookRepository.findByCategoryId(categoryId, pageable);
        } else if (available != null && available) {
            books = bookRepository.findByAvailableCopiesGreaterThan(0, pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }
        
        return books.map(bookMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String query, Pageable pageable) {
        Page<Book> books = bookRepository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                query, query, query, pageable);
        return books.map(bookMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        // Verify category exists
        categoryService.getCategoryById(categoryId);
        Page<Book> books = bookRepository.findByCategoryId(categoryId, pageable);
        return books.map(bookMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findByAvailableCopiesGreaterThan(0, pageable);
        return books.map(bookMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> getTrendingBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Book> books = bookRepository.findTrendingBooks(pageable);
        return bookMapper.toResponseList(books.getContent());
    }
    
    @Transactional
    public BookResponse updateBookAvailability(Long id, int availableCopies) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        if (availableCopies < 0) {
            throw new BusinessLogicException("Available copies cannot be negative");
        }
        
        if (availableCopies > book.getTotalCopies()) {
            throw new BusinessLogicException("Available copies cannot exceed total copies");
        }
        
        book.setAvailableCopies(availableCopies);
        Book updatedBook = bookRepository.save(book);
        return bookMapper.toResponse(updatedBook);
    }
    
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return bookMapper.toResponse(book);
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByName(String name) {
        List<Book> books = bookRepository.findByNameContainingIgnoreCase(name);
        return bookMapper.toResponseList(books);
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByAuthor(String author) {
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        return bookMapper.toResponseList(books);
    }
    

    
    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks() {
        List<Book> books = bookRepository.findAvailableBooks();
        return bookMapper.toResponseList(books);
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> getPopularBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findPopularBooks(pageable);
        return bookMapper.toResponseList(books);
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> getNewBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findNewBooks(pageable);
        return bookMapper.toResponseList(books);
    }
    
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        // Check if book with same ISBN already exists
        if (request.getIsbn() != null && bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new ResourceAlreadyExistsException("Book", "ISBN", request.getIsbn());
        }
        
        Category category = categoryService.getCategoryEntityById(request.getCategoryId());
        
        Book book = bookMapper.toEntity(request, category);
        
        // Validate business rules
        validateBookBusinessRules(book);
        
        Book savedBook = bookRepository.save(book);

        // Send notification to the admin about the new book
//        String adminEmail = "mfbinahid@gmail.com";  // Set your admin email here
//        String message = "A new book titled '" + savedBook.getName() + "' has been added to the library.";
//        notificationService.createNotification(message, adminEmail);
        notificationService.notifyNewBook("mfbinahid@gmail.com", savedBook.getName());

        // Return the saved book response
        return bookMapper.toResponse(savedBook);
    }
    
    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        // Check if new ISBN conflicts with existing book
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
                throw new ResourceAlreadyExistsException("Book", "ISBN", request.getIsbn());
            }
        }
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(request.getCategoryId());
        }
        
        bookMapper.updateEntity(book, request, category);
        
        // Validate business rules
        validateBookBusinessRules(book);
        
        Book updatedBook = bookRepository.save(book);
        return bookMapper.toResponse(updatedBook);
    }
    
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        // Check if book has active borrows
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BusinessLogicException(
                String.format("Cannot delete book '%s' because it has active borrows", book.getName()));
        }
        
        bookRepository.delete(book);
    }
    
    @Transactional
    public void decreaseAvailableCopies(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessLogicException("No available copies to borrow");
        }
        
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
    }
    
    @Transactional
    public void increaseAvailableCopies(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new BusinessLogicException("Cannot increase available copies beyond total copies");
        }
        
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }
    
    // Helper method for internal use by other services
    @Transactional(readOnly = true)
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }
    

    
    @Transactional(readOnly = true)
    public Boolean isBookAvailable(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return book.getAvailableCopies() > 0;
    }
    
    @Transactional(readOnly = true)
    public List<BookResponse> getRecommendedBooks(int limit) {
        // For now, return trending books as recommended books
        // In a real implementation, this could be based on user preferences, ratings, etc.
        Pageable pageable = PageRequest.of(0, limit);
        Page<Book> books = bookRepository.findTrendingBooks(pageable);
        return bookMapper.toResponseList(books.getContent());
    }
}

