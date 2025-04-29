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

    //feito por Stephanie
    public void executarTarefasPorStatus(Status status) {
    List<Tarefa> tarefas = tarefasPorStatus.get(status);
    for (Executavel tarefa : tarefas) {
        tarefa.executar(); // Aqui acontece o polimorfismo
    }
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
}