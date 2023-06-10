import java.security.SecureRandom;
import java.util.ArrayList;

public class Primi {
  private final static double EPSILON = 0.001;
  private final static int m = 60;  //numero di esempi
  private final static int n = 10;  //numero di input
  private final static int h = 8;  //numero di neuroni nello strato intermedio
  private final static SecureRandom random = new SecureRandom();

  public static void main(String[] args) {

        /*
        Generando numeri casuali la maggior parte di questi non saranno numeri primi, quindi per creare un bilanciamento
        nell'insieme degli esempi tra numeri primi e non costruisco due liste rispettivamente con numeri primi e non primi
        */

    ArrayList<Integer> primi = new ArrayList<>();
    costruisciListePrimi(primi);

    ArrayList<Integer> nonPrimi = new ArrayList<>();
    costruisciListeNonPrimi(nonPrimi);

    //inizializza esempi, prendendone metà tra i numeri primi e metà tra i non primi
    int[] esempi = new int[m];
    costruisciEsempi(esempi, primi, nonPrimi);


    if (primi.size() >= esempi.length / 2) {

      //costruisce l' array di esempi in binario a partire da quelli in decimale
      int[][] esempiBinari = new int[m][n];
      costruisciBinari(esempiBinari, esempi);

      //Costruisce le soluzioni. Se il numero è primo la soluzione è 1, altrimenti -1
      int[] soluzioni = new int[m];
      costruisciSoluzioni(esempi, soluzioni);

      //costruisce i pesi w e W
      //w[i][j] è il valore dell' input x_i verso il neurone j dello strato intermedio
      //W[i] è il peso del neurone i nello strato intermedio verso l'uscita Y
      double[][] w = new double[n][h];
      double[] W = new double[h];
      costruisciPesi(w, W);


      System.out.printf("%s%d%n%s%d%n%s%d%n%n", "numero di esempi = ", m, "numero di input = ", n, "numero di neuroni hidden = ", h);

      apprendimento(esempiBinari, w, W, soluzioni);

      System.out.printf("%s%n%n", "L' apprendimento ha generato i seguenti risultati: ");

      stampaRisultati(esempiBinari, esempi, soluzioni, w, W);


            /*
            OSSERVAZIONE
            il numero di bit dell'insieme test deve essere lo stesso di quello dell'insieme degli esempi perché i pesi
            sono tanti quanti gli input e gli input sono il numero di bit, se il numero di bit dell'insieme test fosse
            diverso allora l'operazioni di moltiplicazione tra matrice test e matrice pesi non funzionerebbe.


            Ad esempio se m=10, n=4 (quindi genero numeri al massimo fino a 15) e l' array di esempi fosse
            esempi = [0,1,2,3,4,5,6,7,8,9] allora per l'OSSERVAZIONE sopra l'insieme di test può al massimo raggiungere
            il valore 16 (escluso) ma dato che l'insieme test deve essere diverso dall'insieme esempi allora posso considerarne
            il complementare che sarà test = [10, 11, 12, 13, 14, 15] di lunghezza max - m = 2^n - m = 16 - 10 = 6
            */

      int f = (int) Math.pow(2, n) - m;

      //costruisce l' array di test
      int[] test = new int[f];
      costruisciTest(test, esempi);

      //costruisce l' array di test in binario
      int[][] testBinari = new int[f][n];
      costruisciBinari(testBinari, test);

      //costruisce l' array di soluzione dei test
      int[] soluzioniTest = new int[f];
      costruisciSoluzioni(test, soluzioniTest);

      System.out.printf("%n%n%s%n%n", "L'insieme dei test ha generato i seguenti risultati:");
      stampaRisultatiTest(testBinari, test, soluzioniTest, w, W);

    }

  }

  public static void costruisciListePrimi(ArrayList<Integer> primi) {
    for (int i = 0; i < Math.pow(2, n); i++) {
      if (!nonPrimo(i)) primi.add(i);
    }
  }

  public static boolean nonPrimo(int num) {
    if (num == 0 || num == 1) return true;
    if (num == 2) return false;

    for (int i = 2; i <= num / 2; i++) {
      if (num % i == 0) return true;
    }
    return false;
  }

  public static void costruisciListeNonPrimi(ArrayList<Integer> nonPrimi) {
    for (int i = 0; i < Math.pow(2, n); i++) {
      if (nonPrimo(i)) nonPrimi.add(i);
    }
  }

  public static void costruisciEsempi(int[] a, ArrayList<Integer> primi, ArrayList<Integer> nonPrimi) {
    if (primi.size() < a.length / 2) {
      System.out.printf("%s%n%s%n", "Non ci sono abbastanza numeri primi per creare gli esempi in modo omogeneo!",
        "Aumenta n o diminuisci m");
    } else {

      //costruisce metà degli esempi prendendo i numeri primi
      for (int i = 0; i < a.length / 2; i++) {
        a[i] = primi.get(random.nextInt(primi.size() - 1));
        if (verifica(i, a)) i--;  //verifica che gli esempi non si ripetano
      }

      //costruisce l'altra metà; degli esempi prendendo i numeri NON primi
      for (int i = a.length / 2; i < a.length; i++) {
        a[i] = nonPrimi.get(random.nextInt(nonPrimi.size() - 1));
        if (verifica(i, a)) i--;  //verifica che gli esempi non si ripetano
      }
    }
  }

  public static boolean verifica(int i, int[] a) {
    for (int j = 0; j < i; j++) {
      if (a[j] == a[i]) return true;
    }
    return false;
  }

