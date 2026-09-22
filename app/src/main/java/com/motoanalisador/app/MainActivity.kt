package com.motoanalisador.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var resultado: TextView
    private lateinit var valor: EditText
    private lateinit var kmBusca: EditText
    private lateinit var kmViagem: EditText
    private lateinit var minutos: EditText
    private lateinit var limiteBoa: EditText
    private lateinit var limiteRegular: EditText

    private lateinit var diagnosticoPacote: TextView
    private lateinit var diagnosticoTexto: TextView
    private lateinit var diagnosticoHorario: TextView

    private val prefs by lazy {
        getSharedPreferences("moto_analisador", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val tela = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 45, 40, 45)
            setBackgroundColor(Color.rgb(16, 18, 22))
        }

        scroll.addView(tela)

        fun texto(t: String, size: Float = 18f) =
            TextView(this).apply {
                text = t
                textSize = size
                setTextColor(Color.WHITE)
                setPadding(0, 10, 0, 8)
            }

        tela.addView(texto("🏍️ Moto Analisador", 28f))
        tela.addView(
            texto("Você decide. O app apenas analisa.", 15f)
        )

        valor = campo("Valor da oferta (R$)")
        kmBusca = campo("Km até o passageiro")
        kmViagem = campo("Km da viagem")
        minutos = campo("Tempo estimado total (min)")

        listOf(
            valor,
            kmBusca,
            kmViagem,
            minutos
        ).forEach {
            tela.addView(it)
        }

        val botao = Button(this).apply {
            text = "ANALISAR CORRIDA"

            setOnClickListener {
                analisar()
            }
        }

        tela.addView(
            botao,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        )

        resultado = TextView(this).apply {
            text = "Informe os dados da corrida."
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(20, 28, 20, 28)
            setBackgroundColor(Color.DKGRAY)
        }

        tela.addView(
            resultado,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        )

        val limpar = Button(this).apply {
            text = "LIMPAR"

            setOnClickListener {
                limparCampos()
            }
        }

        tela.addView(
            limpar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10
            }
        )

        tela.addView(
            texto(
                "⚙️ Configuração do R$/km",
                20f
            )
        )

        limiteBoa =
            campo("Boa a partir de (R$/km)")

        limiteRegular =
            campo("Regular a partir de (R$/km)")

        limiteBoa.setText(
            formatarEntrada(
                prefs.getFloat(
                    "limiteBoa",
                    1.50f
                ).toDouble()
            )
        )

        limiteRegular.setText(
            formatarEntrada(
                prefs.getFloat(
                    "limiteRegular",
                    1.00f
                ).toDouble()
            )
        )

        tela.addView(limiteBoa)
        tela.addView(limiteRegular)

        val salvar = Button(this).apply {
            text = "SALVAR CONFIGURAÇÕES"

            setOnClickListener {
                salvarConfiguracoes()
            }
        }

        tela.addView(salvar)

        tela.addView(
            texto(
                "Os limites ficam salvos neste celular.",
                13f
            )
        )

        /*
         * PAINEL DE DIAGNÓSTICO DA VERSÃO 0.3
         */

        tela.addView(
            texto(
                "🔎 Leitura automática — teste 0.3",
                21f
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 30
            }
        )

        tela.addView(
            texto(
                "Nesta etapa vamos verificar quais informações da tela podem ser lidas pelo Moto Analisador.",
                14f
            )
        )

        val ativarLeitura = Button(this).apply {
            text = "ATIVAR LEITURA DA TELA"

            setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Não foi possível abrir as configurações de acessibilidade.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        tela.addView(
            ativarLeitura,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        )

        val atualizarLeitura = Button(this).apply {
            text = "ATUALIZAR LEITURA"

            setOnClickListener {
                atualizarDiagnostico()
            }
        }

        tela.addView(
            atualizarLeitura,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        )

        diagnosticoPacote = TextView(this).apply {
            text = "Aplicativo detectado: nenhum"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(12, 18, 12, 4)
        }

        tela.addView(diagnosticoPacote)

        diagnosticoHorario = TextView(this).apply {
            text = "Última leitura: nenhuma"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(12, 4, 12, 10)
        }

        tela.addView(diagnosticoHorario)

        diagnosticoTexto = TextView(this).apply {
            text = "Nenhum texto capturado ainda."
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(20, 20, 20, 20)
            setBackgroundColor(
                Color.rgb(35, 38, 44)
            )
        }

        tela.addView(
            diagnosticoTexto,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 30
            }
        )

        atualizarDiagnostico()

        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()

        if (::diagnosticoTexto.isInitialized) {
            atualizarDiagnostico()
        }
    }

    private fun atualizarDiagnostico() {

        val pacote =
            prefs.getString(
                MotoAccessibilityService.CHAVE_PACOTE,
                null
            )

        val textoCapturado =
            prefs.getString(
                MotoAccessibilityService.CHAVE_TEXTO_TELA,
                null
            )

        val horario =
            prefs.getLong(
                MotoAccessibilityService.CHAVE_HORARIO,
                0L
            )

        diagnosticoPacote.text =
            if (pacote.isNullOrBlank()) {
                "Aplicativo detectado: nenhum"
            } else {
                "Aplicativo detectado: $pacote"
            }

        diagnosticoTexto.text =
            if (textoCapturado.isNullOrBlank()) {
                "Nenhum texto capturado ainda."
            } else {
                textoCapturado
            }

        diagnosticoHorario.text =
            if (horario <= 0L) {
                "Última leitura: nenhuma"
            } else {
                "Última leitura: " +
                    DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.MEDIUM,
                        Locale("pt", "BR")
                    ).format(
                        Date(horario)
                    )
            }
    }

    private fun campo(
        hintText: String
    ) = EditText(this).apply {

        hint = hintText

        setHintTextColor(
            Color.LTGRAY
        )

        setTextColor(
            Color.WHITE
        )

        inputType =
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun numero(
        e: EditText
    ): Double {

        return e.text
            .toString()
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0
    }

    private fun formatarEntrada(
        v: Double
    ): String {

        return String.format(
            Locale("pt", "BR"),
            "%.2f",
            v
        )
    }

    private fun limites():
        Pair<Double, Double> {

        val boa =
            numero(limiteBoa)
                .takeIf { it > 0 }
                ?: 1.50

        val regular =
            numero(limiteRegular)
                .takeIf { it > 0 }
                ?: 1.00

        return Pair(
            boa,
            regular
        )
    }

    private fun salvarConfiguracoes() {

        val (boa, regular) =
            limites()

        if (regular >= boa) {

            Toast.makeText(
                this,
                "O limite de Regular deve ser menor que o de Boa.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        prefs.edit()
            .putFloat(
                "limiteBoa",
                boa.toFloat()
            )
            .putFloat(
                "limiteRegular",
                regular.toFloat()
            )
            .apply()

        limiteBoa.setText(
            formatarEntrada(boa)
        )

        limiteRegular.setText(
            formatarEntrada(regular)
        )

        Toast.makeText(
            this,
            "Configurações salvas.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun limparCampos() {

        listOf(
            valor,
            kmBusca,
            kmViagem,
            minutos
        ).forEach {
            it.text.clear()
        }

        resultado.text =
            "Informe os dados da próxima corrida."

        resultado.setBackgroundColor(
            Color.DKGRAY
        )

        valor.requestFocus()
    }

    private fun analisar() {

        val oferta =
            numero(valor)

        val distanciaTotal =
            numero(kmBusca) +
                numero(kmViagem)

        val tempo =
            numero(minutos)

        val (boa, regular) =
            limites()

        if (regular >= boa) {

            resultado.text =
                "Revise as configurações: Regular deve ser menor que Boa."

            resultado.setBackgroundColor(
                Color.DKGRAY
            )

            return
        }

        if (
            oferta <= 0 ||
            distanciaTotal <= 0
        ) {

            resultado.text =
                "Preencha corretamente o valor e as distâncias."

            resultado.setBackgroundColor(
                Color.DKGRAY
            )

            return
        }

        val valorKm =
            oferta / distanciaTotal

        val valorHora =
            if (tempo > 0) {
                oferta /
                    (tempo / 60.0)
            } else {
                0.0
            }

        val valorMinimoBoa =
            distanciaTotal * boa

        val classificacao: String
        val cor: Int

        when {

            valorKm >= boa -> {

                classificacao =
                    "🟢 BOA CORRIDA"

                cor =
                    Color.rgb(
                        20,
                        120,
                        65
                    )
            }

            valorKm >= regular -> {

                classificacao =
                    "🟡 CORRIDA REGULAR"

                cor =
                    Color.rgb(
                        190,
                        140,
                        0
                    )
            }

            else -> {

                classificacao =
                    "🔴 CORRIDA RUIM"

                cor =
                    Color.rgb(
                        165,
                        35,
                        35
                    )
            }
        }

        val dinheiro =
            NumberFormat
                .getCurrencyInstance(
                    Locale(
                        "pt",
                        "BR"
                    )
                )

        val horaTexto =
            if (tempo > 0) {

                dinheiro.format(
                    valorHora
                ) + "/hora"

            } else {

                "R$/hora: não informado"
            }

        resultado.setBackgroundColor(
            cor
        )

        resultado.text =
            classificacao +
                "\n\n" +
                dinheiro.format(valorKm) +
                "/km" +
                "\n" +
                horaTexto +
                "\n" +
                String.format(
                    Locale("pt", "BR"),
                    "%.1f",
                    distanciaTotal
                ) +
                " km totais" +
                "\n\nOferta: " +
                dinheiro.format(oferta) +
                "\nPara ser BOA: mínimo " +
                dinheiro.format(
                    valorMinimoBoa
                )
    }
}
