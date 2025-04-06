package com.epam.agency.controller;

import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.util.MessageUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import static com.epam.agency.util.GlobalAttribute.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorControllerImplTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private ErrorControllerImpl errorController;

    @BeforeEach
    void setUp() {
        when(messageUtil.getMessage(StatusMessages.BASE_MESSAGE_BAD_REQUEST.getStatusMessage()))
                .thenReturn("Error message");
    }

    @Test
    void handleError_WhenStatusCodeExists_ShouldReturnErrorViewWithAttributes() {
        // Arrange
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(404);

        // Act
        ModelAndView modelAndView = errorController.handleError(request);

        // Assert
        assertEquals("error", modelAndView.getViewName());
        assertEquals(404, modelAndView.getModel().get(STATUS_CODE));
        assertEquals("NOT_FOUND", modelAndView.getModel().get(STATUS));
        assertEquals("Error message", modelAndView.getModel().get(STATUS_MESSAGE));
    }

    @Test
    void handleError_WhenStatusMessageIsCustomized_ShouldUseCustomMessage() {
        // Arrange
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(404);
        when(messageUtil.getMessage(StatusMessages.BASE_MESSAGE_BAD_REQUEST.getStatusMessage()))
                .thenReturn("Custom error message");

        // Act
        ModelAndView modelAndView = errorController.handleError(request);

        // Assert
        assertEquals("Custom error message", modelAndView.getModel().get(STATUS_MESSAGE));
    }
}
