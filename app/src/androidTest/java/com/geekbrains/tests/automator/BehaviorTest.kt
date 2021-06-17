package com.geekbrains.tests.automator

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class BehaviorTest {

    //Класс UiDevice предоставляет доступ к вашему устройству.
    //Именно через UiDevice вы можете управлять устройством, открывать приложения
    //и находить нужные элементы на экране
    private val uiDevice: UiDevice = UiDevice.getInstance(getInstrumentation())

    //Контекст нам понадобится для запуска нужных экранов и получения packageName
    private val context = ApplicationProvider.getApplicationContext<Context>()

    //Путь к классам нашего приложения, которые мы будем тестировать
    private val packageName = context.packageName

    @Before
    fun setup() {
        //Для начала сворачиваем все приложения, если у нас что-то запущено
        uiDevice.pressHome()

        //Запускаем наше приложение
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        //Мы уже проверяли Интент на null в предыдущем тесте, поэтому допускаем, что Интент у нас не null
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)//Чистим бэкстек от запущенных ранее Активити
        context.startActivity(intent)

        //Ждем, когда приложение откроется на смартфоне чтобы начать тестировать его элементы
        uiDevice.wait(Until.hasObject(By.pkg(packageName).depth(0)), TIMEOUT)
    }

    //Убеждаемся, что приложение открыто. Для этого достаточно найти на экране любой элемент
    //и проверить его на null
    @Test
    fun test_MainActivityIsStarted() {
        //Через uiDevice находим editText
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        //Проверяем на null
        Assert.assertNotNull(editText)
    }

    //Убеждаемся, что поиск работает как ожидается
    @Test
    fun test_SearchIsPositive() {
        //Через uiDevice находим editText
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))

        // Устанавливаем курсор в EditText
        editText.click()

        //Устанавливаем значение
        editText.text = "UiAutomator"

        // Отправляем запрос
        UiObject(UiSelector().packageName(packageName)).clickBottomRight()

        //Отправляем запрос через Espresso
//        Espresso.onView(ViewMatchers.withId(R.id.searchEditText))
//            .perform(ViewActions.pressImeActionButton())

        //Ожидаем конкретного события: появления текстового поля totalCountTextView.
        //Это будет означать, что сервер вернул ответ с какими-то данными, то есть запрос отработал.
        val changedText =
            uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )
        //Убеждаемся, что сервер вернул корректный результат. Обратите внимание, что количество
        //результатов может варьироваться во времени, потому что количество репозиториев постоянно меняется.
        Assert.assertEquals(changedText.text.toString(), "Number of results: 42")
    }

    //Убеждаемся, что DetailsScreen открывается
    @Test
    fun test_OpenDetailsScreen() {
        //Находим кнопку
        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(
                packageName,
                "toDetailsActivityButton"
            )
        )
        //Кликаем по ней
        toDetails.click()

        //Ожидаем конкретного события: появления текстового поля totalCountTextView.
        //Это будет означать, что DetailsScreen открылся и это поле видно на экране.
        val changedText =
            uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )
        //Убеждаемся, что поле видно и содержит предполагаемый текст.
        //Обратите внимание, что текст должен быть "Number of results: 0",
        //так как мы кликаем по кнопке не отправляя никаких поисковых запросов.
        //Чтобы проверить отображение определенного количества репозиториев,
        //вам в одном и том же методе нужно отправить запрос на сервер и открыть DetailsScreen.
        Assert.assertEquals(changedText.text, "Number of results: 0")
    }

    @Test
    fun detailsScreen_numberRepositories() {
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        editText.click()
        editText.text = "UiAutomator"
        UiObject(UiSelector().packageName(packageName)).clickBottomRight()

        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(packageName, "toDetailsActivityButton")
        )
        toDetails.click()

        val changedText = uiDevice.wait(
            Until.findObject(By.res(packageName, "totalCountTextView")),
            TIMEOUT
        )

        Assert.assertEquals(changedText.text, "Number of results: 42")
    }

    @Test
    fun detailsScreen_decrementButton() {
        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(packageName, "toDetailsActivityButton")
        )
        toDetails.click()

        val decrementButton = uiDevice.wait(
            Until.findObject(By.res(packageName, "decrementButton")),
            TIMEOUT
        )
        decrementButton.click()

        val changedText = uiDevice.findObject(
            By.res(packageName, "totalCountTextView")
        )
        Assert.assertEquals(changedText.text, "Number of results: -1")
    }

    @Test
    fun detailsScreen_incrementButton() {
        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(packageName, "toDetailsActivityButton")
        )
        toDetails.click()

        val decrementButton = uiDevice.wait(
            Until.findObject(By.res(packageName, "incrementButton")),
            TIMEOUT
        )
        decrementButton.click()

        val changedText = uiDevice.findObject(
            By.res(packageName, "totalCountTextView")
        )
        Assert.assertEquals(changedText.text, "Number of results: 1")
    }

    @Test
    fun detailsScreen_backButtonToBackMainScreen() {
        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(packageName, "toDetailsActivityButton")
        )
        toDetails.click()

        uiDevice.pressBack()

        val detailsButton = uiDevice.wait(
            Until.findObject(By.res(packageName, "toDetailsActivityButton")),
            TIMEOUT
        )
        Assert.assertNotNull(detailsButton)
    }

    companion object {
        private const val TIMEOUT = 5000L
    }
}
