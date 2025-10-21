document.addEventListener("DOMContentLoaded", () => {
    // ✅ 이메일 발송 버튼
    const sendEmailBtn = document.getElementById("sendEmailBtn");
    if (sendEmailBtn) {
        sendEmailBtn.addEventListener("click", function () {
            const email = document.getElementById("memail").value;
            if (!email) {
                alert("이메일을 입력해주세요.");
                return;
            }

            fetch(`/api/mail/send?receiver=${encodeURIComponent(email)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.code === 200) {
                        document.getElementById("emailMessage").textContent =
                            `인증번호가 ${email}로 발송되었습니다.`;
                        document.getElementById("emailMessage").style.color = "green";
                        document.getElementById("verifySection").style.display = "block";
                    } else {
                        document.getElementById("emailMessage").textContent = data.message;
                        document.getElementById("emailMessage").style.color = "red";
                    }
                })
                .catch(err => {
                    console.error(err);
                    document.getElementById("emailMessage").textContent = "메일 발송 중 오류 발생";
                    document.getElementById("emailMessage").style.color = "red";
                });
        });
    }

    // ✅ 비밀번호 찾기용 인증 버튼 (verifyBtnPW)
    const verifyBtnPW = document.getElementById("verifyBtnPW");
    if (verifyBtnPW) {
        verifyBtnPW.addEventListener("click", function () {
            const email = document.getElementById("memail").value;
            const code = document.getElementById("emailCode").value;

            if (!code) {
                alert("인증번호를 입력해주세요.");
                return;
            }

            fetch('/api/mail/verify', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: `receiver=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.verified) {
                        alert("이메일 인증 완료!");
                        window.emailVerified = true;
                        const findPwBtn = document.getElementById("findPwBtn");
                        if (findPwBtn) {
                            findPwBtn.disabled = false;
                        }
                        const verifyMessage = document.getElementById("verifyMessage");
                        if (verifyMessage) {
                            verifyMessage.textContent = "인증 완료!";
                            verifyMessage.style.color = "green";
                        }
                    } else {
                        alert("인증번호가 맞지 않습니다.");
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert("인증 중 오류 발생");
                });
        });
    }

    // ✅ 회원가입용 인증 버튼 (verifyBtn)
    const verifyBtn = document.getElementById("verifyBtn");
    if (verifyBtn) {
        verifyBtn.addEventListener("click", function () {
            const email = document.getElementById("memail").value;
            const code = document.getElementById("emailCode").value;

            if (!code) {
                alert("인증번호를 입력해주세요.");
                return;
            }

            fetch('/api/mail/verify', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: `receiver=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.verified) {
                        alert("이메일 인증 완료!");
                        document.getElementById("hiddenMemail").value = email;
                        document.getElementById("registerForm").style.display = "block";
                    } else {
                        alert("인증번호가 맞지 않습니다.");
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert("인증 중 오류 발생");
                });
        });
    }
});
