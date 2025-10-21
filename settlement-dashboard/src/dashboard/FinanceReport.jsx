import React, { useState, useEffect } from "react";
import axios from "axios";

export default function FinanceReport() {
    const [incomeData, setIncomeData] = useState([]);
    const [year, setYear] = useState(2025);
    const [month, setMonth] = useState(null);

    // ---------------- 재무제표 임의 데이터 ----------------
    const balanceSheetData = [
        { year: 2023, asset: 1_500_000_000, equity: 800_000_000, liability: 700_000_000 },
        { year: 2024, asset: 1_600_000_000, equity: 850_000_000, liability: 750_000_000 },
        { year: 2025, asset: 1_700_000_000, equity: 900_000_000, liability: 800_000_000 },
    ];

    // 법인세 계산
    const calculateCorporateTax = (profit) => {
        let tax = 0;
        if (profit <= 200_000_000) tax = profit * 0.09;
        else if (profit <= 20_000_000_000) tax = profit * 0.19 - 20_000_000;
        else if (profit <= 300_000_000_000) tax = profit * 0.21 - 420_000_000;
        else tax = profit * 0.24 - 9_420_000_000;
        return Math.max(tax, 0);
    };

    // ---------------- API 호출 ----------------
    const fetchData = async () => {
        try {
            const url = month
                ? `http://localhost:8000/api/sales/daily?year=${year}&month=${month}`
                : `http://localhost:8000/api/sales/monthly?year=${year}`;

            const res = await axios.get(url);
            console.log("✅ API 응답:", res.data);

            // ---------------- 데이터 가공 ----------------
            const processed = res.data.map((item) => ({
                year: Number(item.period.split("-")[0]),
                period: item.period,
                revenue: item.totalRevenue ?? 0,
                costPrice: item.totalCost ?? 0,
                grossProfit: item.profit ?? 0,
                deliveryFee: item.deliveryFee ?? 0,        // ✅ 판관비
                corporateTax: calculateCorporateTax(item.profit ?? 0),
                netIncome: (item.profit ?? 0) - (item.deliveryFee ?? 0) - calculateCorporateTax(item.profit ?? 0),
            }));

            setIncomeData(processed);
        } catch (err) {
            console.error("손익계산서 불러오기 오류:", err);
            setIncomeData([]);
        }
    };

    useEffect(() => {
        fetchData();
    }, [year, month]);

    // ---------------- 스타일 ----------------
    const tableStyle = { width: "100%", borderCollapse: "collapse", marginTop: "10px", fontSize: "14px" };
    const thTdStyle = { border: "1px solid #ddd", padding: "8px", textAlign: "center" };
    const headerStyle = { ...thTdStyle, backgroundColor: "#2196f3", color: "#fff", fontWeight: "bold" };
    const cardStyle = { backgroundColor: "#fff", borderRadius: "16px", padding: "20px", boxShadow: "0 8px 20px rgba(0,0,0,0.1)", textAlign: "center", marginBottom: "30px" };

    // ---------------- 재무제표 총합 ----------------
    const totalBalance = balanceSheetData.reduce(
        (acc, curr) => {
            acc.asset += curr.asset;
            acc.equity += curr.equity;
            acc.liability += curr.liability;
            return acc;
        },
        { asset: 0, equity: 0, liability: 0 }
    );

    // ---------------- 손익계산서 총합 ----------------
    const totalIncome = incomeData.reduce(
        (acc, curr) => {
            acc.revenue += curr.revenue;
            acc.costPrice += curr.costPrice;
            acc.deliveryFee += curr.deliveryFee;
            acc.corporateTax += curr.corporateTax;
            acc.netIncome += curr.netIncome;
            return acc;
        },
        { revenue: 0, costPrice: 0, deliveryFee: 0, corporateTax: 0, netIncome: 0 }
    );

    return (
        <div style={{ padding: "30px", fontFamily: "Arial, sans-serif", backgroundColor: "#f0f4f8" }}>
            {/* ---------------- 재무제표 ---------------- */}
            <h2 style={{ textAlign: "center", marginBottom: "20px" }}>💹 재무제표 💹</h2>
            <div style={cardStyle}>
                <table style={tableStyle}>
                    <thead>
                    <tr>
                        <th style={headerStyle}>연도</th>
                        <th style={headerStyle}>자산</th>
                        <th style={headerStyle}>자본</th>
                        <th style={headerStyle}>부채</th>
                    </tr>
                    </thead>
                    <tbody>
                    {balanceSheetData.map((item) => (
                        <tr key={item.year}>
                            <td style={thTdStyle}>{item.year}</td>
                            <td style={thTdStyle}>{item.asset.toLocaleString()} 원</td>
                            <td style={thTdStyle}>{item.equity.toLocaleString()} 원</td>
                            <td style={thTdStyle}>{item.liability.toLocaleString()} 원</td>
                        </tr>
                    ))}
                    <tr style={{ backgroundColor: "#c8e6c9", fontWeight: "bold" }}>
                        <td style={thTdStyle}>총합</td>
                        <td style={thTdStyle}>{totalBalance.asset.toLocaleString()} 원</td>
                        <td style={thTdStyle}>{totalBalance.equity.toLocaleString()} 원</td>
                        <td style={thTdStyle}>{totalBalance.liability.toLocaleString()} 원</td>
                    </tr>
                    </tbody>
                </table>
            </div>

            {/* ---------------- 손익계산서 ---------------- */}
            <h2 style={{ textAlign: "center", marginBottom: "20px" }}>💰 손익계산서 💰</h2>
            <div style={{ textAlign: "center", marginBottom: "20px" }}>
                연도:
                <select value={year} onChange={(e) => setYear(Number(e.target.value))} style={{ margin: "0 10px" }}>
                    <option value={2025}>2025</option>
                    <option value={2024}>2024</option>
                    <option value={2023}>2023</option>
                </select>
                월:
                <select value={month ?? ""} onChange={(e) => setMonth(e.target.value ? Number(e.target.value) : null)} style={{ margin: "0 10px" }}>
                    <option value="">전체</option>
                    {[...Array(12)].map((_, i) => (
                        <option key={i+1} value={i+1}>{i+1}</option>
                    ))}
                </select>
            </div>

            <div style={cardStyle}>
                <table style={tableStyle}>
                    <thead>
                    <tr>
                        <th style={headerStyle}></th>
                        {incomeData.map((item) => (
                            <th key={item.period} style={headerStyle}>{item.period}</th>
                        ))}
                        <th style={{ ...headerStyle, backgroundColor: "#4caf50" }}>총합</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style={thTdStyle}><b>매출액</b></td>
                        {incomeData.map((item) => <td key={item.period} style={thTdStyle}>{item.revenue.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold" }}>{totalIncome.revenue.toLocaleString()} 원</td>
                    </tr>
                    <tr>
                        <td style={{ ...thTdStyle, color: "##000" }}><b>매출원가</b></td>
                        {incomeData.map((item) => <td key={item.period} style={{ ...thTdStyle, color: "#000" }}>{item.costPrice.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold", color: "#000" }}>{totalIncome.costPrice.toLocaleString()} 원</td>
                    </tr>
                    <tr>
                        <td style={thTdStyle}><b>매출총이익</b></td>
                        {incomeData.map((item) => <td key={item.period} style={thTdStyle}>{item.grossProfit.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold" }}>{totalIncome.revenue - totalIncome.costPrice} 원</td>
                    </tr>
                    <tr>
                        <td style={thTdStyle}><b>판매비와 관리비</b></td>
                        {incomeData.map((item) => <td key={item.period} style={thTdStyle}>{item.deliveryFee.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold" }}>{totalIncome.deliveryFee.toLocaleString()} 원</td>
                    </tr>
                    <tr>
                        <td style={{ ...thTdStyle, color: "#000" }}><b>법인세</b></td>
                        {incomeData.map((item) => <td key={item.period} style={{ ...thTdStyle, color: "#000" }}>{item.corporateTax.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold", color: "#000" }}>{totalIncome.corporateTax.toLocaleString()} 원</td>
                    </tr>
                    <tr>
                        <td style={{ ...thTdStyle, color: "#f44336" }}><b>당기순이익</b></td>
                        {incomeData.map((item) => <td key={item.period} style={{ ...thTdStyle, color: "#f44336" }}>{item.netIncome.toLocaleString()} 원</td>)}
                        <td style={{ ...thTdStyle, fontWeight: "bold", color: "#f44336" }}>{totalIncome.netIncome.toLocaleString()} 원</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
}
