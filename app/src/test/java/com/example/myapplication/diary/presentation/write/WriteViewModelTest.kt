package com.picday.diary.presentation.write

import app.cash.turbine.test
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.domain.usecase.diary.AddDiaryForDateUseCase
import com.picday.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.picday.diary.domain.usecase.diary.GetDiaryByIdUseCase
import com.picday.diary.domain.usecase.diary.GetPhotosUseCase
import com.picday.diary.domain.usecase.diary.ReplacePhotosUseCase
import com.picday.diary.domain.usecase.diary.UpdateDiaryUseCase
import com.picday.diary.presentation.write.photo.WritePhotoItem
import com.picday.diary.presentation.write.photo.WritePhotoState
import com.picday.diary.presentation.write.state.WriteState
import com.picday.diary.presentation.write.state.WriteUiMode
import com.picday.diary.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * WriteViewModel의 비즈니스 로직을 검증하는 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WriteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var diaryRepository: FakeDiaryRepository
    private lateinit var viewModel: WriteViewModel

    @Before
    fun setUp() {
        diaryRepository = FakeDiaryRepository()
        viewModel = WriteViewModel(
            addDiaryForDate = AddDiaryForDateUseCase(diaryRepository),
            updateDiary = UpdateDiaryUseCase(diaryRepository),
            replacePhotos = ReplacePhotosUseCase(diaryRepository),
            getDiaryById = GetDiaryByIdUseCase(diaryRepository),
            getPhotos = GetPhotosUseCase(diaryRepository),
            deleteDiary = DeleteDiaryUseCase(diaryRepository)
        )
    }

    /**
     * 사진 여러 장 추가 시 photoUris가 기존 리스트와 정상적으로 병합되는지 확인
     */
    @Test
    fun `사진 추가 시 기존 리스트와 병합되어야 한다`() = runTest {
        // Given: 이미 한 장의 사진이 있는 상태
        viewModel.onPhotosAdded(listOf("uri1"))

        // When: 두 장의 사진을 추가
        viewModel.onPhotosAdded(listOf("uri2", "uri3"))

        // Then: 총 3장의 사진이 있어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.photoItems.size)
            assertEquals("uri2", state.photoItems[0].uri) // 신규가 앞으로
            assertEquals("uri3", state.photoItems[1].uri)
            assertEquals("uri1", state.photoItems[2].uri)
        }
    }

    /**
     * 동일한 사진을 여러 번 추가해도 중복되어 등록되지 않아야 한다
     */
    @Test
    fun `중복된 사진 URI는 추가되지 않아야 한다`() = runTest {
        // Given: uri1이 이미 존재
        viewModel.onPhotosAdded(listOf("uri1"))

        // When: uri1을 다시 추가 시도
        viewModel.onPhotosAdded(listOf("uri1", "uri2"))

        // Then: uri1은 하나만 존재하고, uri2가 새로 추가되어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.photoItems.size)
            val uris = state.photoItems.map { it.uri }
            assertTrue(uris.contains("uri1"))
            assertTrue(uris.contains("uri2"))
        }
    }

    /**
     * 편집 종료 시 대표 사진은 마지막 사진으로 계산되는지 확인
     */
    @Test
    fun `대표 사진은 마지막 사진으로 계산되어야 한다`() = runTest {
        // Given: uri1이 있는 상태
        viewModel.onPhotosAdded(listOf("uri1"))

        // When: uri2를 추가
        viewModel.onPhotosAdded(listOf("uri2"))

        // Then: 마지막 사진이 대표 사진으로 계산되어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("uri1", viewModel.getRepresentativePhotoUriForExit())
        }
    }

    /**
     * 사진 삭제 시 UI 상태(state = DELETE)가 즉시 반영되는지 확인
     */
    @Test
    fun `사진 삭제 시 해당 아이템의 상태가 DELETE로 변경되어야 한다`() = runTest {
        // Given: 사진 하나 추가
        viewModel.onPhotosAdded(listOf("uri1"))
        val photoId = viewModel.uiState.value.photoItems[0].id

        // When: 삭제 호출
        viewModel.onPhotoRemoved(photoId)

        // Then: 상태가 DELETE여야 하며, CoverPhotoUri에서 제외되어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            val item = state.photoItems.find { it.id == photoId }
            assertEquals(WritePhotoState.DELETE, item?.state)
            assertNull(viewModel.getCoverPhotoUri())
        }
    }

    /**
     * Edit 모드에서 초기화 시 기존 사진들이 로드되고, 이후 신규 사진을 추가해도 기존 사진이 함께 유지되는지 확인
     */
    @Test
    fun `수정 모드에서 기존 사진과 신규 사진이 공존해야 한다`() = runTest {
        // Given: 이미 저장된 데이터가 있는 상태
        val date = LocalDate.now()
        diaryRepository.addDiaryForDate(date, "Title", "Content", listOf("old_uri"))
        val existingDiaryId = diaryRepository.getByDate(date)[0].id

        // When: 수정 모드 진입
        viewModel.onEditClicked(existingDiaryId)

        // Then: EDIT 상태로 전환될 때까지 대기 후 신규 사진 추가 및 검증
        viewModel.uiState.test {
            // 초기 상태 또는 다른 상태에서 EDIT로 변할 때까지 대기
            var state = awaitItem()
            while (state.uiMode != WriteUiMode.EDIT) {
                state = awaitItem()
            }
            
            // 수동으로 신규 사진 추가
            viewModel.onPhotosAdded(listOf("new_uri"))
            
            // 신규 사진이 추가된 최종 상태 확인
            val finalState = awaitItem()
            assertEquals(WriteUiMode.EDIT, finalState.uiMode)
            assertEquals(2, finalState.photoItems.size)
            
            val states = finalState.photoItems.map { it.state }
            assertTrue(states.contains(WritePhotoState.KEEP))
            assertTrue(states.contains(WritePhotoState.NEW))
            
            val uris = finalState.photoItems.map { it.uri }
            assertTrue(uris.contains("old_uri"))
            assertTrue(uris.contains("new_uri"))
        }
    }

    @Test
    fun `baseline과 동일한 상태면 isDirty는 false다`() {
        val state = WriteState(
            uiMode = WriteUiMode.EDIT,
            baselineKey = "EDIT:1",
            baselineTitle = "title",
            baselineContent = "content",
            baselinePhotoUris = listOf("uri1"),
            title = "title",
            content = "content",
            photoItems = listOf(
                WritePhotoItem(id = "1", uri = "uri1", state = WritePhotoState.KEEP)
            )
        )

        assertFalse(computeWriteIsDirty(state))
    }

    @Test
    fun `title이 변경되면 isDirty는 true다`() {
        val state = WriteState(
            uiMode = WriteUiMode.EDIT,
            baselineKey = "EDIT:1",
            baselineTitle = "title",
            baselineContent = "content",
            baselinePhotoUris = listOf("uri1"),
            title = "changed",
            content = "content",
            photoItems = listOf(
                WritePhotoItem(id = "1", uri = "uri1", state = WritePhotoState.KEEP)
            )
        )

        assertTrue(computeWriteIsDirty(state))
    }

    @Test
    fun `사진이 삭제되면 isDirty는 true다`() {
        val state = WriteState(
            uiMode = WriteUiMode.EDIT,
            baselineKey = "EDIT:1",
            baselineTitle = "title",
            baselineContent = "content",
            baselinePhotoUris = listOf("uri1"),
            title = "title",
            content = "content",
            photoItems = listOf(
                WritePhotoItem(id = "1", uri = "uri1", state = WritePhotoState.DELETE)
            )
        )

        assertTrue(computeWriteIsDirty(state))
    }

    @Test
    fun `VIEW 모드에서는 isDirty가 항상 false다`() {
        val state = WriteState(
            uiMode = WriteUiMode.VIEW,
            baselineKey = "VIEW:1",
            baselineTitle = "title",
            baselineContent = "content",
            baselinePhotoUris = listOf("uri1"),
            title = "changed",
            content = "changed",
            photoItems = listOf(
                WritePhotoItem(id = "1", uri = "uri1", state = WritePhotoState.NEW)
            )
        )

        assertFalse(computeWriteIsDirty(state))
    }

    @Test
    fun `baselineKey가 null이면 isDirty는 false다`() {
        val state = WriteState(
            uiMode = WriteUiMode.EDIT,
            baselineKey = null,
            baselineTitle = "title",
            baselineContent = "content",
            baselinePhotoUris = listOf("uri1"),
            title = "changed",
            content = "changed",
            photoItems = listOf(
                WritePhotoItem(id = "1", uri = "uri1", state = WritePhotoState.NEW)
            )
        )

        assertFalse(computeWriteIsDirty(state))
    }

    @Test
    fun `ADD 저장 시 일기와 사진이 생성된다`() = runTest {
        val date = LocalDate.now()
        viewModel.onAddClicked()
        viewModel.onTitleChanged("Title")
        viewModel.onContentChanged("Content")
        viewModel.onPhotosAdded(listOf("uri1", "uri2"))

        var releasedUris: List<String>? = null

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.uiMode != WriteUiMode.ADD) {
                state = awaitItem()
            }

            viewModel.onSave(date) { releasedUris = it }

            do {
                state = awaitItem()
            } while (state.uiMode != WriteUiMode.VIEW)
        }

        val diaries = diaryRepository.getByDate(date)
        assertEquals(1, diaries.size)
        val photos = diaryRepository.getPhotos(diaries[0].id)
        assertEquals(2, photos.size)
        assertNull(releasedUris)
    }

    @Test
    fun `EDIT 저장 시 내용과 사진이 갱신되고 삭제된 content uri가 해제된다`() = runTest {
        val date = LocalDate.now()
        diaryRepository.addDiaryForDate(date, "Old", "Content", listOf("content://old", "content://keep"))
        val diaryId = diaryRepository.getByDate(date)[0].id

        viewModel.onEditClicked(diaryId)

        var releasedUris: List<String>? = null

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.uiMode != WriteUiMode.EDIT) {
                state = awaitItem()
            }

            val removeId = state.photoItems.first { it.uri == "content://old" }.id
            viewModel.onPhotoRemoved(removeId)
            viewModel.onPhotosAdded(listOf("content://new"))
            viewModel.onContentChanged("Changed")

            viewModel.onSave(date) { releasedUris = it }

            do {
                state = awaitItem()
            } while (state.uiMode != WriteUiMode.VIEW)
        }

        assertEquals(listOf("content://old"), releasedUris)

        val updatedDiary = diaryRepository.getDiaryById(diaryId)
        assertEquals("Changed", updatedDiary?.content)

        val updatedPhotoUris = diaryRepository.getPhotos(diaryId).map { it.uri }
        assertTrue(updatedPhotoUris.contains("content://keep"))
        assertTrue(updatedPhotoUris.contains("content://new"))
        assertFalse(updatedPhotoUris.contains("content://old"))
    }

    @Test
    fun `DELETE 호출 시 일기가 제거되고 VIEW로 복귀한다`() = runTest {
        val date = LocalDate.now()
        diaryRepository.addDiaryForDate(date, "Title", "Content")
        val diaryId = diaryRepository.getByDate(date)[0].id

        viewModel.onEditClicked(diaryId)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.uiMode != WriteUiMode.EDIT) {
                state = awaitItem()
            }

            viewModel.onDelete(diaryId)

            do {
                state = awaitItem()
            } while (state.uiMode != WriteUiMode.VIEW)
        }

        assertNull(diaryRepository.getDiaryById(diaryId))
    }

    @Test
    fun `ADD 전환 시 baseline이 설정되고 isDirty는 false에서 시작한다`() = runTest {
        viewModel.onAddClicked()

        val state = viewModel.uiState.value
        assertEquals(WriteUiMode.ADD, state.uiMode)
        assertNotNull(state.baselineKey)
        assertFalse(state.isDirty)

        viewModel.onTitleChanged("changed")

        viewModel.uiState.test {
            val updated = awaitItem()
            assertTrue(updated.isDirty)
        }
    }

    @Test
    fun `EDIT 전환 시 baseline이 설정되고 변경 전에는 isDirty가 false다`() = runTest {
        val date = LocalDate.now()
        diaryRepository.addDiaryForDate(date, "Title", "Content", listOf("uri1"))
        val existingDiaryId = diaryRepository.getByDate(date)[0].id

        viewModel.onEditClicked(existingDiaryId)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.uiMode != WriteUiMode.EDIT) {
                state = awaitItem()
            }
            assertNotNull(state.baselineKey)
            assertFalse(state.isDirty)

            viewModel.onContentChanged("changed")
            val updated = awaitItem()
            assertTrue(updated.isDirty)
        }
    }
}
