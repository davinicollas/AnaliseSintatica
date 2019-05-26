package Analiselexer;

import static Analiselexer.Lexer.ContErroSintatico;

/**
 *
 * @author Davin,Pedro
 *
 * [TODO]: tratar retorno 'null' do Lexer que esta sem Modo Panico
 *
 *
 * Modo Pânico do Parser: para tomar a decisao de escolher uma das regras
 * (quando mais de uma disponivel), temos que olhar para o FIRST(). Essa
 * informacao eh dada pela TP. Caso nao existe a regra na TP que corresponda ao
 * token da entrada, informamos uma mensagem de erro e inicia-se o Modo Panico:
 * [1] calculamos o FOLLOW do NAO-TERMINAL (a esquerda) da regra atual - esta no
 * topo da pilha; [2] se o token da entrada esta neste FOLLOW, desempilha-se o
 * nao-terminal atual - metodo synch(); [3] caso contrario, avancamos a entrada
 * para nova comparacao e mantemos o nao-terminal no topo da pilha (recursiva) -
 * metodo skip().
 *
 * O Modo Panico encerra-se, 'automagicamente', quando um token esperado (FIRST)
 * ou (FOLLOW) aparece.
 *
 *
 */
public class Parser {

    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        token = lexer.proxToken(); // Leitura inicial obrigatoria do primeiro simbolo
        System.out.println("[DEBUG] token: " + token.toString());
    }

    // Fecha os arquivos de entrada e de tokens
    public void fechaArquivos() {
        lexer.fechaArquivo();

    }

    public void erroSintatico(String mensagem) {

        System.out.print("[Erro Sintatico] na linha " + token.getLinha() + " e coluna " + token.getColuna() + ": ");
        System.out.println(mensagem + "\n");
        ContErroSintatico++;
    }

    public void advance() {
        token = lexer.proxToken();
        System.out.println("[DEBUG] token: " + token.toString());
    }

    public void skip(String mensagem) {
        erroSintatico(mensagem);
        advance();
        ContErroSintatico++;

    }

    // verifica token esperado t
    public boolean eat(Tag t) {
        if (token.getClasse() == t) {
            advance();
            return true;
        } else {
            return false;
        }

    }

    /* LEMBRETE:
   // Todas as decisoes do Parser, sao guiadas
   // pela Tabela Preditiva.
   //
     */
    //First--> Follow
    // Programa → Classe $ 
    //first-->public
    //follow-->$
    public void Programa() {
        if (token.getClasse() != Tag.KW_public) // TP[Programa][public]
        {
            skip("Esperado \"public\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        Classe();
        if (!eat(Tag.EOF)) // TP[Programa][public]
        {
            erroSintatico("Esperado \"EOF\", encontrado " + "\"" + token.getLexema() + "\"");

        }

    }

    // Classe → "public" "class" ID "{" ListaMetodo Main "}"
    //first--> public 
    //follow--> $
    public void Classe() {
        //System.out.println("[DEBUG] Classe()");

        /* OBS.: vimos que para o primeiro nao-terminal, eh melhor chamar o metodo skip()
      * para nao prejudicar a leitura no restante do codigo.
      * Se percebermos na TP, 'Programa' e 'Classe' possuem os mesmos
      * FIRST e FOLLOW. Entao a regra para se analisar a sincronizacao no 
      * primeiro instante em que entra nesses metodos eh a mesma.
         */
        if (!eat(Tag.KW_public)) {
            skip("Esperado \"public\", encontrado " + "\"" + token.getLexema() + "\"");
        }

        if (!eat(Tag.KW_class)) { // espera "class"

            /* ATENCAO: no caso 'terminal esperado' vs 'terminal na entrada', de acordo como vimos em sala:
			// o terminal esperado não casou com o terminal da entrada,
			// dai vamos simular o 'desempilha terminal',
			// isto eh, continue a varredura, mantendo a entrada.
             */
            erroSintatico("Esperado \"class\", encontrado " + "\"" + token.getLexema() + "\"");

        }

        if (!eat(Tag.ID)) { // espera "ID"
            erroSintatico("Esperado um \"ID\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.SMB_ABCH)) {
            erroSintatico("Esperado \"{\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        ListaMetodo();
        Main();
        if (!eat(Tag.SMB_FCCH)) {
            erroSintatico("Esperado \"}\", encontrado " + "\"" + token.getLexema() + "\"");
        }
    }

    //DeclaracaoVar → Tipo ID ";"
    // first--> int, boolean, String, float, void
    //follow-->boolean, int, string, float, void, if, while,print, println, ID, return, }
    public void DeclaraVar() {

        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float || token.getClasse() == Tag.KW_void) {
            Tipo();
            if (!eat(Tag.ID)) {
                erroSintatico("Esperado \"ID\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \";\", encontrado " + "\"" + token.getLexema() + "\"");
            }
        } else {
            //follow-->boolean, int, string, float, void, if, while,print, println, ID, return, }
            if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int
                    || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float
                    || token.getClasse() == Tag.KW_void || token.getClasse() == Tag.KW_if
                    || token.getClasse() == Tag.KW_while || token.getClasse() == Tag.KW_print
                    || token.getClasse() == Tag.KW_println || token.getClasse() == Tag.ID
                    || token.getClasse() == Tag.KW_return || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"int, boolean, String, float, void \", encontrado " + "\"" + token.getLexema() + "\"");
                return;

            } else {
                skip("Esperado \"int, boolean, String, float, void\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    DeclaraVar();
                }
            }
        }
    }

    //ListaMetodo → ListaMetodo’ 
    //first-->ε, boolean, int, string, float, void
    //follow-->public
    public void ListaMetodo() {

        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float || token.getClasse() == Tag.KW_void) {
            ListaMetodoLinha();
        } //follow-->public
        else if (token.getClasse() == Tag.KW_public) {
            return;
        } else {
            skip("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ListaMetodo();
            }
        }
    }

    //ListaMetodo’ → Metodo ListaMetodo’ 5 | ε 6
    //first-->ε, boolean, int, string, float, void
    //follow--> public
    public void ListaMetodoLinha() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float || token.getClasse() == Tag.KW_void) {
            Metodo();
            ListaMetodoLinha();
        }//follow--> public
        else if (token.getClasse() == Tag.KW_public) {
            return;
        } else {
            skip("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ListaMetodoLinha();
            }
        }
    }

    //Metodo → Tipo ID "(" RegexListaParam ")" "{" RegexDeclaraVar ListaCmd Retorno "}" 
    //fist-->boolean, int, string, float, void
    //follow--> if, while, print, println, ID, public
    public void Metodo() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float || token.getClasse() == Tag.KW_void) {
            Tipo();
            if (!eat(Tag.ID)) {
                erroSintatico("Esperado um \"ID\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            if (!eat(Tag.SMB_OP)) {
                erroSintatico("Esperado um \"(\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            RegexListaParam();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado um \")\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            if (!eat(Tag.SMB_ABCH)) {
                erroSintatico("Esperado \"{\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            RegexDeclaraVar();
            ListaCmd();
            Retorno();
            if (!eat(Tag.SMB_FCCH)) {
                erroSintatico("Esperado \"}\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            //follow--> if, while, print, println, ID, public
        } else {
            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_public) {
                erroSintatico("Esperado \"boolean, int, string, float, void\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"boolean, int, string, float, void\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Metodo();
                }

            }
        }
    }

    //RegexListaParam → ListaParam 8 | ε 9
    //fist-->ε, boolean, int, string, float, void
    //follow--> )
    public void RegexListaParam() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int
                || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float
                || token.getClasse() == Tag.KW_void) {
            ListaParam();
            //follow--> )
        } else if (token.getClasse() == Tag.SMB_CP) {
            return;

        } else {
            skip("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                RegexListaParam();
            }
        }
    }

    //RegexDeclaraVar → DeclaracaoVar RegexDeclaraVar 10 | ε 11
    //first-->ε, boolean, int, string, float, void
    //follow-->if, while, print, println, ID, return, }
    public void RegexDeclaraVar() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int
                || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float
                || token.getClasse() == Tag.KW_void) {
            DeclaraVar();
            RegexDeclaraVar();
            //FOLLOW--> if, while, print, println, ID, return, }
        } else if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while || token.getClasse() == Tag.KW_print
                || token.getClasse() == Tag.KW_println || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                || token.getClasse() == Tag.SMB_FCCH) {
            return;
        } else {
            skip("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                RegexDeclaraVar();
            }
        }
    }
