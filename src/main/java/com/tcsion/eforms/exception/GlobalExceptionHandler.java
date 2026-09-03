package com.tcsion.eforms.exception;

import com.tcsion.eforms.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.contains("/api/") || (accept != null && accept.contains("application/json"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", ex.getMessage());
        mv.setStatus(HttpStatus.NOT_FOUND);
        return mv;
    }

    @ExceptionHandler(BusinessValidationException.class)
    public Object handleValidation(BusinessValidationException ex, HttpServletRequest request) {
        log.info("Business validation failed: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", ex.getErrors()));
        }
        ModelAndView mv = new ModelAndView("error/validation");
        mv.addObject("errors", ex.getErrors());
        return mv;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        log.info("Duplicate resource: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
        }
        ModelAndView mv = new ModelAndView("error/validation");
        mv.addObject("errors", Arrays.asList(ex.getMessage()));
        return mv;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(TicketAccessDeniedException.class)
    public Object handleTicketAccessDenied(TicketAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Object-level access denied: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        }
        return new ModelAndView("redirect:/access-denied");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access is denied"));
        }
        return new ModelAndView("redirect:/access-denied");
    }

    @ExceptionHandler(OptimisticLockConflictException.class)
    public Object handleOptimisticLockCustom(OptimisticLockConflictException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
        }
        ModelAndView mv = new ModelAndView("error/conflict");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public Object handleOptimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        String message = "This record was updated by another user. Please reload and try again.";
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message));
        }
        ModelAndView mv = new ModelAndView("error/conflict");
        mv.addObject("message", message);
        return mv;
    }

    @ExceptionHandler(FileStorageException.class)
    public Object handleFileStorage(FileStorageException ex, HttpServletRequest request) {
        log.error("File storage error", ex);
        if (isApiRequest(request)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
        ModelAndView mv = new ModelAndView("error/validation");
        mv.addObject("errors", Arrays.asList(ex.getMessage()));
        return mv;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String message = "The uploaded file exceeds the maximum allowed size.";
        if (isApiRequest(request)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message));
        }
        ModelAndView mv = new ModelAndView("error/validation");
        mv.addObject("errors", Arrays.asList(message));
        return mv;
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(TypeMismatchException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid request parameter: " + ex.getPropertyName()));
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        String message = "An unexpected error occurred. Please contact your administrator if this continues.";
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message));
        }
        ModelAndView mv = new ModelAndView("error/500");
        mv.addObject("message", message);
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mv;
    }
}
