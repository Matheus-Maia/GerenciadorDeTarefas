package gerenciadordetarefas;

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
}