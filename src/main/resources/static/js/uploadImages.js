console.log("JS 파일 실행 시작");

import axios from 'https://cdn.jsdelivr.net/npm/axios@1.6.6/dist/esm/axios.min.js';

// ================= UUID 생성 =================
function generateUUID() {
    if (window.crypto && window.crypto.randomUUID) return window.crypto.randomUUID();
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// ================= tempKey 초기화 =================
function initTempKey() {
    const tempInput = document.getElementById("tempKey");
    let tempKey = tempInput?.value;
    if (!tempKey) {
        tempKey = generateUUID();
        if (tempInput) tempInput.value = tempKey;
    }
    console.log("현재 tempKey:", tempKey);
    return tempKey;
}

// ================= 이미지 삭제 =================
async function deleteImage(url, storedFileName, extraData = {}) {
    try {
        const formData = new FormData();
        formData.append("storedFileName", storedFileName);
        for (let key in extraData) {
            formData.append(`extraData[${key}]`, extraData[key]);
        }
        console.log("삭제 요청 보내기:", url, storedFileName, extraData);
        await axios.delete(url, {
            data: formData,
            headers: { "Content-Type": "multipart/form-data" }
        });
        console.log("삭제 성공:", storedFileName);
    } catch (err) {
        console.error("삭제 실패", err);
        throw err;
    }
}

// ================= 이미지 업로드 =================
async function uploadImage(url, files, extraData = {}) {
    const formData = new FormData();
    for (let file of files) formData.append("files", file);
    for (let key in extraData) formData.append(`extraData[${key}]`, extraData[key]);
    console.log("업로드 요청 보내기:", url, files, extraData);
    const response = await axios.post(url, formData, {
        headers: { "Content-Type": "multipart/form-data" }
    });
    console.log("업로드 응답:", response.data);
    return response.data; // { files: [...], urls: [...] } 반환 가정
}

// ================= 미리보기 생성 =================
function addPreviewImage(url, extraData = {}) {
    const preview = document.getElementById("preview");
    if (!preview) return;

    const container = document.createElement("div");
    container.classList.add("preview-item");
    container.style.display = "inline-block";
    container.style.margin = "5px";

    const img = document.createElement("img");
    img.src = url;
    img.style.width = "100px";
    img.style.height = "100px";
    img.style.objectFit = "cover";
    img.style.display = "block";
    img.style.marginBottom = "5px";

    const btn = document.createElement("button");
    btn.textContent = "삭제";
    btn.style.display = "block";

    const form = document.querySelector("form"); // itemFormDto가 있는 폼 선택
    const hiddenInput = document.createElement("input");
    hiddenInput.type = "hidden";
    hiddenInput.name = "fileNames";
    hiddenInput.value = url;
    container.appendChild(hiddenInput);

    if (typeof uploadedImages !== "undefined" && Array.isArray(uploadedImages)) {
        uploadedImages.push(url);
    }

    const storedFileName = url.split("/").pop();

    btn.addEventListener("click", async (e) => {
        e.preventDefault();
        try {
            await deleteImage('/api/upload/delete', storedFileName, extraData);
            container.remove();
        } catch (err) {
            alert("이미지 삭제 실패!");
        }
    });

    container.appendChild(img);
    container.appendChild(btn);
    preview.appendChild(container);
}

// ================= 업로드 버튼 이벤트 =================
document.querySelectorAll(".uploadBtn").forEach(button => {
    button.addEventListener("click", async () => {
        const type = button.dataset.type;
        let input, extraData = {};

        switch(type) {
            case "profile":
                input = document.getElementById("profileInput");
                extraData.userId = document.getElementById("mid").value;
                break;
            case "product":
                input = document.getElementById("productInput");
                extraData.productId = button.dataset.productId || "temp";
                extraData.tempKey = initTempKey();
                break;
            case "board":
                input = document.getElementById("boardInput");
                const boardId = button.dataset.boardId || "temp";
                extraData.boardId = boardId;
                if (boardId === "temp") extraData.tempKey = initTempKey();
                break;
            case "chat":
                input = document.getElementById("chatRoomInput");

                // fetchMyId() 호출 후 myId 사용
                const res = await window.tokenManager.fetchWithToken('/api/members/me');
                const data = await res.json();
                if (!res.ok || !data.data?.mid) return alert("로그인 정보가 없습니다.");
                extraData.userId = data.data.mid;

                extraData.roomId = button.dataset.roomId || document.getElementById("roomId")?.value;
                break;

        }

        if (!input.files.length) return alert("파일을 선택해주세요.");

        try {
            const res = await uploadImage(`/api/upload/image`, input.files, extraData);
            const urls = res.data?.urls || [];
            urls.forEach(url => addPreviewImage(url, extraData));
            console.log(urls);
            alert(`${type} 업로드 성공!`);
        } catch (err) {
            console.error(err);
            alert(`${type} 업로드 실패!`);
        }
    });
});

// ================= 페이지 로딩 시 기존 DB 이미지 미리보기 =================
document.addEventListener("DOMContentLoaded", () => {
    // productId 가져오기
    const productId = document.getElementById("productInput")?.dataset.productId;

    // 기존 DB 이미지 hidden input들
    const existingImages = document.querySelectorAll(".existing-image");
    existingImages.forEach(input => {
        const url = input.value;
        if (url) addPreviewImage(url, { productId }); // 삭제 시 productId 포함
    });
});
