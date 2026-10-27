package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

@Dao
public interface CategoryDao {
    @Insert
    long insertCategory(CategoryEntity category);

    @Update
    int updateCategory(CategoryEntity category);

    @Delete
    int deleteCategory(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE categoryId = :id LIMIT 1")
    CategoryEntity getCategoryById(long id);
    @Query("SELECT COUNT(*) FROM categories WHERE categoryName = :name AND type = :type AND (userId = :userId OR userId IS NULL OR userId <= 0)")
    int countCategoryByName(String name, String type, int userId);
    // Kiểm tra số lượng giao dịch đang gắn với danh mục này
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryID = :categoryId")
    int countTransactionsUsingCategory(long categoryId);

    // Lấy danh mục hiển thị theo loại (Bao gồm danh mục mặc định userId <= 0 hoặc danh mục của user hiện tại)
    @Query("SELECT * FROM categories WHERE type = :type AND (userId IS NULL OR userId <= 0 OR userId = :userId)")
    List<CategoryEntity> getCategoriesByType(String type, long userId);
}