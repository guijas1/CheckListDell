# ✅ Dell Notebook Checklist - ONS

Sistema web para registro e acompanhamento de checklists de notebooks Dell antes da abertura de chamados técnicos. Desenvolvido para uso interno na sala da ONS.

---

## ✨ Funcionalidades

- Cadastro de checklist com:
    - Modelo, Patrimônio e Service Tag
    - Verificações por checkbox
    - Campo de observações
    - Upload de foto (ex: tela quebrada, teclado com falha)
- Listagem de todos os checklists preenchidos
- Visualização detalhada de cada checklist
- Exportação de checklists para PDF (com imagem inclusa)
- Interface moderna e responsiva com Bootstrap 5

---

## ⚙️ Tecnologias utilizadas

- **Java 17**
- **Spring Boot 3**
    - Spring Web
    - Spring Data JPA
    - Thymeleaf
- **MySQL** (local, na rede interna da ONS)
- **Bootstrap 5**
- **OpenPDF** (geração de PDFs)

---

## 🗂️ Estrutura de pacotes

```
br.guijas1.checklistDell
├── controller
├── service
├── repository
├── entity
└── config (opcional)
```

---

## 🖼️ Telas do sistema

- `/checklist` – Cadastro de novo checklist
- `/checklists` – Lista de registros existentes
- `/checklists/{id}` – Detalhamento do registro
- `/checklists/{id}/exportar` – Exportação em PDF

---

## 🧪 Como executar localmente

### Pré-requisitos:
- Java 17
- Maven
- MySQL Server

### Configuração do banco

Crie um banco de dados:

```sql
CREATE DATABASE checklist_ons;
```

E configure no `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/checklist_ons
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### Rodar o projeto

```bash
mvn spring-boot:run
```

Acesse: [http://localhost:8080/checklist](http://localhost:8080/checklist)

---

## 📁 Uploads

As fotos anexadas são armazenadas na pasta `uploads/` localmente. Você pode configurar isso em:

```properties
file.upload-dir=uploads/
```

---

## 📌 Autor

**Guijas Rodrigues**  
Desenvolvedor e Analista na Quality - ONS  
📧 guijas1@ons.org.br

---

## 🛡️ Licença

Uso interno na ONS. Este projeto não possui licença pública.
