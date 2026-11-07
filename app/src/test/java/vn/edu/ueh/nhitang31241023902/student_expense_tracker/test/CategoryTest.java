package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.*;

public class CategoryTest {

    // Kiểm tra tên danh mục theo quy tắc trong AddCategoryActivity
    private boolean isValidCategoryName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    // UT_CAT_01: Tên danh mục hợp lệ
    @Test
    public void testCategoryName_Valid() {
        assertTrue(isValidCategoryName("Ăn uống"));
        assertTrue(isValidCategoryName("Học tập"));
    }

    // UT_CAT_02: Tên danh mục rỗng
    @Test
    public void testCategoryName_Empty() {
        assertFalse(isValidCategoryName(""));
    }

    // UT_CAT_03: Tên danh mục chỉ có khoảng trắng
    @Test
    public void testCategoryName_Whitespace() {
        assertFalse(isValidCategoryName("   "));
    }

    // UT_CAT_04: Tên danh mục null
    @Test
    public void testCategoryName_Null() {
        assertFalse(isValidCategoryName(null));
    }

    // UT_CAT_05: Tên danh mục có một ký tự
    @Test
    public void testCategoryName_OneCharacter() {
        assertTrue(isValidCategoryName("A"));
    }

    // UT_CAT_06: Tên danh mục có khoảng trắng đầu cuối
    @Test
    public void testCategoryName_WithSpaces() {
        assertTrue(isValidCategoryName("  Ăn uống  "));
    }
}