//ListaParam → Param ListaParam’ 12
//first-->boolean, int, string, float, void
//follow--> )   

    public void ListaParam() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int
                || token.getClasse() == Tag.KW_string || token.getClasse() == Tag.KW_float
                || token.getClasse() == Tag.KW_void) {
            Param();
            ListaParamLinha();
        } else {
            // synch: FOLLOW(ListaParam)
            if (token.getClasse() == Tag.SMB_CP) {
                erroSintatico("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"Interge, Float, String, Boolean, Void\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    ListaParam();
                }
            }
        }
    }
//ListaParam’ → ”, ” ListaParam 13 | ε 14
//first--> ε, “,”
//follow--> )

    public void ListaParamLinha() {
        if (eat(Tag.SMB_VIR)) {
            ListaParam();
        } //follow--> ).
        else if (token.getClasse() == Tag.SMB_CP) {
            return;
        } else {
            skip("Esperado \" ," + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ListaParamLinha();
            }
        }
    }

    //Param → Tipo ID 15
    //first-->boolean, int, string, float, void
    //follow-->“,”, )
    public void Param() {
        if (token.getClasse() == Tag.KW_boolean || token.getClasse() == Tag.KW_int || token.getClasse() == Tag.KW_string
                || token.getClasse() == Tag.KW_float || token.getClasse() == Tag.KW_void) {
            Tipo();
            if (!eat(Tag.ID)) {
                erroSintatico("Esperado \"ID\", encontrado " + "\"" + token.getLexema() + "\"");
            }
        }//follow--> “,”, )
        else {
            if (token.getClasse() == Tag.SMB_CP || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"Boolean | Int | String | Float | Void \", encontrado " + "\"" + token.getLexema() + "\"");

            } else {
                skip("Esperado \" Boolean | Int | String | Float | Void" + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Param();
                }

            }
        }
    }

