package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevel: Short,
    val maxLevel: Short,
    var currentLevel: Short
)

data class AudioEffectsState(
    val isVolumeBoostEnabled: Boolean = false,
    val volumeBoostPercent: Int = 100, // 100% to 200%
    val isEqualizerEnabled: Boolean = false,
    val currentPreset: String = "Flat",
    val presets: List<String> = emptyList(),
    val bands: List<EqualizerBand> = emptyList(),
    val isBassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0 to 1000
    val isVirtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0 to 1000
    val balance: Float = 0f // -1.0 (Left) to 1.0 (Right)
)

class AudioEffectsManager {

    private val TAG = "AudioEffectsManager"

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var currentSessionId: Int = 0

    private val _effectsState = MutableStateFlow(AudioEffectsState())
    val effectsState: StateFlow<AudioEffectsState> = _effectsState.asStateFlow()

    fun attachSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        releaseEffects()
        currentSessionId = audioSessionId

        try {
            // Loudness Enhancer (Software Gain 100% - 200%)
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _effectsState.value.isVolumeBoostEnabled
                val boostGainMb = calculateGainMb(_effectsState.value.volumeBoostPercent)
                setTargetGain(boostGainMb)
            }
        } catch (e: Exception) {
            Log.e(TAG, "LoudnessEnhancer initialization error", e)
        }

        try {
            // Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _effectsState.value.isEqualizerEnabled
                val numBands = numberOfBands
                val minLevel = bandLevelRange[0]
                val maxLevel = bandLevelRange[1]
                val bandList = mutableListOf<EqualizerBand>()

                for (i in 0 until numBands) {
                    val bandIdx = i.toShort()
                    val freq = getCenterFreq(bandIdx) / 1000
                    val currentLvl = getBandLevel(bandIdx)
                    bandList.add(
                        EqualizerBand(
                            index = bandIdx,
                            centerFreqHz = freq,
                            minLevel = minLevel,
                            maxLevel = maxLevel,
                            currentLevel = currentLvl
                        )
                    )
                }

                val presetList = mutableListOf<String>()
                val numPresets = numberOfPresets
                for (p in 0 until numPresets) {
                    presetList.add(getPresetName(p.toShort()))
                }
                presetList.add("Custom")

                _effectsState.value = _effectsState.value.copy(
                    bands = bandList,
                    presets = presetList
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Equalizer initialization error", e)
        }

        try {
            // BassBoost
            bassBoost = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    enabled = _effectsState.value.isBassBoostEnabled
                    setStrength(_effectsState.value.bassBoostStrength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "BassBoost initialization error", e)
        }

        try {
            // Virtualizer
            virtualizer = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    enabled = _effectsState.value.isVirtualizerEnabled
                    setStrength(_effectsState.value.virtualizerStrength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Virtualizer initialization error", e)
        }
    }

    private fun calculateGainMb(percent: Int): Int {
        if (percent <= 100) return 0
        // Scale 100% -> 0 mB, 200% -> 2000 mB
        val fraction = (percent - 100) / 100f
        return (fraction * 2000f).toInt()
    }

    fun setVolumeBoostPercent(percent: Int) {
        val clamped = percent.coerceIn(100, 200)
        val isEnabled = clamped > 100
        val gainMb = calculateGainMb(clamped)
        try {
            loudnessEnhancer?.apply {
                enabled = isEnabled
                setTargetGain(gainMb)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying volume boost", e)
        }
        _effectsState.value = _effectsState.value.copy(
            isVolumeBoostEnabled = isEnabled,
            volumeBoostPercent = clamped
        )
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Error setting equalizer state", e)
        }
        _effectsState.value = _effectsState.value.copy(isEqualizerEnabled = enabled)
    }

    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, level)
            val updatedBands = _effectsState.value.bands.map {
                if (it.index == bandIndex) it.copy(currentLevel = level) else it
            }
            _effectsState.value = _effectsState.value.copy(
                bands = updatedBands,
                currentPreset = "Custom"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting band level", e)
        }
    }

    fun usePreset(presetName: String) {
        try {
            val eq = equalizer ?: return
            val numPresets = eq.numberOfPresets
            for (p in 0 until numPresets) {
                if (eq.getPresetName(p.toShort()).equals(presetName, ignoreCase = true)) {
                    eq.usePreset(p.toShort())
                    val updatedBands = _effectsState.value.bands.map { band ->
                        band.copy(currentLevel = eq.getBandLevel(band.index))
                    }
                    _effectsState.value = _effectsState.value.copy(
                        currentPreset = presetName,
                        bands = updatedBands
                    )
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying preset $presetName", e)
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        try {
            bassBoost?.apply {
                this.enabled = enabled
                setStrength(clamped.toShort())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting bass boost", e)
        }
        _effectsState.value = _effectsState.value.copy(
            isBassBoostEnabled = enabled,
            bassBoostStrength = clamped
        )
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        try {
            virtualizer?.apply {
                this.enabled = enabled
                setStrength(clamped.toShort())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting virtualizer", e)
        }
        _effectsState.value = _effectsState.value.copy(
            isVirtualizerEnabled = enabled,
            virtualizerStrength = clamped
        )
    }

    fun setBalance(balance: Float) {
        _effectsState.value = _effectsState.value.copy(balance = balance.coerceIn(-1f, 1f))
    }

    fun releaseEffects() {
        try {
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        loudnessEnhancer = null

        try {
            equalizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null

        try {
            bassBoost?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bassBoost = null

        try {
            virtualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        virtualizer = null
        currentSessionId = 0
    }
}
