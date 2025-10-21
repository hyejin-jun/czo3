import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import ChatBotLauncher from "./chatbot/ChatBotLauncher";

// 본문 React
const root = createRoot(document.getElementById("root"));
root.render(
    <BrowserRouter>
        <App />
    </BrowserRouter>
);

// 챗봇 React (항상 고정)
const chatbotRoot = document.getElementById("chatbot-root");
if (chatbotRoot) {
    const chatbot = createRoot(chatbotRoot);
    chatbot.render(<ChatBotLauncher />);
}
