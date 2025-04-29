package gerenciadordetarefas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.Map;

class PersistenciaTarefasTest {

    @Test
    void testSalvarECarregarTarefas() throws Exception {
        PersistenciaTarefas persistencia = new PersistenciaTarefas();
        Path tempFile = java.nio.file.Files.createTempFile("test-tarefas", ".csv");
        
        // Criação de dados de teste
        GerenciadorTarefas gt = new GerenciadorTarefas();
        gt.adicionarTarefa("Tarefa Teste 1");
        gt.adicionarTarefa("Tarefa Teste 2");
        
        // Teste de salvamento
        persistencia.salvarTarefas(gt.getTodasTarefas(), tempFile.toString());
        
        // Teste de carregamento
        Map<Status, List<Tarefa>> carregadas = persistencia.carregarTarefas(tempFile.toString());
        
        assertEquals(2, carregadas.values().stream().mapToInt(List::size).sum());
        
        // Limpeza
        java.nio.file.Files.deleteIfExists(tempFile);
    }
}