  public static void costruisciBinari(int[][] bin, int[] dec) {
    int[] c = new int[dec.length];

    System.arraycopy(dec, 0, c, 0, c.length);

    for (int i = 0; i < bin.length; i++) {
      for (int j = 0; j < bin[0].length; j++) {
        bin[i][bin[0].length - j - 1] = c[i] % 2;
        c[i] = c[i] / 2;
      }
    }
  }

  public static void costruisciSoluzioni(int[] es, int[] sol) {
    for (int i = 0; i < es.length; i++) {
      sol[i] = (nonPrimo(es[i]) ? -1 : 1);
    }
  }

  public static void costruisciPesi(double[][] w, double[] W) {
    double x, y;

    //costruisce matrice dei pesi w[][]
    for (int i = 0; i < w.length; i++) {
      for (int j = 0; j < w[0].length; j++) {
        x = 2 * Math.random() - 1;
        y = 2 * Math.random() - 1;
        while (x * x + y * y > 1) {
          x = 2 * Math.random() - 1;
          y = 2 * Math.random() - 1;
        }
        w[i][j] = x * x + y * y;
      }
    }

    //costruisce vettore dei pesi W[]
    for (int i = 0; i < W.length; i++) {
      x = 2 * Math.random() - 1;
      y = 2 * Math.random() - 1;
      while (x * x + y * y > 1) {
        x = 2 * Math.random() - 1;
        y = 2 * Math.random() - 1;
      }
      W[i] = x * x + y * y;
    }
  }

  public static void apprendimento(int[][] esempiBinari, double[][] w, double[] W, int[] soluzioni) {
    ArrayList<Double> erroriEsempi = new ArrayList<>();  //tiene traccia dell'errore
    System.out.printf("%s%n%n", "Aggiornamento dell' errore: ");

    for (int i = 0; i < Integer.MAX_VALUE; i++) {

      double E = errore(w, W, esempiBinari, soluzioni);  //calcola l'errore = sum( delta - y )^2 / 2

      //ogni 100 iterazioni verifica che l'errore stia diminuendo di valori non troppo piccoli, altrimenti esce dal ciclo
      if (i % 100 == 0) {

        erroriEsempi.add(E);

        if (erroriEsempi.size() > 1 && Math.abs(erroriEsempi.get(erroriEsempi.size() - 1) - erroriEsempi.get(erroriEsempi.size() - 2)) < 0.0001) {
          System.out.printf("%n%s%f%n%n", "Siamo arrivati ad un minimo locale dato che la variazione di E vale ",
            erroriEsempi.get(erroriEsempi.size() - 2) - erroriEsempi.get(erroriEsempi.size() - 1));
          break;
        }

      }

      if (i % 1000 == 0) System.out.println("i = " + i + ", E = " + E);

      if (E < 0.5) {
        System.out.printf("%s%d%s%f%n%n", "i = ", i, ", E = ", E);
        break;
      }

      //modifica i pesi usando la discesa lungo il gradiente
      modificaPesi(w, W, esempiBinari, soluzioni);
    }

  }

  public static double calcola_y(int[][] x, double[][] w, int mu, int j) {
    double y = 0;

    for (int i = 0; i < n; i++) {
      y += (double) x[mu][i] * w[i][j];
    }
    return Math.tanh(y);
  }

  public static double errore(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni) {
    double errore = 0;
    double[] y = new double[h];

    for (int z = 0; z < soluzioni.length; z++) {
      for (int k = 0; k < W.length; k++) {
        y[k] = calcola_y(esempiBinari, w, z, k);
      }
      errore += Math.pow(soluzioni[z] - calcola_Y(W, y), 2);
    }
    return errore / 2;
  }

  public static void modificaPesi(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni) {

    for (int i = 0; i < h; i++) {
      W[i] += EPSILON * calcolaSomma_W(w, W, esempiBinari, soluzioni, i);
      for (int j = 0; j < n; j++) {
        w[j][i] += EPSILON * calcolaSomma_w(w, W, esempiBinari, soluzioni, j, i);
      }
    }
  }

  public static double calcolaSomma_W(double[][] w, double[] W, int[][] esempiBinari, int[] soluzioni, int j) {
    double somma = 0;
    double[] y = new double[h];

    for (int z = 0; z < m; z++) {
      for (int k = 0; k < h; k++) {
        y[k] = calcola_y(esempiBinari, w, z, k);
      }
      double Y = calcola_Y(W, y);
      somma += (soluzioni[z] - Y) * (1 - Math.pow(Y, 2)) * y[j];
    }
    return somma;
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

    for (int i = 0; i < h; i++) {
      Y += y[i] * W[i];
    }
    return Math.tanh(Y);
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

  public static void costruisciTest(int[] t, int[] e) {
    int j = 0;

    for (int i = 0; i < t.length; i++) {
      if (controlla(j, e)) t[i] = j;  //se non ci sono ripetizioni assegna il valore
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

      System.out.print("Y = " + Y + "    Risultato test :  " + ((Math.abs(soluzioniTest[k] - Y) >= 1) ? "ERRATO" : "GIUSTO"));
      if (Math.abs(soluzioniTest[k] - Y) >= 1) cont++;
      System.out.println();
    }

    System.out.printf("%n%s%d%s%d%s%f%s", "Su ", testBinari.length, " esempi di test ci sono stati ", cont,
      " errori con percentuale di errore ", (float) 100 * cont / testBinari.length, " %");
  }
}