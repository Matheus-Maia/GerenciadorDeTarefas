package gerenciadordetarefas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gerencia as listas de tarefas (A Fazer, Fazendo, Pronto).
 */
public class GerenciadorTarefas {
    private final Map<Status, List<Tarefa>> tarefasPorStatus;

    public GerenciadorTarefas() {
        tarefasPorStatus = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            tarefasPorStatus.put(s, new ArrayList<>());
        }
    }

    // Construtor para inicializar com listas pré-carregadas
    public GerenciadorTarefas(List<Tarefa> aFazer, List<Tarefa> fazendo, List<Tarefa> pronto) {
        this(); // Chama o construtor padrão para inicializar o mapa
        tarefasPorStatus.get(Status.AFAZER).addAll(aFazer);
        tarefasPorStatus.get(Status.FAZENDO).addAll(fazendo);
        tarefasPorStatus.get(Status.PRONTO).addAll(pronto);
    }

    public void adicionarTarefa(String descricao) {
        Tarefa novaTarefa = new Tarefa(descricao); // Status padrão AFAZER
        tarefasPorStatus.get(Status.AFAZER).add(novaTarefa);
    }

    /**
     * Encontra uma tarefa em uma lista específica pelo seu índice.
     * Usado pela interface do usuário para identificar a tarefa a ser manipulada.
     * @param status A lista onde procurar.
     * @param indice O índice da tarefa (base 1, conforme exibido ao usuário).
     * @return Um Optional contendo a Tarefa se encontrada, ou Optional vazio caso contrário.
     */
    public Optional<Tarefa> getTarefaPorIndice(Status status, int indice) {
        List<Tarefa> lista = tarefasPorStatus.get(status);
        // Valida se a lista existe e se o índice está dentro dos limites válidos
        if (lista != null && indice > 0 && indice <= lista.size()) {
            return Optional.of(lista.get(indice - 1)); // Ajusta índice para base 0 da lista
        }
        return Optional.empty(); // Retorna vazio se o índice for inválido ou a lista não existir
    }


    /**
     * Remove uma tarefa do gerenciador.
     * A identificação da tarefa correta depende da implementação de equals() na classe Tarefa (baseado em UUID).
     * @param tarefa O objeto Tarefa a ser removido.
     * @return true se a tarefa foi encontrada e removida, false caso contrário.
     */
    public boolean removerTarefa(Tarefa tarefa) {
        Status statusAtual = tarefa.getStatus();
        List<Tarefa> lista = tarefasPorStatus.get(statusAtual);
        if (lista != null) {
            // List.remove(Object) usa o método equals() da Tarefa para encontrar o item correto
            return lista.remove(tarefa);
        }
        return false; // Tarefa não encontrada na lista correspondente ao seu status
    }

    /**
     * Move uma tarefa de uma lista de status para outra.
     * @param tarefa O objeto Tarefa a ser movido.
     * @param novoStatus O Status de destino da tarefa.
     * @return true se a tarefa foi movida com sucesso, false caso contrário.
     */
    public boolean moverTarefa(Tarefa tarefa, Status novoStatus) {
        Status statusOrigem = tarefa.getStatus();
        if (statusOrigem == novoStatus) {
            System.out.println("A tarefa já está na lista de destino.");
            return false; // Não há o que mover se origem e destino são iguais
        }

        List<Tarefa> listaOrigem = tarefasPorStatus.get(statusOrigem);
        List<Tarefa> listaDestino = tarefasPorStatus.get(novoStatus);

        // Verifica se ambas as listas (origem e destino) existem no mapa
        if (listaOrigem != null && listaDestino != null) {
             // Tenta remover da lista de origem usando equals() (baseado em UUID)
            if (listaOrigem.remove(tarefa)) {
                // Se removido com sucesso, atualiza o status interno da tarefa
                tarefa.setStatus(novoStatus);
                // Atualiza a data de conclusão conforme o novo status
                if (novoStatus == Status.PRONTO) {
                    tarefa.setDataConclusao(new Date()); // Define data de conclusão ao mover para PRONTO
                } else {
                    tarefa.setDataConclusao(null); // Remove data de conclusão se sair de PRONTO
                }
                // Adiciona a tarefa à lista de destino
                listaDestino.add(tarefa);
                return true; // Movimentação bem-sucedida
            } else {
                // Tarefa não encontrada na lista de origem (pode já ter sido movida/removida)
                 System.err.println("Erro: Tarefa com ID " + tarefa.getId() + " não encontrada na lista '" + statusOrigem.getDescricao() + "'.");
                return false;
            }
        }
        System.err.println("Erro: Status de origem ou destino inválido no mapa interno.");
        return false; // Falha se as listas não foram encontradas
    }

    /**
     * Retorna uma visão não modificável da lista de tarefas para um status específico.
     * Protege a lista interna de modificações externas.
     * @param status O status desejado.
     * @return Uma lista não modificável de tarefas.
     */
    public List<Tarefa> getTarefasPorStatus(Status status) {
        // Retorna uma lista vazia não modificável se o status não existir ou não tiver tarefas
        return Collections.unmodifiableList(tarefasPorStatus.getOrDefault(status, Collections.emptyList()));
    }

    /**
     * Retorna um mapa não modificável contendo todas as tarefas organizadas por status.
     * As listas internas também são não modificáveis para proteger a estrutura de dados.
     * @return Mapa não modificável de Status para Lista de Tarefas não modificável.
     */
    public Map<Status, List<Tarefa>> getTodasTarefas() {
        Map<Status, List<Tarefa>> resultado = new EnumMap<>(Status.class);
        // Itera sobre as entradas do mapa interno
        for (Map.Entry<Status, List<Tarefa>> entry : tarefasPorStatus.entrySet()) {
            // Coloca uma visão não modificável de cada lista no mapa de resultado
            resultado.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        // Retorna uma visão não modificável do mapa de resultado
        return Collections.unmodifiableMap(resultado);
    }
}package gerenciadordetarefas;

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
                return; // Sai do método após o fallback
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
}package gerenciadordetarefas;

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
}package gerenciadordetarefas;

