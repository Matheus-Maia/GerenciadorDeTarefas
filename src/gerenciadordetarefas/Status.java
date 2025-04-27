package gerenciadordetarefas;

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
}