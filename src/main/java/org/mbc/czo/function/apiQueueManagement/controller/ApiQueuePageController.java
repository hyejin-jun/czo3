package org.mbc.czo.function.apiQueueManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/queue")
public class ApiQueuePageController {

    @GetMapping
    public String queuePage(@RequestParam("itemId") Long itemId, Model model) {
        model.addAttribute("itemId", itemId);
        return "apiQueueManagement/queue"; // src/main/resources/templates/queue.html
    }


}

