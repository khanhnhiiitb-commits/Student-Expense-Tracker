package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BudgetLogicTestAndWarning {

    // ==========================================
    // PHẦN 1: MÔ PHỎNG LOGIC TÍNH TOÁN NGÂN SÁCH
    // ==========================================

    public double tinhTienConLai(double tongNganSach, double daChi) {
        return tongNganSach - daChi;
    }

    public double tinhPhanTramSuDung(double tongNganSach, double daChi) {
        if (tongNganSach <= 0) return 0; // Tránh lỗi chia cho 0
        return (daChi / tongNganSach) * 100;
    }

    // ==========================================
    // PHẦN 2: MÔ PHỎNG LOGIC CẢNH BÁO NGÂN SÁCH
    // ==========================================
    public String kiemTraCanhBao(double tongNganSach, double daChi) {
        if (tongNganSach <= 0) return "LỖI_NGÂN_SÁCH";

        double phanTram = tinhPhanTramSuDung(tongNganSach, daChi);

        if (phanTram > 100) {
            return "VƯỢT_NGƯỠNG";
        } else if (phanTram >= 80) {
            return "ĐẠT_NGƯỠNG_80";
        } else {
            return "CHƯA_ĐẠT_NGƯỠNG";
        }
    }


    // ==========================================
    // CÁC KỊCH BẢN TEST (TEST CASES) BÊN DƯỚI
    // (Lỗi initializationError xảy ra vì bạn đã xóa mất phần này)
    // ==========================================

    @Test
    public void testTinhTienConLai() {
        // Ngân sách 10tr, tiêu 3tr -> Còn lại 7tr
        double conLai = tinhTienConLai(10000000, 3000000);
        assertEquals(7000000, conLai, 0.001);
    }

    @Test
    public void testTinhPhanTramSuDung() {
        // Ngân sách 10tr, tiêu 5tr -> Sử dụng 50%
        double phanTram = tinhPhanTramSuDung(10000000, 5000000);
        assertEquals(50.0, phanTram, 0.001);
    }

    @Test
    public void testCanhBao_ChuaDatNguong() {
        // Tiêu 2tr/10tr (20%) -> Không cảnh báo
        String ketQua = kiemTraCanhBao(10000000, 2000000);
        assertEquals("CHƯA_ĐẠT_NGƯỠNG", ketQua);
    }

    @Test
    public void testCanhBao_DatNguong80() {
        // Tiêu 8tr/10tr (80%) -> Cảnh báo sắp hết
        String ketQua = kiemTraCanhBao(10000000, 8000000);
        assertEquals("ĐẠT_NGƯỠNG_80", ketQua);
    }

    @Test
    public void testCanhBao_VuotNguong() {
        // Tiêu 12tr/10tr (120%) -> Cảnh báo vượt mức
        String ketQua = kiemTraCanhBao(10000000, 12000000);
        assertEquals("VƯỢT_NGƯỠNG", ketQua);
    }
}