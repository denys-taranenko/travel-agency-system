package com.epam.agency.controller;

import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.group.OnCreate;
import com.epam.agency.exception.AlreadyExistsException;
import com.epam.agency.exception.InvalidDataException;
import com.epam.agency.service.UserService;
import com.epam.agency.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class PageController {
    private final UserService userService;
    private final MessageUtil messageUtil;

    @GetMapping
    public String showMainPage() {
        return "main-page";
    }

    @GetMapping("/contacts")
    public String showContactsPage() {
        return "contacts-page";
    }

    @GetMapping("/about-us")
    public String showAboutUsPage() {
        return "about-us-page";
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage(ModelMap model) {
        model.addAttribute("user", new UserDTO());
        return new ModelAndView("register-page", model);
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@ModelAttribute("user") @Validated(OnCreate.class) UserDTO userDTO,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     ModelMap model) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register-page", model);
        }
        try {
            userService.registerUser(userDTO);
            redirectAttributes.addFlashAttribute("message", "registered");
            return new ModelAndView("redirect:/login");
        } catch (AlreadyExistsException | InvalidDataException exception) {
            model.addAttribute("error", messageUtil.getMessage(exception.getStatusMessage()));
            return new ModelAndView("register-page", model);
        }
    }
}
