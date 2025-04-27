# 🚀 Gerenciador de Tarefas CLI

[![Java](https://img.shields.io/badge/Java-24%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/seu-usuario/gerenciador-tarefas/pulls)

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
```bash
# Clone o repositório
git clone https://github.com/seu-usuario/gerenciador-tarefas.git

# Entre no diretório
cd gerenciador-tarefas

# Compile (Para Testar)
javac -d bin src/gerenciadordetarefas/*.java

# Execute
java -cp bin gerenciadordetarefas.Main
