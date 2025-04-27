package gerenciadordetarefas;

import java.util.*;

/**
 * Classe principal que executa a interface de linha de comando (CLI)
 * para o gerenciador de tarefas.
 */
public class Main {
    // Constante para o nome do arquivo de persistência
    private static final String NOME_ARQUIVO_TAREFAS = "tarefas.csv";
    // Scanner compartilhado para leitura da entrada do usuário (fechado no final)
    private static final Scanner scanner = new Scanner(System.in);
    // Instância para lidar com a persistência de tarefas
    private static final PersistenciaTarefas persistencia = new PersistenciaTarefas();
    // Instância para limpar a tela do consoles
    private static final LimpadorTela limpadorTela = new LimpadorTela();
    // Instância do gerenciador de tarefas (inicializada no main)
    private static GerenciadorTarefas gerenciador;

    /**
     * Ponto de entrada da aplicação.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {

        // 1. Carregar tarefas existentes do arquivo
        Map<Status, List<Tarefa>> tarefasIniciais = persistencia.carregarTarefas(NOME_ARQUIVO_TAREFAS);

        // 2. Inicializar o gerenciador com as tarefas carregadas
        //    Usa getOrDefault para garantir que listas vazias sejam passadas se um status não tiver tarefas
        gerenciador = new GerenciadorTarefas(
                tarefasIniciais.getOrDefault(Status.AFAZER, Collections.emptyList()),
                tarefasIniciais.getOrDefault(Status.FAZENDO, Collections.emptyList()),
                tarefasIniciais.getOrDefault(Status.PRONTO, Collections.emptyList())
        );

        // 3. Iniciar o loop principal do menu
        int opcao;
        do {
            limpadorTela.limparTela(); // Limpa a tela antes de mostrar o menu
            exibirMenu();              // Mostra as opções
            opcao = lerOpcao();        // Lê a escolha do usuário
            processarOpcao(opcao);     // Executa a ação correspondente
        } while (opcao != 0); // Continua até o usuário escolher sair (opção 0)

        // 4. Mensagem de saída e fechamento do scanner
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
        int opcao = -1; // Valor padrão para indicar erro ou opção inválida
        try {
            // Tenta ler o próximo inteiro da entrada
            opcao = scanner.nextInt();
        } catch (InputMismatchException e) {
            // Se o usuário digitar algo que não é um inteiro
            System.out.println("Erro: Por favor, insira um número inteiro válido.");
            opcao = -1; // Mantém -1 para indicar erro
        } finally {
             // SEMPRE consome o restante da linha (o caractere de nova linha '\n')
             // Isso evita problemas na próxima leitura com scanner.nextLine()
             scanner.nextLine();
        }
        return opcao;
    }

    /**
     * Direciona a execução para o método apropriado com base na opção escolhida pelo usuário.
     * @param opcao O número da opção escolhida.
     */
    private static void processarOpcao(int opcao) {
        // Limpa a tela *antes* de processar a opção para um feedback mais limpo
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
        // Pausa a execução após cada ação (exceto sair) para que o usuário veja o resultado
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
        String descricao = scanner.nextLine(); // Lê a linha inteira da descrição
        try {
             // Tenta adicionar a tarefa através do gerenciador
             gerenciador.adicionarTarefa(descricao);
             System.out.println("\nTarefa '" + descricao + "' adicionada com sucesso à lista 'A Fazer'.");
        } catch (IllegalArgumentException e) {
             // Se a descrição for inválida (vazia), captura o erro
             System.err.println("\nErro ao adicionar tarefa: " + e.getMessage());
        }
    }

