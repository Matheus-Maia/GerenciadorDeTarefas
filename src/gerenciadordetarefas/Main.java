package gerenciadordetarefas;

import java.util.*;

/**
 * Classe principal que executa a interface de linha de comando (CLI)
 * para o gerenciador de tarefas.
 */
public class Main {
    private static final String NOME_ARQUIVO_TAREFAS = "tarefas.csv";
    private static final Scanner scanner = new Scanner(System.in);
    private static final PersistenciaTarefas persistencia = new PersistenciaTarefas();
    private static final LimpadorTela limpadorTela = new LimpadorTela();
    private static GerenciadorTarefas gerenciador;

    /**
     * Ponto de entrada da aplicação.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        Map<Status, List<Tarefa>> tarefasIniciais = persistencia.carregarTarefas(NOME_ARQUIVO_TAREFAS);

        gerenciador = new GerenciadorTarefas(
                tarefasIniciais.getOrDefault(Status.AFAZER, Collections.emptyList()),
                tarefasIniciais.getOrDefault(Status.FAZENDO, Collections.emptyList()),
                tarefasIniciais.getOrDefault(Status.PRONTO, Collections.emptyList())
        );

        // Menu loop
        int opcao;
        do {
            limpadorTela.limparTela(); 
            exibirMenu();              
            opcao = lerOpcao();      
            processarOpcao(opcao);     
        } while (opcao != 0);

        System.out.println("\nSaindo do Gerenciador de Tarefas...");
        scanner.close();
    }

    /**
     * Exibe o menu principal de opções para o usuário.
     */
    private static void exibirMenu() {
        System.out.println("\n--- Gerenciador de Tarefas ---");
        System.out.println("1. Adicionar Tarefa");
        System.out.println("2. Listar Tarefas");
        System.out.println("3. Mover Tarefa");
        System.out.println("4. Remover Tarefa");
        System.out.println("5. Salvar Tarefas");
        System.out.println("----------------------------");
        System.out.println("0. Sair");
        System.out.println("----------------------------");
        System.out.print("Escolha uma opção: ");
    }

    /**
     * Lê um número inteiro da entrada do usuário, tratando exceções.
     * @return O número da opção escolhida pelo usuário, ou -1 em caso de erro de entrada.
     */
    private static int lerOpcao() {
        int opcao = -1;
        try {
            opcao = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Erro: Por favor, insira um número inteiro válido.");
            opcao = -1;
        } finally {
             scanner.nextLine();
        }
        return opcao;
    }

    /**
     * Direciona a execução para o método apropriado com base na opção escolhida pelo usuário.
     * @param opcao O número da opção escolhida.
     */
    private static void processarOpcao(int opcao) {
        limpadorTela.limparTela();
        switch (opcao) {
            case 1:
                adicionarTarefa();
                break;
            case 2:
                listarTarefas();
                break;
            case 3:
                moverTarefa();
                break;
            case 4:
                removerTarefa();
                break;
            case 5:
                salvarTarefas();
                break;
            case 0:
                // A opção 0 (Sair) é tratada pelo loop `do-while` em `main`
                break;
            default:
                // Se a opção não for nenhuma das válidas
                System.out.println("Opção inválida. Por favor, tente novamente.");
        }

        if (opcao != 0) {
             pressioneEnterParaContinuar();
        }
    }

    /**
     * Solicita a descrição e adiciona uma nova tarefa à lista "A Fazer".
     */
    private static void adicionarTarefa() {
        System.out.println("--- Adicionar Nova Tarefa ---");
        System.out.print("Digite a descrição da tarefa: ");
        String descricao = scanner.nextLine();
        try {
             gerenciador.adicionarTarefa(descricao);
             System.out.println("\nTarefa '" + descricao + "' adicionada com sucesso à lista 'A Fazer'.");
        } catch (IllegalArgumentException e) {
             System.err.println("\nErro ao adicionar tarefa: " + e.getMessage());
        }
    }

    /**
     * Exibe todas as tarefas cadastradas, organizadas por status.
     */
    private static void listarTarefas() {
        System.out.println("--- Lista de Tarefas ---");
        Map<Status, List<Tarefa>> todasTarefas = gerenciador.getTodasTarefas();
        boolean algumaTarefaExibida = false; 

        for (Status status : Status.values()) {
            List<Tarefa> tarefas = todasTarefas.get(status); 
            System.out.println("\n--- " + status.getDescricao() + " ---");
            if (tarefas == null || tarefas.isEmpty()) {
                System.out.println("(Nenhuma tarefa nesta lista)");
            } else {
                algumaTarefaExibida = true;
                for (int i = 0; i < tarefas.size(); i++) {
                    System.out.printf("%d. %s%n", i + 1, tarefas.get(i).toString());
                }
            }
        }

        if (!algumaTarefaExibida) {
             System.out.println("\n>>> Nenhuma tarefa cadastrada no sistema. <<<");
        }
    }

     /**
      * Guia o usuário para selecionar uma tarefa de uma lista e movê-la para outra.
      */
     private static void moverTarefa() {
        System.out.println("--- Mover Tarefa ---");

        Status statusOrigem = selecionarStatus("De qual lista deseja mover a tarefa?");
        if (statusOrigem == null) return;

        Optional<Tarefa> tarefaOpt = selecionarTarefa(statusOrigem, "Digite o número da tarefa para MOVER:");
        if (tarefaOpt.isEmpty()) return; // Usuário cancelou ou lista vazia/índice inválido

        Tarefa tarefa = tarefaOpt.get();

        Status statusDestino = selecionarStatus("Para qual lista deseja mover a tarefa '" + tarefa.getDescricao() + "'?");
        if (statusDestino == null) return;

        if (gerenciador.moverTarefa(tarefa, statusDestino)) {
            System.out.printf("\nTarefa '%s' movida com sucesso de '%s' para '%s'.%n",
                    tarefa.getDescricao(), statusOrigem.getDescricao(), statusDestino.getDescricao());
        } else {
            System.err.println("\nErro ao tentar mover a tarefa. Verifique se ela ainda existe na lista de origem ou se o status de destino é diferente.");
        }
    }

