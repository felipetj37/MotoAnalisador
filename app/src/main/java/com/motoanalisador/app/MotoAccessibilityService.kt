package com.motoanalisador.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MotoAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "MotoAnalisador"
        const val PREFS = "moto_analisador"
        const val CHAVE_TEXTO_TELA = "ultimo_texto_tela"
        const val CHAVE_PACOTE = "ultimo_pacote"
        const val CHAVE_HORARIO = "ultimo_horario"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(
            TAG,
            "Serviço de acessibilidade conectado."
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        val pacote =
            event.packageName
                ?.toString()
                ?: return

        /*
         * Nesta primeira versão de diagnóstico,
         * ignoramos apenas o próprio Moto Analisador.
         *
         * Assim poderemos descobrir qual pacote e quais
         * textos a tela do aplicativo de motorista
         * realmente disponibiliza ao Android.
         */
        if (pacote == packageName) {
            return
        }

        val raiz =
            rootInActiveWindow
                ?: return

        val textos =
            mutableListOf<String>()

        coletarTextos(
            raiz,
            textos
        )

        raiz.recycle()

        val textoCompleto =
            textos
                .distinct()
                .joinToString("\n")

        if (textoCompleto.isBlank()) {
            return
        }

        getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                CHAVE_TEXTO_TELA,
                textoCompleto
            )
            .putString(
                CHAVE_PACOTE,
                pacote
            )
            .putLong(
                CHAVE_HORARIO,
                System.currentTimeMillis()
            )
            .apply()

        Log.d(
            TAG,
            "Pacote: $pacote\n$textoCompleto"
        )
    }

    private fun coletarTextos(
        node: AccessibilityNodeInfo?,
        destino: MutableList<String>
    ) {

        if (node == null) {
            return
        }

        val texto =
            node.text
                ?.toString()
                ?.trim()

        if (!texto.isNullOrBlank()) {
            destino.add(texto)
        }

        val descricao =
            node.contentDescription
                ?.toString()
                ?.trim()

        if (
            !descricao.isNullOrBlank() &&
            descricao != texto
        ) {
            destino.add(descricao)
        }

        for (
            i in 0 until node.childCount
        ) {

            val filho =
                node.getChild(i)

            coletarTextos(
                filho,
                destino
            )

            filho?.recycle()
        }
    }

    override fun onInterrupt() {

        Log.d(
            TAG,
            "Serviço de acessibilidade interrompido."
        )
    }
}
