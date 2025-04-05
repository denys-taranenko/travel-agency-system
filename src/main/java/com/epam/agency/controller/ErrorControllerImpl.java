package com.epam.agency.controller;

import com.epam.agency.exception.status.StatusMessages;
import com.epam.agency.util.MessageUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static com.epam.agency.util.GlobalAttribute.*;

@Controller
@RequiredArgsConstructor
public class ErrorControllerImpl implements ErrorController {
    private final MessageUtil messageUtil;

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("error");
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        String statusMessage = messageUtil.getMessage(StatusMessages.BASE_MESSAGE_BAD_REQUEST.getStatusMessage());

        if (status != null) {
            int statusCode = 404;

            modelAndView.addObject(STATUS_CODE, statusCode);
            modelAndView.addObject(STATUS, HttpStatus.valueOf(statusCode).getReasonPhrase().toUpperCase().replace(" ", "_"));
            modelAndView.addObject(STATUS_MESSAGE, statusMessage);
        }
        return modelAndView;
    }
}
