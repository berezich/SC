package com.berezich.sportconnector.backend.Endpoint;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Cron2 extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Cron2 has executed");
        new PersonEndpoint().removeOldReqChangeEmailEntities();
    }
}