    /**
     * Exibe todas as tarefas cadastradas, organizadas por status.
     */
    private static void listarTarefas() {
        System.out.println("--- Lista de Tarefas ---");
        // Obtém um mapa não modificável de todas as tarefas do gerenciador
        Map<Status, List<Tarefa>> todasTarefas = gerenciador.getTodasTarefas();

        boolean algumaTarefaExibida = false; // Flag para verificar se há tarefas no sistema

        // Itera sobre os possíveis status na ordem definida no Enum (AFAZER, FAZENDO, PRONTO)
        for (Status status : Status.values()) {
            List<Tarefa> tarefas = todasTarefas.get(status); // Obtém a lista para o status atual
            System.out.println("\n--- " + status.getDescricao() + " ---"); // Imprime o cabeçalho da seção
            if (tarefas == null || tarefas.isEmpty()) {
                // Se não houver tarefas para este status
                System.out.println("(Nenhuma tarefa nesta lista)");
            } else {
                // Se houver tarefas, marca a flag e lista cada uma com um índice
                algumaTarefaExibida = true;
                for (int i = 0; i < tarefas.size(); i++) {
                    // Imprime o índice (base 1) e a representação string da tarefa
                    System.out.printf("%d. %s%n", i + 1, tarefas.get(i).toString());
                }
            }
        }
        // Se nenhuma tarefa foi exibida em nenhuma lista
        if (!algumaTarefaExibida) {
             System.out.println("\n>>> Nenhuma tarefa cadastrada no sistema. <<<");
        }
    }

     /**
      * Guia o usuário para selecionar uma tarefa de uma lista e movê-la para outra.
      */
     private static void moverTarefa() {
        System.out.println("--- Mover Tarefa ---");

        // 1. Selecionar Status de Origem
        Status statusOrigem = selecionarStatus("De qual lista deseja mover a tarefa?");
        if (statusOrigem == null) return; // Usuário cancelou

        // 2. Selecionar a Tarefa específica da lista de origem
        Optional<Tarefa> tarefaOpt = selecionarTarefa(statusOrigem, "Digite o número da tarefa para MOVER:");
        if (tarefaOpt.isEmpty()) return; // Usuário cancelou ou lista vazia/índice inválido

        Tarefa tarefa = tarefaOpt.get(); // Obtém a tarefa do Optional

        // 3. Selecionar Status de Destino
        Status statusDestino = selecionarStatus("Para qual lista deseja mover a tarefa '" + tarefa.getDescricao() + "'?");
        if (statusDestino == null) return; // Usuário cancelou

        // 4. Tentar mover a tarefa
        if (gerenciador.moverTarefa(tarefa, statusDestino)) {
            // Mensagem de sucesso se o gerenciador retornar true
            System.out.printf("\nTarefa '%s' movida com sucesso de '%s' para '%s'.%n",
                    tarefa.getDescricao(), statusOrigem.getDescricao(), statusDestino.getDescricao());
        } else {
            // Mensagem de erro se o gerenciador retornar false (pode ocorrer se a tarefa já foi movida/removida
            // ou se origem e destino eram iguais, embora essa checagem também esteja em `moverTarefa`)
            System.err.println("\nErro ao tentar mover a tarefa. Verifique se ela ainda existe na lista de origem ou se o status de destino é diferente.");
        }
    }

     /**
      * Guia o usuário para selecionar e remover uma tarefa de uma lista.
      */
     private static void removerTarefa() {
        System.out.println("--- Remover Tarefa ---");

        // 1. Selecionar Status da lista onde a tarefa está
        Status status = selecionarStatus("De qual lista deseja remover a tarefa?");
         if (status == null) return; // Usuário cancelou

        // 2. Selecionar a Tarefa específica da lista
        Optional<Tarefa> tarefaOpt = selecionarTarefa(status, "Digite o número da tarefa para REMOVER:");
        if (tarefaOpt.isEmpty()) return; // Usuário cancelou ou lista vazia/índice inválido

        Tarefa tarefa = tarefaOpt.get(); // Obtém a tarefa do Optional

        // 3. Confirmação do usuário
        System.out.print("\nTem certeza que deseja remover permanentemente a tarefa '" + tarefa.getDescricao() + "'? (S/N): ");
        String confirmacao = scanner.nextLine().trim().toUpperCase();

        // 4. Tentar remover se confirmado
        if (confirmacao.equals("S")) {
            if (gerenciador.removerTarefa(tarefa)) {
                // Mensagem de sucesso se o gerenciador retornar true
                System.out.println("\nTarefa removida com sucesso.");
            } else {
                 // Mensagem de erro se o gerenciador retornar false (tarefa pode não existir mais)
                 System.err.println("\nErro: Não foi possível remover a tarefa. Ela pode já ter sido removida ou movida anteriormente.");
            }
        } else {
            // Se o usuário não confirmar com 'S'
            System.out.println("\nRemoção cancelada pelo usuário.");
        }
    }


