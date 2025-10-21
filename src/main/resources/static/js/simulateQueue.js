

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const itemId = urlParams.get("itemId") || '0';

    const userId = "user-" + Math.floor(Math.random() * 1000000);

    const statusDiv = document.getElementById("queueStatus");
    const numberDiv = document.getElementById("queueNumber");

    let sse;
    let intervalId;

    async function enterQueue() {
        try {
            const res = await fetch("/api/queue/try-enter", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ itemId, userId })
            });

            const result = await res.json();
            const position = result.data?.position;

            if (position === -1 || position === 0) {
                // 0 또는 -1 → 바로 입장 처리 (원하는 로직에 맞게)
                statusDiv.innerText = "ENTER! 바로 입장 가능 🎉";
                statusDiv.classList.add("queue-enter");
                window.location.href = "/item/" + itemId;
                return;
            }

            // 대기열 참여 상태
            statusDiv.innerText = "대기열 참여 중...";
            numberDiv.innerText = position;

            // SSE 구독
            if (!sse) {
                sse = new EventSource(`/api/queue/sse?userId=${userId}`);

                sse.addEventListener("queue-enter", (e) => {
                    console.log("SSE data:", e.data);
                    if (e.data === "ENTER") {
                        statusDiv.innerText = "순번 도착! 입장 가능 🎉";
                        statusDiv.classList.add("queue-enter");
                        clearInterval(intervalId);
                        sse.close();
                        window.location.href = "/item/" + itemId;
                    }
                });

                sse.onerror = () => {
                    console.warn("[SSE] 연결 끊김, 1초 후 재연결");
                    sse.close();
                    sse = null;
                    setTimeout(() => enterQueue(), 1000); // 필요시 재연결
                };
            }

            // 2초마다 순번 갱신
            intervalId = setInterval(async () => {
                try {
                    const posRes = await fetch('/api/queue/position', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ itemId, userId })
                    });

                    const data = await posRes.json();
                    const pos = data.data?.position;

                    if (pos === -1) {
                        clearInterval(intervalId);
                        statusDiv.innerText = "순번 도착! 입장 가능 🎉";
                        statusDiv.classList.add("queue-enter");
                        if (sse) sse.close();
                        window.location.href = "/item/" + itemId;
                    } else {
                        numberDiv.innerText = pos;
                    }
                } catch (err) {
                    console.error("[Queue] 순번 갱신 실패:", err);
                }
            }, 500);

            // 페이지 떠나면 interval 정리
            window.addEventListener("beforeunload", () => {
                if (intervalId) clearInterval(intervalId);
                if (sse) sse.close();
            });

        } catch (err) {
            console.error("대기열 참여 오류:", err);
            statusDiv.innerText = "대기열 접속 오류";
            if (sse) sse.close();
        }
    }

    enterQueue();
});
