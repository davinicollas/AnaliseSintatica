package Analiselexer;

/**
 *
 * @author davin, Pedro 
 */
public class Compiladores {

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      
    /**
     Olá gustavo gostaria de pedir para você abrir o arquivo OBS.txt.
     */ 
     Lexer lexer = new Lexer("C:\\Users\\davin\\Desktop\\compiladores\\correção\\codigos_javazim_teste\\teste3.jvz");
      Parser parser = new Parser(lexer);

      // primeiro procedimento do Javinha: Programa()
      parser.Programa();

      parser.fechaArquivos();
      
      //Imprimir a tabela de simbolos
      lexer.printTS();

      System.out.println("Compilação de Programa Realizada!");
   }
   
} 