    /**
     * Exibe os status disponíveis e permite ao usuário escolher um.
     * @param mensagem A instrução a ser exibida ao usuário (ex: "De qual lista...?").
     * @return O Status escolhido pelo usuário, ou null se o usuário cancelar ou escolher uma opção inválida.
     */
    private static Status selecionarStatus(String mensagem) {
        System.out.println(mensagem); // Exibe a pergunta/instrução
        Status[] statusDisponiveis = Status.values(); // Obtém todos os valores do Enum Status

        // Lista os status disponíveis com números
        for (int i = 0; i < statusDisponiveis.length; i++) {
            System.out.printf("%d. %s%n", i + 1, statusDisponiveis[i].getDescricao());
        }
        System.out.printf("----------------------------%n");
        System.out.printf("0. Cancelar%n"); // Opção para cancelar
        System.out.print("Escolha uma opção de status: ");

        int escolha = lerOpcao(); // Lê a escolha numérica do usuário

        // Valida a escolha
        if (escolha > 0 && escolha <= statusDisponiveis.length) {
            // Se a escolha é válida (1, 2 ou 3), retorna o Status correspondente
            return statusDisponiveis[escolha - 1]; // Ajusta para índice base 0
        } else if (escolha == 0) {
             // Se o usuário escolheu cancelar
             System.out.println("\nOperação cancelada.");
            return null;
        } else {
            // Se a escolha foi inválida (nem 0, nem 1, 2, 3)
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
        // Obtém a lista de tarefas (não modificável) para o status dado
        List<Tarefa> tarefas = gerenciador.getTarefasPorStatus(status);

        // Verifica se a lista está vazia
        if (tarefas.isEmpty()) {
            System.out.println("\nNão há tarefas na lista '" + status.getDescricao() + "' para selecionar.");
            return Optional.empty(); // Retorna vazio se não há tarefas
        }

        // Exibe as tarefas numeradas da lista
        System.out.println("\n--- Tarefas em '" + status.getDescricao() + "' ---");
         for (int i = 0; i < tarefas.size(); i++) {
            // Exibe número (base 1), descrição e talvez outros detalhes via toString()
            System.out.printf("%d. %s%n", i + 1, tarefas.get(i).toString());
        }
        System.out.println("----------------------------");
        System.out.println(mensagem); // Exibe a instrução para o usuário
        System.out.print("Escolha o número da tarefa (ou 0 para Cancelar): ");

        int indiceEscolhido = lerOpcao(); // Lê o número escolhido

        // Verifica se o usuário cancelou
        if (indiceEscolhido == 0) {
             System.out.println("\nOperação cancelada.");
             return Optional.empty(); // Retorna vazio se cancelado
        }

        // Tenta obter a tarefa usando o índice escolhido (base 1) através do gerenciador
        Optional<Tarefa> tarefaOpt = gerenciador.getTarefaPorIndice(status, indiceEscolhido);

        // Informa ao usuário se o índice foi inválido
        if (tarefaOpt.isEmpty() && indiceEscolhido != 0) { // Evita msg de erro se cancelou
            System.out.println("\nNúmero de tarefa inválido para a lista '" + status.getDescricao() + "'.");
        }

        // Retorna o Optional contendo a tarefa (se encontrada) ou vazio (se inválido/cancelado)
        return tarefaOpt;
    }


    /**
     * Aciona o processo de salvar todas as tarefas atuais no arquivo.
     */
    private static void salvarTarefas() {
        System.out.println("--- Salvar Tarefas ---");
        System.out.println("Salvando o estado atual das tarefas no arquivo: " + NOME_ARQUIVO_TAREFAS);
        // Chama o método de persistência, passando o mapa de tarefas do gerenciador e o nome do arquivo
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