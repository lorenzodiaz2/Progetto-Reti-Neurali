import java.security.SecureRandom;

public class Div2 {
  private final static double EPSILON = 0.001;
  private static final int m = 80;
  private static final int n = 12;
  private static final int h = 8;
  private static final SecureRandom random = new SecureRandom();

  public static void main(String[] args) {

    //inizializza esempi
    int[] esempi = new int[m];
    costruisciEsempi(esempi);

    //inizializza l' array di esempi in binario
    int[][] esempiBinari = new int[m][n];
    costruisciEsempiBinari(esempiBinari, esempi);

    //inizializza le soluzioni
    int[] soluzioni = new int[m];
    costruisciSoluzioniDiv2(esempi, soluzioni);

    //Inizializza pesi w e W (valori Gaussiani)
    //per w, ogni riga rappresenta un input diverso e le colonne rappresentano in quale neurone nello strato intermedio va quell'input,
    //ad es. w[1][3] è il valore dell'input x1 verso il neurone TRE nello strato intermedio
    double[][] w = new double[n][h];
    double[] W = new double[h];
    costruisciPesi(w);
    costruisciPesiIntermedi(W);

    System.out.printf("%s%d%n%s%d%n%s%d%n%n", "numero di esempi = ", m, "numero di input = ", n, "numero di neuroni hidden = ", h);
    System.out.printf("%s%n%n", "L' apprendimento ha generato i seguenti risultati: ");

    apprendimento(esempiBinari, w, W, soluzioni);

    stampaRisultati(esempiBinari, esempi, soluzioni, w, W);

    //OSSERVAZIONE
    //il numero di bit dell'insieme test deve essere lo stesso di quello dell'insieme degli esempi perché i pesi
    //sono tanti quanti gli input e gli input sono il numero di bit, se il numero di bit dell'insieme test fosse
    //diverso allora l'operazioni di moltiplicazione tra matrici tra matrice test e matrice pesi non funzionerebbe

    //per la grandezza dell'insieme test, questa deve essere il massimo numero che poteva uscire per l'insieme esempi
    //e il numero di esempi, ad esempio se m=10 e max=16 (quindi genero numeri al massimo fino a 15) e se l' array di esempi
    //fosse esempi = [0,1,2,3,4,5,6,7,8,9] allora per l'OSSERVAZIONE sopra l'insieme di test può al massimo raggiungere
    //il valore 16 (escluso) ma dato che l'insieme test deve essere diverso dall'insieme esempi allora devo considerarne
    //il complementare, quindi dovrà essere test = [10, 11, 12, 13, 14, 15] di lunghezza max - m = 16 - 10 = 6

    int f = (int) Math.pow(2, n) - m;

    //inizializza l' array di test
    int[] test = new int[f];
    costruisciTest(test, esempi);

    //inizializza l' array di test in binario
    int[][] testBinari = new int[f][n];
    costruisciTestBinari(testBinari, test);

    int[] soluzioniTest = new int[f];
    costruisciSoluzioniDiv2(test, soluzioniTest);

    System.out.printf("%n%n%s%n%n", "L'insieme dei test ha generato i seguenti risultati:");
    stampaRisultatiTest(testBinari, test, soluzioniTest, w, W);

  }

  public static void costruisciEsempi(int[] a) {
    for (int i = 0; i < a.length; i++) {
      a[i] = random.nextInt((int) Math.pow(2, n));
      if (!verifica(i, a)) i--;
    }

  }

  public static boolean verifica(int i, int[] a) {

    for (int j = 0; j < i; j++) {
      if (a[j] == a[i]) return false;
    }
    return true;
  }

  public static void stampaRisultatiTest(int[][] testBinari, int[] test, int[] soluzioniTest, double[][] w, double[] W) {
    int cont = 0;
    double[] y = new double[h];

    for (int k = 0; k < testBinari.length; k++) {
      System.out.print(test[k] + " :  [");
      for (int j = 0; j < testBinari[0].length; j++) {
        System.out.print(testBinari[k][j] + "   ");
      }
      System.out.print(" ]      soluzione desiderata:  " + soluzioniTest[k] + "    ");

      for (int j = 0; j < h; j++) {
        y[j] = calcola_y(testBinari, w, k, j);
      }
      double Y = calcola_Y(W, y);

      System.out.print("Y = " + Y + "    Risultato test :  " + ((Math.abs(soluzioniTest[k] - Y) > 0.5) ? "ERRATO" : "GIUSTO"));
      if ((Math.abs(soluzioniTest[k] - Y) > 0.5)) cont++;
      System.out.println();
    }

    System.out.printf("%n%s%d%s%d%s%f%s", "Su ", testBinari.length, " esempi di test ci sono stati ", cont, " errori con percentuale di errore ", (float) 100 * cont / testBinari.length, " %");
  }

  public static void stampaRisultati(int[][] esempiBinari, int[] esempi, int[] soluzioni, double[][] w, double[] W) {
    double[] y = new double[h];

    for (int k = 0; k < esempiBinari.length; k++) {
      System.out.print(esempi[k] + " :  [");
      for (int j = 0; j < esempiBinari[0].length; j++) {
        System.out.print(esempiBinari[k][j] + "   ");
      }
      System.out.print(" ]    soluzione desiderata:  " + soluzioni[k] + "   ");

      for (int j = 0; j < h; j++) {
        y[j] = calcola_y(esempiBinari, w, k, j);
      }
      double Y = calcola_Y(W, y);

      System.out.print("Y = " + Y);

      System.out.println();
    }
  }

