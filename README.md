# Simple Car Api

![Java](https://img.shields.io/badge/Java-21-orange)
![Docker](https://img.shields.io/badge/Docker-28.5.2-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-green)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 📖 Sobre o Projeto

Api REST CRUD de carros. O projeto foi desenvolvido com a intenção de praticar os meus conhecimentos em Spring Boot.
Focando em boas práticas de desenvolvimento e testes unitários, sliced e de integração.

## 🚀 Principais Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** 
- **Spring Data JPA**
- **Map Struct**
- **Lombok**
- **Docker**
- **Banco de dados:** [MySQL e H2 para testes]
- **Flyway**

## ⚙️ Como Executar o Projeto

### Pré-requisitos
- Java 21.0.8 instalado.
- IDE de sua preferência.
- Maven 3.9.11 instalado (ou use o wrapper `./mvnw`).
- Docker e docker compose instalados.

### Passo a passo

1. **Clone o repositório**
```bash
git clone https://github.com/LuisFelipe7215/simplecar-api.git
cd simplecar-api
```
A aplicação depende de um banco de dados MySQL rodando em um container no docker. Para isso, será necessário configurar
as variáveis de ambiente:

* Duplique o arquivo `.envTemplate` e renomeie para `.env`. 
* Preencha as variáveis no arquivo `.env` recém-criado.
    > **Nota:** O ENV_ROOT_PASSWORD é a senha do root do banco de dados mysql e o ENV_MYSQL_USER
   > e ENV_MYSQL_PASSWORD são o usuário e senha que será utilizado pelo Spring para fazer a conexão com o banco de dados.
   

Exemplo do `.env`:
```properties
# Configuração do MySQL (Docker)
ENV_ROOT_PASSWORD=root123

# Configuração do Spring e do MySQL
ENV_MYSQL_USER=teste
ENV_MYSQL_PASSWORD=teste123
```
2. Execute o comando abaixo para subir o container do MYSQL configurado no `compose.yaml`:
- **Windows**:
```bash
docker-compose up
```
- **Linux / Mac**:
```bash
docker compose up
```
Caso não queira mais utilizar o container, primeiro execute `CTRL + C` para parar o container e execute o seguinte 
para remover o container:
- **Windows**:
```bash
docker-compose down
```
- **Linux / Mac**:
```bash
docker compose down
```

5. Com o banco de dados online, execute a aplicação:
```bash
./mvnw spring-boot:run
```

**Nota:** As migrações do Flyway rodarão automaticamente ao iniciar a aplicação. O usuário administrador padrão
já é criado durante essas migrações com as seguintes credenciais para fazer requisições POST, PUT e DELETE:
- **Username:** `admin`
- **Senha:** `123456`




## 🧪 Executar os testes

Para executar os testes, rode o comando:

```bash
./mvnw clean verify
```

## 🛠️ Documentação da API

### Gestão de Carros
- **Listar Carros**: `GET /v1/cars`
  - **Resposta**: `200 OK` com uma lista simplificada de carros.
- **Buscar Carro**: `GET /v1/cars/{id}`
  - **Resposta**: `200 OK` com todos os detalhes do carro, incluindo a lista de fotos associadas.
- **Criar Carro**: `POST /v1/cars`
  - **Requisição**: JSON contendo `type`, `brand`, `model`, `year` e `price`.
  - **Resposta**: `201 Created` com o `id` do carro criado.
- **Atualizar Carro**: `PUT /v1/cars`
  - **Requisição**: JSON contendo o `id` do carro e os campos a serem atualizados.
  - **Resposta**: `204 No Content`.
- **Deletar Carro**: `DELETE /v1/cars/{id}`
  - **Resposta**: `204 No Content`.

### Gestão de Fotos
- **Adicionar Foto**: `POST /v1/cars/{carId}/photos`
  - **Requisição**: `multipart/form-data` com o arquivo no campo `file`. O `carId` na URL define a qual carro a foto pertence.
  - **Resposta**: `201 Created` com o `id` da foto salva.
- **Deletar Foto**: `DELETE /v1/cars/photos/{id}`
  - **Resposta**: `204 No Content`.

## 📝 Licença
Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

Feito com ☕ por Luis Felipe.