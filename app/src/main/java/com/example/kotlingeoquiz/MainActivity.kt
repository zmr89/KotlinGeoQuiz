package com.example.kotlingeoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton : Button
    private lateinit var falseButton : Button
    private lateinit var nextButton : ImageButton
    private lateinit var prevButton : ImageButton
    private lateinit var cheatButton : Button
    private lateinit var questionTextView : TextView
    private lateinit var cheatTextView : TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

// quizViewModel ниже можно закоментировать так как она повторяется
        val provider : ViewModelProvider = ViewModelProvider(this)
        val quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatTextView= findViewById(R.id.cheatTextView)


        trueButton.setOnClickListener { view : View ->
            checkAnswer(true)
            trueButton.visibility = View.INVISIBLE
            falseButton.visibility = View.INVISIBLE
        }

        falseButton.setOnClickListener { view : View ->
            checkAnswer(false)
            trueButton.visibility = View.INVISIBLE
            falseButton.visibility = View.INVISIBLE
        }

        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            trueButton.visibility = View.VISIBLE
            falseButton.visibility = View.VISIBLE

            quizViewModel.isCheater = false
        }

        prevButton.setOnClickListener {
            quizViewModel.moveToPrev()
            updateQuestion()
            trueButton.visibility = View.VISIBLE
            falseButton.visibility = View.VISIBLE
        }

        cheatButton.setOnClickListener { view ->
            if (quizViewModel.scoreCheater < 3) {
                val answerIsTrue = quizViewModel.currentQuestionAnswer
                val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val options = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.width, view.height);
                    startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
                } else {
                    startActivityForResult(intent, REQUEST_CODE_CHEAT)
                }
            }

        }

        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT){
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN,false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart(Bundle?) called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume(Bundle?) called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause(Bundle?) called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop(Bundle?) called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy(Bundle?) called")
    }

    private fun updateQuestion(){
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
        cheatTextView.setText("You use cheat: " + quizViewModel.scoreCheater + " of 3")
    }

    private fun checkAnswer(userAnswer : Boolean){
        val correctAnswer = quizViewModel.currentQuestionAnswer
//        val messageResId = if (userAnswer == correctAnswer) {
//            R.string.correct_toast
//        } else {
//            R.string.incorrect_toast
//        }
//        if (userAnswer == correctAnswer) {
//            quizViewModel.score++
//            Log.d("score", "score "+ quizViewModel.score)
//        }
//        if (quizViewModel.currentIndex == (quizViewModel.questionBankSize() - 1)) {
//            questionTextView.setText("You answered correctly " + quizViewModel.score + "  questions out of " + quizViewModel.questionBankSize())
//            quizViewModel.score = 0
//        }

        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        if (userAnswer == correctAnswer) {quizViewModel.score++}
        if (quizViewModel.isCheater){quizViewModel.scoreCheater++}
        if (quizViewModel.currentIndex == (quizViewModel.questionBankSize() - 1)) {
            questionTextView.setText(
                "You answered correctly " + quizViewModel.score
                        + "  questions out of " + quizViewModel.questionBankSize()
                        + "\n You cheated: " + quizViewModel.scoreCheater)
            quizViewModel.score = 0
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }


}