     /**
      * Guia o usuário para selecionar e remover uma tarefa de uma lista.
      */
     private static void removerTarefa() {
        System.out.println("--- Remover Tarefa ---");

        Status status = selecionarStatus("De qual lista deseja remover a tarefa?");
         if (status == null) return;

        Optional<Tarefa> tarefaOpt = selecionarTarefa(status, "Digite o número da tarefa para REMOVER:");
        if (tarefaOpt.isEmpty()) return;

        Tarefa tarefa = tarefaOpt.get();

        System.out.print("\nTem certeza que deseja remover permanentemente a tarefa '" + tarefa.getDescricao() + "'? (S/N): ");
        String confirmacao = scanner.nextLine().trim().toUpperCase();

        // 4. Tentar remover se confirmado
        if (confirmacao.equals("S")) {
            if (gerenciador.removerTarefa(tarefa)) {
                System.out.println("\nTarefa removida com sucesso.");
            } else {
                 System.err.println("\nErro: Não foi possível remover a tarefa. Ela pode já ter sido removida ou movida anteriormente.");
            }
        } else {
            System.out.println("\nRemoção cancelada pelo usuário.");
        }
    }


    /**
     * Exibe os status disponíveis e permite ao usuário escolher um.
     * @param mensagem A instrução a ser exibida ao usuário (ex: "De qual lista...?").
     * @return O Status escolhido pelo usuário, ou null se o usuário cancelar ou escolher uma opção inválida.
     */
    private static Status selecionarStatus(String mensagem) {
        System.out.println(mensagem);
        Status[] statusDisponiveis = Status.values();

        for (int i = 0; i < statusDisponiveis.length; i++) {
            System.out.printf("%d. %s%n", i + 1, statusDisponiveis[i].getDescricao());
        }
        System.out.printf("----------------------------%n");
        System.out.printf("0. Cancelar%n");
        System.out.print("Escolha uma opção de status: ");

        int escolha = lerOpcao();

        if (escolha > 0 && escolha <= statusDisponiveis.length) {
            return statusDisponiveis[escolha - 1];
        } else if (escolha == 0) {
             System.out.println("\nOperação cancelada.");
            return null;
        } else {
            System.out.println("\nOpção de status inválida.");
            return null;
        }
    }

     /**
      * Lista as tarefas de um status específico e permite ao usuário selecionar uma pelo número.
      * @param status O status da lista da qual selecionar a tarefa.
      * @param mensagem A instrução a ser exibida ao usuário (ex: "Digite o número da tarefa...").
      * @return Um Optional contendo a Tarefa selecionada se a seleção for válida, ou Optional.empty() caso contrário (lista vazia, índice inválido ou cancelamento).
      */
     private static Optional<Tarefa> selecionarTarefa(Status status, String mensagem) {
        List<Tarefa> tarefas = gerenciador.getTarefasPorStatus(status);

        if (tarefas.isEmpty()) {
            System.out.println("\nNão há tarefas na lista '" + status.getDescricao() + "' para selecionar.");
            return Optional.empty();
        }

        // Exibe as tarefas numeradas da lista
        System.out.println("\n--- Tarefas em '" + status.getDescricao() + "' ---");
         for (int i = 0; i < tarefas.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, tarefas.get(i).toString());
        }
        System.out.println("----------------------------");
        System.out.println(mensagem);
        System.out.print("Escolha o número da tarefa (ou 0 para Cancelar): ");

        int indiceEscolhido = lerOpcao();

        if (indiceEscolhido == 0) {
             System.out.println("\nOperação cancelada.");
             return Optional.empty();
        }

        // Tenta obter a tarefa usando o índice escolhido através do gerenciador
        Optional<Tarefa> tarefaOpt = gerenciador.getTarefaPorIndice(status, indiceEscolhido);

        if (tarefaOpt.isEmpty() && indiceEscolhido != 0) { // Evita msg de erro se cancelou
            System.out.println("\nNúmero de tarefa inválido para a lista '" + status.getDescricao() + "'.");
        }

        return tarefaOpt;
    }


    /**
     * Aciona o processo de salvar todas as tarefas atuais no arquivo.
     */
    private static void salvarTarefas() {
        System.out.println("--- Salvar Tarefas ---");
        System.out.println("Salvando o estado atual das tarefas no arquivo: " + NOME_ARQUIVO_TAREFAS);
        persistencia.salvarTarefas(gerenciador.getTodasTarefas(), NOME_ARQUIVO_TAREFAS);
        // A mensagem de sucesso/erro já é impressa pelo método salvarTarefas da persistência
    }

     /**
      * Pausa a execução e espera que o usuário pressione Enter para continuar.
      * Útil para permitir que o usuário leia as mensagens antes que a tela seja limpa.
      */
     private static void pressioneEnterParaContinuar() {
        System.out.print("\n[Pressione Enter para voltar ao menu]");
        scanner.nextLine(); // Simplesmente consome a próxima linha (o Enter)
    }
}