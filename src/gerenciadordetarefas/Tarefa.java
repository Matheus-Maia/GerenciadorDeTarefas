package gerenciadordetarefas;

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