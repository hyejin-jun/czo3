// 모달 열기 버튼
const openModalBtn = document.querySelector(".open-modal-btn");
// 모달 전체
const modal = document.querySelector(".modal");
// 닫기 버튼
const closeBtn = document.querySelector(".close");

// 버튼 클릭 시 모달 표시
openModalBtn.addEventListener("click", () => {
    modal.style.display = "block";
});

// 닫기 버튼 클릭 시 모달 숨김
closeBtn.addEventListener("click", () => {
    modal.style.display = "none";
});

// 모달 외부 클릭 시 모달 숨김
window.addEventListener("click", (e) => {
    if (e.target === modal) {
        modal.style.display = "none";
    }
});
