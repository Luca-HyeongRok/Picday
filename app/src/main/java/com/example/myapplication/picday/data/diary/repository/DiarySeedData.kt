package com.example.myapplication.picday.data.diary.repository

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

// InMemory 전용 시드 데이터
fun seedDiaryData(): List<Diary> {
    return listOf(
        Diary(
            id = "1",
            date = LocalDate.of(2023, 10, 5),
            title = "카페 방문",
            content = "도심에 있는 아늑한 카페를 발견했다. 라떼 아트가 훌륭했고 책 읽기 딱 좋은 분위기였다.",
            createdAt = 1696494000000
        ),
        Diary(
            id = "2",
            date = LocalDate.of(2023, 10, 4),
            title = "프로젝트 마감",
            content = "드디어 회사 프로젝트를 끝냈다. 스트레스도 많았지만 보람찬 경험이었다. 이제 좀 쉬자!",
            createdAt = 1696407600000
        ),
        Diary(
            id = "3",
            date = LocalDate.of(2023, 10, 1),
            title = "가을 등산",
            content = "친구들과 함께 등산을 다녀왔다. 정상에서 본 풍경은 정말 숨이 멎을 듯 아름다웠다. 다리는 아프지만 가치가 있었다.",
            createdAt = 1696148400000
        ),
        Diary(
            id = "4",
            date = LocalDate.of(2023, 9, 28),
            title = "비 오는 날",
            content = "하루 종일 비가 내렸다. 집에서 영화를 보고 쿠키를 구우며 시간을 보냈다.",
            createdAt = 1695889200000
        )
    )
}
