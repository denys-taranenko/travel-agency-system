package com.epam.agency.service;

import jakarta.servlet.http.HttpServletResponse;

public interface FileService {

    void generateOrderPdf(String orderId, HttpServletResponse response) throws Exception;

    String[] getAvailableAvatars();
}
