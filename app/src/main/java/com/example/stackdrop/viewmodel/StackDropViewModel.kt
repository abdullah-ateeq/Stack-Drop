package com.example.stackdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stackdrop.data.GamePreferencesRepository
import com.example.stackdrop.model.Difficulty
import com.example.stackdrop.model.DifficultyConfig
import com.example.stackdrop.model.FallingFragment
import com.example.stackdrop.model.GameBlockColor
import com.example.stackdrop.model.GameState
import com.example.stackdrop.model.GameStatistics
import com.example.stackdrop.model.MovingBlock
import com.example.stackdrop.model.StackDropResult
import com.example.stackdrop.model.TowerBlock
import com.example.stackdrop.model.UserSettings
import com.example.stackdrop.util.GamePhysics
import com.example.stackdrop.util.HapticManager
import com.example.stackdrop.util.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

data class GameUiState(
    val gameState: GameState = GameState.READY,
    val difficulty: Difficulty = Difficulty.EASY,
    val towerBlocks: List<TowerBlock> = emptyList(),
    val movingBlock: MovingBlock? = null,
    val fallingFragments: List<FallingFragment> = emptyList(),
    val height: Int = 0,
    val score: Int = 0,
    val perfectDrops: Int = 0,
    val perfectStreak: Int = 0,
    val bestRoundStreak: Int = 0,
    val cameraOffsetY: Float = 0f,
    val targetCameraOffsetY: Float = 0f,
    val towerWobbleDeg: Float = 0f,
    val feedbackMessage: String? = null,
    val countdownText: String = "",
    val boardWidth: Float = 0f,
    val boardHeight: Float = 0f,
    val lastResult: StackDropResult? = null
)

class StackDropViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application)
    val userSettings: StateFlow<UserSettings> = repository.settings
    val statistics: StateFlow<GameStatistics> = repository.statistics

    val soundPlayer = SoundPlayer().apply {
        isEnabled = userSettings.value.soundEnabled
    }
    val hapticManager = HapticManager(application).apply {
        isEnabled = userSettings.value.hapticsEnabled
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var feedbackJob: Job? = null
    private var wobbleTimer = 0f
    private var nextBlockDirection = 1f // 1f = moving right, -1f = moving left
    private var fragmentIdCounter = 0L

    val blockHeight = 44f
    val gravity = 2200f

    init {
        viewModelScope.launch {
            userSettings.collect { settings ->
                soundPlayer.isEnabled = settings.soundEnabled
                hapticManager.isEnabled = settings.hapticsEnabled
            }
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (_uiState.value.gameState == GameState.READY || _uiState.value.gameState == GameState.GAME_OVER) {
            _uiState.update { it.copy(difficulty = difficulty) }
        }
    }

    fun setBoardDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val current = _uiState.value
        if (current.boardWidth != width || current.boardHeight != height) {
            _uiState.update { it.copy(boardWidth = width, boardHeight = height) }
            if (current.gameState == GameState.READY) {
                initializeBaseBlock(width, height, current.difficulty)
            }
        }
    }

    fun initializeBaseBlock(boardWidth: Float, boardHeight: Float, difficulty: Difficulty) {
        val config = DifficultyConfig.forDifficulty(difficulty)
        val baseWidth = boardWidth * config.initialWidthRatio
        val baseX = (boardWidth - baseWidth) / 2f
        val baseY = boardHeight - 120f

        val baseBlock = TowerBlock(
            id = 0,
            x = baseX,
            y = baseY,
            width = baseWidth,
            height = blockHeight,
            color = GameBlockColor.COLOR_1
        )

        _uiState.update {
            it.copy(
                towerBlocks = listOf(baseBlock),
                movingBlock = null,
                fallingFragments = emptyList(),
                height = 0,
                score = 0,
                perfectDrops = 0,
                perfectStreak = 0,
                bestRoundStreak = 0,
                cameraOffsetY = 0f,
                targetCameraOffsetY = 0f,
                towerWobbleDeg = 0f,
                feedbackMessage = null,
                countdownText = "",
                gameState = GameState.READY
            )
        }
    }

    fun startGame() {
        val state = _uiState.value
        val width = if (state.boardWidth > 0f) state.boardWidth else 400f
        val height = if (state.boardHeight > 0f) state.boardHeight else 700f

        initializeBaseBlock(width, height, state.difficulty)
        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(gameState = GameState.COUNTDOWN, countdownText = "3") }
        soundPlayer.playCountdown(false)

        countdownJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(countdownText = "2") }
            soundPlayer.playCountdown(false)
            delay(400)
            _uiState.update { it.copy(countdownText = "1") }
            soundPlayer.playCountdown(false)
            delay(400)
            _uiState.update { it.copy(countdownText = "DROP!") }
            soundPlayer.playCountdown(true)
            delay(350)
            _uiState.update { it.copy(countdownText = "", gameState = GameState.MOVING) }
            spawnMovingBlock()
        }
    }

    private fun spawnMovingBlock() {
        val state = _uiState.value
        val topBlock = state.towerBlocks.lastOrNull() ?: return
        val config = DifficultyConfig.forDifficulty(state.difficulty)

        val speed = (config.initialSpeed + state.height * config.speedIncreasePerHeight)
            .coerceAtMost(config.maxSpeed)
        val velocityX = speed * nextBlockDirection
        nextBlockDirection = -nextBlockDirection

        val blockWidth = topBlock.width
        val initialX = if (velocityX > 0) 0f else (state.boardWidth - blockWidth).coerceAtLeast(0f)
        val initialY = topBlock.y - blockHeight - 160f
        val color = GameBlockColor.fromIndex(state.height + 1)

        val moving = MovingBlock(
            x = initialX,
            y = initialY,
            width = blockWidth,
            height = blockHeight,
            velocityX = velocityX,
            velocityY = 0f,
            color = color,
            isDropping = false
        )

        _uiState.update { it.copy(movingBlock = moving, gameState = GameState.MOVING) }
    }

    fun tapToDrop() {
        val current = _uiState.value
        if (current.gameState != GameState.MOVING || current.movingBlock == null) return

        val droppedBlock = current.movingBlock.copy(
            isDropping = true,
            velocityX = 0f,
            velocityY = 100f
        )
        _uiState.update {
            it.copy(
                movingBlock = droppedBlock,
                gameState = GameState.DROPPING
            )
        }
        soundPlayer.playDrop()
        hapticManager.performDrop()
    }

    fun updateGame(deltaTime: Float) {
        val state = _uiState.value
        if (state.gameState == GameState.PAUSED || state.boardWidth <= 0f) return

        // 1. Update Camera lerp
        val smoothing = if (userSettings.value.reducedMotion) 0.35f else (deltaTime * 8f).coerceIn(0f, 1f)
        val newCameraOffsetY = GamePhysics.lerp(state.cameraOffsetY, state.targetCameraOffsetY, smoothing)

        // 2. Update Tower wobble
        var newWobble = 0f
        if (wobbleTimer > 0f) {
            wobbleTimer -= deltaTime
            if (!userSettings.value.reducedMotion && wobbleTimer > 0f) {
                newWobble = sin(wobbleTimer * 28f) * 2.2f * (wobbleTimer / 0.25f)
            }
        }

        // 3. Update Falling Fragments (max 2)
        val updatedFragments = mutableListOf<FallingFragment>()
        val fragmentGravity = 1800f
        for (fragment in state.fallingFragments) {
            val newY = fragment.y + fragment.velocityY * deltaTime
            val newX = fragment.x + fragment.velocityX * deltaTime
            val newVy = fragment.velocityY + fragmentGravity * deltaTime
            val newRotation = if (userSettings.value.reducedMotion) 0f else fragment.rotation + fragment.rotationSpeed * deltaTime
            val newAlpha = (fragment.alpha - deltaTime * 1.5f).coerceAtLeast(0f)

            // Remove if off screen or fully faded
            if (newY < state.boardHeight + state.cameraOffsetY + 100f && newAlpha > 0.05f) {
                updatedFragments.add(
                    fragment.copy(
                        x = newX,
                        y = newY,
                        velocityY = newVy,
                        rotation = newRotation,
                        alpha = newAlpha
                    )
                )
            }
        }

        // 4. Update Moving Block
        val moving = state.movingBlock
        if (moving != null) {
            if (moving.isDropping) {
                val newVy = moving.velocityY + gravity * deltaTime
                val newY = moving.y + newVy * deltaTime

                val topBlock = state.towerBlocks.last()
                val targetLandingY = topBlock.y - blockHeight

                if (newY >= targetLandingY) {
                    // Collision with tower plane!
                    handleLanding(moving.copy(y = targetLandingY, velocityY = newVy))
                    return
                } else {
                    _uiState.update {
                        it.copy(
                            movingBlock = moving.copy(y = newY, velocityY = newVy),
                            cameraOffsetY = newCameraOffsetY,
                            towerWobbleDeg = newWobble,
                            fallingFragments = updatedFragments
                        )
                    }
                    return
                }
            } else if (state.gameState == GameState.MOVING) {
                // Horizontal bouncing
                var newX = moving.x + moving.velocityX * deltaTime
                var newVx = moving.velocityX
                val maxBoundary = (state.boardWidth - moving.width).coerceAtLeast(0f)

                if (newX <= 0f) {
                    newX = 0f
                    newVx = abs(moving.velocityX)
                } else if (newX >= maxBoundary) {
                    newX = maxBoundary
                    newVx = -abs(moving.velocityX)
                }

                _uiState.update {
                    it.copy(
                        movingBlock = moving.copy(x = newX, velocityX = newVx),
                        cameraOffsetY = newCameraOffsetY,
                        towerWobbleDeg = newWobble,
                        fallingFragments = updatedFragments
                    )
                }
                return
            }
        }

        // If in game over fall animation
        if (state.gameState == GameState.GAME_OVER && state.movingBlock != null) {
            val missBlock = state.movingBlock
            val newVy = missBlock.velocityY + gravity * deltaTime
            val newY = missBlock.y + newVy * deltaTime
            if (newY > state.boardHeight + state.cameraOffsetY + 250f) {
                _uiState.update { it.copy(movingBlock = null) }
            } else {
                _uiState.update {
                    it.copy(
                        movingBlock = missBlock.copy(y = newY, velocityY = newVy),
                        cameraOffsetY = newCameraOffsetY,
                        fallingFragments = updatedFragments
                    )
                }
            }
            return
        }

        _uiState.update {
            it.copy(
                cameraOffsetY = newCameraOffsetY,
                towerWobbleDeg = newWobble,
                fallingFragments = updatedFragments
            )
        }
    }

    private fun handleLanding(landedBlock: MovingBlock) {
        val state = _uiState.value
        val topBlock = state.towerBlocks.last()
        val config = DifficultyConfig.forDifficulty(state.difficulty)

        val overlap = GamePhysics.calculateOverlap(
            towerLeft = topBlock.x,
            towerRight = topBlock.x + topBlock.width,
            blockLeft = landedBlock.x,
            blockRight = landedBlock.x + landedBlock.width
        )

        if (overlap.isMiss) {
            handleGameOverMiss(landedBlock)
            return
        }

        val isPerfect = GamePhysics.isPerfect(
            towerLeft = topBlock.x,
            towerRight = topBlock.x + topBlock.width,
            blockLeft = landedBlock.x,
            blockRight = landedBlock.x + landedBlock.width,
            tolerance = config.perfectToleranceDp * 2.5f // Scaled to density
        )

        val newHeight = state.height + 1
        val baseScore = GamePhysics.calculateScore(newHeight)
        var roundScoreAdd = baseScore
        val newPerfectStreak = if (isPerfect) state.perfectStreak + 1 else 0
        val newPerfectDrops = if (isPerfect) state.perfectDrops + 1 else state.perfectDrops
        val newBestRoundStreak = maxOf(state.bestRoundStreak, newPerfectStreak)

        var finalX: Float
        var finalWidth: Float
        var feedbackText: String? = null

        val currentFragments = state.fallingFragments.takeLast(1).toMutableList()

        if (isPerfect) {
            // Perfect snap: no trimming!
            finalX = topBlock.x
            finalWidth = topBlock.width
            roundScoreAdd += 100 // Perfect bonus

            if (newPerfectStreak > 0 && newPerfectStreak % 5 == 0) {
                roundScoreAdd += 250
                feedbackText = "PERFECT STREAK! +250"
            } else {
                feedbackText = "PERFECT!"
            }

            soundPlayer.playPerfect()
            hapticManager.performPerfect()
        } else {
            // Trim overhang
            finalX = overlap.overlapLeft
            finalWidth = overlap.overlapWidth

            // Create falling offcut fragment
            if (overlap.leftOverhang > 0f) {
                fragmentIdCounter++
                currentFragments.add(
                    FallingFragment(
                        id = fragmentIdCounter,
                        x = landedBlock.x,
                        y = landedBlock.y,
                        width = overlap.leftOverhang,
                        height = blockHeight,
                        velocityX = -90f,
                        velocityY = 60f,
                        rotation = 0f,
                        rotationSpeed = -180f,
                        alpha = 1f,
                        color = landedBlock.color
                    )
                )
            }
            if (overlap.rightOverhang > 0f) {
                fragmentIdCounter++
                currentFragments.add(
                    FallingFragment(
                        id = fragmentIdCounter,
                        x = overlap.overlapRight,
                        y = landedBlock.y,
                        width = overlap.rightOverhang,
                        height = blockHeight,
                        velocityX = 90f,
                        velocityY = 60f,
                        rotation = 0f,
                        rotationSpeed = 180f,
                        alpha = 1f,
                        color = landedBlock.color
                    )
                )
            }

            wobbleTimer = 0.25f // 250ms wobble
            soundPlayer.playStack()
            hapticManager.performDrop()
        }

        // Check milestones
        val milestone = GamePhysics.getMilestoneMessage(newHeight)
        if (milestone != null) {
            feedbackText = milestone
            soundPlayer.playMilestone()
        }

        if (feedbackText != null) {
            showFeedback(feedbackText)
        }

        val newBlock = TowerBlock(
            id = newHeight,
            x = finalX,
            y = landedBlock.y,
            width = finalWidth,
            height = blockHeight,
            color = landedBlock.color
        )

        val updatedTower = state.towerBlocks + newBlock

        // Camera follow check:
        // When tower height exceeds ~65% of available board height, scroll camera upward
        val totalTowerHeight = (state.boardHeight - 120f) - landedBlock.y
        val cameraThreshold = state.boardHeight * 0.55f
        val newTargetCameraOffsetY = if (totalTowerHeight > cameraThreshold) {
            totalTowerHeight - cameraThreshold
        } else {
            0f
        }

        _uiState.update {
            it.copy(
                towerBlocks = updatedTower,
                movingBlock = null,
                fallingFragments = currentFragments.takeLast(2),
                height = newHeight,
                score = state.score + roundScoreAdd,
                perfectDrops = newPerfectDrops,
                perfectStreak = newPerfectStreak,
                bestRoundStreak = newBestRoundStreak,
                targetCameraOffsetY = newTargetCameraOffsetY,
                gameState = GameState.LANDED
            )
        }

        // Spawn next block after brief delay
        viewModelScope.launch {
            delay(120)
            if (_uiState.value.gameState == GameState.LANDED) {
                spawnMovingBlock()
            }
        }
    }

    private fun handleGameOverMiss(missedBlock: MovingBlock) {
        soundPlayer.playGameOver()
        hapticManager.performGameOver()

        val state = _uiState.value
        val result = StackDropResult(
            difficulty = state.difficulty,
            height = state.height,
            score = state.score,
            perfectDrops = state.perfectDrops,
            bestPerfectStreak = state.bestRoundStreak,
            gameOver = true
        )

        val (isNewHeight, isNewScore) = repository.recordGame(result)
        val finalResult = result.copy(
            isNewBestHeight = isNewHeight,
            isNewBestScore = isNewScore
        )

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                movingBlock = missedBlock.copy(velocityY = 300f),
                lastResult = finalResult
            )
        }
    }

    private fun showFeedback(message: String) {
        feedbackJob?.cancel()
        _uiState.update { it.copy(feedbackMessage = message) }
        feedbackJob = viewModelScope.launch {
            delay(900)
            _uiState.update { it.copy(feedbackMessage = null) }
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.MOVING || _uiState.value.gameState == GameState.DROPPING) {
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            val wasDropping = _uiState.value.movingBlock?.isDropping == true
            _uiState.update {
                it.copy(gameState = if (wasDropping) GameState.DROPPING else GameState.MOVING)
            }
        }
    }

    fun restartGame() {
        startGame()
    }

    fun exitGame() {
        countdownJob?.cancel()
        feedbackJob?.cancel()
        val state = _uiState.value
        initializeBaseBlock(
            boardWidth = if (state.boardWidth > 0f) state.boardWidth else 400f,
            boardHeight = if (state.boardHeight > 0f) state.boardHeight else 700f,
            difficulty = state.difficulty
        )
    }

    fun updateSettings(settings: UserSettings) {
        repository.updateSettings(settings)
    }

    fun resetStatistics() {
        repository.resetStatistics()
    }
}
