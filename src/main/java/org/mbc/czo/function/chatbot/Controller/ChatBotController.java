package org.mbc.czo.function.chatbot.Controller;
import org.mbc.czo.function.chatbot.Entity.ChatBotNode;
import org.mbc.czo.function.chatbot.Repository.ChatBotNodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://192.168.0.183:3000")
public class ChatBotController {

    private final ChatBotNodeRepository nodeRepository;

    public ChatBotController(ChatBotNodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @GetMapping("/node")
    public ResponseEntity<?> getNode(@RequestParam String keyword) {
        ChatBotNode node = nodeRepository.findByKeyword(keyword).orElse(null);
        if (node == null) {
            return ResponseEntity.ok(Map.of("reply", "키워드 맞게 입력해 주세요 😅", "options", new String[]{}));
        }
        return ResponseEntity.ok(Map.of("reply", node.getReply(), "options", node.getOptions()));
    }
}

