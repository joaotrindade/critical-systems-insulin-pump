O código fonte de cada Variante e do Módulo Principal estão dentro da pasta homónima respectiva.

O código já compilado e pronto a correr pode ser encontrado (com instruções) na pasta "Compilado"

Tanto as variantes como o módulo central foram compilados em Windows.

Módulo Principal e Variante 1:
1 - Abrir Command Prompt na pasta do projeto.
2 - Introduzir >javac *.java
3 - Introduzir >jar cfe NomeDoJar.jar Main Main.class
4 - Introduzir >java -jar NomdeDoJar.jar
Tanto o Módulo Principal como a Variante1 têm como ponto de entrada o ficheiro Main.java, logo as instruções servem para os dois casos.


Variante 2:
Foi utilizada a consola de compilação fornecida com o Microsoft Visual Studio 2013 (C++12)
1 - Abrir Developer Command Prompt for Visual Studio (Menu Inicial -> Visual Studio -> Visual Studio Tools -> "Developer Command Prompt for VS2013"
2 - Navegar para a pasta com o ficheiro Main.cpp
3 - Introduzir> cl Main.cpp
4 - É criado o executavel Main.exe

Variante 3:
1 - Abrir Command Prompt na pasta do projeto.
2 - Introduzir >gnatmake var
3 - Correr o executável gerado "Var.exe"

Os output's gerados têm anexados no final do nome do ficheiro uma timestamp referente ao momento em que foi criado o ficheiro (iniciada a execução).