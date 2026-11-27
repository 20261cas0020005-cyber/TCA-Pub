import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

class Carta {// ele guarda o nome de cada carta, se for par ele vira
    String cartaNome;
    boolean virado = false;

    public Carta(String cartaNome) {
        this.cartaNome = cartaNome;
    }
}

class Jogador {// Informações da conexão de cada jogador
    Socket conn;
    PrintWriter out;
    BufferedReader in;
    String name;
    int points = 0;
    int vez;

    public Jogador(Socket conn, String name, PrintWriter out, BufferedReader in) throws IOException {
        if (conn.equals(null))
            return;
        // Se o método que chamou "new Jogador()" já tiver criado um "in" ou um "out",
        // pra quê fazer outro? Podemos utilizar o mesmo "in" e "out".
        // Se o método que chamou "new Jogador()" não criou nem "in" nem "out", não tem
        // problema, criamos aqui mesmo.
        this.conn = !conn.equals(null) ? conn : null;
        this.name = !name.isEmpty() ? name : null;
        this.out = !out.equals(null) ? out : (new PrintWriter(conn.getOutputStream(), true));
        this.in = !in.equals(null) ? in : (new BufferedReader(new InputStreamReader(conn.getInputStream())));
    }
}

public class BrincandoComTCA {
    public static final int PORT = 25565; // Nossa porta padrão para partidas LAN // Por que, Gabriel/Walter? // Porque
                                          // é a mesma porta que o minecraft usa >_<
    public static final int TEMPO_SEGUNDOS_MOSTRAR_TABULEIRO = 5;
    public static final int MAX_TENTATIVAS_SENHA = 5;
    public static final int TEMPO_SEGUNDOS_ENTRE_SENHAS = 1; // Contra brute-force
    public static final int TEMPO_SEGUNDOS_ESPERA_LIMITETENTATIVAS = 5;
    public static final char CARACTERE_ESCONDIDO = '_';

    public static final Scanner TECLADO = new Scanner(System.in);
    public static Carta[][] tabuleiro;
    public static List<Jogador> jogadores;
    public static boolean partidaRunning = false;
    public static int cursorEstaNaFileira;
    public static int maiorPalavraCaracteres;

    public static void imprimir(String texto) {
        System.out.println(texto);
    }

    public static void telaInicial() {

        System.out.print("      ██╗ ██████╗  ██████╗  ██████╗    ██████╗  █████╗\r\n" + //
                "     ██║██╔═══██╗██╔════╝ ██╔═══██╗   ██╔══██╗██╔══██╗\r\n" + //
                "     ██║██║   ██║██║  ███╗██║   ██║   ██║  ██║███████║\r\n" + //
                "██   ██║██║   ██║██║   ██║██║   ██║   ██║  ██║██╔══██║\r\n" + //
                "╚█████╔╝╚██████╔╝╚██████╔╝╚██████╔╝   ██████╔╝██║  ██║\r\n" + //
                " ╚════╝  ╚═════╝  ╚═════╝  ╚═════╝    ╚═════╝ ╚═╝  ╚═╝\r\n" + //
                "\r\n" + //
                "                                   /\r\n" + //
                "███╗   ███╗███████╗███╗   ███╗ ██████╗ ██████╗ ██╗ █████╗\r\n" + //
                "████╗ ████║██╔════╝████╗ ████║██╔═══██╗██╔══██╗██║██╔══██╗\r\n" + //
                "██╔████╔██║█████╗  ██╔████╔██║██║   ██║██████╔╝██║███████║\r\n" + //
                "██║╚██╔╝██║██╔══╝  ██║╚██╔╝██║██║   ██║██╔══██╗██║██╔══██║\r\n" + //
                "██║ ╚═╝ ██║███████╗██║ ╚═╝ ██║╚██████╔╝██║  ██║██║██║  ██║\r\n" + //
                "╚═╝     ╚═╝╚══════╝╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝\r\n" + //
                "\r\n" + //
                "██████╗  ██████╗    ██╗    ██╗████████╗\r\n" + //
                "██╔══██╗██╔═══██╗   ██║    ██║╚══██╔══╝\r\n" + //
                "██║  ██║██║   ██║   ██║ █╗ ██║   ██║\r\n" + //
                "██║  ██║██║   ██║   ██║███╗██║   ██║\r\n" + //
                "██████╔╝╚██████╔╝   ╚███╔███╔╝   ██║\r\n" + //
                "╚═════╝  ╚═════╝     ╚══╝╚══╝    ╚═╝");

    }

    public static void telinhafim() {
        System.out.println("███████╗███╗   ██╗██████╗     ██████╗  █████╗ ███╗   ███╗███████╗\r\n" + //
                "██╔════╝████╗  ██║██╔══██╗   ██╔════╝ ██╔══██╗████╗ ████║██╔════╝\r\n" + //
                "█████╗  ██╔██╗ ██║██║  ██║   ██║  ███╗███████║██╔████╔██║█████╗\r\n" + //
                "██╔══╝  ██║╚██╗██║██║  ██║   ██║   ██║██╔══██║██║╚██╔╝██║██╔══╝\r\n" + //
                "███████╗██║ ╚████║██████╔╝   ╚██████╔╝██║  ██║██║ ╚═╝ ██║███████╗\r\n" + //
                "╚══════╝╚═╝  ╚═══╝╚═════╝     ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝");
    }

    public static void enter() {
        System.out.printf("Aperte ");
        mudarCor(2);
        System.out.printf("ENTER");
        mudarCor(COR_PADRAO);
        System.out.printf(" para começar\n");
    }

    public static void Play() {
        System.out.printf("Para começarmos... Aperte ");
        mudarCor(2);
        System.out.printf("ENTER");
        mudarCor(COR_PADRAO);
        System.out.printf(" e confirme!\n");
    }
    // Métodos do Ló

