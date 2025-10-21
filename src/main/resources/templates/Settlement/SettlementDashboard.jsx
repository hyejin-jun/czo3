import React, { useEffect, useState } from "react";
import axios from "axios";
import "./SettlementDashboard.css";

const SettlementDashboard = () => {
    const [settlements, setSettlements] = useState([]);

    useEffect(() => {
        fetchSettlements();
    }, []);

    const fetchSettlements = async () => {
        try {
            const res = await axios.get("/api/settlement/daily");
            setSettlements(res.data);
        } catch (err) {
            console.error("정산 데이터 로딩 실패", err);
        }
    };

    const calculateToday = async () => {
        try {
            await axios.post("/api/settlement/calculate");
            fetchSettlements();
        } catch (err) {
            console.error("오늘 정산 실패", err);
        }
    };

    return (
        <div className="dashboard-container">
            <h1 className="dashboard-title">관리자 통계 / 정산</h1>

            <button className="calculate-btn" onClick={calculateToday}>
                오늘 정산 계산
            </button>

            <div className="cards-container">
                {settlements.map((s) => (
                    <div key={s.settlementId} className="card">
                        <div className="card-header">{s.date}</div>
                        <div className="card-body">
                            <p>총 매출: {s.totalSales.toLocaleString()}원</p>
                            <p>총 원가: {s.totalCost.toLocaleString()}원</p>
                            <p>순이익: {s.netProfit.toLocaleString()}원</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SettlementDashboard;