//Retorno → "return" Expressao";" 16 | ε 17
    //first--> ε, return
    //follow--> }
    public void Retorno() {
        if (eat(Tag.KW_return)) {
            Expressao();
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \";\", encontrado " + "\"" + token.getLexema() + "\"");
            }
        } //follow--> } 
        else if (token.getClasse() == Tag.SMB_FCCH) {
            return;
        } else {
            skip("Esperado \" Return " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                Retorno();
            }
        }
    }

    //Main → public | static | void | main | ( | |) | { |  RegexDeclaraVar ListaCmd |}| 18
    //first-->public
    //follow-->}
    public void Main() {
        if (!eat(Tag.KW_public)) {
            erroSintatico("Esperado \"public\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.KW_static)) {
            erroSintatico("Esperado \"static\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.KW_void)) {
            erroSintatico("Esperado \"void\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.KW_main)) {
            erroSintatico("Esperado \"main\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.SMB_OP)) {
            erroSintatico("Esperado \"( \", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.SMB_CP)) {
            erroSintatico("Esperado \") \", encontrado " + "\"" + token.getLexema() + "\"");
        }
        if (!eat(Tag.SMB_ABCH)) {
            erroSintatico("Esperado \"{\", encontrado " + "\"" + token.getLexema() + "\"");
        }
        RegexDeclaraVar();
        ListaCmd();
        if (!eat(Tag.SMB_FCCH)) {
            erroSintatico("Esperado \"}\", encontrado " + "\"" + token.getLexema() + "\"");

        } else {
            if (token.getClasse() == Tag.SMB_FCCH) {
                //erroSintatico("Esperado \"}\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"  Public\" " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Main();
                }
            }
        }
    }

    // Tipo --> boolean" | "int" | "string" | "float" | "void"
    //first-->boolean, int, string, float, void
    //follow-->ID
    public void Tipo() {
        if (!eat(Tag.KW_int) && !eat(Tag.KW_boolean) && !eat(Tag.KW_string) && !eat(Tag.KW_float) && !eat(Tag.KW_void)) {

            if (token.getClasse() == Tag.ID) {
                erroSintatico("Esperado \"boolean, int, String,float,void\", encontrado " + "\"" + token.getLexema() + "\"");

            } else {
                skip("Esperado \"boolean, int, String,float,void\", encontrado " + "\"" + token.getLexema() + "\"");
                Tipo(); // mantem 'TipoPrimitivo()' na pilha recursiva
            }
        }
    }

    //ListaCmd → ListaCmd’
    //first-->ε, if, while, print, println, ID
    //follow-->return, }
    public void ListaCmd() {
        //fist--> ListaCmd' ε, if, while, print, println, ID
        if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                || token.getClasse() == Tag.KW_print
                || token.getClasse() == Tag.KW_println || token.getClasse() == Tag.ID) {
            ListaCmdLinha();
            //follow--> return, }
        } else if (token.getClasse() == Tag.KW_return || token.getClasse() == Tag.SMB_FCCH) {
            return;
        } else {
            skip("Esperado \"if, while, print, println, ID \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ListaCmd();
            }
        }
    }
//ListaCmd’ → Cmd ListaCmd’ 25 | ε 26
// first--> ε, if, while, print, println, ID
//follow-->return, }

    public void ListaCmdLinha() {
        if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                || token.getClasse() == Tag.ID) {
            Cmd();
            ListaCmdLinha();
        } //follow-->return, }
        else if (token.getClasse() == Tag.KW_return || token.getClasse() == Tag.SMB_FCCH) {
            return;
        } else {
            skip("Esperado \"if, while, print , println , ID\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ListaCmdLinha();
            }
        }
    }
///Cmd → CmdIF 27 | CmdWhile 28 | CmdPrint 29 | CmdPrintln 30 | ID Cmd’ 31
//first-->if, while, print, println, ID
    //follow-->if, while, print, println, ID, return, }

    public void Cmd() {
        //first--> cmdIF
        if (eat(Tag.ID)) {
            CmdLinha();
        } else if (token.getClasse() == Tag.KW_if) {

            cmdIf();

        } else if (token.getClasse() == Tag.KW_println) {

            cmdPrintln();

        } else if (token.getClasse() == Tag.KW_while) {
            cmdWhile();

        } else if (token.getClasse() == Tag.KW_print) {
            cmdPrint();

        } //follow--> if, while, print, println, ID, return, }
        else {
            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while || token.getClasse() == Tag.KW_print
                    || token.getClasse() == Tag.KW_println || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"if, while, print, println , ID\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"if, while, print, println, ID\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Cmd();
                }

            }
        }
    }

    //Cmd’ → CmdAtrib 32 | CmdMetodo 33
    //first--> =,(
    //follow-->if, while, print, println, ID, return, }
    public void CmdLinha() {
        if (token.getClasse() == Tag.RELOP_ASSIGN) {

            CmdAtrib();

        } //follow-->if, while, print, println, ID, return, }
        else if (token.getClasse() == Tag.SMB_OP) {
            cmdMetodo();
        } else {
            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"= , (\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \" = , ( \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    CmdLinha();
                }
            }
        }
    }
