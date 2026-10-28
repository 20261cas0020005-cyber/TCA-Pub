import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

class Carta {
    String cartaNome;
    boolean virado = false;

    public Carta(String cartaNome) {
        this.cartaNome = cartaNome;
    }
}

class Jogador {
    Socket conn;
    PrintWriter out;
    BufferedReader in;
    String name;
    int vez;
    int points = 0;

    public Jogador(Socket conn, String name, PrintWriter out, BufferedReader in) throws IOException {
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

    public static final Scanner TECLADO = new Scanner(System.in);
    public static Carta[][] tabuleiro;
    public static List<Jogador> jogadores;
    public static boolean partidaRunning = false;
    public static int cursorEstaNaFileira;
    public static int maiorPalavraCaracteres;

    public static void imprimir(String texto) {
        System.out.println(texto);
    }

    public static void fim() {
        System.out.print("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
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

    // Métodos do Ló

    public static int pegarNumeroAleatorio(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static void gotoXY(int linha, int coluna) {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df", escCode, linha, coluna));
    }

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

    public static void jogadorGotoXY(Jogador jogador, int linha, int coluna) {
        char escCode = 0x1B;
        jogador.out.print(String.format("%c[%d;%df", escCode, linha, coluna));
        jogador.out.flush();
    }

    public static void allJogadoresGotoXY(int linha, int coluna) {
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

    public static void imprimirMatriz(int[][] matriz) {
        if (matriz == null)
            return;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                System.out.printf("%d ", matriz[i][j]);
            }
            System.out.println();
            cursorEstaNaFileira++;
        }
    }

    public static void imprimirTabuleiroAtual() {
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(tabuleiro);

        // Indices
        System.out.printf("  ");
        for (int i = 0; i < tabuleiro[0].length; i++) {
            System.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                    + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
        System.out.println();
        cursorEstaNaFileira++;

        for (int i = 0; i < tabuleiro.length; i++) {
            System.out.printf("%d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {
                if (tabuleiro[i][j].virado) {
                    System.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                } else {
                    for (int k = 0; k < maiorPalavraCaracteres; k++) {
                        System.out.printf("_");
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
            System.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                    + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
        System.out.println();
        cursorEstaNaFileira++;

        for (int i = 0; i < tabuleiro.length; i++) {
            System.out.printf("%d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {
                System.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
            }
            System.out.println();
            cursorEstaNaFileira++;
        }
    }

    public static void imprimirMatrizEscondida(Carta[][] matriz) {
        if (matriz == null)
            return;
        if (maiorPalavraCaracteres == 0)
            maiorPalavraCaracteres = pegarQntCaracteresMaiorPalavra(matriz);

        // Indices
        System.out.printf("  ");
        for (int i = 0; i < tabuleiro[0].length; i++) {
            System.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                    + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
        System.out.println();
        cursorEstaNaFileira++;

        for (int i = 0; i < matriz.length; i++) {
            System.out.printf("%d ", i + 1);
            for (int j = 0; j < matriz[0].length; j++) {
                // System.out.printf( "%-*s", maiorPalavraCaracteres+1, matriz[i][j].cartaNome
                // ); // OLD VERSION (descobri que só funciona em C e C++ ._. )
                for (int k = 0; k < maiorPalavraCaracteres; k++)
                    System.out.printf("_");
                System.out.printf(" ");
            }
            System.out.println();
            cursorEstaNaFileira++;
        }
    }

    public static char converterStringToCharSanityzed(String texto) {
        String sanityzedText = texto.trim().toLowerCase();

        if (sanityzedText.isEmpty()) {
            return ' ';
        }
        return sanityzedText.charAt(0);
    }

    public static boolean descobrirConnExiste(Socket socket) {
        for (Jogador jogador : jogadores) {
            if (jogador.conn.equals(socket))
                return true;
        }
        return false;
    }

    public static boolean descobrirNomeExiste(String name) {
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

    public static void sendAllConnectedPlayers(String text, Socket exceptSocket, String exceptName) throws IOException {
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
                jogador.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                        + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
                jogador.out.printf("%d ", i + 1); // Index
                for (int j = 0; j < tabuleiro[0].length; j++) {

                    if (tabuleiro[i][j].virado) {
                        jogador.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                    } else {
                        for (int k = 0; k < maiorPalavraCaracteres; k++) {
                            jogador.out.printf("_");
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
            jogador.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                    + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
            jogador.out.printf("%d ", i + 1); // Index
            for (int j = 0; j < tabuleiro[0].length; j++) {

                if (tabuleiro[i][j].virado) {
                    jogador.out.printf("%-" + (maiorPalavraCaracteres + 1) + "s", tabuleiro[i][j].cartaNome);
                } else {
                    for (int k = 0; k < maiorPalavraCaracteres; k++) {
                        jogador.out.printf("_");
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
                jogador.out.printf(" ".repeat((int) Math.floor(maiorPalavraCaracteres / 2)) + (i + 1)
                        + " ".repeat(maiorPalavraCaracteres / 2) + " ".repeat(maiorPalavraCaracteres % 1 > 0 ? 1 : 0)); // Output:
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
                jogador.out.printf("%d ", i + 1); // Index
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
            jogadorGotoXY(jogador, i, 0);

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

            for (int j = 0; j < (maiorPalavraCaracteres + 1) * tabuleiro[0].length * 2; j++) {
                System.out.printf(" ");
            }
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
                jogadorGotoXY(jogador, i, 0);

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

    public static void mostrarCartaMultiP(int[] cartaCoord) throws IOException, InterruptedException {
        Thread.sleep(400);
        if (cartaCoord == null)
            return;

        int objetivo = cartaCoord[1] + 1; // +1 porque: A primeira linha é o índice(primeira linha indica a coluna para
                                          // o jogador)

        // ToDo: Índice pode ocupar mais de 2 posições
        gotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3); // +3 porque: pula o Índice, que ocupa
                                                                                  // 2 posições || (cartaCoord[0]-1)
                                                                                  // porque: Se eu quero sobreescrever a
                                                                                  // quinta carta, eu vou até a quarta e
                                                                                  // começo a imprimir a quinta carta,
                                                                                  // sobreescrevendo-a ||
                                                                                  // *(maiorPalavraCaracteres+1) porque:
                                                                                  // avançar os caracteres da carta para
                                                                                  // o lado, +1 por causa dos espaços
                                                                                  // entre cada palavra
        allJogadoresGotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3); // O mesmo para os
                                                                                              // jogadores LAN
        // Cursor no começo da palavra

        // Primeiro, tira os "_"(underlines) e sobreescreve para espaços vazios
        for (int j = 0; j < maiorPalavraCaracteres; j++) {
            System.out.printf(" ");
            sendAllConnectedPlayers(" ", null, null);
        }
        // Depois, volta para o começo da palavra
        gotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3);
        allJogadoresGotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3);

        // Depois, imprime palavra normalmente
        Thread.sleep(400);
        System.out.printf(tabuleiro[cartaCoord[1] - 1][cartaCoord[0] - 1].cartaNome);
        sendAllConnectedPlayers(tabuleiro[cartaCoord[1] - 1][cartaCoord[0] - 1].cartaNome, null, null);

        // Volta para o fim do tabuleiro(coluna do "cursorEstaNaFileira")
        gotoXY(cursorEstaNaFileira, 0);
        allJogadoresGotoXY(cursorEstaNaFileira, 0);

    }

    public static void mostrarCartaSingleP(int[] cartaCoord) throws IOException, InterruptedException {
        Thread.sleep(400);
        if (cartaCoord == null)
            return;

        int objetivo = cartaCoord[1] + 1; // +1 porque: A primeira linha é o índice(primeira linha indica a coluna para
                                          // o jogador)

        // ToDo: Índice pode ocupar mais de 2 posições
        gotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3); // +3 porque: pula o Índice, que ocupa
                                                                                  // 2 posições || (cartaCoord[0]-1)
                                                                                  // porque: Se eu quero sobreescrever a
                                                                                  // quinta carta, eu vou até a quarta e
                                                                                  // começo a imprimir a quinta carta,
                                                                                  // sobreescrevendo-a ||
                                                                                  // *(maiorPalavraCaracteres+1) porque:
                                                                                  // avançar os caracteres da carta para
                                                                                  // o lado, +1 por causa dos espaços
                                                                                  // entre cada palavra
        // Cursor no começo da palavra

        // Primeiro, tira os "_"(underlines) e sobreescreve para espaços vazios
        for (int j = 0; j < maiorPalavraCaracteres; j++) {
            System.out.printf(" ");
        }

        // Depois, volta para o começo da palavra
        gotoXY(objetivo, (cartaCoord[0] - 1) * (maiorPalavraCaracteres + 1) + 3);

        // Depois, imprime palavra normalmente
        Thread.sleep(400);
        System.out.printf(tabuleiro[cartaCoord[1] - 1][cartaCoord[0] - 1].cartaNome);

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
            sendAllConnectedPlayers("Jogador adicionado! Digam oi para " + name + "!\n", jogadorConn, name);

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
            int[] cartaCoord1 = new int[2], cartaCoord2 = new int[2];
            System.out.printf("Escolha coordenadas X e coordenadas Y para a primeira carta! Formato: X Y\n");
            Thread.sleep(400);

            while (true) {
                while (true) {
                    inputsStr[0] = TECLADO.next();
                    inputsStr[1] = TECLADO.next();
                    try {
                        cartaCoord1[0] = Integer.parseInt(inputsStr[0]);
                        cartaCoord1[1] = Integer.parseInt(inputsStr[1]);
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Formato incorreto! Formato(números naturais): X Y");
                        sendAllConnectedPlayers("0x1B[%20;%20f", null, null);
                        cursorEstaNaFileira++;
                    }
                }

                atualizarTabuleiroServer();
                if (cartaCoord1[0] > tabuleiro[0].length || cartaCoord1[1] > tabuleiro.length || cartaCoord1[0] < 0
                        || cartaCoord1[1] < 0) {
                    System.out.println("Carta fora de alcance.");
                    cursorEstaNaFileira++;
                    continue;
                }
                if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado) {
                    System.out.println("Carta já virada! Escolha outra.");
                    cursorEstaNaFileira++;
                } else
                    break;
            }
            System.out.printf("Bom! Agora, a segunda carta!\n");
            while (true) {
                while (true) {
                    inputsStr[0] = TECLADO.next();
                    inputsStr[1] = TECLADO.next();
                    try {
                        cartaCoord2[0] = Integer.parseInt(inputsStr[0]);
                        cartaCoord2[1] = Integer.parseInt(inputsStr[1]);
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Formato incorreto! Formato(números naturais): X Y");
                        cursorEstaNaFileira++;
                    }
                }

                atualizarTabuleiroServer();
                if (cartaCoord2[0] > tabuleiro[0].length || cartaCoord2[1] > tabuleiro.length || cartaCoord2[0] < 0
                        || cartaCoord2[1] < 0) {
                    System.out.println("Carta fora de alcance.");
                    cursorEstaNaFileira++;
                    continue;
                }
                if (tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado
                        || (cartaCoord1[0] == cartaCoord2[0]) && (cartaCoord1[1] == cartaCoord2[1])) {
                    System.out.println("Carta já virada! Escolha outra.");
                    cursorEstaNaFileira++;
                } else
                    break;
            }
            // fim do input

            atualizarTabuleiroServer();

            mostrarCartaSingleP(cartaCoord1);
            mostrarCartaSingleP(cartaCoord2);

            Thread.sleep(400);
            // Se virou cartas iguais
            if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0]
                    - 1].cartaNome == tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].cartaNome) {
                tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado = true;
                tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado = true;
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

            inputChr = converterStringToCharSanityzed(inputStr);

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
                        novaConexao(client, senha);
                    } catch (IOException ignore) {
                    }
                }
            }).start();

            // Feedback ao usuário servidor
            String meuIPPrivado = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(i -> java.util.Collections.list(i.getInetAddresses()).stream())
                    .filter(a -> a instanceof java.net.Inet4Address && a.isSiteLocalAddress())
                    .map(a -> a.getHostAddress()).findFirst().orElse("Falha ao procurar IP.");

            System.out.printf(
                    "\nPartida hospedada em: %s:%s\nPara outros jogadores se conectarem, insiram 'nc %s %s' num terminal.",
                    meuIPPrivado, PORT, meuIPPrivado, PORT);
            Thread.sleep(2000);

            // Começar partida ao comando (enter)

            while (true) {
                System.out.printf("\n\nPara começar a partida, apenas... aperte Enter e confirme.\n");
                TECLADO.nextLine();
                System.out.printf("Tem certeza que deseja iniciar a partida? (y/N) ");

                inputChr = converterStringToCharSanityzed(TECLADO.nextLine());
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
            int[] cartaCoord1 = new int[2];
            int[] cartaCoord2 = new int[2];
            Jogador jogadorNaVez;
            String[] inputsStr = new String[2];

            while (partidaRunning) {
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
                    sendAllConnectedPlayers("Vez de Server. Aguarde sua vez.\n", null, null);
                } else {
                    sendAllConnectedPlayers("Vez de " + jogadorNaVez.name + ". Aguarde sua vez.\n", jogadorNaVez.conn,
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

                    System.out.printf(
                            "Sua vez! Escolha coordenadas X e coordenadas Y para a primeira carta! Formato: X Y\n");
                    Thread.sleep(400);
                    while (true) {
                        while (true) {
                            inputsStr[0] = TECLADO.next();
                            inputsStr[1] = TECLADO.next();
                            try {
                                cartaCoord1[0] = Integer.parseInt(inputsStr[0]);
                                cartaCoord1[1] = Integer.parseInt(inputsStr[1]);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Formato incorreto! Formato(números naturais): X Y");
                                cursorEstaNaFileira++;
                            }
                        }

                        atualizarTabuleiroServer();

                        if (cartaCoord1[0] > tabuleiro[0].length || cartaCoord1[1] > tabuleiro.length
                                || cartaCoord1[0] < 0 || cartaCoord1[1] < 0) {
                            System.out.println("Carta fora de alcance.");
                            cursorEstaNaFileira++;
                            continue;
                        }
                        if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado) {
                            System.out.println("Carta já virada! Escolha outra.");
                            cursorEstaNaFileira++;
                        } else
                            break;
                    }

                    System.out.printf("Bom! Agora, a segunda carta!\n");

                    while (true) {
                        while (true) {
                            inputsStr[0] = TECLADO.next();
                            inputsStr[1] = TECLADO.next();
                            try {
                                cartaCoord2[0] = Integer.parseInt(inputsStr[0]);
                                cartaCoord2[1] = Integer.parseInt(inputsStr[1]);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Formato incorreto! Formato(números naturais): X Y");
                                cursorEstaNaFileira++;
                            }
                        }
                        atualizarTabuleiroServer();
                        if (cartaCoord2[0] > tabuleiro[0].length || cartaCoord2[1] > tabuleiro.length
                                || cartaCoord2[0] < 0 || cartaCoord2[1] < 0) {
                            System.out.println("Carta fora de alcance.");
                            cursorEstaNaFileira++;
                            continue;
                        }

                        if (tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado
                                || (cartaCoord1[0] == cartaCoord2[0]) && (cartaCoord1[1] == cartaCoord2[1])) {
                            System.out.println("Carta já virada! Escolha outra.");
                            cursorEstaNaFileira++;
                        } else
                            break;

                    }
                    // fim do input

                    atualizarTabuleiroJogadores();

                    mostrarCartaMultiP(cartaCoord1);
                    mostrarCartaMultiP(cartaCoord2);

                    Thread.sleep(400);
                    // Se virou cartas iguais
                    if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0]
                            - 1].cartaNome == tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].cartaNome) {
                        tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado = true;
                        tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado = true;
                        pontuacaoServer += 100;
                    } else {
                        vez++;
                    }

                } else {
                    String[] inputCoordStr = new String[0];

                    jogadorNaVez.out.printf(
                            "Sua vez! Escolha coordenadas X e coordenadas Y para a primeira carta! Formato: X Y\n");
                    while (true) {
                        while (true) {
                            try {
                                try {
                                    inputCoordStr = jogadorNaVez.in.readLine().trim().split("\\s+");
                                } catch (IOException e) {
                                    jogadorNaVez.out.printf(
                                            "Ocorreu um erro com seu input, re-insira-o. Lembre-se: Apenas números naturais no formato: X Y\n");
                                    continue;
                                }
                                if (inputCoordStr.length < 2)
                                    continue;

                                cartaCoord1[0] = Integer.parseInt(inputCoordStr[0]);
                                cartaCoord1[1] = Integer.parseInt(inputCoordStr[1]);
                                break;
                            } catch (NumberFormatException e) {
                                jogadorNaVez.out.printf(
                                        "Ocorreu um erro com seu input, re-insira-o. Lembre-se: Apenas números naturais no formato: X Y\n");
                            }
                        }

                        limparTela(jogadorNaVez);
                        enviarTabuleiroAtual(jogadorNaVez);

                        if (cartaCoord1[0] > tabuleiro[0].length || cartaCoord1[1] > tabuleiro.length
                                || cartaCoord1[0] < 0 || cartaCoord1[1] < 0) {
                            jogadorNaVez.out.println("Carta fora de alcance.");
                            cursorEstaNaFileira++;
                            continue;
                        }

                        if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado) {
                            jogadorNaVez.out.println("Carta já virada! Escolha outra.");
                            cursorEstaNaFileira++;
                        } else
                            break;
                    }

                    jogadorNaVez.out.printf("Bom! Agora, a segunda carta!\n");

                    while (true) {
                        while (true) {
                            try {
                                try {
                                    inputCoordStr = jogadorNaVez.in.readLine().trim().split("\\s+");
                                } catch (IOException e) {
                                    jogadorNaVez.out.printf(
                                            "Ocorreu um erro com seu input, re-insira-o. Lembre-se: Apenas números naturais no formato: X Y\n");
                                    continue;
                                }
                                if (inputCoordStr.length < 2)
                                    continue;
                                cartaCoord2[0] = Integer.parseInt(inputCoordStr[0]);
                                cartaCoord2[1] = Integer.parseInt(inputCoordStr[1]);
                                break;
                            } catch (NumberFormatException e) {
                                jogadorNaVez.out.printf(
                                        "Ocorreu um erro com seu input, re-insira-o. Lembre-se: Apenas números naturais no formato: X Y");
                            }
                        }

                        limparTela(jogadorNaVez);
                        enviarTabuleiroAtual(jogadorNaVez);

                        if (cartaCoord2[0] > tabuleiro[0].length || cartaCoord2[1] > tabuleiro.length
                                || cartaCoord2[0] < 0 || cartaCoord2[1] < 0) {
                            jogadorNaVez.out.println("Carta fora de alcance.");
                            cursorEstaNaFileira++;
                            continue;
                        }

                        if (tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado
                                || (cartaCoord1[0] == cartaCoord2[0]) && (cartaCoord1[1] == cartaCoord2[1])) {
                            jogadorNaVez.out.println("Carta já virada! Escolha outra.");
                            cursorEstaNaFileira++;
                        } else
                            break;
                    }
                    // fim do input

                    atualizarTabuleiroJogadores();

                    mostrarCartaMultiP(cartaCoord1);
                    mostrarCartaMultiP(cartaCoord2);

                    Thread.sleep(400);
                    // Se virou cartas iguais
                    if (tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0]
                            - 1].cartaNome == tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].cartaNome) {
                        tabuleiro[cartaCoord1[1] - 1][cartaCoord1[0] - 1].virado = true;
                        tabuleiro[cartaCoord2[1] - 1][cartaCoord2[0] - 1].virado = true;
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

            // ToDo: Melhorar final de partida(Ascii, melhor jogador da partida, som de
            // vitória)

            System.out.printf("Sua pontuação: " + pontuacaoServer + ".\n");
            for (Jogador jogador : jogadores) {
                jogador.out.printf("Sua pontuação: " + jogador.points + ".\n");
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
        TECLADO.nextLine();

        // 🌟Entradas
        Thread.sleep(500);
        mudarCor(15);
        System.out.println("\nComeçaremos criando um tamanho para nossa mesa de jogo.");
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
                    break;
                } catch (NumberFormatException e) {
                    Thread.sleep(100);
                    mudarCor(1);
                    System.out.println("\nEntrada inválida. Números, por favor.\n");
                    mudarCor(COR_PADRAO);
                    Thread.sleep(600);
                }
            }

            System.out.println();

            while (true) {
                mudarCor(15);
                System.out.printf("Insira uma quantidade de colunas: ");
                mudarCor(COR_PADRAO);
                try {
                    colunas = Integer.parseInt(TECLADO.nextLine());
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
            linhas.get(listaPalavras.length - 1); // Quick-check para checar a integridade(mínima, mesmo que parcial) do
                                                  // arquivo. (se não possui o mínimo de linhas necessário, problema.)
        } catch (IOException e) {
            mudarCor(1);
            System.out.println(
                    "Arquivo de dicionário não encontrado ou muito pequeno, por favor, reinstale o jogo ou crie um dicionário.");
            mudarCor(COR_PADRAO);
            return;
        }

        String palavra;
        for (int i = 0; i < listaPalavras.length; i++) {
            palavra = linhas.get(pegarNumeroAleatorio(0, linhas.size() - 1)); // Palavra aleatória do dicionário

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
                tabuleiro[i][j] = new Carta(listaPalavras[mascara[i][j]]);
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

            if (escolhaSPMP > 2 || escolhaSPMP < 1){
                mudarCor(1);
                System.out.println("\nSua resposta precisa ser '1' ou '2'!");
                mudarCor(COR_PADRAO);
            }else
                break;
            Thread.sleep(300);
        

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
}
