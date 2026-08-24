# 🤖 AI Dev Assistant — GitHub Code Review (Spring AI + RAG + MCP)

> **⏸️ СТАТУС ПРОЕКТА: ЗАМОРОЖЕН (ON HOLD)**  
> Разработка временно приостановлена. В данном файле зафиксировано текущее состояние кодовой базы, архитектура и пошаговый план запуска для быстрого возвращения к проекту.

---

## 📌 Описание проекта

**AI Dev Assistant** — это бэкенд-сервис на Java (Spring Boot + Spring AI), который принимает ссылку на любой публичный GitHub-репозиторий и проводит автоматический **AI Code Review** с использованием:
* **RAG (Retrieval-Augmented Generation)** над кодовой базой с хранением эмбеддингов в **PostgreSQL (pgvector)**.
* **MCP (Model Context Protocol / Tool Calling)** для динамического чтения файлов и структуры репозитория языковой моделью на лету.
* **Локальной LLM через Ollama** (Qwen 2.5 Coder / DeepSeek Coder) и модели эмбеддингов `nomic-embed-text`.

---

## 🛠 Технологический стек

* **Язык**: Java 21 LTS
* **Фреймворк**: Spring Boot 3.4.2, Spring Data JPA
* **AI Фреймворк**: Spring AI 1.0.0-M6
* **LLM & Embeddings**: Ollama (`qwen2.5-coder:7b`, `nomic-embed-text`)
* **Векторная БД**: PostgreSQL 16 + расширение `pgvector`
* **Интеграция с GitHub**: Kohsuke GitHub API
* **Документация API**: SpringDoc OpenAPI 2.8.5 (Swagger UI)
* **Сборка**: Maven Wrapper (`mvnw`), Docker Compose

---

## 🏗 Архитектура и реализованные компоненты

```
[ HTTP POST /api/v1/reviews ] 
           │ (URL репозитория GitHub)
           ▼
┌───────────────────────────────────────────────────────────┐
│                     Spring AI Backend                     │
│                                                           │
│  1. Ingestion / RAG Pipeline:                             │
│     GitHub API ──► TokenTextSplitter ──► Ollama Embedding │
│                                                 │         │
│                                                 ▼         │
│                                            [ pgvector ]   │
│                                                           │
│  2. AI Code Review Agent:                                 │
│     ├── RAG Retrieval (Архитектура, Security, Bugs)       │
│     └── MCP Tools (динамическое дочитывание файлов)       │
│                                                           │
│  3. Генерация Structured CodeReviewReport (JSON / DTO)    │
│  4. Сохранение истории в таблицу code_reviews             │
└───────────────────────────────────────────────────────────┘
```

### Реализованные классы:
* **`RepositoryIndexerService`**: сканирует дерево файлов репозитория, фильтрует бинарники и папки сборки, разбивает код на чанки через `TokenTextSplitter` и сохраняет эмбеддинги в `VectorStore` (pgvector).
* **`GitHubMcpTools`**: функции с аннотацией `@Tool` (`readFileContent`, `listDirectory`, `getRepositoryInfo`), позволяющие модели автономно исследовать репозиторий.
* **`CodeReviewService`**: осуществляет поиск по векторам, формирует промпт с RAG-контекстом, вызывает модель через `ChatClient` и маппит результат в структурированный DTO `CodeReviewReport`.
* **`CodeReviewController`** & **`GitHubRepositoryController`**: REST эндпоинты со Swagger UI аннотациями.
* **`GitHubConfig`** & **`ExtractService`**: подключение к GitHub API и парсинг различных форматов URL репозиториев.

---

## 🚀 Инструкция по разморозке и запуску проекта

Когда вернетесь к проекту, выполните следующие шаги:

### 1. Настройка JDK в IntelliJ IDEA
> [!IMPORTANT]
> Проект требует **Java 21 LTS**. Не используйте экспериментальные версии JDK (например, JDK 25), иначе Lombok упадет с `ExceptionInInitializerError`.

* **File → Project Structure → Project**: выберите **SDK: Java 21 (Temurin 21)**.
* **File → Settings → Build, Execution, Deployment → Compiler → Java Compiler**: выберите **Project bytecode version: 21**.

### 2. Запуск PostgreSQL с pgvector
Запустите базу данных через Docker Compose:
```bash
docker compose up -d postgres
```
*База поднимется на порту `5432` с БД `ai_assistant_db`.*

### 3. Запуск Ollama
Убедитесь, что в Ollama (локально или на удаленной машине) скачаны модели:
```bash
# Модель для эмбеддингов RAG (обязательно):
ollama pull nomic-embed-text

# Модель для генерации кода и ревью:
ollama pull qwen2.5-coder:7b
```

Если Ollama запущена на другой машине в локальной сети, укажите ее адрес в переменной `OLLAMA_BASE_URL` или в `application.properties`:
```properties
spring.ai.ollama.base-url=http://<IP_МАШИНЫ_С_OLLAMA>:11434
```

### 4. Запуск Spring Boot приложения
```bash
./mvnw clean spring-boot:run
```

### 5. Использование через Swagger UI
Откройте в браузере:
👉 **http://localhost:8080/swagger-ui.html**

* **`POST /api/v1/reviews`**:
  ```json
  {
    "url": "https://github.com/spring-projects/spring-petclinic"
  }
  ```
* **`GET /api/v1/reviews/history?owner=spring-projects&repo=spring-petclinic`** — получение истории проведенных ревью.

---

## 📋 План доработок (TODO при возобновлении):
- [ ] Добавить асинхронную обработку (очередь / `@Async` / SSE / WebSocket) для анализа очень больших репозиториев.
- [ ] Реализовать отправку ревью в виде комментариев прямо в Pull Request GitHub.
- [ ] Добавить экспорт отчета ревью в Markdown / PDF.
- [ ] Настроить автоматическое удаление старых векторов из `pgvector` при переиндексации репозитория.