//CmdIF → "if" "(" Expressao ")" "{" Cmd "}" CmdIF’ 34
//fisrt--> if
    //follow--> if, while, print, println, ID, return, }

    public void cmdIf() {
        if (eat(Tag.KW_if)) {
            if (!eat(Tag.SMB_OP)) {
                erroSintatico("Esperado \" ( \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Expressao();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \" ) \", encontrado " + "\"" + token.getLexema() + "\"");

            }
            if (!eat(Tag.SMB_ABCH)) {
                erroSintatico("Esperado \" { \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Cmd();
            if (!eat(Tag.SMB_FCCH)) {
                erroSintatico("Esperado \" } \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            cmdIfLinha();
        } else {
            // follow-->if, while, print, println, ID, return, }

            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"if\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"if\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    cmdIf();
                }
            }

        }
    }
//CmdIF’ → "else" "{" Cmd "}" 35 | ε 36
//fist-->else, ε
    //follow-->if, while, print, println, ID, return, }

    public void cmdIfLinha() {
        if (eat(Tag.KW_else)) {
            if (!eat(Tag.SMB_ABCH)) {
                erroSintatico("Esperado \"{ \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Cmd();
            if (!eat(Tag.SMB_FCCH)) {
                erroSintatico("Esperado \" } \", encontrado " + "\"" + token.getLexema() + "\"");
            }

        } //follow-->if, while, print, println, ID, return, }
        else if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                || token.getClasse() == Tag.SMB_FCCH) {
            return;
        } else {
            skip("Esperado \" else \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                cmdIfLinha();
            }
        }
    }
//CmdWhile → "while" "(" Expressao ")" "{" Cmd "}" 37
//fist-->while
    //follow-->if, while, print, println, ID, return, }

    public void cmdWhile() {
        if (eat(Tag.KW_while)) {
            if (!eat(Tag.SMB_OP)) {
                erroSintatico("Esperado \" (  \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Expressao();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \" ) \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            if (!eat(Tag.SMB_ABCH)) {
                erroSintatico("Esperado \" { \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Cmd();
            if (!eat(Tag.SMB_FCCH)) {
                erroSintatico("Esperado \" } \", encontrado " + "\"" + token.getLexema() + "\"");
            }
        }//follow-->if | while | print | println | ID | return | }
        else {

            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"while\", encontrado " + "\"" + token.getLexema() + "\"");

                return;
            } else {
                skip("Esperado \"while\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    cmdWhile();
                }
            }
        }
    }
//CmdPrint → "print" "(" Expressao ")" ";" 38
//fist-->print
    //follow-->if, while, print, println, ID, return, }

    public void cmdPrint() {
        if (eat(Tag.KW_print)) {
            if (!eat(Tag.SMB_OP)) {
                erroSintatico("Esperado \" (  \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Expressao();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \" )  \", encontrado " + "\"" + token.getLexema() + "\"");

            }
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \" ; \", encontrado " + "\"" + token.getLexema() + "\"");

            } //follow-->if, while, print, println, ID, return, }
        } else {

            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"print\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \" print \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    cmdPrint();
                }
            }
        }
    }
