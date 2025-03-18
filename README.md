![Static Badge](https://img.shields.io/badge/status-em_andamento-purple?style=flat&color=%2302006b) ![Static Badge](https://img.shields.io/badge/UI-Compose-purple?style=flat&color=%235a0073)

### Sobre o projeto

O Intercambista é um app para cotação de moedas. Nele, o usuário consegue acompanhar diariamente a cotação das meodas de sua escolha. 

Proposta do projeto : 
- Implementação de app utilizando 2 APIs distintas para complementação de dados sem sobrecarga em nenhuma das duas;
- Estudo do jetpack Compose em um projeto real, bem como a análise dos prós e contras em relação ao XML. (Paradigma declarativo x imperativo)


Desafios : 
- Junção dos dados das APIs para criação de um objeto ideal para o projeto;
- Otimização das requisições a fim de evitar chamadas desnecessárias ou limitantes por parte da API;
- A API de cotação, apresenta uma limitação onde mesmo disponbilizando as moedas disponíveis, nem todas tem cotação, sendo necessário filtrar uma a uma para que esta limitação seja perceptível ao usuário.


### Recursos

- Seleção de moeda base;
- Bandeira dos países para fácil reconhecimento de qual país a moeda pertence;
- Atualização diária da cotação;
- Cálculo facilitado para conversão da moeda base para as moedas favoritadas.


### Tecnologias utilizadas

- Room
- Jetpack Compose
- Arquitetura MVVM
- Injeção de dependência com Dagger-Hilt
- Armazenamento de informações utilizando dataStore ao invés de SharedPreferences
