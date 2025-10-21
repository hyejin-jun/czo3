// SalaryListPage.jsx
import React, { useEffect, useState } from "react";
import axios from "axios";

export default function SalaryListPage() {
    const [salaries, setSalaries] = useState([]);
    const [newFormData, setNewFormData] = useState({
        sname: "",
        sposition: "",
        sbasic: 0,
        sbonus: 0,
        stax: 0,
        month: new Date().getMonth() + 1, // 기본 현재 월
        active: "true",
    });
    const [editSid, setEditSid] = useState(null);
    const [editFormData, setEditFormData] = useState({});
    const [loading, setLoading] = useState(false);
    const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);

    const isValidSid = (sid) => sid !== undefined && sid !== null && !isNaN(Number(sid));

    // 월별 급여 불러오기
    const fetchSalaries = async (month = selectedMonth) => {
        setLoading(true);
        try {
            const res = await axios.get(`http://localhost:8000/api/salaries/month/${month}`);
            const data = Array.isArray(res.data)
                ? res.data.map((item) => ({
                    ...item,
                    sid: item.sid !== undefined ? Number(item.sid) : undefined,
                    sbasic: Number(item.sbasic || 0),
                    sbonus: Number(item.sbonus || 0),
                    stax: Number(item.stax || 0),
                    month: Number(item.month || month),
                    active: item.active === true ? "true" : "false",
                }))
                : [];
            setSalaries(data);
        } catch (err) {
            console.error("불러오기 실패:", err);
            alert("데이터 불러오기 실패: " + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSalaries(selectedMonth);
    }, [selectedMonth]);

    const handleMonthChange = (e) => {
        const month = Number(e.target.value);
        setSelectedMonth(month);
        setNewFormData((prev) => ({ ...prev, month }));
    };

    const handleNewChange = (e) => {
        const { name, value, type } = e.target;
        setNewFormData((prev) => ({
            ...prev,
            [name]: type === "number" ? Number(value) : value,
        }));
    };

    const handleEditChange = (e) => {
        const { name, value, type } = e.target;
        setEditFormData((prev) => ({
            ...prev,
            [name]: type === "number" ? Number(value) : value,
        }));
    };

    const handleSave = async () => {
        try {
            const payload = { ...newFormData, active: newFormData.active === "true" };
            const res = await axios.post("http://localhost:8000/api/salaries", payload);
            const saved = {
                ...res.data,
                sid: Number(res.data.sid),
                active: payload.active ? "true" : "false",
            };
            setSalaries((prev) => [saved, ...prev]);
            setNewFormData({
                sname: "",
                sposition: "",
                sbasic: 0,
                sbonus: 0,
                stax: 0,
                month: selectedMonth,
                active: "true",
            });
            alert("추가 성공");
        } catch (err) {
            console.error("추가 실패:", err);
            alert("추가 실패: " + (err.response?.data?.message || err.message));
        }
    };

    const handleEdit = (salary) => {
        const sidNum = salary.sid;
        if (!isValidSid(sidNum)) {
            alert("수정할 항목에 sid가 없습니다.");
            return;
        }
        setEditSid(sidNum);
        setEditFormData({
            ...salary,
            sbasic: Number(salary.sbasic || 0),
            sbonus: Number(salary.sbonus || 0),
            stax: Number(salary.stax || 0),
            month: Number(salary.month || selectedMonth), // 기존 월 가져오기
            active: salary.active === true ? "true" : "false",
        });
    };

    const handleUpdate = async () => {
        if (!isValidSid(editSid)) {
            alert("수정 실패: 유효한 sid가 아닙니다.");
            return;
        }
        try {
            const payload = { ...editFormData, active: editFormData.active === "true" };
            const res = await axios.put(`http://localhost:8000/api/salaries/${editSid}`, payload);
            const updated = {
                ...res.data,
                sid: Number(res.data.sid),
                active: payload.active ? "true" : "false",
            };
            setSalaries((prev) => prev.map((s) => (s.sid === editSid ? updated : s)));
            setEditSid(null);
            setEditFormData({});
            alert("수정 성공");
        } catch (err) {
            console.error("수정 실패:", err);
            alert("수정 실패: " + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (sid) => {
        if (!isValidSid(sid)) {
            alert("삭제 실패: 잘못된 sid: " + sid);
            return;
        }
        if (!window.confirm("정말 삭제하시겠습니까?")) return;
        try {
            await axios.delete(`http://localhost:8000/api/salaries/${sid}`);
            setSalaries((prev) => prev.filter((s) => s.sid !== sid));
            alert("삭제 성공");
        } catch (err) {
            console.error("삭제 실패:", err);
            alert("삭제 실패: " + (err.response?.data?.message || err.message));
        }
    };

    const calculateSalary = ({ sbasic, sbonus, stax }) =>
        (Number(sbasic || 0) + Number(sbonus || 0) - Number(stax || 0)).toLocaleString() + "원";

    const activeSalaries = salaries.filter((s) => s.active === "true");
    const totalSbasic = activeSalaries.reduce((acc, s) => acc + (s.sbasic || 0), 0);
    const totalSbonus = activeSalaries.reduce((acc, s) => acc + (s.sbonus || 0), 0);
    const totalStax = activeSalaries.reduce((acc, s) => acc + (s.stax || 0), 0);
    const totalSalary = activeSalaries.reduce(
        (acc, s) => acc + ((s.sbasic || 0) + (s.sbonus || 0) - (s.stax || 0)),
        0
    );

    return (
        <div className="flex justify-center items-start min-h-screen bg-green-50 p-8">
            <div className="w-full max-w-7xl p-6 bg-white shadow-xl rounded-2xl border border-green-200 text-center">
                <h1 className="text-3xl font-bold text-green-700 mb-6 text-center">급여 관리</h1>

                <div className="mb-4">
                    <label className="mr-2 font-bold">월 선택:</label>
                    <select value={selectedMonth} onChange={handleMonthChange} className="border p-1 rounded">
                        {Array.from({ length: 12 }, (_, i) => (
                            <option key={i + 1} value={i + 1}>{i + 1}월</option>
                        ))}
                    </select>
                </div>

                {loading && <div className="mb-4">로딩 중...</div>}

                <div className="overflow-x-auto">
                    <table className="w-full border-collapse text-center">
                        <thead>
                        <tr className="bg-pink-200">
                            <th className="p-3 rounded-tl-2xl">이름</th>
                            <th className="p-3">직급</th>
                            <th className="p-3">월</th>
                            <th className="p-3">기본급</th>
                            <th className="p-3">보너스</th>
                            <th className="p-3">세금</th>
                            <th className="p-3">실급여</th>
                            <th className="p-3">재직 여부</th>
                            <th className="p-3 rounded-tr-2xl">작업</th>
                        </tr>
                        </thead>
                        <tbody>
                        {/* 입력폼 */}
                        <tr className="bg-pink-50">
                            <td><input name="sname" value={newFormData.sname} onChange={handleNewChange} className="border p-1 rounded w-full text-center" /></td>
                            <td>
                                <select name="sposition" value={newFormData.sposition} onChange={handleNewChange} className="border p-1 rounded w-full text-center">
                                    <option value="">선택</option>
                                    <option value="인턴">인턴</option>
                                    <option value="사원">사원</option>
                                    <option value="대리">대리</option>
                                    <option value="과장">과장</option>
                                    <option value="차장">차장</option>
                                    <option value="부장">부장</option>
                                    <option value="이사">이사</option>
                                </select>
                            </td>
                            <td>
                                <select name="month" value={newFormData.month} onChange={handleNewChange} className="border p-1 rounded w-full text-center">
                                    {Array.from({ length: 12 }, (_, i) => (
                                        <option key={i + 1} value={i + 1}>{i + 1}월</option>
                                    ))}
                                </select>
                            </td>
                            <td><input name="sbasic" type="number" value={newFormData.sbasic} onChange={handleNewChange} className="border p-1 rounded w-full text-center" /></td>
                            <td><input name="sbonus" type="number" value={newFormData.sbonus} onChange={handleNewChange} className="border p-1 rounded w-full text-center" /></td>
                            <td><input name="stax" type="number" value={newFormData.stax} onChange={handleNewChange} className="border p-1 rounded w-full text-center" /></td>
                            <td>{calculateSalary(newFormData)}</td>
                            <td>
                                <select name="active" value={newFormData.active} onChange={handleNewChange} className="border p-1 rounded w-full text-center">
                                    <option value="true">재직</option>
                                    <option value="false">퇴사</option>
                                </select>
                            </td>
                            <td><button onClick={handleSave} className="bg-pink-400 hover:bg-pink-500 text-white px-3 py-1 rounded">추가</button></td>
                        </tr>

                        {/* 기존 데이터 */}
                        {salaries.map((s, idx) => (
                            <tr key={s.sid ?? idx} className="hover:bg-green-50">
                                {editSid === s.sid ? (
                                    <>
                                        <td><input name="sname" value={editFormData.sname || ""} onChange={handleEditChange} className="border p-1 rounded w-full text-center"/></td>
                                        <td>
                                            <select name="sposition" value={editFormData.sposition || ""} onChange={handleEditChange} className="border p-1 rounded w-full text-center">
                                                <option value="">선택</option>
                                                <option value="인턴">인턴</option>
                                                <option value="사원">사원</option>
                                                <option value="대리">대리</option>
                                                <option value="과장">과장</option>
                                                <option value="차장">차장</option>
                                                <option value="부장">부장</option>
                                                <option value="이사">이사</option>
                                            </select>
                                        </td>
                                        <td>
                                            <select name="month" value={editFormData.month || selectedMonth} onChange={handleEditChange} className="border p-1 rounded w-full text-center">
                                                {Array.from({ length: 12 }, (_, i) => (
                                                    <option key={i + 1} value={i + 1}>{i + 1}월</option>
                                                ))}
                                            </select>
                                        </td>
                                        <td><input name="sbasic" type="number" value={editFormData.sbasic || 0} onChange={handleEditChange} className="border p-1 rounded w-full text-center"/></td>
                                        <td><input name="sbonus" type="number" value={editFormData.sbonus || 0} onChange={handleEditChange} className="border p-1 rounded w-full text-center"/></td>
                                        <td><input name="stax" type="number" value={editFormData.stax || 0} onChange={handleEditChange} className="border p-1 rounded w-full text-center"/></td>
                                        <td>{calculateSalary(editFormData)}</td>
                                        <td>
                                            <select name="active" value={editFormData.active} onChange={handleEditChange} className="border p-1 rounded w-full text-center">
                                                <option value="true">재직</option>
                                                <option value="false">퇴사</option>
                                            </select>
                                        </td>
                                        <td>
                                            <button onClick={handleUpdate} className="bg-blue-500 text-white px-3 py-1 rounded mr-2">저장</button>
                                            <button onClick={() => { setEditSid(null); setEditFormData({}); }} className="bg-gray-400 text-white px-3 py-1 rounded">취소</button>
                                        </td>
                                    </>
                                ) : (
                                    <>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{s.sname}</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{s.sposition}</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{s.month}월</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{(s.sbasic || 0).toLocaleString()}원</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{(s.sbonus || 0).toLocaleString()}원</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{(s.stax || 0).toLocaleString()}원</td>
                                        <td className={s.active === "true" ? "" : "line-through text-gray-500"}>{calculateSalary(s)}</td>
                                        <td>{s.active === "true" ? "재직" : "퇴사"}</td>
                                        <td>
                                            <button onClick={() => handleEdit(s)} className="bg-yellow-400 text-white px-3 py-1 rounded mr-2">수정</button>
                                            <button onClick={() => handleDelete(s.sid)} className="bg-red-500 text-white px-3 py-1 rounded">삭제</button>
                                        </td>
                                    </>
                                )}
                            </tr>
                        ))}

                        {/* 합계 행 */}
                        <tr className="bg-green-100 font-bold">
                            <td>총합 (재직자)</td>
                            <td></td>
                            <td></td>
                            <td>{totalSbasic.toLocaleString()}원</td>
                            <td>{totalSbonus.toLocaleString()}원</td>
                            <td>{totalStax.toLocaleString()}원</td>
                            <td>{totalSalary.toLocaleString()}원</td>
                            <td colSpan={2}></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