//CmdPrintln → "println" "(" Expressao ")" ";" 39
//fist-->println
    //follow-->if, while, print, println, ID, return, }

    public void cmdPrintln() {
        if (eat(Tag.KW_println)) {
            if (!eat(Tag.SMB_OP)) {
                erroSintatico("Esperado \"( \", encontrado " + "\"" + token.getLexema() + "\"");
            }
            Expressao();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \" ) \", encontrado " + "\"" + token.getLexema() + "\"");

            }
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \" ; \", encontrado " + "\"" + token.getLexema() + "\"");

            } //follow-->if | while | print | println | ID | return | }
        } else {

            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \" println\", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"println\", encontrado " + "\"" + token.getLexema() + "\"");

                if (token.getClasse() != Tag.EOF) {
                    cmdPrintln();
                }
            }
        }
    }

    //CmdAtrib → "=" Expressao ";" 40
    //fist-->=
    //follow-->if, while, print, println, ID, return, }
    public void CmdAtrib() {
        if (eat(Tag.RELOP_ASSIGN)) {
            Expressao();
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \" ; \", encontrado " + "\"" + token.getLexema() + "\"");
            }
        } else {
            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print
                    || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \" = \", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"= \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    CmdAtrib();
                }
            }
        }
    }

//CmdMetodo → "(" RegexExp4 ")" ";" 41
//fist-->(
//follow-->if, while, print, println, ID, return, }
    public void cmdMetodo() {
        if (eat(Tag.SMB_OP)) {
            RegexExp4();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \")\", encontrado " + "\"" + token.getLexema() + "\"");
            }
            if (!eat(Tag.SMB_SEMICOLON)) {
                erroSintatico("Esperado \";\", encontrado " + "\"" + token.getLexema() + "\"");
            }
        } else {
            if (token.getClasse() == Tag.KW_if || token.getClasse() == Tag.KW_while
                    || token.getClasse() == Tag.KW_print || token.getClasse() == Tag.KW_println
                    || token.getClasse() == Tag.ID || token.getClasse() == Tag.KW_return
                    || token.getClasse() == Tag.SMB_FCCH) {
                erroSintatico("Esperado \"( \", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"(  \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    cmdMetodo();
                }
            }
        }
    }

    //Expressao → Exp1 Exp’ 42
    //first--> ID, ConstInteira, contFloat, ConstString, true, false,“-” (negação), “!”, (
    //follow--> ; ,  ) , ","
    //ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, (
    public void Expressao() {

        if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira
                || token.getClasse() == Tag.contFloat || token.getClasse() == Tag.ConstString
                || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO
                || token.getClasse() == Tag.SMB_OP) {

            Expressao1();
            ExpressaoLinha();
        } else {
            // synch: FOLLOW(Expressao) “;”, ), “,”
            if (token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                    || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"ID, int, float, string, true, false,“-” (negação), “!”, (\", encontrado " + "\"" + token.getLexema() + "\"");
            } else {
                skip("Esperado \"ID, int, float, string, true, false,“-” (negação), “!”, (\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Expressao();
                }
            }
        }
    }

    //ExpressaoLinha → "&&" Exp1 Exp’ 43 | "||" Exp1 Exp’ 44 | ε 45
    //first-->&&, ||, ε
    //follow-->“;”, ), “,”
    public void ExpressaoLinha() {
        if (eat(Tag.OP_AND) || eat(Tag.OP_OR)) {
            Expressao1();
            ExpressaoLinha();
        } else if (token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                || token.getClasse() == Tag.SMB_VIR) {
            return;

        } else {
            skip("Esperado \"&&, || \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                ExpressaoLinha();

            }
        }
    }

    //Expressao1 → Exp2 Exp1’ 46
    //fist-->ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, (
    //follow-->&&, ||, “;”, ), “,”
    public void Expressao1() {
        //first-->ID | ConstInteira | ConstReal | ConstString | true | false | “-” (negação) | “!” | (
        if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira
                || token.getClasse() == Tag.contFloat || token.getClasse() == Tag.ConstString
                || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO
                || token.getClasse() == Tag.SMB_OP) {
            Expressao2();
            Expressao1Linha();
        } else {

            // synch: FOLLOW(Expressao) ;&&, ||, “;”, ), “,”
            if (token.getClasse() == Tag.OP_OR || token.getClasse() == Tag.OP_AND
                    || token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                    || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, ( \", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, (\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Expressao1();

                }

            }
        }
    }

    //Expressao1Linha--> "<" Exp2 Exp1’  | "<=" Exp2 Exp1’ | ">" Exp2 Exp1’ | ">=" Exp2 Exp1’  | "==" Exp2 Exp1’  | "!=" Exp2 Exp1’  | ε 
    //fist--> <, <=, >, >=, ==, !=, ε
    //follow-->&&, ||, “;”, ), “,”
    public void Expressao1Linha() {

        if (eat(Tag.RELOP_LT) || eat(Tag.RELOP_LE) || eat(Tag.RELOP_GT)
                || eat(Tag.RELOP_GE) || eat(Tag.RELOP_EQ) || eat(Tag.RELOP_NE)) {
            Expressao2();
            Expressao1Linha();
        } else if (token.getClasse() == Tag.OP_OR || token.getClasse() == Tag.OP_AND
                || token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                || token.getClasse() == Tag.SMB_VIR) {
            return;
        } else {
            skip("Esperado \"<,<=,>,>=, ==, !=\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                Expressao1Linha();
            }
        }
    }

    //Expressao2-->Exp3 Exp2’
    //fist-->ID, ConstInteira, constFloat, ConstString, true, false,“-” (negação), “!”, (
    //follow--> <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”
    public void Expressao2() {
        if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira
                || token.getClasse() == Tag.contFloat || token.getClasse() == Tag.ConstString
                || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO
                || token.getClasse() == Tag.SMB_OP) {
            Expressao3();
            Expressao2Linha();
        }//follow--> <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”
        else {

            if (token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                    || token.getClasse() == Tag.RELOP_GT
                    || token.getClasse() == Tag.RELOP_GE || token.getClasse() == Tag.RELOP_EQ
                    || token.getClasse() == Tag.RELOP_NE || token.getClasse() == Tag.OP_AND
                    || token.getClasse() == Tag.OP_OR || token.getClasse() == Tag.SMB_SEMICOLON
                    || token.getClasse() == Tag.SMB_CP || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"ID, ConstInteira, constFloat, ConstString, true, false,“-” (negação), “!”, ( \", encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"ID, ConstInteira, ConstReal, ConstString, true, false,- (negação), !, (\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Expressao2();
                }
            }
        }
    }

    //Expressao2Linha → + Exp3 Exp2’ 55 | - Exp3 Exp2’ 56 | ε 57
    //fist-->+, -, ε
    //follow--><, <=, >, >=, ==, !=, &&, ||, ;, ), “,”
    public void Expressao2Linha() {

        if (eat(Tag.RELOP_SUM) || eat(Tag.RELOP_MINUS)) {
            Expressao3();
            Expressao2Linha();
        } else if (token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                || token.getClasse() == Tag.RELOP_GT || token.getClasse() == Tag.RELOP_GE
                || token.getClasse() == Tag.RELOP_EQ || token.getClasse() == Tag.RELOP_NE
                || token.getClasse() == Tag.OP_AND
                || token.getClasse() == Tag.OP_OR || token.getClasse() == Tag.SMB_SEMICOLON
                || token.getClasse() == Tag.SMB_CP || token.getClasse() == Tag.SMB_VIR) {
            return;
        } else {
            skip("Esperado \"+,- \", encontrado " + "\"" + token.getLexema() + "\"");
            //if (token.getClasse() != Tag.EOF) {
            Expressao2Linha();
            //}
        }
    }
