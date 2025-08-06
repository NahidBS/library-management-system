package com.library.management.service;

import com.library.management.dto.request.BorrowCreateRequest;
import com.library.management.dto.response.BorrowResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Borrow;
import com.library.management.entity.User;
import com.library.management.exception.BusinessLogicException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.BorrowMapper;
import com.library.management.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {
    
    private final BorrowRepository borrowRepository;
    private final BorrowMapper borrowMapper;
    private final UserService userService;
    private final BookService bookService;
    
    private static final int MAX_BORROW_LIMIT = 5;
    private static final int BORROW_PERIOD_DAYS = 14;
    private static final int EXTENSION_DAYS = 7;
    private static final int MAX_EXTENSIONS = 2;
    
    @Transactional
    public BorrowResponse borrowBook(BorrowCreateRequest request) {
        return createBorrow(request);
    }
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getAllBorrows(Long userId, Long bookId, Boolean active, Boolean overdue, Pageable pageable) {
        Page<Borrow> borrows;
        
        if (userId != null && bookId != null) {
            borrows = borrowRepository.findByUserIdAndBookId(userId, bookId, pageable);
        } else if (userId != null) {
            if (active != null && active) {
                borrows = borrowRepository.findByUserIdAndReturnDateIsNull(userId, pageable);
            } else {
                borrows = borrowRepository.findByUserId(userId, pageable);
            }
        } else if (bookId != null) {
            borrows = borrowRepository.findByBookId(bookId, pageable);
        } else if (active != null && active) {
            borrows = borrowRepository.findByReturnDateIsNull(pageable);
        } else if (overdue != null && overdue) {
            borrows = borrowRepository.findOverdueBorrows(LocalDate.now(), pageable);
        } else {
            borrows = borrowRepository.findAll(pageable);
        }
        
        return borrows.map(borrowMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getUserBorrows(Long userId, Boolean active, Pageable pageable) {
        Page<Borrow> borrows;
        if (active != null && active) {
            borrows = borrowRepository.findByUserIdAndReturnDateIsNull(userId, pageable);
        } else {
            borrows = borrowRepository.findByUserId(userId, pageable);
        }
        return borrows.map(borrowMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getActiveBorrows(Pageable pageable) {
        Page<Borrow> borrows = borrowRepository.findByReturnDateIsNull(pageable);
        return borrows.map(borrowMapper::toResponse);
    }
    
    @Transactional
    public BorrowResponse extendDueDate(Long borrowId) {
        return extendBorrow(borrowId);
    }
    
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getUserBorrowingHistory(Long userId, Pageable pageable) {
        Page<Borrow> borrows = borrowRepository.findByUserIdOrderByBorrowDateDesc(userId, pageable);
        return borrows.map(borrowMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public BorrowResponse getBorrowById(Long id) {
        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", "id", id));
        return borrowMapper.toResponse(borrow);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowResponse> getBorrowsByUserId(Long userId) {
        List<Borrow> borrows = borrowRepository.findByUserId(userId);
        return borrowMapper.toResponseList(borrows);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowResponse> getActiveBorrowsByUserId(Long userId) {
        List<Borrow> borrows = borrowRepository.findByUserIdAndStatus(userId, Borrow.BorrowStatus.ACTIVE);
        return borrowMapper.toResponseList(borrows);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowResponse> getOverdueBorrows() {
        List<Borrow> borrows = borrowRepository.findOverdueBorrows(LocalDate.now());
        return borrowMapper.toResponseList(borrows);
    }
    
    @Transactional
    public BorrowResponse createBorrow(BorrowCreateRequest request) {
        User user = userService.getUserEntityById(request.getUserId());
        Book book = bookService.getBookEntityById(request.getBookId());
        
        // Validate business rules
        validateBorrowBusinessRules(user, book);
        
        // Check if user already has this book borrowed
        if (borrowRepository.findActiveBorrowByUserAndBook(user.getId(), book.getId()).isPresent()) {
            throw new BusinessLogicException("User already has this book borrowed");
        }
        
        Borrow borrow = borrowMapper.toEntity(request, user, book);
        
        // Decrease available copies
        bookService.decreaseAvailableCopies(book.getId());
        
        Borrow savedBorrow = borrowRepository.save(borrow);
        return borrowMapper.toResponse(savedBorrow);
    }
    
    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", "id", borrowId));
        
        if (borrow.getStatus() != Borrow.BorrowStatus.ACTIVE) {
            throw new BusinessLogicException("Book is not currently borrowed");
        }
        
        borrow.setReturnDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);
        
        // Increase available copies
        bookService.increaseAvailableCopies(borrow.getBook().getId());
        
        Borrow updatedBorrow = borrowRepository.save(borrow);
        return borrowMapper.toResponse(updatedBorrow);
    }
    
    @Transactional
    public BorrowResponse extendBorrow(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow", "id", borrowId));
        
        if (borrow.getStatus() != Borrow.BorrowStatus.ACTIVE) {
            throw new BusinessLogicException("Only active borrows can be extended");
        }
        
        if (borrow.getExtensionCount() >= MAX_EXTENSIONS) {
            throw new BusinessLogicException(
                String.format("Maximum number of extensions (%d) reached", MAX_EXTENSIONS));
        }
        
        if (borrowMapper.isOverdue(borrow)) {
            throw new BusinessLogicException("Overdue books cannot be extended");
        }
        
        borrow.setDueDate(borrow.getDueDate().plusDays(EXTENSION_DAYS));
        borrow.setExtensionCount(borrow.getExtensionCount() + 1);
        
        Borrow updatedBorrow = borrowRepository.save(borrow);
        return borrowMapper.toResponse(updatedBorrow);
    }
    
    @Transactional
    public void updateOverdueStatus() {
        List<Borrow> overdueBorrows = borrowRepository.findOverdueBorrows(LocalDate.now());
        
        for (Borrow borrow : overdueBorrows) {
            if (borrow.getStatus() == Borrow.BorrowStatus.ACTIVE) {
                borrow.setStatus(Borrow.BorrowStatus.OVERDUE);
                borrowRepository.save(borrow);
            }
        }
    }
    
    private void validateBorrowBusinessRules(User user, Book book) {
        // Check if book is available
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessLogicException("Book is not available for borrowing");
        }
        
        // Check user's borrow limit
        long activeBorrowCount = userService.getActiveBorrowCount(user.getId());
        if (activeBorrowCount >= MAX_BORROW_LIMIT) {
            throw new BusinessLogicException(
                String.format("User has reached maximum borrow limit of %d books", MAX_BORROW_LIMIT));
        }
        
        // Check if user has overdue books
        List<Borrow> overdueBorrows = borrowRepository.findByUserIdAndStatus(user.getId(), Borrow.BorrowStatus.OVERDUE);
        if (!overdueBorrows.isEmpty()) {
            throw new BusinessLogicException("User has overdue books and cannot borrow new books");
        }
    }
}