  public static void apprendimento(int[][] esempiBinari, double[][] w, double[] W, int[] soluzioni) {

    for (int i = 0; i < Integer.MAX_VALUE; i++) {

      double E = errore(w, W, esempiBinari, soluzioni);

      if (i % 1000 == 0) System.out.println("i = " + i + ", E = " + E);

      if (E < 0.2 || i > 1000000) {
        System.out.println("i = " + i + ", E = " + E);
        break;
      }

      modificaPesi(w, W, esempiBinari, soluzioni);
      modificaPesiIntermedi(w, W, esempiBinari, soluzioni);
    }

  }

  public static double errore(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni) {
    double errore = 0;
    double[] y = new double[h];

    for (int z = 0; z < soluzioni.length; z++) {
      for (int k = 0; k < W.length; k++) {
        y[k] = calcola_y(esempiBinari, w, z, k);
      }
      errore += Math.pow((soluzioni[z] - calcola_Y(W, y)), 2);
    }
    return errore / 2;
  }

  public static void modificaPesiIntermedi(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni) {

    for (int i = 0; i < W.length; i++) {
      W[i] += EPSILON * calcolaSomma_W(w, W, esempiBinari, soluzioni, i);
    }
  }

  public static double calcolaSomma_W(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni, int j) {
    double somma = 0;
    double[] y = new double[h];

    for (int z = 0; z < m; z++) {
      for (int k = 0; k < W.length; k++) {
        y[k] = calcola_y(esempiBinari, w, z, k);
      }
      double Y = calcola_Y(W, y);
      somma += (soluzioni[z] - Y) * (1 - Math.pow(Y, 2)) * y[j];
    }
    return somma;
  }

  public static void modificaPesi(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni) {

    for (int i = 0; i < w.length; i++) {
      for (int j = 0; j < w[0].length; j++) {
        w[i][j] += EPSILON * calcolaSomma_w(w, W, esempiBinari, soluzioni, i, j);
      }
    }
  }

  public static double calcolaSomma_w(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni, int i, int j) {
    double somma = 0;
    double[] y = new double[h];

    for (int z = 0; z < m; z++) {

      for (int k = 0; k < W.length; k++) {
        y[k] = calcola_y(esempiBinari, w, z, k);
      }
      double Y = calcola_Y(W, y);
      somma += (soluzioni[z] - Y) * (1 - Math.pow(Y, 2)) * (1 - Math.pow(y[j], 2)) * W[j] * esempiBinari[z][i];
    }
    return somma;
  }

  public static double calcola_Y(double[] W, double[] y) {
    double Y = 0;

    for (int i = 0; i < y.length; i++) {
      Y += y[i] * W[i];
    }
    return Math.tanh(Y);
  }

  public static double calcola_y(int[][] x, double[][] w, int mu, int j) {
    double y = 0;

    for (int i = 0; i < w.length; i++) {
      y += (double) x[mu][i] * w[i][j];
    }
    return Math.tanh(y);
  }

  public static void costruisciPesi(double[][] a) {
    double x, y;
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[0].length; j++) {
        x = 2 * Math.random() - 1;
        y = 2 * Math.random() - 1;
        while (x * x + y * y > 1) {
          x = 2 * Math.random() - 1;
          y = 2 * Math.random() - 1;
        }
        a[i][j] = x * x + y * y;
      }
    }
  }

  public static void costruisciPesiIntermedi(double[] a) {
    double x, y;
    for (int i = 0; i < a.length; i++) {
      x = 2 * Math.random() - 1;
      y = 2 * Math.random() - 1;
      while (x * x + y * y > 1) {
        x = 2 * Math.random() - 1;
        y = 2 * Math.random() - 1;
      }
      a[i] = x * x + y * y;
    }
  }

  public static void costruisciSoluzioniDiv2(int[] a, int[] b) {
    for (int i = 0; i < a.length; i++) {
      b[i] = (a[i] % 2 == 0 ? 0 : 1);
    }
  }

  public static void costruisciEsempiBinari(int[][] a, int[] b) {
    int[] c = new int[b.length];

    System.arraycopy(b, 0, c, 0, c.length);

    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[0].length; j++) {
        a[i][a[0].length - j - 1] = c[i] % 2;
        c[i] = c[i] / 2;
      }
    }
  }

  public static void costruisciTestBinari(int[][] a, int[] b) {
    int[] c = new int[b.length];

    System.arraycopy(b, 0, c, 0, c.length);

    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[0].length; j++) {
        a[i][a[0].length - j - 1] = c[i] % 2;
        c[i] = c[i] / 2;
      }
    }
  }

  public static void costruisciTest(int[] t, int[] e) {
    int j = 0;

    for (int i = 0; i < t.length; i++) {

      if (controlla(j, e)) t[i] = j;
      else i--;

      j++;
    }

  }

  public static boolean controlla(int n, int[] es) {

    for (int e : es) {
      if (e == n) return false;
    }
    return true;
  }
}