//Expressao3 → Exp4 Exp3’ 58
//fist → ID, , ConstInteira, constFloat, ConstString, true, false,“-” (negação), !, (
//follow-->+, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”

    public void Expressao3() {
        if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira
                || token.getClasse() == Tag.contFloat || token.getClasse() == Tag.ConstString
                || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO
                || token.getClasse() == Tag.SMB_OP) {
            Expressao4();
            Expressao3linha();
        } // follow-->+, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”
        else {
            if (token.getClasse() == Tag.RELOP_SUM || token.getClasse() == Tag.RELOP_MINUS
                    || token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                    || token.getClasse() == Tag.RELOP_GT || token.getClasse() == Tag.RELOP_GE
                    || token.getClasse() == Tag.RELOP_EQ
                    || token.getClasse() == Tag.RELOP_NE || token.getClasse() == Tag.OP_AND || token.getClasse() == Tag.OP_OR || token.getClasse() == Tag.SMB_SEMICOLON
                    || token.getClasse() == Tag.SMB_CP || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"ID, , ConstInteira, ConstReal, ConstString, true, false,“-” (negação), !, (\" , encontrado " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"ID, , ConstInteira, ConstReal, ConstString, true, false,“-” (negação), !, (\", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Expressao3();
                }
            }
        }
    }

    //Expressao3linha →* Exp4 Exp3’ 59 | / Exp4 Exp3’ 60 | ε 61
    //fist-->*, /, ε
    //follow-->+, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”
    public void Expressao3linha() {
        if (eat(Tag.RELOP_MULT) || eat(Tag.RELOP_DIV)) {
            Expressao4();
            Expressao3linha();

        } //follow-->+, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ), “,”
        else if (token.getClasse() == Tag.RELOP_SUM || token.getClasse() == Tag.RELOP_MINUS
                || token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                || token.getClasse() == Tag.RELOP_GT || token.getClasse() == Tag.RELOP_GE
                || token.getClasse() == Tag.RELOP_EQ || token.getClasse() == Tag.RELOP_NE
                || token.getClasse() == Tag.OP_AND || token.getClasse() == Tag.OP_OR
                || token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                || token.getClasse() == Tag.SMB_VIR) {
            return;
// return;
        } else {
            skip("Esperado \" * , /\", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                Expressao3linha();
            }
        }
    }

