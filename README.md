# 🚀 Gerenciador de Tarefas CLI

[![Java](https://img.shields.io/badge/Java-24%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Matheus-Maia/GerenciadorDeTarefas/pulls)

Um gerenciador de tarefas em linha de comando (CLI) com persistência em CSV, desenvolvido em Java seguindo boas práticas de OOP.

![Demo GIF](link-para-gif-demo.gif) <!-- Adicione um GIF de demonstração -->

## 📌 Funcionalidades

- ✅ **CRUD de Tarefas**: Adicione, liste, mova e remova tarefas
- 📂 **Persistência Automática**: Salva tarefas em arquivo CSV
- 📊 **3 Status**: `A Fazer`, `Fazendo`, `Pronto`
- 📅 **Datas Automáticas**: Registro de criação e conclusão
- 🖥️ **Interface Limpa**: Menu interativo com limpeza de tela

## ⚙️ Tecnologias

- **Java 24**
- Enumerações (`Status`)
- Generics e Collections (`EnumMap`, `Collections.unmodifiableList`)
- Tratamento de exceções customizado
- Persistência em CSV sem bibliotecas externas

## 🚀 Começando

### Pré-requisitos
- Java JDK 11+
- Git (opcional)

### Instalação

### Clone o repositório
```bash
**git clone ...**
```
### Entre no diretório
```bash
cd GerenciadorDeTarefas
```
### Compile (Para Testar)
```bash
javac -d bin src/gerenciadordetarefas/*.java
```
### Execute
```bash
java -cp bin gerenciadordetarefas.Main
```
## 🤝 Contribuições

Se você deseja contribuir com este projeto, siga os passos abaixo:

1.  **Faça um Fork do projeto** para sua conta do GitHub.

    No canto superior direito da página do repositório, clique no botão "Fork".

2.  **Crie sua Branch** para desenvolver sua funcionalidade ou correção de bug.

    Abra o seu terminal (ou Git Bash) e execute o seguinte comando, substituindo `feature/nova-feature` por um nome descritivo para sua branch:

    ```bash
    git checkout -b feature/nova-feature
    ```

3.  **Commit suas mudanças** com uma mensagem clara e concisa explicando o que você fez.

    ```bash
    git commit -m 'Adiciona nova feature'
    ```

    Certifique-se de que suas mudanças sigam as convenções do projeto e que você não inclua arquivos desnecessários.

4.  **Push para a sua Branch** no seu repositório forkado.

    ```bash
    git push origin feature/nova-feature
    ```

5.  **Abra um Pull Request** para o repositório principal.

    * Vá para a página do seu repositório forkado no GitHub.
    * Você verá um botão "Compare & pull request". Clique nele.
    * Verifique se a branch base é `main` e a sua branch é `feature/nova-feature` (ou o nome da sua branch).
    * Adicione um título descritivo e uma explicação detalhada das suas mudanças no Pull Request.
    * Clique no botão "Create pull request".

Agradecemos suas contribuições! Após a revisão e aprovação, suas mudanças serão mergeadas no projeto principal.
