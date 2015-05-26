Para correr, devem ser corridas todas as variantes e, depois, o programa principal. O programa principal só irá começar a enviar as mensagens a pedir cálculos após todas as variantes ficarem conectadas, no entanto, se a meio da execução uma variante se desconectar, o programa continuará a correr com as outras duas variantes.

Correr as variantes:
>java -jar SCRI_Variante1.jar (ou correr o ficheiro run_Variante1.bat)
>SCRI_Variante2 (ou correr o ficheiro run_Variante2.bat)
>SCRI_Variante3 

Correr Módulo Central:
>java -jar SCRI_ModuloCentral.jar

Opcionalmente podem ser passados 2 parâmetros à chamada acima:
>java -jar SCRI_ModuloCentral.jar [ficheiro input] [nome ficheiro output]
Também é possível chamar passando só o nome do ficheiro de input
>java -jar SCRI_ModuloCentral.jar [ficheiro input]
Caso nao seja passado qualquer valor, o programa corre o ficheiro "input.exe" da pasta. Para tal, poderá simplesmente executar o ficheiro run_ModuloCentral.bat

Para correr todo o sistema com o ficheiro input "input.txt" pode correr o ficheiro "run_Main.bat"