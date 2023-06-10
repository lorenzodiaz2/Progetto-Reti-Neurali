import java.security.SecureRandom;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

public class PlotExample extends JPanel {
  private final static double EPSILON = 0.01;
  private final static int m = 80;
  private final static int n = 12;
  private final static int h = 8;
  private static final SecureRandom random = new SecureRandom();
  private static final ArrayList<Object> cord1 = new ArrayList<>();
  private static final ArrayList<Object> cord2 = new ArrayList<>();
  int marg = 60;

  public static void main(String[] args) {

    //inizializza esempi
    int[] esempi = new int[m];
    costruisciEsempi(esempi, (int) Math.pow(2, n));

    //inizializza l' array di esempi in binario
    int[][] esempiBinari = new int[m][n];
    costruisciEsempiBinari(esempiBinari, esempi);

    //inizializza le soluzioni
    int[] soluzioni = new int[m];
    costruisciSoluzioni(esempi, soluzioni);

    while (!verificaEsempi(soluzioni)) {
      costruisciEsempi(esempi, (int) Math.pow(2, n));
      costruisciSoluzioni(esempi, soluzioni);
    }

    System.out.printf("%s%n%n", "Gli esempi sono stati costruiti!");

    //Inizializza pesi w e W (valori Gaussiani)
    //per w, ogni riga rappresenta un input diverso e le colonne rappresentano in quale neurone nello strato intermedio va quell'input,
    //ad es. w[1][3] è il valore dell'input x1 verso il neurone tre nello strato intermedio
    double[][] w = new double[n][h];
    double[] W = new double[h];
    costruisciPesi(w);
    costruisciPesiIntermedi(W);

    double[][] w_copia = new double[n][h];
    double[] W_copia = new double[h];
    copia(w, w_copia, W, W_copia);

    double[][][] w_nuova = new double[1000000][n][h];
    double[][] W_nuova = new double[1000000][h];

    System.out.printf("%s%d%n%s%d%n%s%d%n%n", "numero di esempi = ", m, "numero di input = ", n, "numero di neuroni hidden = ", h);
    System.out.printf("%s%n%n", "L' apprendimento ha generato i seguenti risultati: ");

    ArrayList<Object> erroriEsempi = new ArrayList<>();
    ArrayList<Object> erroriTest = new ArrayList<>();
    ArrayList<Object> contatore = new ArrayList<>();

    apprendimento(esempiBinari, w, W, soluzioni, esempi, erroriEsempi, erroriTest, contatore, w_nuova, W_nuova);

    cord1.addAll(erroriEsempi);

    cord2.addAll(erroriTest);

    stampaRisultati(esempiBinari, esempi, soluzioni, w, W);

    double min = minimo(erroriTest);
    int contat = (int) contatore.get(erroriTest.indexOf(min));

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

    //inizializza l' array di test in binario77z
    int[][] testBinari = new int[f][n];
    costruisciTestBinari(testBinari, test);

    int[] soluzioniTest = new int[f];
    costruisciSoluzioni(test, soluzioniTest);

    System.out.printf("%n%n%s%n%n", "L'insieme dei test ha generato i seguenti risultati:");
    stampaRisultatiTest(testBinari, test, soluzioniTest, w_nuova[contat / 10], W_nuova[contat / 10]);

    //create an instance of JFrame class
    JFrame frame = new JFrame();
    // set size, layout and location for frame.
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new PlotExample());
    frame.setSize(400, 400);
    frame.setLocation(200, 200);
    frame.setVisible(true);
  }

  public static double minimo(ArrayList<Object> err) {
    double min = (double) err.get(0);
    for (int i = 1; i < err.size(); i++) {
      if ((double) err.get(i) < min) min = (double) err.get(i);
    }
    return min;
  }

  public static boolean verificaEsempi(int[] es) {
    int tipo0 = 0;
    int tipo1 = 0;

    for (int e : es) {
      if (e == 0) tipo0++;
      else tipo1++;
    }

    return (double) tipo0 / (tipo0 + tipo1) >= 0.4 && (double) tipo1 / (tipo0 + tipo1) >= 0.4;
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
    System.out.printf("%n%s%d%s%d%s%f%s", "Su ", testBinari.length, " esempi di test ci sono stati ", cont, " errori con percentuale di errore ", (float) cont / testBinari.length * 100, " %");
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

  public static void apprendimento(int[][] esempiBinari, double[][] w, double[] W, int[] soluzioni, int[] esempi, ArrayList<Object> erroriEsempi,
                                   ArrayList<Object> erroriTest, ArrayList<Object> contatore, double[][][] w_n, double[][] W_n) {
    int k = 0;

    for (int i = 0; i < Integer.MAX_VALUE; i++) {

      double E = errore(w, W, esempiBinari, soluzioni);

      if (i % 10 == 0) {
        erroriEsempi.add(E);

        if (erroriEsempi.size() > 1 && Math.abs((double) erroriEsempi.get(erroriEsempi.size() - 1) - (double) erroriEsempi.get(erroriEsempi.size() - 2)) < 0.001) {
          System.out.printf("%n%s%f%n%n", "Siamo arrivati ad un minimo locale dato che la variazione di E vale ",
            (double) erroriEsempi.get(erroriEsempi.size() - 2) - (double) erroriEsempi.get(erroriEsempi.size() - 1));
          break;
        }

        int f = (int) Math.pow(2, n) - m;
        int[] test = new int[f];
        costruisciTest(test, esempi);
        int[][] testBinari = new int[f][n];
        costruisciTestBinari(testBinari, test);
        int[] soluzioniTest = new int[f];
        costruisciSoluzioni(test, soluzioniTest);

        double errTest = errore(w, W, testBinari, soluzioniTest);

        erroriTest.add(errTest);

        contatore.add(i);

        copia(w, w_n[k], W, W_n[k]);

        k += 1;
      }

      if (i % 100 == 0) System.out.println("i = " + i + ", E = " + E);

      if (E < 0.2) {
        break;
      }

      modificaPesi(w, W, esempiBinari, soluzioni);
      modificaPesiIntermedi(w, W, esempiBinari, soluzioni);
    }

  }

  public static void copia(double[][] w, double[][] w_copia, double[] W, double[] W_copia) {
    for (int i = 0; i < w.length; i++) {
      System.arraycopy(w[i], 0, w_copia[i], 0, w[0].length);
    }

    System.arraycopy(W, 0, W_copia, 0, W.length);
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

  public static void costruisciSoluzioni(int[] a, int[] b) {
    for (int i = 0; i < a.length; i++) {
      b[i] = (a[i] % 3 == 0 ? 1 : 0);
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

  public static void costruisciEsempi(int[] a, int max) {
    for (int i = 0; i < a.length; i++) {
      a[i] = random.nextInt(max);

      if (!verifica(i, a)) i--;

    }

  }

  public static boolean verifica(int i, int[] a) {

    for (int j = 0; j < i; j++) {
      if (a[j] == a[i]) return false;
    }
    return true;
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

  protected void paintComponent(Graphics grf) {
    //create instance of the Graphics to use its methods
    super.paintComponent(grf);
    Graphics2D graph = (Graphics2D) grf;

    //Sets the value of a single preference for the rendering algorithms.
    graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // get width and height
    int width = getWidth();
    int height = getHeight();

    // draw graph
    graph.draw(new Line2D.Double(marg, marg, marg, height - marg));
    graph.draw(new Line2D.Double(marg, height - marg, width - marg, height - marg));

    //find value of x and scale to plot points
    double x_1 = (double) (width - 2 * marg) / (cord1.size() - 1);
    double scale = (double) (height - 2 * marg) / getMax();

    //set color for points
    graph.setPaint(Color.RED);

    // set points to the graph
    for (int i = 0; i < cord1.size(); i++) {
      double x1 = marg + i * x_1;
      double y1 = height - marg - scale * (double) cord1.get(i);
      graph.fill(new Ellipse2D.Double(x1 - 2, y1 - 2, 4, 4));
    }

    //set color for points
    graph.setPaint(Color.BLUE);

    for (int i = 0; i < cord2.size(); i++) {
      double x1 = marg + i * x_1;
      double y1 = height - marg - scale * (double) cord2.get(i);
      graph.fill(new Ellipse2D.Double(x1 - 2, y1 - 2, 4, 4));
    }
  }

  private double getMax() {
    double max = -Integer.MAX_VALUE;
    for (Object o : cord1) {
      if ((double) o > max)
        max = (double) o;

    }

    for (Object o : cord2) {
      if ((double) o > max)
        max = (double) o;

    }
    return max;
  }
}