//Expressao4 → ID Exp4’ 62 | ConstInteira 63 | ConstReal 64 | ConstString 65| "true" 66 | "false" 67 | OpUnario Expressao 68 | "(" Expressao")"
//fist-->ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, (
//follow-->*, /, +, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ),“,”
    public void Expressao4() {
        if (eat(Tag.ConstInteira) || eat(Tag.contFloat) || eat(Tag.ConstString) || eat(Tag.KW_true) || eat(Tag.KW_false)) {
            return;
        } else if (eat(Tag.ID)) {
            Expressao4linha();
        } else if (eat(Tag.OP_NEGATIVO) || eat(Tag.OP_NAO)) {
            Expressao();
        } else if (eat(Tag.SMB_OP)) {
            // erroSintatico("Esperado \"( \", encontrado " + "\"" + token.getLexema() + "\"");
            Expressao();
            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \")\", encontrado " + "\"" + token.getLexema() + "\"");
            }
        } else {
            //follow-->*, /, +, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ),“,”

            if (token.getClasse() == Tag.RELOP_MULT || token.getClasse() == Tag.RELOP_DIV
                    || token.getClasse() == Tag.RELOP_SUM || token.getClasse() == Tag.RELOP_MINUS
                    || token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                    || token.getClasse() == Tag.RELOP_GT || token.getClasse() == Tag.RELOP_GE
                    || token.getClasse() == Tag.RELOP_EQ || token.getClasse() == Tag.RELOP_NE
                    || token.getClasse() == Tag.OP_AND || token.getClasse() == Tag.OP_OR
                    || token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                    || token.getClasse() == Tag.SMB_VIR) {
                erroSintatico("Esperado \"ID| ConstInteira | Real | String | true | false | OpUnitario| ( \" " + "\"" + token.getLexema() + "\"");
                return;
            } else {
                skip("Esperado \"ID| ConstInteira | Real | String | true | false | OpUnitario| ( \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    Expressao4();
                }
            }
        }
    }

    //Expressao4linha → "(" RegexExp4 ")" 70 | ε 71
    //fist-->(, ε
    //follow-->*, /, +, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ),“,”
    public void Expressao4linha() {

        if (eat(Tag.SMB_OP)) {
            //erroSintatico("Esperado \" ( \", encontrado " + "\"" + token.getLexema() + "\"");
            RegexExp4();

            if (!eat(Tag.SMB_CP)) {
                erroSintatico("Esperado \") \", encontrado " + "\"" + token.getLexema() + "\"");

            }
        }//follow-->*, /, +, -, <, <=, >, >=, ==, !=, &&, ||, “;”, ),“,”
        else if (token.getClasse() == Tag.RELOP_MULT || token.getClasse() == Tag.RELOP_DIV
                || token.getClasse() == Tag.RELOP_SUM || token.getClasse() == Tag.RELOP_MINUS
                || token.getClasse() == Tag.RELOP_LT || token.getClasse() == Tag.RELOP_LE
                || token.getClasse() == Tag.RELOP_GT || token.getClasse() == Tag.RELOP_GE
                || token.getClasse() == Tag.RELOP_EQ || token.getClasse() == Tag.RELOP_NE
                || token.getClasse() == Tag.OP_AND || token.getClasse() == Tag.OP_OR
                || token.getClasse() == Tag.SMB_SEMICOLON || token.getClasse() == Tag.SMB_CP
                || token.getClasse() == Tag.SMB_VIR) {

            return;                // return;
        } else {
            skip("Esperado \"( \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                Expressao4linha();
            }
        }

    }

    //RegexExp4 → Expressao RegexExp4’ 72 | ε 73
    //fist-->ε, ID, ConstInteira, contFloat, ConstString, true, false,“-” (negação), “!”, (
    //follow--> )
    public void RegexExp4() {

        if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira
                || token.getClasse() == Tag.contFloat || token.getClasse() == Tag.ConstString
                || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO
                || token.getClasse() == Tag.SMB_OP) {
            Expressao();
            RegexExp4Linha();
        } else if (token.getClasse() == Tag.SMB_CP) {
            return;

        } else {
            skip("Esperado \"ID, ConstInteira, ConstReal, ConstString, true, false,“-” (negação), “!”, ( \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                RegexExp4();
            }

        }
    }

    //RegexExp4Linha → "," Expressao RegexExp4’ 74 | ε 75
    //fist-->“,”, ε
    //follow-->)
    public void RegexExp4Linha() {
        if (eat(Tag.SMB_VIR)) {
            Expressao();
            RegexExp4Linha();
        } else if (token.getClasse() == Tag.SMB_CP) {
            return;
        } else {
            skip("Esperado \" , \", encontrado " + "\"" + token.getLexema() + "\"");
            if (token.getClasse() != Tag.EOF) {
                RegexExp4Linha();
            }
        }
    }

