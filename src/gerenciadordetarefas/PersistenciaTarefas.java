package gerenciadordetarefas;

import java.util.Map;
import java.util.List;

/**
 * Interface para operações de persistência de tarefas em diferentes formatos.
 */
public interface PersistenciaTarefas {
    
    /**
     * Salva as tarefas em um arquivo no formato específico da implementação.
     * 
     * @param tarefas Mapa contendo as tarefas organizadas por status
     * @param arquivo Caminho do arquivo onde as tarefas serão salvas
     */
    void salvar(Map<Status, List<Tarefa>> tarefas, String arquivo);
    
    /**
     * Carrega as tarefas de um arquivo no formato específico da implementação.
     * 
     * @param arquivo Caminho do arquivo contendo as tarefas salvas
     * @return Mapa contendo as tarefas organizadas por status
     */
    Map<Status, List<Tarefa>> carregar(String arquivo);
}