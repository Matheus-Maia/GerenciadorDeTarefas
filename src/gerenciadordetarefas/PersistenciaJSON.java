package gerenciadordetarefas;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

/**
 * Implementação de persistência de tarefas em formato JSON.
 * Formato do arquivo: JSON com estrutura de array de objetos de tarefas
 */
public class PersistenciaJSON implements PersistenciaTarefas {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * {@inheritDoc}
     * @param tarefas Mapa de tarefas a serem salvas
     * @param arquivo Caminho do arquivo JSON de destino
     */
    @Override
    public void salvar(Map<Status, List<Tarefa>> tarefas, String arquivo) {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
            
            writer.println("{");
            writer.println("\"tarefas\": [");
            
            boolean firstTarefa = true;
            for (List<Tarefa> lista : tarefas.values()) {
                for (Tarefa tarefa : lista) {
                    if (!firstTarefa) {
                        writer.println(",");
                    }
                    writer.print(formatarParaJSON(tarefa));
                    firstTarefa = false;
                }
            }
            
            writer.println("\n]");
            writer.println("}");
        } catch (IOException e) {
            System.err.println("Erro ao salvar JSON: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException Esta implementação não suporta carregamento de JSON
     */
    @Override
    public Map<Status, List<Tarefa>> carregar(String arquivo) {
        throw new UnsupportedOperationException("Carregamento de JSON não implementado.");
    }

    /**
     * Formata uma tarefa para o formato JSON
     * @param tarefa Tarefa a ser formatada
     * @return String com representação JSON da tarefa
     */
    private String formatarParaJSON(Tarefa tarefa) {
        return String.format(
            "  {\n" +
            "    \"id\": \"%s\",\n" +
            "    \"descricao\": \"%s\",\n" +
            "    \"status\": \"%s\",\n" +
            "    \"dataCriacao\": \"%s\",\n" +
            "    \"dataConclusao\": %s\n" +
            "  }",
            tarefa.getId(),
            tarefa.getDescricao().replace("\"", "\\\""),
            tarefa.getStatus(),
            DATE_FORMAT.format(tarefa.getDataCriacao()),
            tarefa.getDataConclusao() != null 
                ? "\"" + DATE_FORMAT.format(tarefa.getDataConclusao()) + "\"" 
                : "null"
        );
    }
}