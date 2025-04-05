package com.epam.agency.service;

import com.epam.agency.dto.OrderDTO;
import com.epam.agency.dto.UserDTO;
import com.epam.agency.dto.VoucherDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final TemplateEngine templateEngine;
    private final OrderService orderService;
    private final UserService userService;
    private final VoucherService voucherService;

    @Override
    public void generateOrderPdf(String orderId, HttpServletResponse response) throws Exception {
        OrderDTO order = orderService.getOrderById(orderId);
        UserDTO user = userService.getUserDTOById(order.getUserId());
        VoucherDTO voucher = voucherService.getVoucherDTOById(order.getVoucherId());

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=order_" + orderId + ".pdf");

        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("user", user);
        context.setVariable("voucher", voucher);

        String htmlContent = templateEngine.process("order-details-pdf-page", context);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(response.getOutputStream());
    }

    @Override
    public String[] getAvailableAvatars() {
        File avatarsDir = new File("src/main/resources/static/images/avatars");
        return avatarsDir.list((dir, name) ->
                (name.endsWith(".png") || name.endsWith(".jpg")) && !name.equals("default-avatar.png"));
    }
}
