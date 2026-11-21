package com.rasecalay.juegayaprende

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceHelper
    private lateinit var screenSetup: LinearLayout
    private lateinit var screenMenu: LinearLayout
    private lateinit var screenStage: LinearLayout
    private lateinit var etChildName: EditText
    private lateinit var tvWelcomeTitle: TextView
    private lateinit var tvInstruction: TextView
    private lateinit var btnToggleMode: Button
    private lateinit var stageGridContainer: FrameLayout
    private lateinit var ivBeeGuide: ImageView

    private var childName: String = ""
    private var currentTopic: String = ""
    private var currentMode: String = "learn"
    private var currentInstruction: String = ""
    private var isLocked: Boolean = false
    private var beePulseAnim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        setContentView(R.layout.activity_main)

        voiceHelper = VoiceHelper(this)

        screenSetup = findViewById(R.id.screenSetup)
        screenMenu = findViewById(R.id.screenMenu)
        screenStage = findViewById(R.id.screenStage)
        etChildName = findViewById(R.id.etChildName)
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle)
        tvInstruction = findViewById(R.id.tvInstruction)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        stageGridContainer = findViewById(R.id.stageGridContainer)
        ivBeeGuide = findViewById(R.id.ivBeeGuide)

        val prefs = getSharedPreferences("juega_aprende_prefs", Context.MODE_PRIVATE)
        childName = prefs.getString("child_name", "") ?: ""

        if (childName.isNotEmpty()) {
            showMenu(speakGreeting = false)
        }

        findViewById<Button>(R.id.btnSaveName).setOnClickListener {
            val raw = etChildName.text.toString().trim()
            if (raw.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre para empezar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            childName = raw.replaceFirstChar { it.uppercase() }
            prefs.edit().putString("child_name", childName).apply()
            showMenu(speakGreeting = true)
        }

        val btnChangeVoice = findViewById<Button>(R.id.btnChangeVoice)
        btnChangeVoice.setOnClickListener {
            val info = voiceHelper.switchToNextVoice()
            btnChangeVoice.text = "🗣️ Voz ($info)"
        }

        findViewById<Button>(R.id.btnChangeName).setOnClickListener {
            voiceHelper.stop()
            hideBeeGuide()
            etChildName.setText(childName)
            screenMenu.visibility = View.GONE
            screenStage.visibility = View.GONE
            screenSetup.visibility = View.VISIBLE
        }

        findViewById<LinearLayout>(R.id.cardColors).setOnClickListener {
            voiceHelper.playTapSound()
            openTopic("colors")
        }

        findViewById<LinearLayout>(R.id.cardNumbers).setOnClickListener {
            voiceHelper.playTapSound()
            openTopic("numbers")
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            voiceHelper.stop()
            hideBeeGuide()
            isLocked = false
            showMenu(speakGreeting = false)
        }

        findViewById<LinearLayout>(R.id.bannerInstruction).setOnClickListener {
            if (currentInstruction.isNotEmpty()) {
                voiceHelper.speak(currentInstruction)
            }
        }

        btnToggleMode.setOnClickListener {
            voiceHelper.playTapSound()
            hideBeeGuide()
            if (currentMode == "learn") startQuizMode() else startLearnMode()
        }
    }

    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun showMenu(speakGreeting: Boolean) {
        tvWelcomeTitle.text = "¡Hola, $childName! 🌟"
        screenSetup.visibility = View.GONE
        screenStage.visibility = View.GONE
        screenMenu.visibility = View.VISIBLE
        if (speakGreeting) {
            voiceHelper.speak("¡Hola $childName! Qué alegría jugar contigo. Elige colores o números.")
        }
    }

    private fun openTopic(topic: String) {
        currentTopic = topic
        screenMenu.visibility = View.GONE
        screenStage.visibility = View.VISIBLE
        startLearnMode()
    }

    private fun startLearnMode() {
        currentMode = "learn"
        isLocked = false
        hideBeeGuide()
        btnToggleMode.text = "🎮 ¡A jugar!"
        btnToggleMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#43A047"))
        stageGridContainer.removeAllViews()

        val grid = GridLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            columnCount = if (currentTopic == "colors") 4 else 5
            rowCount = if (currentTopic == "colors") 3 else 2
        }

        if (currentTopic == "colors") {
            currentInstruction = "$childName, toca cada color para aprender su nombre. Cuando estés lista, toca el botón verde para jugar."
            tvInstruction.text = "Toca los 12 colores para escucharlos"
            GameData.COLORS.forEach { item ->
                val card = createCardView(item.hexColor, item.emoji, item.name, null, item.darkText, true)
                card.setOnClickListener {
                    voiceHelper.playTapSound()
                    animateBounce(card)
                    voiceHelper.speak("Color ${item.name}")
                }
                grid.addView(card)
            }
        } else {
            currentInstruction = "$childName, toca cada número del uno al diez para aprenderlo. Cuando estés lista, toca el botón verde para jugar."
            tvInstruction.text = "Toca los números del 1 al 10"
            GameData.NUMBERS.forEach { item ->
                val card = createCardView(item.hexColor, item.number.toString(), item.word, item.itemsEmoji, false, true)
                card.setOnClickListener {
                    voiceHelper.playTapSound()
                    animateBounce(card)
                    voiceHelper.speak("Número ${item.word}")
                }
                grid.addView(card)
            }
        }
        stageGridContainer.addView(grid)
        voiceHelper.speak(currentInstruction)
    }

    private fun startQuizMode() {
        currentMode = "quiz"
        btnToggleMode.text = "📖 Repasar"
        btnToggleMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#0288D1"))
        nextQuizRound()
    }

    private fun nextQuizRound() {
        isLocked = false
        hideBeeGuide()
        stageGridContainer.removeAllViews()

        val rowLayout = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        if (currentTopic == "colors") {
            val options = GameData.COLORS.shuffled().take(4)
            val target = options.random()
            currentInstruction = "$childName, toca el color${target.name}"
            tvInstruction.text = "Toca el color ${target.name}"
            var targetCardView: View? = null

            options.forEach { item ->
                val card = createCardView(item.hexColor, item.emoji, item.name, null, item.darkText, false)
                if (item.name == target.name) targetCardView = card
                card.setOnClickListener {
                    handleQuizClick(card, targetCardView, item.name == target.name, "el color ${item.name}")
                }
                rowLayout.addView(card)
            }
        } else {
            val options = GameData.NUMBERS.shuffled().take(4)
            val target = options.random()
            currentInstruction = "$childName, busca el número ${target.word}"
            tvInstruction.text = "Busca el número ${target.number}"
            var targetCardView: View? = null

            options.forEach { item ->
                val card = createCardView(item.hexColor, item.number.toString(), item.word, item.itemsEmoji, false, false)
                if (item.number == target.number) targetCardView = card
                card.setOnClickListener {
                    handleQuizClick(card, targetCardView, item.number == target.number, "el número ${item.word}")
                }
                rowLayout.addView(card)
            }
        }

        stageGridContainer.addView(rowLayout)
        voiceHelper.speak(currentInstruction)
    }

    private fun handleQuizClick(clickedView: View, correctView: View?, isCorrect: Boolean, spokenLabel: String) {
        if (isLocked) return
        if (isCorrect) {
            isLocked = true
            hideBeeGuide()
            voiceHelper.playSuccessSound()
            animateBounce(clickedView)
            voiceHelper.speak(GameData.getPraise(childName)) {
                runOnUiThread { nextQuizRound() }
            }
        } else {
            voiceHelper.playWrongSound()
            animateShake(clickedView)
            voiceHelper.speak("Ese es $spokenLabel. ¡Mira a la abejita!")
            if (correctView != null) flyBeeToTarget(correctView)
        }
    }

    private fun flyBeeToTarget(targetView: View) {
        targetView.post {
            val targetLoc = IntArray(2)
            targetView.getLocationOnScreen(targetLoc)
            val destX = targetLoc[0] + (targetView.width / 2f) - (ivBeeGuide.width / 2f)
            val destY = targetLoc[1] + 12f

            if (ivBeeGuide.visibility != View.VISIBLE) {
                ivBeeGuide.x = (resources.displayMetrics.widthPixels - ivBeeGuide.width - 24).toFloat()
                ivBeeGuide.y = (resources.displayMetrics.heightPixels - ivBeeGuide.height - 24).toFloat()
                ivBeeGuide.visibility = View.VISIBLE
            }

            ivBeeGuide.animate()
                .x(destX).y(destY).setDuration(650L)
                .setInterpolator(OvershootInterpolator(1.2f))
                .withEndAction {
                    beePulseAnim?.cancel()
                    beePulseAnim = ObjectAnimator.ofPropertyValuesHolder(
                        ivBeeGuide,
                        PropertyValuesHolder.ofFloat("scaleX", 1.15f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.15f)
                    ).apply {
                        duration = 450L
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        start()
                    }
                }.start()
        }
    }

    private fun hideBeeGuide() {
        beePulseAnim?.cancel()
        ivBeeGuide.scaleX = 1f
        ivBeeGuide.scaleY = 1f
        ivBeeGuide.visibility = View.GONE
    }

    private fun createCardView(
        bgHex: String, topEmoji: String, mainText: String,
        subText: String?, darkText: Boolean, isCompact: Boolean
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 42f
                setColor(Color.parseColor(bgHex))
                setStroke(8, Color.parseColor("#40000000"))
            }
            layoutParams = if (isCompact) {
                GridLayout.LayoutParams().apply {
                    width = 0; height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
            } else {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(12, 12, 12, 12)
                }
            }
        }

        val isNumberCard = (subText != null)
        val badge = TextView(this).apply {
            text = topEmoji
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            if (isNumberCard) {
                textSize = if (isCompact) 48f else 112f
                setTextColor(Color.parseColor("#FFF59D"))
                setShadowLayer(8f, 3f, 3f, Color.parseColor("#80000000"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            } else {
                val badgeSize = if (isCompact) 86 else 145
                textSize = if (isCompact) 30f else 54f
                setTextColor(Color.parseColor("#212121"))
                setBackgroundResource(R.drawable.bg_circulo_suave)
                layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize)
            }
        }
        card.addView(badge)

        val label = TextView(this).apply {
            text = mainText
            textSize = if (isCompact) 17f else 24f
            gravity = Gravity.CENTER
            setTextColor(if (darkText) Color.parseColor("#212121") else Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 4, 0, 0)
        }
        card.addView(label)

        if (subText != null) {
            val itemsView = TextView(this).apply {
                text = subText
                textSize = if (isCompact) 12f else 18f
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 0)
            }
            card.addView(itemsView)
        }
        return card
    }

    private fun animateBounce(view: View) {
        view.animate().scaleX(1.12f).scaleY(1.12f).setDuration(180L).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(180L).start()
        }.start()
    }

    private fun animateShake(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, -18f, 18f, -12f, 12f, 0f).apply {
            duration = 350L; start()
        }
    }

    override fun onDestroy() {
        voiceHelper.shutdown()
        super.onDestroy()
    }
}
