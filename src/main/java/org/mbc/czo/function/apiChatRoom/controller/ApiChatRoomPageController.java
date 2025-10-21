package org.mbc.czo.function.apiChatRoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/chat")
public class ApiChatRoomPageController {

    @GetMapping("/")
    public String joinPage() {
        return "apiChatRoom/chatMain"; // templates/apiChatRoom/chatMain.html
    }
}
