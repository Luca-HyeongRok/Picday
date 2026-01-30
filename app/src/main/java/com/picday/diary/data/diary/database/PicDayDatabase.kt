package com.picday.diary.data.diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.entity.DiaryEntity
import com.picday.diary.data.diary.entity.DiaryPhotoEntity

@Database(
    entities = [DiaryEntity::class, DiaryPhotoEntity::class], // 데이터베이스에 포함될 엔티티(테이블) 목록
    version = 2, // 데이터베이스 버전
    exportSchema = false // 스키마를 내보낼지 여부 (일반적으로 false)
)
abstract class PicDayDatabase : RoomDatabase() { // Room 데이터베이스 추상 클래스
    abstract fun diaryDao(): DiaryDao // Diary 테이블 접근을 위한 DAO
    abstract fun diaryPhotoDao(): DiaryPhotoDao // DiaryPhoto 테이블 접근을 위한 DAO

    companion object { // 데이터베이스 관련 정적 멤버를 위한 컴패니언 객체
        val MIGRATION_1_2 = object : Migration(1, 2) { // 버전 1에서 2로의 마이그레이션 정의
            override fun migrate(database: SupportSQLiteDatabase) { // 데이터베이스 마이그레이션 로직
                database.execSQL("ALTER TABLE diary ADD COLUMN coverPhotoUri TEXT") // diary 테이블에 coverPhotoUri 컬럼 추가
            }
        }
    }
}
