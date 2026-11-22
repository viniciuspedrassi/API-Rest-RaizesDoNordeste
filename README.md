Raízes do Nordeste — API Back-End
1. Sobre o projeto
   A rede Raízes do Nordeste é uma franquia de restaurantes que vem crescendo em território nacional, ganhando destaque por sua culinária típica e saborosa. Entretanto, esse crescimento também trouxe desafios funcionais e operacionais, evidenciando a necessidade de melhorias estruturais e tecnológicas para otimizar o desempenho de suas operações.

Uma solução para esses desafios é a centralização das informações relacionadas às operações da rede Raízes do Nordeste, como produtos, pedidos, estoques e unidades. Dessa forma, é possível organizar e integrar esses dados, facilitando o gerenciamento das atividades e o controle das operações da franquia.

Como proposta de solução tecnológica, desenvolvi uma API REST utilizando Java e Spring Boot no back-end, com persistência de dados em um banco MySQL. A escolha dessas tecnologias se deve à sua ampla utilização no desenvolvimento de aplicações empresariais, buscando proporcionar uma solução robusta, eficiente e segura para as operações da franquia.
2. Tecnologias utilizadas
- Java 21 + Spring Boot: Framework Java utilizado para desenvolver a API REST.
- MySQL: Banco de dados responsável pela persistência das informações.
- Spring Data JPA / Hibernate: Mapeamento das entidades e acesso ao banco de dados.
- Spring Security e JWT: Autenticação e controle de acesso aos recursos da API.
- Maven: Gerenciamento de dependências e construção do projeto.
- Swagger / OpenAPI: Documentação dos endpoints da API.
- Postman: Ferramenta utilizada para realizar os testes das requisições HTTP.
3. Funcionalidades
- Usuários e segurança: autenticação JWT e permissões para administrador, gerente, atendente e cozinha.
- Produtos e unidades: cadastro e consulta de produtos e gerenciamento das unidades da franquia.
- Estoque: controle das quantidades por unidade, registro de entradas e saídas e bloqueio de operações sem saldo suficiente.
- Pedidos: criação, consulta, filtragem por canal de atendimento e atualização de status.
- Pagamentos: simulação de aprovação e recusa, com atualização do estoque após a aprovação.
- Fidelização: consentimento do cliente, acúmulo e resgate de pontos.
- Documentação e erros: Swagger/OpenAPI e respostas HTTP padronizadas.
4. Configuração do ambiente
   Pré-requisitos
   Para a execução deste projeto, será necessária a instalação prévia do Java 21, MySQL e Maven, ou a utilização do Maven Wrapper que acompanha o projeto.
   Configuração do banco de dados
   Será necessário criar um banco de dados MySQL e configurar a conexão no arquivo localizado no seguinte caminho:

src/main/resources/application.properties

Por questões de segurança, sugiro que as credenciais do banco e a chave JWT sejam configuradas por meio de variáveis de ambiente.

As variáveis utilizadas no projeto são:
- DB_URL: endereço de conexão com o banco.
- DB_USERNAME: usuário do MySQL.
- DB_PASSWORD: senha do MySQL.
- JWT_SECRET: chave utilizada para gerar e validar os tokens JWT.
  Criação do administrador inicial
  A aplicação possui uma classe responsável pela criação do primeiro usuário com perfil ADMINISTRADOR, chamada AdministradorInicializador.

Para utilizá-la:
1. Configure as variáveis de ambiente ADMIN_EMAIL e ADMIN_PASSWORD.
2. Ative temporariamente o perfil bootstrap-admin.
3. Inicie a aplicação.
4. Após a criação do administrador, desative o perfil e reinicie a aplicação normalmente.
   Essa classe verifica se já existe um administrador cadastrado, evitando a criação de outro administrador inicial.
   Preparação dos dados de teste
   O arquivo database/dadosTeste.sql contém os registros iniciais da unidade Aracaju e dos produtos utilizados nos testes.

Observação: executar esse arquivo somente em um banco de dados novo e vazio, após a criação das tabelas.

Os demais registros do banco devem ser preparados pela própria API:
- Cadastrar um atendente utilizando a autenticação do administrador.
- Cadastrar um cliente de demonstração.
- Registrar uma entrada de estoque do produto 1 na unidade 1, garantindo uma quantidade suficiente para realizar os testes.
  É importante lembrar que as tabelas utilizam AUTO_INCREMENT, gerando automaticamente os IDs quando eles não são informados. Por isso, os registros precisam ser preparados corretamente para corresponder aos IDs utilizados no Postman.

As senhas devem ser processadas pela aplicação utilizando BCrypt.
Execução da aplicação
Na pasta que contém o arquivo pom.xml, executar:

Linux/macOS:
./mvnw spring-boot:run
Windows:
mvnw.cmd spring-boot:run
Após iniciar a aplicação, a API estará disponível no endereço:

http://localhost:8080
5. Autenticação e segurança
   Utilizei JWT para autenticação dos usuários nas requisições e BCrypt para o armazenamento seguro das senhas por meio de hash.

Também diferenciei os perfis de acesso entre ADMINISTRADOR, GERENTE, ATENDENTE e COZINHA, cada um com permissões e restrições específicas.

Para gerenciar a autenticação e o controle dessas permissões, utilizei o Spring Security.
6. Documentação e testes
   Swagger
   Através do Swagger, foi possível visualizar os endpoints da API, seus métodos HTTP, parâmetros e respostas, além de realizar requisições para verificar seu funcionamento.

Endereço local: http://localhost:8080/swagger-ui.html
Postman
Utilizei o Postman para organizar e executar diversos cenários de teste, dos quais documentei 12, incluindo operações bem-sucedidas e situações de erro, verificando também a padronização das mensagens de resposta.

Além disso, configurei variáveis de ambiente e scripts para facilitar a reprodução dos testes.
7. Considerações finais
   Este trabalho me proporcionou a oportunidade de relembrar e estudar diversos conteúdos abordados ao longo da faculdade, além de revisar temas que eu ainda não compreendia totalmente e que agora consegui assimilar melhor.

Também me incentivou a pesquisar e conhecer ferramentas com as quais ainda não havia trabalhado, como Spring Security, JWT e Swagger, ampliando meus conhecimentos e minha experiência no desenvolvimento back-end.