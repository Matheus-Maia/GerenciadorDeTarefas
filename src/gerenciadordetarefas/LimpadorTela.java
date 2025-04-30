package gerenciadordetarefas;

import java.io.IOException;

/**
 * Utilitário para limpar a tela do console.
 * Funciona em Windows (cls) e sistemas baseados em Unix (clear).
 * Fornece um fallback simples para outros sistemas.
 */
public class LimpadorTela {

    /**
     * Tenta limpar a tela do console executando o comando apropriado do sistema operacional.
     */
    public void limparTela() {
        try {
            ProcessBuilder pb;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows")) {
                // Comando para Windows
                pb = new ProcessBuilder("cmd", "/c", "cls");
                Process process = pb.inheritIO().start();
                process.waitFor();
                return;  // Sai após limpar no Windows
            } 

            if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Comando Unix corrigido e com tratamento de erro
                pb = new ProcessBuilder("sh", "-c", "clear 2>/dev/null");  // Corrigido /dev/null
                Process process = pb.inheritIO().start();
                int exitCode = process.waitFor();

                // Se o clear falhar (ex: NetBeans), usa fallback
                if (exitCode != 0) {
                    fallbackLimpeza();
                }
                return;  // Sai após limpar no Unix
            }

            // Fallback para outros sistemas
            fallbackLimpeza();

        } catch (IOException | InterruptedException e) {
            fallbackLimpeza();  // Fallback em caso de exceção
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } catch (SecurityException e) {
            fallbackLimpeza();
        }
    }

    private void fallbackLimpeza() {
        for (int i = 0; i < 30; i++) {
            System.out.println();
        }
    }
}