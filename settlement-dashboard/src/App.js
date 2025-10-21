
import { Routes, Route, useLocation } from "react-router-dom";
import Basic from "./Basic"; // 헤더 역할
import SalesChart from "./dashboard/SalesChart";
import FinanceReport from "./dashboard/FinanceReport";
import SalaryListPage from "./dashboard/SalaryListPage";
import Footer from "./Footer";


function App() {
    const currentLocation = useLocation(); // ✅ 이름 변경

    return (
        <div className="layout">
            {/* 헤더 */}
            <Basic />

            <main className="content">
                {/* 메인 홈에서만 보이도록 조건부 렌더링 */}
                {currentLocation.pathname === "/" && (
                    <h1 style={{ textAlign: "center", margin: "20px 0", color: "#333" }}>
                        관리자 페이지입니다
                    </h1>
                )}

                {/* 페이지별 컴포넌트 */}
                <Routes>
                    <Route
                        path="/dashboard/SettlementDashboard"
                        element={<SalesChart />}
                    />
                    <Route path="/dashboard/FinanceReport" element={<FinanceReport />} />
                    <Route path="/dashboard/SalaryListPage" element={<SalaryListPage />} />
                </Routes>
            </main>

            {/* 푸터 */}
            <Footer />
        </div>
    );
}

export default App;