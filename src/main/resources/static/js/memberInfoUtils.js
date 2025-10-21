
console.log("memberInfoUtils실행")
export async function initMemberPage() {
    // fetchWithToken 사용 시, AccessToken 없으면 자동 발급됨
    await loadMemberInfo();
}
// 회원정보를 폼에 채우는 함수
export async function loadMemberInfo() {
    try {
        const res = await window.tokenManager.fetchWithToken('/api/members/me');
        const result = await res.json();

        if (res.ok && result.data) {
            const member = result.data;

            setFormValue("mid", member.mid);
            setFormValue("mname", member.mname);
            setFormValue("mphoneNumber", member.mphoneNumber);
            setFormValue("memail", member.memail);
            setFormValue("mpostcode", member.mpostcode || "");
            setFormValue("maddress", member.maddress || "");
            setFormValue("mdetailAddress", member.mdetailAddress || "");
            setFormValue("profileImagePath", member.profileImagePath || "");
        } else {
            console.error("회원정보 불러오기 실패:", result);
        }
    } catch (err) {
        console.error("회원정보 불러오기 오류:", err);
    }
}

// 편의 함수: id로 input 요소 찾아 value 설정
function setFormValue(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = value;
}
