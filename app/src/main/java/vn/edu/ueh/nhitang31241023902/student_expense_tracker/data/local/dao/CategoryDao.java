package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

@Dao
public interface CategoryDao {
    @Insert
    void insertCategory(CategoryEntity category);

    @Insert
    void insertCategories(List<CategoryEntity> categories);

    @Query("SELECT * FROM categories WHERE categoryId = :id LIMIT 1")
    CategoryEntity getCategoryById(long id);
    @Query("SELECT DISTINCT * FROM categories WHERE type = :type AND (userId IS NULL OR userId = :userId OR userId = -1)")
    List<CategoryEntity> getCategoriesByType(String type, int userId);
}