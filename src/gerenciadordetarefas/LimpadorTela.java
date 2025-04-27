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
            String os = System.getProperty("os.name").toLowerCase(); // Obtém o nome do SO em minúsculas

            // Verifica o sistema operacional e define o comando apropriado
            if (os.contains("windows")) {
                pb = new ProcessBuilder("cmd", "/c", "cls"); // Comando para Windows
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                pb = new ProcessBuilder("clear"); // Comando para Unix/Linux/macOS
            } else {
                // Fallback para sistemas operacionais não reconhecidos: imprime várias linhas em branco
                System.out.println("Sistema operacional não suportado para limpeza de tela automática.");
                System.out.println("Imprimindo linhas em branco como alternativa...");
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
                return;
            }

            // Configura o processo para usar os mesmos fluxos de entrada/saída/erro do processo Java atual
            Process process = pb.inheritIO().start();
            // Espera o comando de limpeza terminar sua execução
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            // Se ocorrer um erro ao executar o comando (ex: comando não encontrado, permissão negada)
            // imprime uma mensagem de erro mas não interrompe a aplicação.
            System.err.println("Aviso: Erro ao tentar limpar o terminal: " + e.getMessage());
            // Restaura o status de interrupção se a thread foi interrompida durante waitFor()
            if (e instanceof InterruptedException) {
                 Thread.currentThread().interrupt();
            }
        } catch (SecurityException e) {
             // Se houver restrições de segurança impedindo a execução de processos externos
             System.err.println("Aviso: Restrição de segurança impediu a limpeza do terminal: " + e.getMessage());
        }
    }
}