    public static int pegarNumeroAleatorio(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static void gotoXY(int linha, int coluna) {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df", escCode, linha, coluna));
    }

    // peguei do ló
    public static final int PRETO = 0;
    public static final int VERMELHO = 1;
    public static final int VERDE = 2;
    public static final int AMARELO = 3;
    public static final int AZUL = 4;
    public static final int MAGENTA = 5;
    public static final int CIANO = 6;
    public static final int CINZA = 7;
    public static final int BRANCO = 15;
    public static final int COR_PADRAO = -1; // a cor original do terminal

    public static void mudarCor(int cor) {
        if (cor >= 0 && cor <= 255) {
            System.out.print("\033[38;5;" + cor + "m");
        } else {
            System.out.print("\033[39m"); // cor padrão do terminal
        }
    }

    public static void mudarCorFundo(int cor) {
        if (cor >= 0 && cor <= 255) {
            System.out.print("\033[48;5;" + cor + "m");
        } else {
            System.out.print("\033[49m"); // cor padrão do terminal
        }
    }

    public static void jogadorCursorGoToXY(Jogador jogador, int linha, int coluna) {
        char escCode = 0x1B;
        jogador.out.print(String.format("%c[%d;%df", escCode, linha, coluna));
        jogador.out.flush();
    }

    public static void allJogadoresCursorGoToXY(int linha, int coluna) {
        char escCode = 0x1B;
        for (Jogador jogador : jogadores) {
            jogador.out.print(String.format("%c[%d;%df", escCode, linha, coluna));
            jogador.out.flush();
        }
    }

    public static boolean devolverExistenciaDeCorrespondencia(String[] vetor, String palavra) {
        if (vetor == null || palavra.isEmpty())
            return false;
        for (int i = 0; i < vetor.length; i++) {
            if (vetor[i] == null)
                continue;
            if (vetor[i].equals(palavra))
                return true;
        }
        return false;
    }

    public static int pegarQntCaracteresMaiorPalavra(Carta[][] matriz) {
        if (matriz == null)
            return 0;
        int maior = 0;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                if (matriz[i][j].cartaNome.length() > maior)
                    maior = matriz[i][j].cartaNome.length();
            }
        }
        return maior;
    }

    public static void imprimirTabuleiroAtual() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        // Indices
        System.out.printf("  ");

        for (int i = 0; i < tabuleiro[0].length; i++) {
            System.out.printf(" ".repeat((int) Math.floor((maiorPalavraCaracteres+1) / 2)) + (i + 1)
                    + " ".repeat((maiorPalavraCaracteres+1) / 2) + " ".repeat((maiorPalavraCaracteres+1) / 2 % 1 > 0 ? 1 : 0));
        }

        System.out.println();
        cursorEstaNaFileira++;

        for (int i = 0; i < tabuleiro.length; i++) {
            System.out.printf("%2d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {
                if (tabuleiro[i][j].virado) {
                    System.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                } else {
                    for (int k = 0; k < maiorPalavraCaracteres; k++) {
                        mudarCor(4);
                        System.out.print(CARACTERE_ESCONDIDO);
                        mudarCor(COR_PADRAO);
                    }
                    System.out.printf(" ");
                }
            }
            System.out.println();
            cursorEstaNaFileira++;
        }
    }

    public static void imprimirTabuleiroCompleto() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        // Indices
        System.out.printf("  ");
        for (int i = 0; i < tabuleiro[0].length; i++) {
            System.out.printf(" ".repeat((int) Math.floor((maiorPalavraCaracteres+1) / 2)) + (i + 1)
                    + " ".repeat((maiorPalavraCaracteres+1) / 2) + " ".repeat((maiorPalavraCaracteres+1) / 2 % 1 > 0 ? 1 : 0));
        }
        System.out.println();
        cursorEstaNaFileira++;

        for (int i = 0; i < tabuleiro.length; i++) {
            System.out.printf("%2d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {
                System.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
            }
            System.out.println();
            cursorEstaNaFileira++;
        }
    }

    public static char consertarEntrada(String texto) {
        String entradaConsertada = texto.trim().toLowerCase();

        if (entradaConsertada.isEmpty()) {
            return ' ';
        }
        return entradaConsertada.charAt(0);
    }

    public static boolean descobrirConnExiste(Socket socket) {
        for (Jogador jogador : jogadores) {
            if (jogador.conn.equals(socket))
                return true;
        }
        return false;
    }

    public static boolean descobrirNomeExiste(String name) {// para ponhar o nome quando é multiplayer
        if (name.equals("SERVER"))
            return true; // Aqui NÃO pode falhar, melhor ser redundante mesmo.

        for (Jogador jogador : jogadores) {
            if (jogador.name.equals(name))
                return true;
        }
        return false;
    }

    public static void enumerarJogadores() {
        int vez = 1;
        for (Jogador jogador : jogadores) {
            jogador.vez = vez++;
        }
    }

    public static void enviarTextoTodosJogadores(String text, Socket exceptSocket, String exceptName)
            throws IOException {
        for (Jogador jogador : jogadores) {
            if (jogador.name.equals(exceptName) || jogador.conn.equals(exceptSocket))
                continue;
            jogador.out.print(text);
            jogador.out.flush();
        }
    }

    public static void imprimirEEnviarParaTodosTabuleiroAtual() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        for (Jogador jogador : jogadores) {
            // Indices
            jogador.out.printf("  ");
            for (int i = 0; i < tabuleiro[0].length; i++) {
                jogador.out.printf(" ".repeat((int) Math.floor((maiorPalavraCaracteres+1) / 2)) + (i + 1)
                        + " ".repeat((maiorPalavraCaracteres+1) / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
                                                                                                                        // (maiorPalavraCaracteres/2)
                                                                                                                        // i
                                                                                                                        // (maiorPalavraCaracteres/2)
                                                                                                                        // (caso
                                                                                                                        // "maiorPalavraCaracteres/2"
                                                                                                                        // dê
                                                                                                                        // número
                                                                                                                        // quebrado,
                                                                                                                        // adicionar
                                                                                                                        // 1
                                                                                                                        // espaço
                                                                                                                        // no
                                                                                                                        // final)
            }
            jogador.out.println();

            for (int i = 0; i < tabuleiro.length; i++) {
                jogador.out.printf("%2d ", i + 1); // Index
                for (int j = 0; j < tabuleiro[0].length; j++) {

                    if (tabuleiro[i][j].virado) {
                        jogador.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                    } else {
                        for (int k = 0; k < maiorPalavraCaracteres; k++) {
                            jogador.out.print(CARACTERE_ESCONDIDO);
                        }
                        jogador.out.printf(" ");
                    }

                }
                jogador.out.println();
            }

        }
        imprimirTabuleiroAtual();
    }

    public static void enviarTabuleiroAtual(Jogador jogador) {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        // Indices
        jogador.out.printf("  ");
        for (int i = 0; i < tabuleiro[0].length; i++) {
            jogador.out.printf(" ".repeat((int) Math.floor((maiorPalavraCaracteres+1) / 2)) + (i + 1)
                    + " ".repeat((maiorPalavraCaracteres+1) / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
                                                                                                                    // (maiorPalavraCaracteres/2)
                                                                                                                    // i
                                                                                                                    // (maiorPalavraCaracteres/2)
                                                                                                                    // (caso
                                                                                                                    // "maiorPalavraCaracteres/2"
                                                                                                                    // dê
                                                                                                                    // número
                                                                                                                    // quebrado,
                                                                                                                    // adicionar
                                                                                                                    // 1
                                                                                                                    // espaço
                                                                                                                    // no
                                                                                                                    // final)
        }
        jogador.out.println();

        for (int i = 0; i < tabuleiro.length; i++) {
            jogador.out.printf("%2d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {

                if (tabuleiro[i][j].virado) {
                    jogador.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                } else {
                    for (int k = 0; k < maiorPalavraCaracteres; k++) {
                        jogador.out.print(CARACTERE_ESCONDIDO);
                    }
                    jogador.out.printf(" ");
                }

            }
            jogador.out.println();
        }

    }

    public static void imprimirEEnviarParaTodosTabuleiroCompleto() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        for (Jogador jogador : jogadores) {
            // Indices
            jogador.out.printf("  ");
            for (int i = 0; i < tabuleiro[0].length; i++) {
                jogador.out.printf(" ".repeat((int) Math.floor((maiorPalavraCaracteres+1) / 2)) + (i + 1)
                        + " ".repeat((maiorPalavraCaracteres+1) / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
                                                                                                                        // (maiorPalavraCaracteres/2)
                                                                                                                        // i
                                                                                                                        // (maiorPalavraCaracteres/2)
                                                                                                                        // (caso
                                                                                                                        // "maiorPalavraCaracteres/2"
                                                                                                                        // dê
                                                                                                                        // número
                                                                                                                        // quebrado,
                                                                                                                        // adicionar
                                                                                                                        // 1
                                                                                                                        // espaço
                                                                                                                        // no
                                                                                                                        // final)
            }
            jogador.out.println();

            for (int i = 0; i < tabuleiro.length; i++) {
                jogador.out.printf("%2d ", i + 1); // Index
                for (int j = 0; j < tabuleiro[0].length; j++) {
                    jogador.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                }
                jogador.out.println();
            }

        }
        imprimirTabuleiroCompleto();
    }

    public static void moverCursoresParaOInicio() {
        int n = cursorEstaNaFileira;
        for (int j = 0; j < n - 1; j++) { // "-1" porque: Se o cursor estiver na coluna 5, sobe 4 vezes
            System.out.print("\033[A"); // Sobe linha

            for (Jogador jogador : jogadores) {
                jogador.out.print("\033[A");
            }
            cursorEstaNaFileira--;

        }
        // Está na primeira coluna
        System.out.printf("\r"); // Vai para primeiro caractere
        for (Jogador jogador : jogadores) {
            jogador.out.print("\r");
        }
    }

    public static void moverCursoresParaOFim() {
        // cursorEstaNaFileira = 2; precisa mover 3 (5-2)
        // 0
        // 0
        // 0
        // 0
        // 0
        // 0 ←
        // 0
        int n = tabuleiro[0].length - cursorEstaNaFileira;
        for (int j = 0; j < n; j++) { // Para cada coluna no tabuleiro
            System.out.print("\033[B"); // Desce linha ( vai para a última coluna )

            for (Jogador jogador : jogadores) {
                jogador.out.print("\033[B");
            }
            cursorEstaNaFileira--;
        }
        System.out.printf("\r"); // Vai para primeiro caractere
        for (Jogador jogador : jogadores) {
            jogador.out.print("\r");
        }
    }

    public static void limparTelas() throws IOException {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        limparTela();
        limparTelaJogadores();
    }

    public static void limparTela(Jogador jogador) throws IOException {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);
        for (int i = 0; i <= cursorEstaNaFileira + tabuleiro.length + 4; i++) { // + tabuleiro.length para não depender
                                                                                // apenas do cursor || +4 por causa de
                                                                                // uns inputs perdidos aqui e ali
            jogadorCursorGoToXY(jogador, i, 0);

            for (int j = 0; j < (maiorPalavraCaracteres + 1) * tabuleiro[0].length * 2; j++) {
                jogador.out.printf(" ");
            }
        }
        jogador.out.printf("\033[H\033[2J");
    }

    public static void limparTela() throws IOException {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);
        for (int i = 0; i <= cursorEstaNaFileira + 4; i++) { // +4 por causa de uns inputs perdidos aqui e ali
            gotoXY(i, 0);

            System.out.printf(" ".repeat((maiorPalavraCaracteres + 1) * tabuleiro[0].length));
        }
        cursorEstaNaFileira = 1;
        System.out.printf("\033[H\033[2J");
    }

    public static void limparTelaJogadores() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);
        for (Jogador jogador : jogadores) {

            for (int i = 0; i <= cursorEstaNaFileira + tabuleiro.length + 4; i++) { // + tabuleiro.length para não
                                                                                    // depender apenas do cursor || +4
                                                                                    // por causa de uns inputs perdidos
                                                                                    // aqui e ali
                jogadorCursorGoToXY(jogador, i, 0);

                for (int j = 0; j < (maiorPalavraCaracteres + 1) * tabuleiro[0].length * 2; j++) {
                    jogador.out.printf(" ");
                }
            }
            jogador.out.printf("\033[H\033[2J");
        }

    }

    public static void atualizarTabuleiroServer() throws IOException {
        limparTela();

        imprimirTabuleiroAtual();
    }

    public static void atualizarTabuleiroJogadores() throws IOException {
        limparTelas();

        imprimirEEnviarParaTodosTabuleiroAtual();
    }

    public static void mostrarCartaMultiP(int x, int y) throws IOException, InterruptedException {
        Thread.sleep(400);
        if (x < 0 || y < 0)
            return;

        int objetivo = y + 1; // +1 porque: A primeira linha é o índice(primeira linha indica a coluna para o
                              // jogador)

        gotoXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4); // +4 porque: pula o Índice, que ocupa 3 posições
                                                                      // || (cartaCoord[0]-1) porque: Se eu quero
                                                                      // sobreescrever a quinta carta, eu vou até a
                                                                      // quarta e começo a imprimir a quinta carta,
                                                                      // sobreescrevendo-a ||
                                                                      // *(maiorPalavraCaracteres+1) porque: avançar os
                                                                      // caracteres da carta para o lado, +1 por causa
                                                                      // dos espaços entre cada palavra
        allJogadoresCursorGoToXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4); // O mesmo para os jogadores LAN
        // Cursor no começo da palavra

        // Primeiro, tira os CARACTERE_ESCONDIDO(underlines) e sobreescreve para espaços
        // vazios
        System.out.printf(" ".repeat(maiorPalavraCaracteres));
        enviarTextoTodosJogadores(" ".repeat(maiorPalavraCaracteres), null, null);

        // Depois, volta para o começo da palavra
        gotoXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4);
        allJogadoresCursorGoToXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4);

        // Depois, imprime palavra normalmente
        Thread.sleep(400);
        System.out.printf(tabuleiro[y - 1][x - 1].cartaNome);
        enviarTextoTodosJogadores(tabuleiro[y - 1][x - 1].cartaNome, null, null);

        // Volta para o fim do tabuleiro(coluna do "cursorEstaNaFileira")
        gotoXY(cursorEstaNaFileira, 0);
        allJogadoresCursorGoToXY(cursorEstaNaFileira, 0);

    }

    public static void mostrarCartaSingleP(int x, int y) throws IOException, InterruptedException {
        Thread.sleep(400);
        if (x < 0 || y < 0)
            return;

        int objetivo = y + 1; // +1 porque: A primeira linha é o índice(primeira linha indica a coluna para
                              // o jogador)

        gotoXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4); // +4 porque: pula o Índice, que ocupa 3 posições
                                                                      // || (cartaCoord[0]-1) porque: Se eu quero
                                                                      // sobreescrever a quinta carta, eu vou até a
                                                                      // quarta e começo a imprimir a quinta carta,
                                                                      // sobreescrevendo-a ||
                                                                      // *(maiorPalavraCaracteres+1) porque: avançar os
                                                                      // caracteres da carta para o lado, +1 por causa
                                                                      // dos espaços entre cada palavra
        // Cursor no começo da palavra

        // Primeiro, tira os CARACTERE_ESCONDIDO(underlines) e sobreescreve para espaços
        // vazios
        System.out.printf(" ".repeat(maiorPalavraCaracteres));

        // Depois, volta para o começo da palavra
        gotoXY(objetivo, (x - 1) * (maiorPalavraCaracteres + 1) + 4);

        // Depois, imprime palavra normalmente
        Thread.sleep(400);
        System.out.printf(tabuleiro[y - 1][x - 1].cartaNome);

        // Volta para o fim do tabuleiro(coluna do "cursorEstaNaFileira")
        gotoXY(cursorEstaNaFileira, 0);

    }

    public static void checarFimDeJogo() {
        boolean todasViradas = true;
        for (Carta[] cartaV : tabuleiro) {
            for (Carta carta : cartaV) {
                if (!carta.virado) {
                    todasViradas = false;
                    break;
                }
            }
            if (!todasViradas)
                break;
        }

        if (todasViradas) {
            partidaRunning = false;
        }
    }

    public static void novaConexao(Socket jogadorConn, String senhaServer) {
        try {
            final String senha = senhaServer;
            // O que faz: Pede senha, se a partida começou impede o jogador, adiciona na
            // lista de jogadores com o nome próprio
            PrintWriter out = new PrintWriter(jogadorConn.getOutputStream(), true);

            // Autenticação de senhaInput da partida
            BufferedReader in = new BufferedReader(new InputStreamReader(jogadorConn.getInputStream()));
            String tentativaSenha = "";
            int tentativasRestantes = MAX_TENTATIVAS_SENHA;
            while (!tentativaSenha.equals(senha)) { // Não sai do loop sem a senha do server
                try {
                    if (jogadorConn.equals(null))
                        return; // Rage-Kitou

                    if (tentativasRestantes == 0) { // Limite tentativas para senha
                        out.printf("%d tentativas incorretas! Aguarde %d segundos...\n", MAX_TENTATIVAS_SENHA,
                                TEMPO_SEGUNDOS_ESPERA_LIMITETENTATIVAS);
                        Thread.sleep(TEMPO_SEGUNDOS_ESPERA_LIMITETENTATIVAS * 1000);
                        tentativasRestantes = MAX_TENTATIVAS_SENHA;
                    }

                    if (partidaRunning)
                        break;

                    out.printf("O servidor possui uma senha, por favor, insira-a: "); // Inserir senha
                    tentativasRestantes--;
                    tentativaSenha = in.readLine();
                    Thread.sleep(TEMPO_SEGUNDOS_ENTRE_SENHAS * 1000);
                } catch (InterruptedException ignore) {
                }
                ; // Mesmo com erro, não sai do loop sem a senha
            }
            if (partidaRunning) {
                out.printf("A partida já está em andamento.\n");
                return;
            }
            // Passou, então a senha confere

            // Adiciona jogador
            String name;
            while (true) {
                out.printf("Insira um username: ");
                name = in.readLine();

                if (partidaRunning)
                    break;

                if (descobrirNomeExiste(name) || name.isEmpty()) {
                    out.printf("Nome já está sendo utilizado, por favor, escolha outro.\n");
                } else
                    break;
            }

            if (partidaRunning) {
                out.printf("A partida já está em andamento.\n");
                return;
            }
            jogadores.add(new Jogador(jogadorConn, name, out, in));

            System.out.printf("Jogador %s adicionado! Jogadores jogando: (%d)\n", name, jogadores.size() + 1); // +1 por
                                                                                                               // causa
                                                                                                               // do
                                                                                                               // SERVER
                                                                                                               // que
                                                                                                               // não é
                                                                                                               // contabilizado
            out.printf("Bem-vindo! Você agora está jogando!\n");
            enviarTextoTodosJogadores("Jogador adicionado! Digam oi para " + name + "!\n", jogadorConn, name);

        } catch (IOException ignore) {
        }
    }

    public static void comecarPartidaSinglePlayer() throws IOException, InterruptedException {

        cursorEstaNaFileira = 1;

        int pontuacao = 0;

        limparTela();

        // Sneak Peak
        imprimirTabuleiroCompleto();

        Thread.sleep(TEMPO_SEGUNDOS_MOSTRAR_TABULEIRO * 1000);

        atualizarTabuleiroServer();

        partidaRunning = true;
        while (partidaRunning) {
            String[] inputsStr = new String[2];
            int[] cartasCoord = new int[4];
            int cartasValidas = 0;

            while (cartasValidas < 1) {
                System.out.printf("Sua vez! Escolha coordenadas X e coordenadas Y! Formato: X Y ou X1 Y1 X2 Y2\n");
                cursorEstaNaFileira++;
                Thread.sleep(400);

                inputsStr = TECLADO.nextLine().trim().split("\\s+");

                if (inputsStr.length < 2 || inputsStr.length > 4) {
                    System.out.printf("Coordenadas inseridas inválidas: %d entradas. Esperado: 2 ou 4 entradas.\n",
                            inputsStr.length);
                    cursorEstaNaFileira++;
                    Thread.sleep(400);
                    continue;
                }

                try {
                    cartasCoord[0] = Integer.parseInt(inputsStr[0]);
                    cartasCoord[1] = Integer.parseInt(inputsStr[1]);

                    if (cartasCoord[0] > tabuleiro[0].length || cartasCoord[1] > tabuleiro.length // Carta maior que
                                                                                                  // tabuleiro
                            || cartasCoord[0] <= 0 || cartasCoord[1] <= 0) { // Valor negativo
                        System.out.println("Carta fora de alcance.");
                        cursorEstaNaFileira++;
                        Thread.sleep(400);
                        continue;
                    }
                    if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado) { // Carta já foi virada
                        System.out.println("Carta já virada! Escolha outra.");
                        Thread.sleep(400);
                        cursorEstaNaFileira++;
                        continue;
                    }

                    cartasValidas++;
                } catch (NumberFormatException e) {
                    System.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                    cursorEstaNaFileira++;
                    Thread.sleep(400);
                    continue;
                }

                if (inputsStr.length == 4) {
                    try {
                        cartasCoord[2] = Integer.parseInt(inputsStr[2]);
                        cartasCoord[3] = Integer.parseInt(inputsStr[3]);

                        if (cartasCoord[2] <= 0 || cartasCoord[3] <= 0 // Valor negativo
                                || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3]) // Carta
                                                                                                            // repetida
                                || tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado) { // Carta já virada

                            continue; // Deixa passar (vai perguntar a segunda carta depois)
                        }

                        cartasValidas++;
                    } catch (NumberFormatException ignore) {
                    }
                }
            }

            while (cartasValidas < 2) {

                System.out.printf("Bom! Agora, a segunda carta!\n");
                cursorEstaNaFileira++;

                inputsStr = TECLADO.nextLine().trim().split("\\s+");

                if (inputsStr.length < 2) {
                    System.out.printf("Coordenadas inseridas inválidas: %d entradas. Esperado: 2 entradas.\n",
                            inputsStr.length);
                    cursorEstaNaFileira++;
                    Thread.sleep(400);
                    continue;
                }

                try {
                    cartasCoord[2] = Integer.parseInt(inputsStr[0]);
                    cartasCoord[3] = Integer.parseInt(inputsStr[1]);

                    if (cartasCoord[2] > tabuleiro[0].length || cartasCoord[3] > tabuleiro.length // Carta fora de
                                                                                                  // alcance
                            || cartasCoord[2] <= 0 || cartasCoord[3] <= 0) { // Valor negativo
                        System.out.println("Carta fora de alcance.");
                        cursorEstaNaFileira++;
                        Thread.sleep(400);
                        continue;
                    }
                    if (tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado // Carta já virada
                            || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3])) { // Carta
                                                                                                           // igual à
                                                                                                           // primeira
                                                                                                           // carta
                        System.out.println("Carta já virada! Escolha outra.");
                        Thread.sleep(400);
                        cursorEstaNaFileira++;
                        continue;
                    }

                    cartasValidas++;
                } catch (NumberFormatException e) {
                    System.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                    cursorEstaNaFileira++;
                }
            }
            // fim do input

            atualizarTabuleiroServer();

            mostrarCartaSingleP(cartasCoord[0], cartasCoord[1]);
            mostrarCartaSingleP(cartasCoord[2], cartasCoord[3]);

            Thread.sleep(400);
            // Se virou cartas iguais
            if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0]
                    - 1].cartaNome == tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].cartaNome) {
                tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado = true;
                tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado = true;
                pontuacao += 100;
            }

            checarFimDeJogo();

            atualizarTabuleiroServer();
        }

        limparTela();
        mudarCor(3);
        telinhafim();
        mudarCor(COR_PADRAO);
        System.out.printf("Sua pontuação: " + pontuacao + ".\n");

    }

    public static void comecarPartidaMultiPlayer() throws InterruptedException {
        String inputStr;
        char inputChr;
        String senhaInput = "";
        int pontuacaoServer = 0;

        // Criando Senha
        System.out.println("\nDeseja adicionar uma senha? (y/n)");
        while (true) {
            inputStr = TECLADO.nextLine();

            if (inputStr.isEmpty()) {
                System.out.printf("\nDeseja adicionar uma senha? (y/n)\n");
                continue;
            }

            inputChr = consertarEntrada(inputStr);

            if (!(inputChr == 'y' || inputChr == 'n')) {
                Thread.sleep(500);
                System.out.printf("\nA reposta precisa ser 'Y' ou 'N'.\n");
                continue;
            }

            break;
        }

        if (inputChr == 'y') {
            while (true) {
                System.out.printf("\nCrie uma senha para a partida: ");
                senhaInput = TECLADO.nextLine();
                System.out.printf("Confirme a senha: ");
                if (senhaInput.equals(TECLADO.nextLine())) {
                    System.out.println("Ok!");
                    break;
                } else
                    System.out.printf("\nNão coincidem.");
                Thread.sleep(1000);
            }
        }
        final String senha = senhaInput;
        // Senha criada

        // Constrói(Inicializa) lista de jogadores
        jogadores = new ArrayList<>();

        // Hospedando ↓
        System.out.println("\nHospedando partida na rede local LAN...");
        try (ServerSocket server = new ServerSocket(PORT)) {

            // REALMENTE passa a ouvir por conexões
            new Thread(() -> {
                while (!partidaRunning) {
                    try {
                        Socket client = server.accept();
                        new Thread(() -> {
                            novaConexao(client, senha);
                        }).start();
                    } catch (IOException ignore) {
                    }
                }
            }).start();

            // Feedback ao usuário servidor
            String meuIPPrivado = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(n -> java.util.Collections.list(n.getInetAddresses()).stream())
                    .filter(a -> a instanceof java.net.Inet4Address && a.getHostAddress().startsWith("192"))
                    .map(java.net.InetAddress::getHostAddress).findFirst().orElse("<Error>");

            System.out.printf(
                    "\nPartida hospedada em: %s:%s\nPara outros jogadores se conectarem, insiram 'nc %s %s' num terminal.\n",
                    meuIPPrivado, PORT, meuIPPrivado, PORT);
            Thread.sleep(2000);
            System.out.println();

            // Começar partida ao comando (enter)

            while (true) {
                Play();
                TECLADO.nextLine();
                System.out.printf("Tem certeza que deseja iniciar a partida? (y/N) ");

                inputChr = consertarEntrada(TECLADO.nextLine());
                if (inputChr == 'y')
                    break;
            }
            partidaRunning = true;

            // Partida começou
            cursorEstaNaFileira = 1;
            limparTela();
            limparTelaJogadores();

            // Sneak Peak
            imprimirEEnviarParaTodosTabuleiroCompleto();

            Thread.sleep(TEMPO_SEGUNDOS_MOSTRAR_TABULEIRO * 1000);

            atualizarTabuleiroJogadores();

            // Ordem para cada jogador jogar sua vez
            enumerarJogadores();

            int vez = 0, vezDo;
            int qntJogadores = jogadores.size() + 1; // +1 por causa do server
            int[] cartasCoord = new int[4];
            int cartasValidas;
            Jogador jogadorNaVez;
            String[] inputsStr;

            while (partidaRunning) {
                inputsStr = new String[2];
                cartasValidas = 0;
                vezDo = vez % qntJogadores;

                jogadorNaVez = null;
                for (Jogador jogador : jogadores) {
                    if (jogador.vez == vezDo) {
                        jogadorNaVez = jogador;
                    }
                }

                // Anuncia vez do jogador
                // se jogadorNaVez continuar "null", é vez do Server
                if (jogadorNaVez == null) {
                    enviarTextoTodosJogadores("Vez de Server. Aguarde sua vez.\n", null, null);
                } else {
                    enviarTextoTodosJogadores("Vez de " + jogadorNaVez.name + ". Aguarde sua vez.\n", jogadorNaVez.conn,
                            jogadorNaVez.name);
                    System.out.printf("Vez de " + jogadorNaVez.name + ". Aguarde sua vez.\n");
                }

                // Input do jogador
                if (jogadorNaVez == null) {
                    // Tabuleiro
                    // Tabuleiro
                    // Tabuleiro
                    // Print
                    // Resposta ←

                    while (cartasValidas < 1) {
                        System.out.printf(
                                "Sua vez! Escolha coordenadas X e coordenadas Y! Formato: X Y ou X1 Y1 X2 Y2\n");
                        cursorEstaNaFileira++;
                        Thread.sleep(400);

                        inputsStr = TECLADO.nextLine().trim().split("\\s+");

                        if (inputsStr.length < 2 || inputsStr.length > 4) {
                            System.out.printf(
                                    "Coordenadas inseridas inválidas: %d entradas. Esperado: 2 ou 4 entradas.\n",
                                    inputsStr.length);
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        try {
                            cartasCoord[0] = Integer.parseInt(inputsStr[0]);
                            cartasCoord[1] = Integer.parseInt(inputsStr[1]);

                            if (cartasCoord[0] > tabuleiro[0].length || cartasCoord[1] > tabuleiro.length // Carta maior
                                                                                                          // que
                                                                                                          // tabuleiro
                                    || cartasCoord[0] <= 0 || cartasCoord[1] <= 0) { // Valor negativo
                                System.out.println("Carta fora de alcance.");
                                cursorEstaNaFileira++;
                                Thread.sleep(400);
                                continue;
                            }
                            if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado) { // Carta já foi virada
                                System.out.println("Carta já virada! Escolha outra.");
                                Thread.sleep(400);
                                cursorEstaNaFileira++;
                                continue;
                            }

                            cartasValidas++;
                        } catch (NumberFormatException e) {
                            System.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        if (inputsStr.length == 4) {
                            try {
                                cartasCoord[2] = Integer.parseInt(inputsStr[2]);
                                cartasCoord[3] = Integer.parseInt(inputsStr[3]);

                                if (cartasCoord[2] <= 0 || cartasCoord[3] <= 0 // Valor negativo
                                        || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3]) // Carta
                                                                                                                    // repetida
                                        || tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado) { // Carta já
                                                                                                       // virada

                                    continue; // Deixa passar (vai perguntar a segunda carta depois)
                                }

                                cartasValidas++;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }

                    while (cartasValidas < 2) {

                        System.out.printf("Bom! Agora, a segunda carta!\n");
                        cursorEstaNaFileira++;

                        inputsStr = TECLADO.nextLine().trim().split("\\s+");

                        if (inputsStr.length < 2) {
                            System.out.printf("Coordenadas inseridas inválidas: %d entradas. Esperado: 2 entradas.\n",
                                    inputsStr.length);
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        try {
                            cartasCoord[2] = Integer.parseInt(inputsStr[0]);
                            cartasCoord[3] = Integer.parseInt(inputsStr[1]);

                            if (cartasCoord[2] > tabuleiro[0].length || cartasCoord[3] > tabuleiro.length // Carta fora
                                                                                                          // de alcance
                                    || cartasCoord[2] <= 0 || cartasCoord[3] <= 0) { // Valor negativo
                                System.out.println("Carta fora de alcance.");
                                cursorEstaNaFileira++;
                                Thread.sleep(400);
                                continue;
                            }
                            if (tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado // Carta já virada
                                    || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3])) { // Carta
                                                                                                                   // igual
                                                                                                                   // à
                                                                                                                   // primeira
                                                                                                                   // carta
                                System.out.println("Carta já virada! Escolha outra.");
                                Thread.sleep(400);
                                cursorEstaNaFileira++;
                                continue;
                            }

                            cartasValidas++;
                        } catch (NumberFormatException e) {
                            System.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                            cursorEstaNaFileira++;
                        }
                    }
                    // fim do input

                    atualizarTabuleiroServer();
                    atualizarTabuleiroJogadores();

                    mostrarCartaMultiP(cartasCoord[0], cartasCoord[1]);
                    mostrarCartaMultiP(cartasCoord[2], cartasCoord[3]);

                    Thread.sleep(400);
                    // Se virou cartas iguais
                    if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0]
                            - 1].cartaNome == tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].cartaNome) {
                        tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado = true;
                        tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado = true;
                        pontuacaoServer += 100;
                    } else {
                        vez++;
                    }

                } else {

                    while (cartasValidas < 1) {
                        jogadorNaVez.out.printf(
                                "Sua vez! Escolha coordenadas X e coordenadas Y! Formato: X Y ou X1 Y1 X2 Y2\n");
                        cursorEstaNaFileira++;

                        inputsStr = jogadorNaVez.in.readLine().trim().split("\\s+");

                        if (inputsStr.length < 2 || inputsStr.length > 4) {
                            jogadorNaVez.out.printf(
                                    "Coordenadas inseridas inválidas: %d entradas. Esperado: 2 ou 4 entradas.\n",
                                    inputsStr.length);
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        try {
                            cartasCoord[0] = Integer.parseInt(inputsStr[0]);
                            cartasCoord[1] = Integer.parseInt(inputsStr[1]);

                            if (cartasCoord[0] > tabuleiro[0].length || cartasCoord[1] > tabuleiro.length
                                    || cartasCoord[0] <= 0 || cartasCoord[1] <= 0) {
                                jogadorNaVez.out.println("Carta fora de alcance.");
                                cursorEstaNaFileira++;
                                Thread.sleep(400);
                                continue;
                            }
                            if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado) {
                                jogadorNaVez.out.println("Carta já virada! Escolha outra.");
                                Thread.sleep(400);
                                cursorEstaNaFileira++;
                                continue;
                            }

                            cartasValidas++;
                        } catch (NumberFormatException e) {
                            jogadorNaVez.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        // →TENTA← conseguir uma quarta carta, para adiantar o trabalho do jogador
                        if (inputsStr.length == 4) {
                            try {
                                cartasCoord[2] = Integer.parseInt(inputsStr[2]);
                                cartasCoord[3] = Integer.parseInt(inputsStr[3]);

                                if (cartasCoord[2] <= 0 || cartasCoord[3] <= 0 // Valor negativo
                                        || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3]) // Carta
                                                                                                                    // repetida
                                        || tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado) { // Carta já
                                                                                                       // virada
                                    continue; // Deixa passar (vai perguntar a segunda carta depois)
                                }

                                cartasValidas++;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }

                    while (cartasValidas < 2) {

                        jogadorNaVez.out.printf("Bom! Agora, a segunda carta!\n");
                        cursorEstaNaFileira++;

                        inputsStr = jogadorNaVez.in.readLine().trim().split("\\s+");

                        if (inputsStr.length < 2) {
                            jogadorNaVez.out.printf(
                                    "Coordenadas inseridas inválidas: %d entradas. Esperado: 2 entradas.\n",
                                    inputsStr.length);
                            cursorEstaNaFileira++;
                            Thread.sleep(400);
                            continue;
                        }

                        try {
                            cartasCoord[2] = Integer.parseInt(inputsStr[0]);
                            cartasCoord[3] = Integer.parseInt(inputsStr[1]);

                            if (cartasCoord[2] > tabuleiro[0].length || cartasCoord[3] > tabuleiro.length // Carta fora
                                                                                                          // de alcance
                                    || cartasCoord[2] <= 0 || cartasCoord[3] <= 0) { // Valor negativo
                                jogadorNaVez.out.println("Carta fora de alcance.");
                                cursorEstaNaFileira++;
                                Thread.sleep(400);
                                continue;
                            }
                            if (tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado // Carta já virada
                                    || (cartasCoord[0] == cartasCoord[2]) && (cartasCoord[1] == cartasCoord[3])) { // Carta
                                                                                                                   // igual
                                                                                                                   // à
                                                                                                                   // primeira
                                                                                                                   // carta
                                jogadorNaVez.out.println("Carta já virada! Escolha outra.");
                                Thread.sleep(400);
                                cursorEstaNaFileira++;
                                continue;
                            }

                            cartasValidas++;
                        } catch (NumberFormatException e) {
                            jogadorNaVez.out.println("Formato incorreto! Formato esperado(números naturais): X Y");
                            cursorEstaNaFileira++;
                        }
                    }

                    atualizarTabuleiroJogadores();

                    mostrarCartaMultiP(cartasCoord[0], cartasCoord[1]);
                    mostrarCartaMultiP(cartasCoord[2], cartasCoord[3]);

                    Thread.sleep(400);
                    // Se virou cartas iguais
                    if (tabuleiro[cartasCoord[1] - 1][cartasCoord[0]
                            - 1].cartaNome == tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].cartaNome) {
                        tabuleiro[cartasCoord[1] - 1][cartasCoord[0] - 1].virado = true;
                        tabuleiro[cartasCoord[3] - 1][cartasCoord[2] - 1].virado = true;
                        jogadorNaVez.points += 100;
                    } else {
                        vez++;
                    }

                }

                atualizarTabuleiroJogadores();

                // Checa
                checarFimDeJogo();

            }

            limparTelas();

            jogadores.sort(Comparator.comparingInt((Jogador j) -> j.points).reversed());

            int place = 1;
            boolean serverFoiImprimido = false; // Evitar que imprima a pontuação do servidor para cada jogador com
                                                // pontuação igual à do servidor

            for (Jogador jogadorRank : jogadores) {
                if (pontuacaoServer >= jogadorRank.points && !serverFoiImprimido) { // Server teve maior pontuação,
                                                                                    // então imprime ele primeiro, e só
                                                                                    // em seguida a pontuação do jogador
                                                                                    // (sim, o server tem prioridade no
                                                                                    // Ranking).
                    System.out.printf("%dst Place (%d points): %s\n", place, pontuacaoServer, "SERVER");
                    serverFoiImprimido = true;
                    place++;
                }
                System.out.printf("%dst Place (%d points): %s\n", place, jogadorRank.points, jogadorRank.name);
                place++;
            }
            if (!serverFoiImprimido) {
                System.out.printf("%dst Place (%d points): %s\n", place, pontuacaoServer, "SERVER");
            }

            for (Jogador jogadorToPrint : jogadores) {
                place = 1;
                serverFoiImprimido = false;

                for (Jogador jogadorRank : jogadores) {
                    if (pontuacaoServer >= jogadorRank.points && !serverFoiImprimido) { // Server teve maior pontuação,
                                                                                        // então imprime ele primeiro, e
                                                                                        // só em seguida a pontuação do
                                                                                        // jogador (sim, o server tem
                                                                                        // prioridade no Ranking).
                        jogadorToPrint.out.printf("%dst Place (%d points): %s\n", place, pontuacaoServer, "SERVER");
                        serverFoiImprimido = true;
                        place++;
                    }
                    jogadorToPrint.out.printf("%dst Place (%d points): %s\n", place, jogadorRank.points,
                            jogadorRank.name);
                    place++;
                }

                if (!serverFoiImprimido) {
                    jogadorToPrint.out.printf("%dst Place (%d points): %s\n", place, pontuacaoServer, "SERVER");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * ██ ██ ██████ ██████ ██ ██
     * ██ ██ ██████ ██████ ██ ██
     * ████ ████ ██ ██ ██ ████ ██
     * ████ ████ ██ ██ ██ ████ ██
     * ██ ██ ██ ██████████ ██ ██ ██ ██
     * ██ ██ ██ ██████████ ██ ██ ██ ██
     * ██ ██ ██ ██ ██ ██ ████
     * ██ ██ ██ ██ ██ ██ ████
     * ██ ██ ██ ██ ██████ ██ ██
     * ██ ██ ██ ██ ██████ ██ ██
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // OBS: os emojis de estrelinha não são de nenhuma I.A., elas são para demarcar
        // blocos importantes do main (primeiro pilar do pensamento computacional,
        // decomposição). Fonte: https://pt.piliapp.com/symbol/star/

        // ⭐Preparação (criando variáveis, etc...)
        int colunas = 0, fileiras = 0;

        List<Integer> listaIndexes; // Index(es)
        String[] listaPalavras; // Lista das palavras, como um dicionário ↓

        // Matriz de casas: palavra daquela carta + se ela já foi virada
        int[][] mascara; // Index(es) das palavras posicionadas, para depois as posicionar no tabuleiro
                         // conforme a máscara
        // Exemplo:
        /*
         * 
         * String[] dicionario = {"banana", "maçã", "pêra", "terra", "gorila",
         * "guarda-chuva", "alienígena", "verde", "adjetivo"}
         * 
         * mascara:
         * 1 4 8 8
         * 3 1 4 3
         * 5 5 6 2
         * 2 5 0 0
         * 
         * tabuleiro:
         * dicionario[1] dicionario[4] dicionario[8] dicionario[8]
         * dicionario[3] dicionario[1] dicionario[4] dicionario[3]
         * dicionario[5] dicionario[5] dicionario[6] dicionario[2]
         * dicionario[2] dicionario[5] dicionario[0] dicionario[0]
         */

        // Tela de start/bem-vindo

        mudarCor(3);
        telaInicial();
        mudarCor(COR_PADRAO);
        System.out.println();
        enter();

        TECLADO.nextLine();

        // 🌟Entradas
        Thread.sleep(500);
        mudarCor(15);
        System.out.print("\nComeçaremos criando um tamanho para nossa mesa de jogo.");
        mudarCor(COR_PADRAO);
        Thread.sleep(500);
        System.out.println();

        while (true) {
            while (true) {
                mudarCor(15);
                System.out.printf("Insira uma quantidade de fileiras: ");
                mudarCor(COR_PADRAO);
                try {
                    fileiras = Integer.parseInt(TECLADO.nextLine());
                    if (fileiras > 10) {
                        mudarCor(1);
                        System.out.println("Tamanho incompativel, digite novamente MAX 10");
                        mudarCor(COR_PADRAO);
                        Thread.sleep(400);
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    Thread.sleep(100);
                    mudarCor(1);
                    System.out.println("\nEntrada inválida. Números, por favor.\n");
                    mudarCor(COR_PADRAO);
                    Thread.sleep(600);
                }
            }

            while (true) {
                mudarCor(15);
                System.out.printf("Insira uma quantidade de colunas: ");
                mudarCor(COR_PADRAO);

                try {
                    colunas = Integer.parseInt(TECLADO.nextLine());
                    if (colunas > 10) {
                        mudarCor(1);
                        System.out.println("Tamanho incompativel, digite novamente MAX 10");
                        mudarCor(COR_PADRAO);
                        Thread.sleep(400);// tempe em milisegundo

                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    Thread.sleep(100);
                    mudarCor(1);
                    System.out.println("\nEntrada inválida. Números, por favor.\n");
                    mudarCor(COR_PADRAO);
                    Thread.sleep(600);
                }
            }

            if ((colunas * fileiras) % 2 == 0 && fileiras > 0 && colunas > 0) {
                break;
            } else {
                // Se tiver número ímpar de casas, nem todas as Cartas terão seu par
                // correspondente
                mudarCor(1);
                System.out.printf(
                        "\nAs peças que seriam utilizadas para criar um formato de %d:%d, serão de quantidade ÍMPAR (nem todas as Cartas terão um par correspondente) ou um formato de tamanho inválido foi inserido (zero ou negativo).\nInsira outro formato.\n",
                        fileiras, colunas);
                mudarCor(COR_PADRAO);
                System.out.println();
                Thread.sleep(2000);

            }
        }

        // 🌟Processamento e preparações para a partida iniciar
        mascara = new int[fileiras][colunas]; // Mascara base com números, para depois substituir cada número por uma
                                              // palavras/imagens correspondente

        // Preenchendo "listaIndexes"
        listaIndexes = new ArrayList<>(); // Lista com valores para as "Cartas" em pares (para depois distribuir
                                          // aleatoriamente pela matriz)
        int contador = 0;
        for (int i = 0; i < fileiras * colunas; i += 2) {
            listaIndexes.add(contador);
            listaIndexes.add(contador);
            contador++;
        }
        // resultado esperado: {0, 0, 1, 1, 2, 2, 3, 3, 4, 4, ...}

        // Preenchendo "listaPalavras"
        listaPalavras = new String[listaIndexes.size() / 2];

        List<String> linhas;
        try {
            linhas = Files.readAllLines(Paths.get("dicionario.txt")); // Carrega arquivo de palavras
            linhas.get(listaPalavras.length); // Quick-check para checar a integridade(mínima, mesmo que parcial) do
                                              // arquivo. (se não possui o mínimo de linhas necessário, problema.)
        } catch (IndexOutOfBoundsException | IOException e) {
            mudarCor(1);
            System.out.println(
                    "Arquivo de dicionário não encontrado ou muito pequeno, por favor, reinstale o jogo ou crie um dicionário.");
            mudarCor(COR_PADRAO);
            return;
        }

        String palavra;
        for (int i = 0; i < listaPalavras.length; i++) {
            palavra = linhas.get(pegarNumeroAleatorio(1, linhas.size() - 1)); // Palavra aleatória do dicionário

            // Checa se já existe na listaPalavras
            if (devolverExistenciaDeCorrespondencia(listaPalavras, palavra)) {
                // Sim, Refaz
                i--;
                continue;
            }
            // Passou, não existe

            listaPalavras[i] = palavra;
        }

        // Distribuindo Indexes na matrix
        int index;
        for (int i = 0; i < mascara.length; i++) {
            for (int j = 0; j < mascara[0].length; j++) { // Para cada posição na matriz...

                index = pegarNumeroAleatorio(0, listaIndexes.size() - 1); // Pega valor aleatório da "listaIndexes"

                mascara[i][j] = listaIndexes.get(index); // Posiciona
                listaIndexes.remove(index); // Remove da lista para não ser repetido
            }
        }
        // Mascara feita

        // Preencher tabuleiro real
        tabuleiro = new Carta[fileiras][colunas];
        for (int i = 0; i < tabuleiro.length; i++) {
            for (int j = 0; j < tabuleiro[0].length; j++) {
                tabuleiro[i][j] = new Carta(
                        listaPalavras[mascara[i][j]]);
            }
        }
        // ✨Tabuleiro pronto, pronto para jogar.

        // Mas... Single Player ou Multi Player?...

        int escolhaSPMP; // (SP = Single Player, MP = Multi Player)
        while (true) {
            while (true) {
                mudarCor(2);
                System.out.printf("\nQuer jogar single-player ou multi-player?\n1. Single-Player\n2. Multi-Player.\n");
                mudarCor(COR_PADRAO);
                try {
                    escolhaSPMP = Integer.parseInt(TECLADO.nextLine());
                    break;
                } catch (NumberFormatException e) {
                    Thread.sleep(700);
                    mudarCor(1);
                    System.out.println("\nEntrada inválida. Números, por favor.");
                    mudarCor(COR_PADRAO);
                    Thread.sleep(1000);
                }
            }

            if (escolhaSPMP > 2 || escolhaSPMP < 1) {
                mudarCor(1);
                System.out.println("\nSua resposta precisa ser '1' ou '2'!");
                mudarCor(COR_PADRAO);
            } else {
                break;
            }
            Thread.sleep(300);

        }

        switch (escolhaSPMP) {
            case (1):
                comecarPartidaSinglePlayer();
                break;

            case (2):
                comecarPartidaMultiPlayer();
                break;

            default:
        }
    }

}
