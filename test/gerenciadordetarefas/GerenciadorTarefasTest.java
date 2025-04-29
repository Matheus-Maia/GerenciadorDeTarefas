package gerenciadordetarefas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

class GerenciadorTarefasTest {
    
    private GerenciadorTarefas gerenciador;
    
    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorTarefas();
    }

    @Test
    void testAdicionarTarefa() {
        gerenciador.adicionarTarefa("Teste 1");
        List<Tarefa> tarefas = gerenciador.getTarefasPorStatus(Status.AFAZER);
        assertEquals(1, tarefas.size());
        assertEquals("Teste 1", tarefas.get(0).getDescricao());
    }

    @Test
    void testMoverTarefa() {
        gerenciador.adicionarTarefa("Teste Movimento");
        Tarefa tarefa = gerenciador.getTarefasPorStatus(Status.AFAZER).get(0);
        
        boolean resultado = gerenciador.moverTarefa(tarefa, Status.FAZENDO);
        assertTrue(resultado);
        assertEquals(0, gerenciador.getTarefasPorStatus(Status.AFAZER).size());
        assertEquals(1, gerenciador.getTarefasPorStatus(Status.FAZENDO).size());
    }
}
