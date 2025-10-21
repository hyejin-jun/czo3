
// ========================
// AccessToken 관리
// ========================
var accessToken = null;
var refreshing = null; // 여기서 전역 선언

function setAccessToken(token) {
    if (accessToken === token) return; // 이미 같은 토큰이면 스케줄링 안 함
    accessToken = token;
    console.log("[TokenManager] setAccessToken 호출됨:", accessToken);
    scheduleTokenRefresh(accessToken);
}
function getAccessToken() {
    console.log("[TokenManager] getAccessToken 호출됨:", accessToken);
    return accessToken;
}
function clearAccessToken() {
    accessToken = null;
    console.log("[TokenManager] clearAccessToken 호출됨");
}

// ========================
// RefreshToken으로 AccessToken 발급
// ========================
async function refreshAccessToken(retry = 10) {
    try {
        const res = await fetch('/api/auth/refresh', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!res.ok) throw new Error('RefreshToken 만료');
        const data = await res.json();
        setAccessToken(data.data.accessToken);
        return data.data.accessToken;
    } catch (err) {
        console.error("[TokenManager] AccessToken 재발급 실패:", err);
        if (retry > 0) {
            console.log("[TokenManager] 재시도...");
            await new Promise(r => setTimeout(r, 500)); // 0.5초 후 재시도
            return refreshAccessToken(retry - 1);
        }
        clearAccessToken();
        return null;
    }
}

async function refreshAccessTokenOnce() {
    if (refreshing) return refreshing; // 이미 진행 중이면 기다리기
    refreshing = (async () => {
        try {
            const res = await fetch('/api/auth/refresh', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });
            if (!res.ok) throw new Error('RefreshToken 만료');
            const data = await res.json();
            setAccessToken(data.data.accessToken);
            return data.data.accessToken;
        } finally {
            refreshing = null; // 완료되면 초기화
        }
    })();
    return refreshing;
}

// ========================
// JWT 만료 시간 추출
// ========================
function getTokenExpiry(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log("[TokenManager] 토큰 만료 시간 추출:", payload.exp);
        return payload.exp; // 초 단위 timestamp
    } catch (e) {
        console.error("[TokenManager] 토큰 만료 시간 추출 실패:", e);
        return null;
    }
}

// ========================
// AccessToken 자동 갱신
// ========================
let refreshTimeout;
function scheduleTokenRefresh(token) {
    if (!token) return;

    const exp = getTokenExpiry(token);
    if (!exp) return;

    const now = Math.floor(Date.now() / 1000);

    // 만료 10초 전 갱신, 최소 1초 지연 보장
    const delay = Math.max((exp - now - 10) * 1000, 1000);

    console.log("[TokenManager] AccessToken 자동 갱신 스케줄:", delay, "ms 후");

    if (refreshTimeout) clearTimeout(refreshTimeout);
    refreshTimeout = setTimeout(async () => {
        console.log("[TokenManager] 자동 갱신 타이머 실행");
        const newToken = await refreshAccessToken();

        // 새 AccessToken 스케줄링
        if (newToken) scheduleTokenRefresh(newToken);
    }, delay);
}

// ========================
// API 호출 시 AccessToken 자동 갱신
// ========================
async function fetchWithToken(url, options = {}) {
    if (!options.headers) options.headers = {};
    let token = getAccessToken();
    if (!token) token = await refreshAccessTokenOnce();
    options.headers['Authorization'] = 'Bearer ' + token;

    let res = await fetch(url, options);
    if (res.status === 401) {
        token = await refreshAccessTokenOnce();
        options.headers['Authorization'] = 'Bearer ' + token;
        res = await fetch(url, options);
    }
    return res;
}

// ========================
// 페이지 로드 시 RefreshToken으로 AccessToken 발급
// ========================

async function initOnPageLoad() {
    console.log("[TokenManager] initOnPageLoad 시작");
    const token = getAccessToken();
    const exp = token ? getTokenExpiry(token) : null;
    const now = Math.floor(Date.now() / 1000);

    // 유효한 AccessToken 없거나 만료 임박이면 refresh
    if (!token || !exp || exp - now < 10) {
        await refreshAccessTokenOnce();
    } else {
        scheduleTokenRefresh(token);
    }
}
// ========================
// 전역 노출
// ========================
window.tokenManager = {
    setAccessToken,
    getAccessToken,
    clearAccessToken,
    refreshAccessToken,
    fetchWithToken,
    scheduleTokenRefresh,
    initOnPageLoad
};

console.log("[TokenManager] 초기화 완료");
