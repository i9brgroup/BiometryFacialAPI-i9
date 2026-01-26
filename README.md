# Facial-auth-i9: API de gerenciamento de autenticação segura no sistema de cadastro da Biometria Facial

## 1. Descrição Geral
O `facial-auth-i9` é uma API robusta desenvolvida para atuar como o núcleo de segurança e gerenciamento de dados do sistema de Biometria Facial da I9 BR GROUP. O sistema foca em garantir que o acesso aos dados dos colaboradores seja estritamente controlado através de uma arquitetura segura e escalável.

## 2. Funcionalidades Principais

### Gerenciamento de Colaboradores (Employees)
* **Busca Inteligente:** Permite a localização de funcionários por nomes ou IDs.
* **Integração com BiometryEngine:** A API é responsável por buscar os dados e enviar Payloads estruturados para o serviço `BiometryEngine` (processador Python). Este serviço externo é o encarregado de:
    * Carregar a foto do colaborador.
    * Gerar o Embedding (Template Facial).
    * Salvar as informações processadas no banco de dados.

### Segurança e Multi-Tenancy (Múltiplos Inquilinos)
A API implementa um modelo de isolamento de dados rigoroso baseado no `SiteID`.
* **Isolamento de Dados:** Cada Login está vinculado a um `SiteID` específico. A lógica programada garante que o usuário acesse e atualize apenas os dados vinculados ao seu próprio Site.
* **Exemplo Prático:**
    * Um usuário logado com `junior@i9brgroup.com` que possui o `SiteID: 600` terá visibilidade exclusiva para os colaboradores cadastrados sob o `SiteID: 600`.
    * Tentativas de acesso a dados de outros SiteIDs são bloqueadas automaticamente pela camada de persistência.

## 3. Arquitetura Técnica

### Autenticação e Autorização
* **JWT (JSON Web Token):** Todas as comunicações protegidas utilizam tokens JWT assinados.
* **Arquitetura Stateless:** A API não mantém estado de sessão no servidor. Cada requisição é independente e deve conter um token criptografado válido no header de autorização.
* **Criptografia:** Senhas são protegidas com algoritmos de hash (BCrypt) e os tokens garantem a integridade das informações de identidade.

### Stack Tecnológica
* **Linguagem:** Java 21
* **Framework:** Spring Boot 4.0.1
* **Banco de Dados:** Microsoft SQL Server
* **Integração Cloud:** AWS SDK para armazenamento de imagens no S3
* **Persistência:** Spring Data JPA com filtros dinâmicos de Hibernate (para Multi-Tenancy)

---
*Documentação atualizada conforme as definições de negócio e arquitetura do sistema.*