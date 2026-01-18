package com.example.myapplication.picday.presentation.write

import app.cash.turbine.test
import com.example.myapplication.picday.fakes.FakeDiaryRepository
import com.example.myapplication.picday.presentation.write.photo.WritePhotoState
import com.example.myapplication.picday.presentation.write.state.WriteUiMode
import com.example.myapplication.picday.util.MainDispatcherRule
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

    private lateinit var repository: FakeDiaryRepository
    private lateinit var viewModel: WriteViewModel

    @Before
    fun setUp() {
        repository = FakeDiaryRepository()
        viewModel = WriteViewModel(repository)
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
     * 새로 추가된 사진들이 리스트의 앞에 오게 되어 대표 사진(첫 번째)으로 선택되는지 확인
     */
    @Test
    fun `새로 추가된 사진이 대표 사진으로 설정되어야 한다`() = runTest {
        // Given: uri1이 있는 상태
        viewModel.onPhotosAdded(listOf("uri1"))

        // When: uri2를 추가
        viewModel.onPhotosAdded(listOf("uri2"))

        // Then: 리스트의 첫 번째(대표 사진)는 uri2여야 함
        assertEquals("uri2", viewModel.getCoverPhotoUri())
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
        repository.addDiaryForDate(date, "Title", "Content", listOf("old_uri"))
        val existingDiaryId = repository.getByDate(date)[0].id

        // When: 수정 모드 진입 및 신규 사진 추가
        // Note: Viewmodel의 onEditClicked는 내부에서 viewModelScope.launch(IO)를 사용하므로
        // UnconfinedTestDispatcher를 사용하는 MainDispatcherRule 덕분에 즉시 완료됨
        viewModel.onEditClicked(existingDiaryId)
        viewModel.onPhotosAdded(listOf("new_uri"))

        // Then: 기존 old_uri(KEEP 상태)와 신규 new_uri(NEW 상태)가 모두 있어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(WriteUiMode.EDIT, state.uiMode)
            assertEquals(2, state.photoItems.size)
            
            val states = state.photoItems.map { it.state }
            assertTrue(states.contains(WritePhotoState.KEEP))
            assertTrue(states.contains(WritePhotoState.NEW))
            
            val uris = state.photoItems.map { it.uri }
            assertTrue(uris.contains("old_uri"))
            assertTrue(uris.contains("new_uri"))
        }
    }
}
