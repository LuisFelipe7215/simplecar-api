# Simple Car Api

![Java](https://img.shields.io/badge/Java-21-orange)
![Docker](https://img.shields.io/badge/Docker-28.5.2-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-green)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 📖 Sobre o Projeto

Api REST CRUD de carros. O projeto foi desenvolvido com a intenção de praticar os meus conhecimentos em Spring Boot.
Focando em boas práticas de desenvolvimento, documentação via swagger e testes unitários, sliced e de integração.

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** (Autenticação e Autorização)
- **Spring Data JPA** (Persistência de dados)
- **Map Struct** (Mapeamento de DTOs)
- **Lombok** (Redução de boilerplate)
- **Open Api / Swagger UI** (Documentação da API)
- **Docker** (Containerização)
- **Banco de dados:** [MySQL e H2 para testes]

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

A aplicação depende de um banco de dados MySQL rodando em um container no docker. Siga a ordem abaixo para evitar erros:

2. Duplique o arquivo `.envTemplate` e renomeie para `.env`.
    - **Linux / Mac:** `cp .envTemplate .env`
    - **Windows** Copie e cole renomeando.


3. Preencha as variáveis no arquivo `.env` recém-criado.
    > **Nota:** Recomenda-se preencher tanto as credenciais de `ROOT` (para administração) quanto criar um `USER`
específico para a aplicação.
   

Exemplo do `.env`:
```properties
# Configuração do MySQL (Docker)
ENV_ROOT_PASSWORD=root123

# Configuração do Spring e do MySQL
ENV_MYSQL_USER=teste
ENV_MYSQL_PASSWORD=teste123
```

4. Além disso, terá que preencher `ADMIN_USERNAME` e `ADMIN_PASSWORD` para definir o usuário e senha do admin para fazer
requisições `POST`, `PUT` e `DELETE`. Caso não seja preenchido, ele será definido automaticamente com os seguintes valores:
o username será `admin` e a senha `123456`.

Exemplo ainda no `.env`:
```properties
ADMIN_USERNAME=admin
ADMIN_PASSWORD=teste123
```

5. Execute o comando abaixo para subir o container do MYSQL configurado no `compose.yaml`:
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

6. Com o banco online, execute a aplicação:
```bash
./mvnw spring-boot:run
```

A API de carros estará disponível em `http://localhost:8080/v1/cars` e de usuários em `http://localhost:8080/v1/users`.


## 📄 Documentação da API (Swagger)

A documentação completa dos endpoints, schemas de objetos e exceções pode ser acessada através do Swagger UI.

1. Inicie a aplicação: `./mvnw spring-boot:run`
2. Acesse no navegador: `http://localhost:8080/swagger-ui.html`
> O arquivo JSON da especificação OpenAPI pode ser encontrado em `http://localhost:8080/v3/api-docs`

## 🧪 Executar os testes

Para executar os testes, rode o comando:

```bash
./mvnw clean verify
```

## 📝 Licença
Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

Feito com ☕ por Luis Felipe.
