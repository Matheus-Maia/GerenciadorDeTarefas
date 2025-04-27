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
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            for (List<Tarefa> lista : tarefasPorStatus.values()) {  
                for (Tarefa tarefa : lista) {
                    writer.println(formatarParaCsv(tarefa));
                }
            }
            System.out.println("Tarefas salvas com sucesso em " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro crítico ao salvar tarefas: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    /**
     * Formata um objeto Tarefa em uma linha de string CSV.
     * @param tarefa A tarefa a ser formatada.
     * @return Uma string representando a tarefa no formato CSV.
     */
    private String formatarParaCsv(Tarefa tarefa) {
        return String.join(SEPARADOR,
                tarefa.getId().toString(), 
                tarefa.getStatus().name(),
                tarefa.getDescricao().replace(SEPARADOR, " "), 

                String.valueOf(tarefa.getDataCriacao().getTime()),
                (tarefa.getDataConclusao() != null ? String.valueOf(tarefa.getDataConclusao().getTime()) : "") 
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
            tarefasCarregadas.put(s, new ArrayList<>());         }

        File arquivo = new File(nomeArquivo);
        // Verifica se o arquivo existe antes de tentar ler
        if (!arquivo.exists()) {
            System.out.println("Arquivo de tarefas '" + nomeArquivo + "' não encontrado. Iniciando com listas vazias.");
            return tarefasCarregadas;         }


        try (Scanner scanner = new Scanner(arquivo)) {
            int numeroLinha = 0;
          
            while (scanner.hasNextLine()) {
                numeroLinha++;
                String linha = scanner.nextLine();
                // Ignora linhas vazias
                if (linha.trim().isEmpty()) {
                    continue;
                }
                try {

                    Tarefa tarefa = parseCsv(linha);
                                      
                   tarefasCarregadas.get(tarefa.getStatus()).add(tarefa);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException e) {
                   
                    System.err.printf("Erro ao processar linha %d (ignorada): '%s' - %s%n", numeroLinha, linha, e.getMessage());
                } catch (Exception e) {
                   
                     System.err.printf("Erro inesperado ao processar linha %d (ignorada): '%s' - %s%n", numeroLinha, linha, e.getMessage());
                     e.printStackTrace();
                }
            }
            System.out.println("Tarefas carregadas com sucesso de " + nomeArquivo);
        } catch (FileNotFoundException e) {
             
             System.err.println("Erro crítico: Arquivo não encontrado após verificação inicial: " + e.getMessage());
        } catch (Exception e) { 
            System.err.println("Erro inesperado ao carregar o arquivo de tarefas: " + e.getMessage());
            e.printStackTrace();
        }
        return tarefasCarregadas;     }

     /**
      * Converte uma linha de string CSV em um objeto Tarefa.
      * @param linhaCsv A linha lida do arquivo CSV.
      * @return O objeto Tarefa criado a partir da linha CSV.
      * @throws IllegalArgumentException Se o formato da linha for inválido ou dados essenciais estiverem faltando.
      * @throws ArrayIndexOutOfBoundsException Se a linha não contiver o número esperado de campos.
      * @throws NullPointerException Se ocorrer um erro inesperado com os dados.
      */
     private Tarefa parseCsv(String linhaCsv) {
       
        // Ex: "id,STATUS,Descricao,12345," -> split(",", 5) resulta em 5 partes, a última sendo ""
        String[] partes = linhaCsv.split(SEPARADOR, 5);
        // Validação básica: precisa de pelo menos ID, Status, Descrição, DataCriação
        if (partes.length < 4) {
            throw new IllegalArgumentException(String.format("Formato CSV inválido - esperado pelo menos 4 partes, encontrado %d", partes.length));
        }

        // Valida e converte cada parte
        UUID id = UUID.fromString(partes[0].trim()); 
        Status status = Status.fromString(partes[1].trim()); // Status é a segunda parte
        String descricao = partes[2].trim(); // Descrição é a terceira parte
        Date dataCriacao = new Date(Long.parseLong(partes[3].trim())); // Data de criação é a quarta

        Date dataConclusao = null;
        if (partes.length == 5 && !partes[4].trim().isEmpty()) {
            try {
                dataConclusao = new Date(Long.parseLong(partes[4].trim()));
            } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Formato inválido para data de conclusão: " + partes[4], e);
            }
        }
        
        if (descricao.isEmpty()) {
            throw new IllegalArgumentException("Descrição da tarefa não pode ser vazia no arquivo CSV.");
        }

        
        Tarefa tarefa = new Tarefa(id, descricao, dataCriacao, dataConclusao, status);
        return tarefa;
    }
}