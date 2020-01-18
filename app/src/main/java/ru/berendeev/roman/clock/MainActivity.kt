package ru.berendeev.roman.clock

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

private const val KEY_RIGHT = "KEY_RIGHT"
private const val KEY_WRONG = "KEY_WRONG"
private const val KEY_INPUT_ENTITY = "KEY_INPUT_ENTITY"

class MainActivity : AppCompatActivity() {

    private var right = 0
    private var wrong = 0

    private lateinit var inputEntity: TimeInputEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fillInButtons()
        showResult()
        if (savedInstanceState != null) {
            right = savedInstanceState.getInt(KEY_RIGHT, 0)
            wrong = savedInstanceState.getInt(KEY_WRONG, 0)
            inputEntity = savedInstanceState.getSerializable(KEY_INPUT_ENTITY) as TimeInputEntity
            clockView.setTime(inputEntity.time)
        } else {
            initInputEntity()
        }
        showInputLine()
        showResult()
        setListeners()
    }

    private fun setListeners() {
        readyButton.setOnClickListener {
            onReady()
        }
        deleteButton.setOnClickListener {
            inputEntity.deleteNumber()
            showInputLine()
        }
    }

    private fun onReady() {
        if (inputEntity.isFilledIn()) {
            if (inputEntity.isRight()) {
                right += 1
            } else {
                wrong += 1
            }
            showResult()
            initInputEntity()
        }
    }

    private fun initInputEntity() {
        val random = Random(System.currentTimeMillis())
        val time = random.nextLong(0L, Long.MAX_VALUE)
        inputEntity = TimeInputEntity(time = time)
        clockView.setTime(time)
    }

    private fun showResult() {
        rightView.text = "$right"
        wrongView.text = "$wrong"
    }

    private fun showInputLine() {
        val result = getString(R.string.time_input, inputEntity.hours, inputEntity.minutes, inputEntity.seconds)
        timeField.text = result
    }

    private fun fillInButtons() {
        val firstLineNumbers = listOf(
            Command.Number(1),
            Command.Number(2),
            Command.Number(3)
        )
        val secondLineNumbers = listOf(
            Command.Number(4),
            Command.Number(5),
            Command.Number(6)
        )
        val thirdLineNumbers = listOf(
            Command.Number(7),
            Command.Number(8),
            Command.Number(9),
            Command.Number(0)
        )
        val callback = object : SimpleAdapter.Callback {
            override fun onNumberClick(number: Command.Number) {
                onNumberClick(number.number)
            }
        }
        SimpleAdapter(firstLine, firstLineNumbers, callback)
        SimpleAdapter(secondLine, secondLineNumbers, callback)
        SimpleAdapter(thirdLine, thirdLineNumbers, callback)
    }

    private fun onNumberClick(number: Int) {
        inputEntity.addNumber(number)
        showInputLine()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_RIGHT, right)
        outState.putInt(KEY_WRONG, wrong)
        outState.putSerializable(KEY_INPUT_ENTITY, inputEntity)
        super.onSaveInstanceState(outState)
    }
}

class SimpleAdapter(private val viewGroup: ViewGroup, numbers: List<Command.Number>, callback: Callback) {

    init {
        numbers.forEach { number ->
            val view = createView()
            view.text = number.number.toString()
            view.setOnClickListener {
                callback.onNumberClick(number)
            }
            viewGroup.addView(view)
        }
    }

    private fun createView(): TextView {
        val button = Button(viewGroup.context)
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        button.layoutParams = layoutParams
        return button
    }

    interface Callback {
        fun onNumberClick(number: Command.Number)
    }
}

class TimeInputEntity(
    var hours: String = "",
    var minutes: String = "",
    var seconds: String = "",
    val time: Long
): Serializable {

    fun addNumber(number: Int) {
        if (hours.length < 2) {
            hours += number
            return
        }
        if (minutes.length < 2) {
            minutes += number
            return
        }
        if (seconds.length < 2) {
            seconds += number
            return
        }
    }

    fun isFilledIn(): Boolean {
        return seconds.length == 2
    }

//    fun isRight(): Boolean {
//        val timeHours = (time % TimeUnit.DAYS.toMillis(1)) / TimeUnit.HOURS.toMillis(1) % 12
//        val timeMinutes = (time % TimeUnit.HOURS.toMillis(1)) / TimeUnit.MINUTES.toMillis(1)
//        val timeSeconds = (time % TimeUnit.MINUTES.toMillis(1)) / TimeUnit.SECONDS.toMillis(1)
//        val hoursString = (if (timeHours < 10) "0" else "") + timeHours
//        val minutesString = (if (timeMinutes < 10) "0" else "") + timeMinutes
//        val secondsString = (if (timeSeconds < 10) "0" else "") + timeSeconds
//        return hoursString == hours && minutesString == minutes && secondsString == seconds
//    }

    private fun isEqualsWithError(first: Int, second: Int, error: Int): Boolean {
        return abs(first - second) <= error
    }

    fun isRight(): Boolean {
        val timeHours = (time % TimeUnit.DAYS.toMillis(1)) / TimeUnit.HOURS.toMillis(1) % 12
        val timeMinutes = (time % TimeUnit.HOURS.toMillis(1)) / TimeUnit.MINUTES.toMillis(1)
        val timeSeconds = (time % TimeUnit.MINUTES.toMillis(1)) / TimeUnit.SECONDS.toMillis(1)
        val inputHours = hours.toInt()
        val inputMinutes = minutes.toInt()
        val inputSeconds = seconds.toInt()
        return timeHours.toInt() == inputHours &&
                isEqualsWithError(timeMinutes.toInt(), inputMinutes, 2) &&
                isEqualsWithError(timeSeconds.toInt(), inputSeconds, 10)
    }

    fun deleteNumber() {
        when {
            seconds.isNotEmpty() -> seconds = seconds.drop(1)
            minutes.isNotEmpty() -> minutes = minutes.drop(1)
            hours.isNotEmpty() -> hours = hours.drop(1)
        }
    }
}

sealed class Command {
    data class Number(val number: Int) : Command()
    object Delete : Command()
    object Done : Command()
}
