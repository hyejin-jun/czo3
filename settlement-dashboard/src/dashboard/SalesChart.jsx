import React, { useState, useEffect } from "react";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from "recharts";
import axios from "axios";

export default function SalesChart() {
    const [chartData, setChartData] = useState([]);
    const [viewType, setViewType] = useState("monthly"); // "monthly" or "daily"
    const [year, setYear] = useState(2025);
    const [month, setMonth] = useState(new Date().getMonth() + 1);

    const [totalSales, setTotalSales] = useState(0);
    const [totalCost, setTotalCost] = useState(0);
    const [totalProfit, setTotalProfit] = useState(0);
    const [corporateTax, setCorporateTax] = useState(0);
    const [vat, setVat] = useState(0);

    // 법인세 계산
    const calculateCorporateTax = (profit) => {
        if (profit <= 200_000_000) return profit * 0.09;
        if (profit <= 20_000_000_000) return profit * 0.19 - 20_000_000;
        if (profit <= 300_000_000_000) return profit * 0.21 - 420_000_000;
        return profit * 0.24 - 9_420_000_000;
    };



    useEffect(() => {
        const fetchData = async () => {
            try {
                const url = viewType === "monthly"
                    ? `http://localhost:8000/cart/order/api/sales/monthly?year=${year}`
                    : `http://localhost:8000/cart/order/api/sales/daily?year=${year}&month=${month}`;

                const res = await axios.get(url);
                const rawData = Array.isArray(res.data) ? res.data : [];
                console.log("API 응답:", rawData);

                let formattedData = [];

                if (viewType === "monthly") {
                    formattedData = Array.from({ length: 12 }, (_, i) => {
                        const entry = rawData.find(d => d.period.endsWith(`-${String(i + 1).padStart(2, "0")}`));
                        const revenue = Number(entry?.revenue ?? 0);
                        const cost = Number(entry?.cost ?? 0);
                        const profit = Number(entry?.profit ?? (revenue - cost));
                        return {
                            period: `${i + 1}월`,
                            totalRevenue: revenue,
                            totalCost: cost,
                            profit: profit
                        };
                    });
                } else {
                    const daysInMonth = new Date(year, month, 0).getDate();
                    formattedData = Array.from({ length: daysInMonth }, (_, i) => {
                        const day = i + 1;
                        const entry = rawData.find(d => {
                            const date = new Date(d.period);
                            return !isNaN(date) && date.getDate() === day;
                        });
                        const revenue = Number(entry?.revenue ?? 0);
                        const cost = Number(entry?.cost ?? 0);
                        const profit = Number(entry?.profit ?? (revenue - cost));
                        return {
                            period: `${day}일`,
                            totalRevenue: revenue,
                            totalCost: cost,
                            profit: profit
                        };
                    });
                }

                setChartData(formattedData);

                // 카드용 합계 계산
                const totalRevenue = formattedData.reduce((sum, d) => sum + d.totalRevenue, 0);
                const totalCost = formattedData.reduce((sum, d) => sum + d.totalCost, 0);
                const totalProfit = formattedData.reduce((sum, d) => sum + d.profit, 0);
                setTotalSales(totalRevenue);
                setTotalCost(totalCost);
                setTotalProfit(totalProfit);
                setCorporateTax(calculateCorporateTax(totalProfit));
                setVat(totalRevenue * 0.1);

            } catch (err) {
                console.error("API 호출 실패:", err);
            }
        };

        fetchData();
    }, [viewType, year, month]);

    return (
        <div style={{ padding: "20px" }}>
            <h2 style={{ textAlign: "center" }}>{year}년 매출 통계</h2>

            {/* 토글 버튼 */}
            <div style={{ marginBottom: "20px", textAlign: "center" }}>
                <button
                    onClick={() => setViewType("monthly")}
                    style={{
                        padding: "8px 16px",
                        marginRight: "10px",
                        backgroundColor: viewType === "monthly" ? "#4caf50" : "#ddd",
                        color: viewType === "monthly" ? "#fff" : "#000",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer"
                    }}
                >
                    월별
                </button>
                <button
                    onClick={() => setViewType("daily")}
                    style={{
                        padding: "8px 16px",
                        backgroundColor: viewType === "daily" ? "#4caf50" : "#ddd",
                        color: viewType === "daily" ? "#fff" : "#000",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer"
                    }}
                >
                    일별
                </button>

                {viewType === "daily" && (
                    <select
                        value={month}
                        onChange={e => setMonth(Number(e.target.value))}
                        style={{ marginLeft: "15px", padding: "6px" }}
                    >
                        {Array.from({ length: 12 }, (_, i) => (
                            <option key={i + 1} value={i + 1}>{i + 1}월</option>
                        ))}
                    </select>
                )}
            </div>

            {/* 카드 5개 */}
            <div style={{
                display: "flex",
                justifyContent: "center",
                gap: "20px",
                marginBottom: "30px",
                flexWrap: "nowrap"
            }}>
                <Card title="총 매출" value={totalSales} color="green"/>
                <Card title="총 원가" value={totalCost} color="red"/>
                <Card title="총 이익" value={totalProfit} color="blue"/>
                <Card title="법인세" value={corporateTax} color="purple"/>
                <Card title="부가가치세" value={vat} color="orange"/>
            </div>

            {/* 라인 차트 */}
            <ResponsiveContainer width="100%" height={400}>
                <LineChart data={chartData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3"/>
                    <XAxis dataKey="period"/>
                    <YAxis tickFormatter={value => `${(value / 100_000_000).toFixed(0)}억`}/>
                    <Tooltip formatter={value => Number(value).toLocaleString()}/>
                    <Legend/>
                    <Line type="monotone" dataKey="totalRevenue" stroke="#8884d8" name="매출액"/>
                    <Line type="monotone" dataKey="totalCost" stroke="#82ca9d" name="매출원가"/>
                    <Line type="monotone" dataKey="profit" stroke="#ff7300" name="총이익"/>
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}

function Card({ title, value, color }) {
    return (
        <div style={{
            backgroundColor: "#fff",
            boxShadow: "0 4px 20px rgba(0,0,0,0.1)",
            borderRadius: "12px",
            padding: "20px 30px",
            textAlign: "center",
            minWidth: "120px"
        }}>
            <h3 style={{ margin: 0, color }}>{title}</h3>
            <p style={{ fontSize: "18px", fontWeight: "bold", color: "gray" }}>
                {Number(value).toLocaleString()} 원
            </p>
        </div>
    );
}
