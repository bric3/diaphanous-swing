package io.github.bric3.diaphanous.demo

import io.github.bric3.diaphanous.backdrop.MacosVibrancyBlendingMode
import io.github.bric3.diaphanous.backdrop.MacosVibrancyMaterial
import io.github.bric3.diaphanous.backdrop.MacosVibrancySpec
import io.github.bric3.diaphanous.backdrop.MacosVibrancyState
import io.github.bric3.diaphanous.backdrop.WindowBackdrop
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider

class WindowBackdropControls(
    private val onChange: (style: MacosVibrancySpec) -> Unit
) : JPanel() {
    companion object {
        private const val DEFAULT_BACKDROP_ALPHA = 0.55
        private const val DEFAULT_BLUR_STRENGTH = 55
    }

    init {
        isOpaque = false
    }
    private val nativeDefaultAlpha = WindowBackdrop.defaultAlpha()
    private val initialAlpha = if (nativeDefaultAlpha in 0.0..1.0) nativeDefaultAlpha else DEFAULT_BACKDROP_ALPHA
    private val initialMaterial = WindowBackdrop.defaultMaterial()
        .filter { it is MacosVibrancyMaterial }
        .map { it as MacosVibrancyMaterial }
        .orElse(MacosVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
    private val initialBlurStrength = WindowBackdrop.defaultMaterial()
        .filter { it is MacosVibrancyMaterial }
        .map { it as MacosVibrancyMaterial }
        .map(::blurStrengthForMaterial)
        .orElse(DEFAULT_BLUR_STRENGTH)

    private val alphaValue = JLabel("%.2f".format(initialAlpha)).apply {
        foreground = Color(230, 230, 230, 190)
    }

    private val alphaSlider = JSlider(0, 100, (initialAlpha * 100.0).toInt()).apply {
        name = "alphaSlider"
        isOpaque = false
        addChangeListener {
            val alpha = value / 100.0
            alphaValue.text = "%.2f".format(alpha)
            onChange(currentSpec())
        }
    }

    private val blurValue = JLabel("$initialBlurStrength").apply {
        foreground = Color(230, 230, 230, 190)
    }

    private val blurSlider = JSlider(0, 100, initialBlurStrength).apply {
        name = "blurSlider"
        isOpaque = false
        addChangeListener {
            blurValue.text = value.toString()
            materialCombo.selectedItem = materialForBlurStrength(value)
            onChange(currentSpec())
        }
    }

    private val materialCombo = JComboBox(MacosVibrancyMaterial.entries.toTypedArray()).apply {
        name = "materialCombo"
        selectedItem = initialMaterial
        addActionListener { onChange(currentSpec()) }
    }

    private val blendingCombo = JComboBox(MacosVibrancyBlendingMode.entries.toTypedArray()).apply {
        name = "blendingModeCombo"
        selectedItem = MacosVibrancyBlendingMode.BEHIND_WINDOW
        addActionListener { onChange(currentSpec()) }
    }

    private val stateCombo = JComboBox(MacosVibrancyState.entries.toTypedArray()).apply {
        name = "vibrancyStateCombo"
        selectedItem = MacosVibrancyState.FOLLOWS_WINDOW_ACTIVE_STATE
        addActionListener { onChange(currentSpec()) }
    }

    private val emphasizedCheck = JCheckBox("Emphasized").apply {
        name = "emphasizedCheck"
        isOpaque = false
        addActionListener { onChange(currentSpec()) }
    }

    private fun blurStrengthForMaterial(material: MacosVibrancyMaterial): Int = when (material) {
        MacosVibrancyMaterial.CONTENT_BACKGROUND -> 10
        MacosVibrancyMaterial.WINDOW_BACKGROUND -> 30
        MacosVibrancyMaterial.SIDEBAR -> 50
        MacosVibrancyMaterial.MENU -> 70
        MacosVibrancyMaterial.HUD_WINDOW -> 90
        else -> DEFAULT_BLUR_STRENGTH
    }

    private fun materialForBlurStrength(value: Int): MacosVibrancyMaterial = when {
        value < 20 -> MacosVibrancyMaterial.CONTENT_BACKGROUND
        value < 40 -> MacosVibrancyMaterial.WINDOW_BACKGROUND
        value < 60 -> MacosVibrancyMaterial.SIDEBAR
        value < 80 -> MacosVibrancyMaterial.MENU
        else -> MacosVibrancyMaterial.HUD_WINDOW
    }

    fun currentSpec(): MacosVibrancySpec = MacosVibrancySpec.builder()
        .material(materialCombo.selectedItem as MacosVibrancyMaterial)
        .blendingMode(blendingCombo.selectedItem as MacosVibrancyBlendingMode)
        .state(stateCombo.selectedItem as MacosVibrancyState)
        .emphasized(emphasizedCheck.isSelected)
        .backdropAlpha(alphaSlider.value / 100.0)
        .build()

    val component by lazy { windowBackdropControls() }
    private fun windowBackdropControls(): JPanel {
        val alphaLabel = JLabel("Backdrop alpha")
        val blurLabel = JLabel("Blur strength")
        val materialLabel = JLabel("Material")
        val blendingLabel = JLabel("Blending mode")
        val stateLabel = JLabel("State")

        materialCombo.addActionListener {
            val material = materialCombo.selectedItem as MacosVibrancyMaterial
            val strength = blurStrengthForMaterial(material)
            if (blurSlider.value != strength) {
                blurSlider.value = strength
            }
            onChange(currentSpec())
        }

        val controlsPanel = DemoApp.transparentPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            fill = GridBagConstraints.HORIZONTAL
            gridy = 0
            gridx = 0
            gridwidth = 1
        }
        controlsPanel.add(alphaLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(alphaValue, gbc)

        gbc.apply {
            gridy = 1
            gridx = 0
            weightx = 1.0
            gridwidth = 2
        }
        controlsPanel.add(alphaSlider, gbc)

        gbc.apply {
            gridy = 2
            gridx = 0
            gridwidth = 1
            weightx = 0.0
        }
        controlsPanel.add(blurLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(blurValue, gbc)

        gbc.apply {
            gridy = 3
            gridx = 0
            weightx = 1.0
            gridwidth = 2
        }
        controlsPanel.add(blurSlider, gbc)

        gbc.apply {
            gridy = 4
            gridx = 0
            gridwidth = 1
            weightx = 0.0
        }
        controlsPanel.add(materialLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(materialCombo, gbc)

        gbc.apply {
            gridy = 5
            gridx = 0
        }
        controlsPanel.add(blendingLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(blendingCombo, gbc)

        gbc.apply {
            gridy = 6
            gridx = 0
        }
        controlsPanel.add(stateLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(stateCombo, gbc)

        gbc.apply {
            gridy = 7
            gridx = 0
            gridwidth = 2
        }
        controlsPanel.add(emphasizedCheck, gbc)
        return controlsPanel
    }

}
