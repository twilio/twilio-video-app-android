package com.twilio.video.app.ui.mvvm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.twilio.video.app.util.plus
import io.reactivex.Completable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class BaseViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `init should update state with initial view state`() {
        val detailViewModel = TestViewModel(TestViewState())

        assertThat(detailViewModel.viewState.value, equalTo(TestViewState()))
    }

    @Test
    fun `onCleared should clear the disposables`() {
        val viewModel = TestViewModel(TestViewState())
        viewModel.setSomeBoolToFalse()

        viewModel.onClearedTest()

        assertThat(viewModel.rxDisposables.size(), equalTo(0))
    }

    @Test
    fun `updateState should update the old view state with a new view state`() {
        val viewModel = TestViewModel(TestViewState())

        viewModel.setSomeBoolToFalse()

        assertThat(viewModel.viewState.value!!, equalTo(TestViewState(false)))
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState should throw an IllegalStateException given a null view state`() {
        val viewModel = TestViewModel(TestViewState())

        viewModel.mutableViewState.value = null

        viewModel.updateStateTest()
    }

    @Test(expected = IllegalStateException::class)
    fun `updateViewEffect should throw an IllegalStateException given a null view state`() {
        val viewModel = TestViewModel(TestViewState())

        viewModel.mutableViewState.value = null

        viewModel.updateViewEffectTest()
    }

    @Test
    fun `viewEffect should emit a new view state but not to future subscriptions`() {
        val viewModel = TestViewModel(TestViewState())

        val testObserver = viewModel.viewEffects.test()
        viewModel.setSomeBoolToFalseViewEffect()
        val testObsever2 = viewModel.viewEffects.test()

        testObserver.assertValue(TestViewState(someBool = false))
        testObsever2.assertEmpty()
    }

    @Test
    fun `withState returns a property from the current view state`() {
        val viewModel = TestViewModel(TestViewState())

        viewModel.setSomeBoolToFalse()

        var result = true
        viewModel.withStateTest { result = it.someBool }

        assertThat(result, equalTo(false))
    }

    data class TestViewState(val someBool: Boolean = true)
    class TestViewEvent

    class TestViewModel(initialViewState: TestViewState) : BaseViewModel<TestViewEvent, TestViewState>(initialViewState) {

        override fun processInput(viewEvent: TestViewEvent) {

        }

        fun setSomeBoolToFalse() {
            rxDisposables + Completable.fromAction {}
                    .subscribe {
                        updateState { it.copy(someBool = false) }
                    }
        }

        fun updateStateTest() = updateState {it.copy()}

        fun updateViewEffectTest() = updateState {it.copy()}

        fun withStateTest(action : (currentState: TestViewState) -> Unit) = withState(action)

        fun setSomeBoolToFalseViewEffect() {
            viewEffect {
                it.copy(someBool = false)
            }
        }
    }
}