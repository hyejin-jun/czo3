import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function Basic({ children }) {
    const navigate = useNavigate();

    // 드롭다운 토글
    const toggleDropdown = (e) => {
        const menuItem = e.currentTarget;
        document.querySelectorAll('.menu-item').forEach(item => {
            if (item !== menuItem) item.classList.remove('active');
        });
        menuItem.classList.toggle('active');
    };

    // 로그인/로그아웃 버튼 상태 업데이트
    useEffect(() => {
        const updateButtons = () => {
            const token = window.tokenManager?.getAccessToken();
            const loginBtn = document.getElementById('login-btn');
            const logoutBtn = document.getElementById('logout-btn');

            if (!loginBtn || !logoutBtn) return;

            if (token) {
                loginBtn.style.display = 'none';
                logoutBtn.style.display = 'inline-block';
            } else {
                loginBtn.style.display = 'inline-block';
                logoutBtn.style.display = 'none';
            }
        };

        updateButtons();

        const logoutBtn = document.getElementById("logout-btn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", async () => {
                if (!window.confirm("로그아웃 하시겠습니까?")) return;

                try {
                    const res = await fetch("/api/auth/logout", {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Authorization": "Bearer " + window.tokenManager.getAccessToken(),
                            "Content-Type": "application/json"
                        }
                    });

                    if (res.ok) {
                        alert("로그아웃 성공");
                        window.tokenManager.clearAccessToken();
                        navigate("/");
                    } else {
                        const err = await res.json();
                        alert("로그아웃 실패: " + (err.message || res.status));
                    }
                } catch (e) {
                    console.error("로그아웃 요청 실패", e);
                    alert("로그아웃 요청 중 오류 발생");
                }
            });
        }

        document.addEventListener('click', function(event) {
            if (!event.target.closest('.menu-item')) {
                document.querySelectorAll('.menu-item').forEach(item => item.classList.remove('active'));
            }
        });
    }, [navigate]);

    return (
        <div className="wrapper">
            <header className="header">
                <div className="logo">
                    <a href="http://localhost:8000/" className="menu-item">
                        <img src="/images/benner.png" alt="Re:Life Logo"/>
                    </a>
                </div>

                <div className="menu-bar">
                    <a href="http://localhost:8000/" className="menu-item">HOME</a>

                    <div className="menu-item" onClick={toggleDropdown}>
                        NOTICE
                        <div className="dropdown">
                            <a href="http://localhost:8000/board/list" className="px-4 py-2 font-bold">공지사항</a>
                            <a href="http://localhost:8000/board/userList" className="px-4 py-2 font-bold">문의사항</a>
                            <a href="http://localhost:8000/" className="px-4 py-2 font-bold">이벤트</a>
                        </div>
                    </div>

                    <div className="menu-item" onClick={(e) => toggleDropdown(e)}>
                        SHOP
                        <div className="dropdown">
                            {/* 관리자 전용 메뉴 (기본 숨김) */}
                            <a href="/admin/item/new" id="menuItemForm" style={{display: 'none'}}>아이템폼</a>
                            <a href="/admin/items" id="menuItemList" style={{display: 'none'}}>아이템델타</a>

                            {/* 일반 메뉴 */}
                            <a href="http://localhost:8000/products">주방용품</a>
                            <a href="#">생활용품</a>
                            <a href="#">패션 잡화</a>
                            <a href="#">홈 케어</a>
                            <a href="#">펫 라이프</a>
                            <a href="#">웰빙</a>
                        </div>
                    </div>


                    <div className="menu-item" onClick={toggleDropdown}>
                        STATISTIC
                        <div className="dropdown">
                            <a href="http://localhost:3000/dashboard/SalaryListPage">급여 정산</a>
                            <a href="http://localhost:3000/dashboard/SettlementDashboard">통계 정산</a>
                            <a href="http://localhost:3000/dashboard/FinanceReport">재무제표</a>
                        </div>
                    </div>

                    <div className="menu-item">
                        <a href="http://localhost:8000/cart" className="menu-item">CART</a>
                    </div>

                    <div className="menu-item">
                        <a href="http://localhost:8000/members/userMyPage">MYPAGE</a>
                    </div>

                    <div className="user-actions">
                        <div className="search-container">
                            <input type="text" placeholder="검색" aria-label="Search"/>
                            <button className="Search-button">Search</button>
                            <button
                                id="login-btn"
                                className="login-button"
                                onClick={() => window.location.href = 'http://localhost:8000/members/login'}
                            >
                                Login
                            </button>

                            <button id="logout-btn" className="logout-button" style={{display: 'none'}}>Logout</button>
                        </div>
                    </div>
                </div>
            </header>
            <div className="divider"/>

        </div>
    );
}
