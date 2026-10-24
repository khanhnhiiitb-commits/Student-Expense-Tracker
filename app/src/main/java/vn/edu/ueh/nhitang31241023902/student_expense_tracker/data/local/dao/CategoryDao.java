package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(CategoryEntity category);

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategories();
}