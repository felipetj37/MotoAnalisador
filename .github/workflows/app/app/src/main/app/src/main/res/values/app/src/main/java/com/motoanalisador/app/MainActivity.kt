package com.motoanalisador.app

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var valor: EditText
    private lateinit var kmBusca: EditText
    private lateinit var kmViagem: EditText
    private lateinit var minutos: EditText
    private lateinit var resultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tela = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 50, 40, 40)
            setBackgroundColor(Color.rgb(18, 20, 24))
        }

        fun texto(conteudo: String, tamanho: Float): TextView {
            return TextView(this).apply {
                text = conteudo
                textSize = tamanho
                setTextColor(Color.WHITE)
                setPadding(0, 10, 0, 10)
            }
        }

        tela.addView(texto("🏍️ Moto Analisador", 28f))
        tela.addView(texto("Você decide. O app apenas analisa.", 15f))

        valor = campo("Valor da oferta (R$)")
        kmBusca = campo("Km até o passageiro")
        kmViagem = campo("Km da viagem")
        minutos = campo("Tempo estimado total (min)")

        tela.addView(valor)
        tela.addView(kmBusca)
        tela.addView(kmViagem)
        tela.addView(minutos)

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
                topMargin = 25
            }
        )

        resultado = TextView(this).apply {
            text = "Informe os dados da corrida."
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(20, 35, 20, 35)
        }

        tela.addView(
            resultado,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        )

        tela.addView(
            texto(
                "🟢 Boa: R$ 1,50/km ou mais\n" +
                "🟡 Regular: R$ 1,00 a R$ 1,49/km\n" +
                "🔴 Ruim: abaixo de R$ 1,00/km",
                14f
            )
        )

        setContentView(tela)
    }

    private fun campo(dica: String): EditText {
        return EditText(this).apply {
            hint = dica
            setHintTextColor(Color.LTGRAY)
            setTextColor(Color.WHITE)

            inputType =
                InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    private fun numero(campo: EditText): Double {
        return campo.text.toString()
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0
    }

    private fun analisar() {

        val oferta = numero(valor)
        val distanciaBusca = numero(kmBusca)
        val distanciaViagem = numero(kmViagem)
        val tempo = numero(minutos)

        val distanciaTotal = distanciaBusca + distanciaViagem

        if (oferta <= 0 || distanciaTotal <= 0) {
            resultado.text = "Preencha corretamente o valor e as distâncias."
            resultado.setBackgroundColor(Color.DKGRAY)
            return
        }

        val valorKm = oferta / distanciaTotal

        val valorHora =
            if (tempo > 0)
                oferta / (tempo / 60.0)
            else
                0.0

        val classificacao: String
        val cor: Int

        when {
            valorKm >= 1.50 -> {
                classificacao = "🟢 BOA CORRIDA"
                cor = Color.rgb(20, 120, 65)
            }

            valorKm >= 1.00 -> {
                classificacao = "🟡 CORRIDA REGULAR"
                cor = Color.rgb(190, 140, 0)
            }

            else -> {
                classificacao = "🔴 CORRIDA RUIM"
                cor = Color.rgb(165, 35, 35)
            }
        }

        val dinheiro =
            NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            )

        resultado.setBackgroundColor(cor)

        resultado.text =
            classificacao +
            "\n\n" +
            dinheiro.format(valorKm) + "/km" +
            "\n" +
            dinheiro.format(valorHora) + "/hora" +
            "\n" +
            String.format(
                Locale("pt", "BR"),
                "%.1f",
                distanciaTotal
            ) +
            " km totais" +
            "\n\nOferta: " +
            dinheiro.format(oferta)
    }
}
