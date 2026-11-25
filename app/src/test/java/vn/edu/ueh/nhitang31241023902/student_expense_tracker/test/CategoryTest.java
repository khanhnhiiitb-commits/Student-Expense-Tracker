
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.*;

public class CategoryTest {

    // Mô phỏng quy tắc kiểm tra tên trong AddCategoryActivity
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

    // UT_CAT_03: Tên chỉ chứa khoảng trắng
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

    // UT_CAT_06: Tên có khoảng trắng đầu cuối
    @Test
    public void testCategoryName_WithSpaces() {
        String name = "  Ăn uống  ";

        assertTrue(isValidCategoryName(name));
        assertEquals("Ăn uống", name.trim());
    }

    // UT_CAT_07: Tên danh mục dài 256 ký tự
    @Test
    public void testCategoryName_LongName() {
        String name = new String(new char[256]).replace('\0', 'A');

        assertTrue(isValidCategoryName(name));
    }

    // UT_CAT_08: Tên chứa ký tự đặc biệt
    @Test
    public void testCategoryName_SpecialCharacters() {
        assertTrue(isValidCategoryName("Ăn uống @#$"));
    }
    // UT_CAT_09: Không cho phép tên danh mục trùng
    @Test
    public void testCategoryName_Duplicate() {
        String existingName = "Ăn uống";
        String newName = "Ăn uống";

        // Quy tắc hiện tại chỉ kiểm tra tên có rỗng hay không
        boolean actual = isValidCategoryName(newName);

        // Yêu cầu: tên đã tồn tại phải bị từ chối
        assertFalse(actual);
    }
}
