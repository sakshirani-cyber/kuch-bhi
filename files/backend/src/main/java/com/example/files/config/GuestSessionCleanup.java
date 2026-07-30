package com.example.files.config;

import com.example.files.repository.FileRepository;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuestSessionCleanup implements HttpSessionListener {

    @Autowired
    private FileRepository fileRepository;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String guestId = (String) se.getSession().getAttribute("guestId");
        if (guestId != null) {
            try {
                fileRepository.deleteByUserId(guestId);
            } catch (Exception e) {
                // session cleanup 
            }
        }
    }
}
