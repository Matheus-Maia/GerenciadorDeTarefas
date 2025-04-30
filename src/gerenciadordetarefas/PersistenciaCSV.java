package gerenciadordetarefas;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * Implementação de persistência de tarefas em formato CSV.
 * Formato do arquivo: id,status,descricao,dataCriacaoMillis,dataConclusaoMillis
 */
public class PersistenciaCSV implements PersistenciaTarefas {
    
    private static final String SEPARADOR = ",";

    /**
     * {@inheritDoc}
     * @param tarefas Mapa de tarefas a serem salvas
     * @param arquivo Caminho do arquivo CSV de destino
     */
    @Override
    public void salvar(Map<Status, List<Tarefa>> tarefas, String arquivo) {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
            
            for (List<Tarefa> lista : tarefas.values()) {
                for (Tarefa tarefa : lista) {
                    writer.println(formatarParaCSV(tarefa));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar CSV: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * @param arquivo Caminho do arquivo CSV a ser carregado
     */
    @Override
    public Map<Status, List<Tarefa>> carregar(String arquivo) {
        Map<Status, List<Tarefa>> tarefas = new EnumMap<>(Status.class);
        
        // Inicializa listas vazias para todos os status
        for (Status s : Status.values()) {
            tarefas.put(s, new ArrayList<>());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            
            String linha;
            while ((linha = reader.readLine()) != null) {
                Tarefa tarefa = parseCSV(linha);
                tarefas.get(tarefa.getStatus()).add(tarefa);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar CSV: " + e.getMessage());
        }
        return tarefas;
    }

    /**
     * Formata uma tarefa para o formato CSV
     * @param tarefa Tarefa a ser formatada
     * @return String no formato CSV
     */
    private String formatarParaCSV(Tarefa tarefa) {
        return String.join(SEPARADOR,
                tarefa.getId().toString(),
                tarefa.getStatus().name(),
                tarefa.getDescricao().replace(SEPARADOR, " "),
                String.valueOf(tarefa.getDataCriacao().getTime()),
                tarefa.getDataConclusao() != null 
                    ? String.valueOf(tarefa.getDataConclusao().getTime()) 
                    : ""
        );
    }

    /**
     * Converte uma linha CSV em objeto Tarefa
     * @param linha Linha do arquivo CSV
     * @return Objeto Tarefa criado
     * @throws IllegalArgumentException Se a linha estiver em formato inválido
     */
    private Tarefa parseCSV(String linha) {
        String[] partes = linha.split(SEPARADOR, 5);

        // Validação básica da estrutura
        if (partes.length < 4) {
            throw new IllegalArgumentException("Linha CSV inválida: " + linha);
        }

        try {
            // Parse dos componentes
            UUID id = UUID.fromString(partes[0].trim());
            Status status = Status.fromString(partes[1].trim());
            String descricao = partes[2].trim();

            // Conversão de datas
            Date dataCriacao = new Date(Long.parseLong(partes[3].trim()));
            Date dataConclusao = null;

            // Campo opcional (parte 4)
            if (partes.length >= 5 && !partes[4].isEmpty()) {
                dataConclusao = new Date(Long.parseLong(partes[4].trim()));
            }

            return new Tarefa(
                id,
                descricao,
                dataCriacao,
                dataConclusao,
                status
            );

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro ao processar linha CSV: " + linha, e);
        }
    }
}