//OpUnario → "-" 76 | "!" 77
//fist-->“-” (negação), “!”
    //follow-->ID, ConstInteira, ConstReal, ConstString,true, false, “-” (negação), “!”, (
    public void operadorUnario() {

//first--> -,!
        if (!eat(Tag.OP_NEGATIVO) && !eat(Tag.OP_NAO)) {
            //  erroSintatico("Esperado \"- | ! \", encontrado " + "\"" + token.getLexema() + "\"");
            // follow de OpUnario -->ID, ConstInteira, ConstReal, ConstString,true, false, “-” (negação), “!”, ( 
            if (token.getClasse() == Tag.ID || token.getClasse() == Tag.ConstInteira || token.getClasse() == Tag.contFloat
                    || token.getClasse() == Tag.ConstString || token.getClasse() == Tag.KW_true || token.getClasse() == Tag.KW_false
                    || token.getClasse() == Tag.OP_NEGATIVO || token.getClasse() == Tag.OP_NAO || token.getClasse() == Tag.SMB_OP) {

                erroSintatico("Esperado \"negativo -, e não !\", encontrado " + "\"" + token.getLexema() + "\"");
            } else {
                skip("Esperado \"negativo -, e não ! \", encontrado " + "\"" + token.getLexema() + "\"");
                if (token.getClasse() != Tag.EOF) {
                    operadorUnario();
                }

            }

        }
    }
}
