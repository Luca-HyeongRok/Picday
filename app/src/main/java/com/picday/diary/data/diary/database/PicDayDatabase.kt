package com.picday.diary.data.diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.dao.ExpenseDao
import com.picday.diary.data.diary.entity.DiaryEntity
import com.picday.diary.data.diary.entity.DiaryPhotoEntity
import com.picday.diary.data.diary.entity.ExpenseEntity

@Database(
    entities = [DiaryEntity::class, DiaryPhotoEntity::class, ExpenseEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PicDayDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryPhotoDao(): DiaryPhotoDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary ADD COLUMN coverPhotoUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 다이어리와 1:N 관계를 갖는 지출 테이블을 생성한다.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expense` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `diaryId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `category` TEXT,
                        `receiptImagePath` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`diaryId`) REFERENCES `diary`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_diaryId` ON `expense` (`diaryId`)")
            }
        }
    }
}
