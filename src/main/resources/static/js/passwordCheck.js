const pwMessage = document.getElementById("pwMessage");
const checkBtn = document.getElementById("checkBtn");
const submitBtn = document.getElementById("submitBtn");

// 1단계: 비밀번호 검증
checkBtn.addEventListener("click", () => {
    const pw = document.getElementById("mpassword").value;
    const pwCheck = document.getElementById("mpasswordcheck").value;

    // 정규식: 최소 6자리, 영문자+숫자 필수
    const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/;

    if (!pwRegex.test(pw)) {
        pwMessage.textContent = "비밀번호는 최소 6자리 이상, 영문자와 숫자를 포함해야 합니다.";
        pwMessage.className = "error";
        submitBtn.disabled = true;
        return;
    }

    if (pw !== pwCheck) {
        pwMessage.textContent = "비밀번호 확인이 일치하지 않습니다.";
        pwMessage.className = "error";
        submitBtn.disabled = true;
        return;
    }

    pwMessage.textContent = "비밀번호 확인 완료. 제출할 수 있습니다.";
    pwMessage.className = "success";

    // 조건 만족 → Submit 버튼 활성화
    submitBtn.disabled = false;
});