import java.io.*;
import java.util.*;

/**
 * Responsável por carregar e salvar as tarefas em um arquivo.
 * Utiliza um formato CSV simples com UUID como identificador.
 * Formato: ID,Status,Descricao,DataCriacaoMillis,DataConclusaoMillis(ou vazio)
 * ATENÇÃO: Descrições contendo vírgula (,) ou quebra de linha ainda podem corromper o arquivo.
 * Considerando usar bibliotecas CSV mais robustas (como Apache Commons CSV)
 */
public class PersistenciaTarefas {

    private static final String SEPARADOR = ",";

    /**
     * Salva o estado atual das tarefas no arquivo especificado.
     * @param tarefasPorStatus Mapa contendo as tarefas organizadas por status.
     * @param nomeArquivo O caminho do arquivo onde salvar as tarefas.
     */
    public void salvarTarefas(Map<Status, List<Tarefa>> tarefasPorStatus, String nomeArquivo) {
        // Usa try-with-resources para garantir que o PrintWriter seja fechado
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            // Itera sobre todas as listas de tarefas no mapa
            for (List<Tarefa> lista : tarefasPorStatus.values()) {
                // Itera sobre cada tarefa na lista
                for (Tarefa tarefa : lista) {
                    // Formata a tarefa para CSV e escreve no arquivo
                    writer.println(formatarParaCsv(tarefa));
                }
            }
            System.out.println("Tarefas salvas com sucesso em " + nomeArquivo);
        } catch (IOException e) {
            // Imprime erro se houver problema ao escrever no arquivo
            System.err.println("Erro crítico ao salvar tarefas: " + e.getMessage());
            e.printStackTrace(); // Mostra o stack trace para depuração
        }
    }

    /**
     * Formata um objeto Tarefa em uma linha de string CSV.
     * @param tarefa A tarefa a ser formatada.
     * @return Uma string representando a tarefa no formato CSV.
     */
    private String formatarParaCsv(Tarefa tarefa) {
        // Formato: ID,Status,Descricao,DataCriacaoMillis,DataConclusaoMillis(ou vazio)
        return String.join(SEPARADOR,
                tarefa.getId().toString(), // ID como String
                tarefa.getStatus().name(), // Nome do Enum (ex: AFAZER)
                tarefa.getDescricao().replace(SEPARADOR, " "), // Substitui vírgulas na descrição para evitar quebrar o CSV
                String.valueOf(tarefa.getDataCriacao().getTime()), // Data de criação em milissegundos
                (tarefa.getDataConclusao() != null ? String.valueOf(tarefa.getDataConclusao().getTime()) : "") // Data de conclusão em ms ou vazio
        );
    }

    /**
     * Carrega as tarefas do arquivo especificado.
     * @param nomeArquivo O caminho do arquivo de onde carregar as tarefas.
     * @return Um mapa contendo as tarefas carregadas, organizadas por status. Retorna mapa com listas vazias se o arquivo não existir.
     */
    public Map<Status, List<Tarefa>> carregarTarefas(String nomeArquivo) {
        // Inicializa o mapa que conterá as tarefas carregadas
        Map<Status, List<Tarefa>> tarefasCarregadas = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            tarefasCarregadas.put(s, new ArrayList<>()); // Garante que cada status tenha uma lista (mesmo que vazia)
        }

        File arquivo = new File(nomeArquivo);
        // Verifica se o arquivo existe antes de tentar ler
        if (!arquivo.exists()) {
            System.out.println("Arquivo de tarefas '" + nomeArquivo + "' não encontrado. Iniciando com listas vazias.");
            return tarefasCarregadas; // Retorna mapa com listas vazias
        }

        // Usa try-with-resources para garantir que o Scanner seja fechado
        try (Scanner scanner = new Scanner(arquivo)) {
            int numeroLinha = 0;
            // Lê o arquivo linha por linha
            while (scanner.hasNextLine()) {
                numeroLinha++;
                String linha = scanner.nextLine();
                // Ignora linhas vazias
                if (linha.trim().isEmpty()) {
                    continue;
                }
                try {
                    // Tenta converter a linha CSV em um objeto Tarefa
                    Tarefa tarefa = parseCsv(linha);
                    // Adiciona a tarefa carregada à lista correspondente ao seu status
                    tarefasCarregadas.get(tarefa.getStatus()).add(tarefa);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException e) {
                    // Captura erros específicos de parsing ou formato inválido
                    System.err.printf("Erro ao processar linha %d (ignorada): '%s' - %s%n", numeroLinha, linha, e.getMessage());
                } catch (Exception e) {
                     // Captura qualquer outro erro inesperado durante o parsing da linha
                     System.err.printf("Erro inesperado ao processar linha %d (ignorada): '%s' - %s%n", numeroLinha, linha, e.getMessage());
                     e.printStackTrace();
                }
            }
            System.out.println("Tarefas carregadas com sucesso de " + nomeArquivo);
        } catch (FileNotFoundException e) {
             // Este catch é tecnicamente redundante devido ao check de arquivo.exists(), mas é boa prática mantê-lo.
             System.err.println("Erro crítico: Arquivo não encontrado após verificação inicial: " + e.getMessage());
        } catch (Exception e) { // Captura outras exceções gerais durante a leitura do arquivo
            System.err.println("Erro inesperado ao carregar o arquivo de tarefas: " + e.getMessage());
            e.printStackTrace();
        }
        return tarefasCarregadas; // Retorna o mapa com as tarefas carregadas
    }

     /**
      * Converte uma linha de string CSV em um objeto Tarefa.
      * @param linhaCsv A linha lida do arquivo CSV.
      * @return O objeto Tarefa criado a partir da linha CSV.
      * @throws IllegalArgumentException Se o formato da linha for inválido ou dados essenciais estiverem faltando.
      * @throws ArrayIndexOutOfBoundsException Se a linha não contiver o número esperado de campos.
      * @throws NullPointerException Se ocorrer um erro inesperado com os dados.
      */
     private Tarefa parseCsv(String linhaCsv) {
        // Aumenta o limite do split para garantir que a data de conclusão seja capturada corretamente, mesmo se vazia
        // Ex: "id,STATUS,Descricao,12345," -> split(",", 5) resulta em 5 partes, a última sendo ""
        String[] partes = linhaCsv.split(SEPARADOR, 5);
        // Validação básica: precisa de pelo menos ID, Status, Descrição, DataCriação
        if (partes.length < 4) {
            throw new IllegalArgumentException(String.format("Formato CSV inválido - esperado pelo menos 4 partes, encontrado %d", partes.length));
        }

        // Valida e converte cada parte
        UUID id = UUID.fromString(partes[0].trim()); // Lê o ID da primeira parte
        Status status = Status.fromString(partes[1].trim()); // Status é a segunda parte
        String descricao = partes[2].trim(); // Descrição é a terceira parte
        Date dataCriacao = new Date(Long.parseLong(partes[3].trim())); // Data de criação é a quarta

        // Data de conclusão (opcional, quinta parte)
        Date dataConclusao = null;
        if (partes.length == 5 && !partes[4].trim().isEmpty()) {
            try {
                dataConclusao = new Date(Long.parseLong(partes[4].trim()));
            } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Formato inválido para data de conclusão: " + partes[4], e);
            }
        }
         // Validação da descrição (não pode ser vazia após o trim)
        if (descricao.isEmpty()) {
            throw new IllegalArgumentException("Descrição da tarefa não pode ser vazia no arquivo CSV.");
        }

        // Cria a tarefa usando o construtor interno com ID
        Tarefa tarefa = new Tarefa(id, descricao, dataCriacao, dataConclusao, status);
        return tarefa;
    }
}package gerenciadordetarefas;

