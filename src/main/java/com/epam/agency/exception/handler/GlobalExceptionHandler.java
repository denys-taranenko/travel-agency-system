package com.epam.agency.exception.handler;

import com.epam.agency.exception.AccessDeniedException;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.exception.status.StatusCodes;
import com.epam.agency.util.MessageUtil;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static com.epam.agency.util.GlobalAttribute.*;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageUtil messageUtil;

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFoundException(HttpServletRequest request, Model model, NoResourceFoundException exception) {
        return handleException(request, model, exception, StatusCodes.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(HttpServletRequest request, Model model, AccessDeniedException exception) {
        return handleException(request, model, exception, StatusCodes.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AlreadyExistsException.class)
    public String handleAlreadyExistsException(HttpServletRequest request, Model model, AlreadyExistsException exception) {
        return handleException(request, model, exception, StatusCodes.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidDataException.class, StripeException.class})
    public String handleInvalidDataException(HttpServletRequest request, Model model, InvalidDataException exception) {
        return handleException(request, model, exception, StatusCodes.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(HttpServletRequest request, Model model, Exception exception) {
        return handleException(request, model, exception, StatusCodes.SERVER_ERROR);
    }

    private String handleException(HttpServletRequest request, Model model, Exception exception, StatusCodes status) {
        String statusMessage = messageUtil.getMessage("error." + status.name().toLowerCase().replace("_", "."));

        model.addAttribute(STATUS_CODE, status.getHttpCode());
        model.addAttribute(STATUS, status);
        model.addAttribute(STATUS_MESSAGE, statusMessage);

        return "error";
    }
}
