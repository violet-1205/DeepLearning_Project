package com.python.home.serviceimp;

import com.python.home.service.HomeService;
import org.springframework.stereotype.Service;

@Service
public class HomeServiceImpl implements HomeService {
    @Override
    public String getWelcomeMessage() {
        return "Hello World";
    }
}
