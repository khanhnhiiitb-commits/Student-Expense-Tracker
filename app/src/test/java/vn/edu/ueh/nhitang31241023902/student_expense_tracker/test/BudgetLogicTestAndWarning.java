package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BudgetLogicTestAndWarning {

    public double tinhTienConLai(double tongNganSach, double daChi) {
        return tongNganSach - daChi;
    }

    public double tinhPhanTramSuDung(double tongNganSach, double daChi) {
        if (tongNganSach <= 0) return 0;
        return (daChi / tongNganSach) * 100;
    }

    public String kiemTraCanhBao(double tongNganSach, double daChi) {
        if (tongNganSach <= 0) return "Ngân sách không hợp lệ";

        double phanTram = tinhPhanTramSuDung(tongNganSach, daChi);

        if (phanTram > 100) {
            return "Vượt ngân sách";
        } else if (phanTram > 80 && phanTram <= 100) {
            return "Cảnh báo đã đạt ngưỡng";
        } else if (phanTram == 80) {
            return "Đạt ngưỡng 80%";
        } else {
            return "Chưa đạt ngưỡng";
        }
    }

    // --- 8 TEST CASES ---
    @Test
    public void testNganSachKhongHopLe() {
        assertEquals("Ngân sách không hợp lệ", kiemTraCanhBao(0, 500000));
        assertEquals("Ngân sách không hợp lệ", kiemTraCanhBao(-1000000, 500000));
    }

    @Test
    public void testDaChiBangKhong() {
        assertEquals("Chưa đạt ngưỡng", kiemTraCanhBao(10000000, 0));
    }

    @Test
    public void testDuoi80PhanTram() {
        assertEquals("Chưa đạt ngưỡng", kiemTraCanhBao(10000000, 5000000));
    }

    @Test
    public void testDung80PhanTram() {
        assertEquals("Đạt ngưỡng 80%", kiemTraCanhBao(10000000, 8000000));
    }

    @Test
    public void testTren80DenDuoi100PhanTram() {
        assertEquals("Cảnh báo đã đạt ngưỡng", kiemTraCanhBao(10000000, 9000000));
    }

    @Test
    public void testDung100PhanTram() {
        assertEquals("Cảnh báo đã đạt ngưỡng", kiemTraCanhBao(10000000, 10000000));
    }

    @Test
    public void testVuotMotLuongNho() {
        assertEquals("Vượt ngân sách", kiemTraCanhBao(10000000, 10000001));
    }

    @Test
    public void testPhanTramKhiVuotNganSach() {
        double phanTram = tinhPhanTramSuDung(10000000, 15000000);
        assertEquals(150.0, phanTram, 0.001);
        assertEquals("Vượt ngân sách", kiemTraCanhBao(10000000, 15000000));
    }
}