/**
 * Enumeração representando os possíveis status de uma tarefa.
 */
public enum Status {
    AFAZER("A Fazer"),
    FAZENDO("Fazendo"),
    PRONTO("Pronto");

    private final String descricao;

    Status(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return this.name(); // Usado para salvar/carregar
    }

    public static Status fromString(String text) {
        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Nenhum status encontrado para a string: " + text);
    }
}package gerenciadordetarefas;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.UUID; // Importar UUID

/**
 * Representa uma tarefa individual na lista de TODO, identificada por um UUID único.
 */
public class Tarefa {
    private final UUID id; // ID único e imutável para cada tarefa
    private String descricao;
    private Date dataCriacao;
    private Date dataConclusao;
    private Status status;
    // Formato de data para exibição amigável
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // Construtor principal: Gera um novo ID
    public Tarefa(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da tarefa não pode ser vazia.");
        }
        this.id = UUID.randomUUID(); // Gera um ID único
        this.descricao = descricao.trim();
        this.dataCriacao = new Date();
        this.dataConclusao = null;
        this.status = Status.AFAZER;
    }

    // Construtor interno para carregar do arquivo (agora inclui ID)
    Tarefa(UUID id, String descricao, Date dataCriacao, Date dataConclusao, Status status) {
        this.id = id; // Usa o ID lido do arquivo
        this.descricao = descricao;
        this.dataCriacao = dataCriacao;
        this.dataConclusao = dataConclusao;
        this.status = status;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public Date getDataCriacao() {
        return dataCriacao == null ? null : (Date) dataCriacao.clone();
    }

    public Date getDataConclusao() {
        return dataConclusao == null ? null : (Date) dataConclusao.clone();
    }

    public Status getStatus() {
        return status;
    }

    // Setters
    public void setDescricao(String descricao) {
         if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da tarefa não pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }

    void setStatus(Status status) {
        this.status = status;
    }

    void setDataConclusao(Date dataConclusao) {
        this.dataConclusao = dataConclusao == null ? null : (Date) dataConclusao.clone();
    }

    void setDataCriacao(Date dataCriacao) {
         this.dataCriacao = dataCriacao == null ? null : (Date) dataCriacao.clone();
    }


    @Override
    public String toString() {
        return String.format("Descrição: %s (Criada em: %s%s, Status: %s)",
                descricao,
                DATE_FORMAT.format(dataCriacao),
                (dataConclusao != null ? ", Concluída em: " + DATE_FORMAT.format(dataConclusao) : ""),
                status.getDescricao());
    }

    /**
     * Implementação de equals baseada *somente* no ID único.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarefa tarefa = (Tarefa) o;
        return Objects.equals(id, tarefa.id); // Compara apenas os IDs
    }

    /**
     * Implementação de hashCode baseada *somente* no ID único.
     * Consistente com equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id); // Gera hash apenas a partir do ID
    }
}