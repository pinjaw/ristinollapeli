
package logiikka;

import apu.Hakemisto;
import apu.Lista;
import apu.Matikka;

/**
 * HIEKKALAATIKKO JOSSA TESTAILEN JUTTUJA.
 */

public class Aly2 {
    
    private Hakemisto varasto;
    private int[][] suunnat;
    private char botinMerkki;
    private int pituus;
    private boolean ekaSiirto;
    private final int SADE;
    private final int SYVYYS;
    private final int AARETON;
    private int[] koordinaatit;
    
    public Aly2() {
        varasto = new Hakemisto();
        suunnat = new int[][]{{0, 1, 0, -1}, {1, 0, -1, 0}, {-1, -1, 1, 1}, {-1, 1, 1, -1}};
        SADE = 2;
        SYVYYS = 6;
        AARETON = 1000000000;
        ekaSiirto = true;
        koordinaatit = new int[2];
    }

    /**
     * Metodilla asetetaan kumman v�risill� nappuloilla botti pelaa, mustilla vai valkoisilla.
     * @param vari 1 on musta ja 0 on valkoinen.
     */
    public void setMerkki(int vari) {
        if (vari == 1) botinMerkki = 'X';
        else botinMerkki = 'O';
    }

    /**
     * Metodilla kerrotaan botille mink� kokoisella laudalla pelataan.
     * T�m� pit�� aina olla sama kuin pelilaudan koko, ett� botti antaa oikeat koordinaatit.
     * @param pituus 
     */
    public void setPituus(int pituus) {
        this.pituus = pituus;
    }
    
    /**
     * Metodissa on kovakoodattuna botin ensimm�inen siirto sek� mustilla ett� valkoisilla nappuloilla pelatessa.
     * @param lauta, pelilauta char[][]-taulukkona
     * @return koordinaatit, joihin botti haluaa asettaa nappulansa.
     */
    private int[] ekaSiirto(char[][] lauta) {
        ekaSiirto = false;
        int[] koordinaatit = new int[2];
        koordinaatit[0] = pituus / 2;
        koordinaatit[1] = pituus / 2;
        if(botinMerkki == 'O') {
            for(int i = 0; i < pituus; i++){
                for(int j = 0; j < pituus; j++){
                    if(lauta[i][j] == 'X'){
                        if (i < pituus/2 && j < pituus/2){
                            koordinaatit[0] = i + 1;
                            koordinaatit[1] = j + 1;
                        } else if (i < pituus/2 && j > pituus/2){
                            koordinaatit[0] = i + 1;
                            koordinaatit[1] = j - 1;
                        } else if (i > pituus/2 && j < pituus/2) {
                            koordinaatit[0] = i - 1;
                            koordinaatit[1] = j + 1;
                        } else if (i > pituus/2 && j > pituus/2) {
                            koordinaatit[0] = i - 1;
                            koordinaatit[1] = j - 1;
                        } else if (i == pituus/2) {
                            koordinaatit[0] = i;
                            if(j <= pituus/2) koordinaatit[1] = j + 1;
                            else koordinaatit[1] = j - 1;
                        } else {
                            koordinaatit[1] = j;
                            if(i > pituus/2) koordinaatit[0] = i - 1;
                            else koordinaatit[0] = i + 1;
                        }
                    }
                }
            }
        }
        return koordinaatit;
    }
    
    /**
     * Metodi saa parametrin� pelilaudan, joka kertoo teko�lylle pelin tilanteen, ja
     * palauttaa koordinaatit, joihin botti halutaa tehd� seuraavan siirtonsa.
     * @param lauta pelilauta char[][]-taulukkona.
     * @return koordinaatit, joihin botti haluaa asettaa nappulansa.
     */
    public int[] teeSiirto(char[][] lauta) {
        if(ekaSiirto) return ekaSiirto(lauta);
        int alfa = -AARETON;
        int beetta = AARETON;
        int[] koordinaatit = new int[2];
        int tulos = 0;
        Lista<Siirto> siirrot = new Lista<>();
        for(int i = 0; i < pituus; i++){
            for(int j = 0; j < pituus; j++){
                if (potentiaalinenSiirto(i, j, lauta)) {
                    lauta[i][j] = botinMerkki;
                    siirrot.lisaa(new Siirto(pohjaHeuristiikka(lauta, botinMerkki), i, j));
                    lauta[i][j] = '+';
                }
            }
        }
        siirrot.jarjesta();
        siirrot.kaanna();
        for(int u = 0; u < siirrot.pituus(); u++){
            Siirto s = siirrot.hae(u);
            int i = s.getX();
            int j = s.getY();
            lauta[i][j] = botinMerkki;
            if(u == 0){
                tulos = arvioi(lauta, 1, -alfa, -beetta, -1);
            } else {
                tulos = arvioi(lauta, 1, -alfa - 1, -alfa, -1);
                if (alfa < tulos && tulos < beetta) {
                    tulos = arvioi(lauta, 1, -beetta, -tulos, -1);
                }
            }
            if(alfa > tulos){
                koordinaatit[0] = i;
                koordinaatit[1] = j;
            }
            alfa = Matikka.max(alfa, tulos);
            if(alfa >= beetta) {
                lauta[i][j] = '+';
                break;
            }
            lauta[i][j] = '+';
        }
        return koordinaatit;
    }
    
    /**
     * Metodi saa parametrina pelilaudan, joka kuvaa pelitilanteen, ja palauttaa tilanteelle numeroarvion.
     * Metodi tallentaa botin oliomuuttujaan koordinaatit parhaan siirron koordinaatit.
     * @param minimax 1 tarkoittaa maximitasoa ja -1 tarkoittaa minimitasoa.
     * @param taso kertoo mill� pelipuun tasolla ollaan menossa. Muuttuja pit�� huolen siit� ett� rekursio p��ttyy. Juurisolmu on tasolla 1.
     * @param lauta pelilauta char[][]-taulukkona.
     * @param alfa alaraja arviolle.
     * @param beetta yl�raja arviolle.
     * @return numeroarvio pelitilanteelle.
     * @see Aly#koordinaatit
     */
    private int arvioi(char[][] lauta, int taso,  int alfa, int beetta, int minimax) {
        
        char sijoitettavaMerkki;
        if(botinMerkki == 'X') sijoitettavaMerkki = (minimax == 1) ? 'X' : 'O';
        else sijoitettavaMerkki = (minimax == 1) ? 'O': 'X';
        
        if (taso == SYVYYS) return pohjaHeuristiikka(lauta, sijoitettavaMerkki);
        
        if(onkoVoittoa(lauta)) {
            if (minimax == 1) return AARETON;
            return -AARETON;
        }
        
        Lista<Siirto> siirrot = new Lista<>();
        for (int i = 0; i < pituus; i++) {
            for (int j = 0; j < pituus; j++) {
                if (potentiaalinenSiirto(i, j, lauta)) {
                    lauta[i][j] = sijoitettavaMerkki;
                    siirrot.lisaa(new Siirto(pohjaHeuristiikka(lauta, sijoitettavaMerkki), i, j));
                    lauta[i][j] = '+';
                }
            }
        }
        
        siirrot.jarjesta();
        if(minimax == 1) siirrot.kaanna();
        
        int tulos = 0;
        for(int u = 0; u < siirrot.pituus(); u++){
            Siirto s = siirrot.hae(u);
            int i = s.getX();
            int j = s.getY();
            lauta[i][j] = sijoitettavaMerkki;
            if(u == 0){
                tulos = -arvioi(lauta, taso + 1, -alfa, -beetta, -minimax);
            } else {
                tulos = -arvioi(lauta, taso + 1, -alfa - 1, -alfa, -minimax);
                if (alfa < tulos && tulos < beetta) {
                    tulos = -arvioi(lauta, taso + 1, -beetta, -tulos, -minimax);
                }
            }
            alfa = Matikka.max(alfa, tulos);
            if(alfa >= beetta) {
                lauta[i][j] = '+';
                break;
            }
            lauta[i][j] = '+';
        }
        return alfa;
    }
    
    /**
     * Metodi saa parametrina pelilaudan ja kertoo, onko peli p��ttynyt.
     * @param lauta pelilauta char[][]-taulukkona.
     * @return 
     */
    public boolean onkoVoittoa(char[][] lauta) {
        int[] vastaus = new int[5];
        for(int i = 0; i < lauta.length; i++){
            for(int j = 0; j < lauta.length; j++){
                for(int k = 0; k < 4; k++){
                    laskePisinSuora(i, j, suunnat[i], lauta, vastaus);
                    if(vastaus[0] >= 5) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Metodi saa parametrina pelilaudan ja seuraavaksi vuorossa olevan pelaajan pelimerkin ja palauttaa numeroarvion annetulle tilanteelle.
     * Metodi antaa arvion pelitilanteen hyvyydest� botin kannalta.
     * Arvio on positiivinen, jos tilanne on parempi botille, ja negatiivinen, jos se on parempi vastustajalle. T�t� metodia k�ytet��n
     * arvioi-metodin viimeisell� tasolla, jolloin alkoille ei en�� lasketa numeroarvoa sen lapsien avulla.
     * @param lauta pelilauta char[][]-taulukkona.
     * @param vuoro pelimerkki, X tai O. Kertoo, kenen vuoro on seuraavaksi tehd� siirto.
     * @return numeroarvio pelitilanteelle.
     * @see Aly#arvioi(int, int, char[][], int, int) 
     */
    public int pohjaHeuristiikka(char[][] lauta, char vuoro) {
        int tulos = 0;
        int[] tilasto = new int[16]; 
        //ykk�set 0, kakkoset 1, avoimet kakkoset 2, kolmoset 3, avoimet kolmoset 4, neloset 5, avoimet neloset 6, vitoset 7 
        //vastustajan vastaavat (8 - 15)
        char vastustajanMerkki = botinMerkki == 'X' ? 'O' : 'X';
        int[] vastaus = new int[5];
        boolean avoinNelja = false;
        boolean avoinNeljaVastustajalla = false;
        for (int i = 0; i < pituus; i++) {
            for (int j = 0; j < pituus; j++) {
                if (lauta[i][j] == botinMerkki) {
                    
                    for(int k = 0; k < 4; k++){
                        laskePisinSuora(i, j, suunnat[k], lauta, vastaus);
                        uhkaarvio(vastaus, lauta, true, tilasto, i, j);
                        tulos += tilasto[0];
                        tilasto[0] = 0;
                    }
                    
                } else if (lauta[i][j] == vastustajanMerkki) {
                    
                    for(int k = 0; k < 4; k++){
                        laskePisinSuora(i, j, suunnat[k], lauta, vastaus);
                        uhkaarvio(vastaus, lauta, false, tilasto, i, j);
                        tulos -= tilasto[8];
                        tilasto[8] = 0;
                    }
                    
                }
            }
        }
        
        if(tilasto[7] > 0) return AARETON;
        if(tilasto[6] > 0) avoinNelja = true;
        if(tilasto[15] > 0) return -AARETON;
        if(tilasto[14] > 0) avoinNeljaVastustajalla = true;
        
        if(avoinNeljaVastustajalla && !avoinNelja) return -AARETON + 1;
        if(avoinNelja && !avoinNeljaVastustajalla) return AARETON - 1;
        if(avoinNelja && avoinNeljaVastustajalla){
            if(vuoro == botinMerkki) return AARETON - 1;
            else return -AARETON + 1;
        }
        
        return tulos + laskeTulos(tilasto);
    }
    
    /**
     * Pisteytt�� pelitilanteen muut kuin nelj�n ja viidensuorat.
     * @param tilasto
     * @return tulos
     */
    private int laskeTulos(int[] tilasto) {
        for(int i = 0; i < tilasto.length; i++){
            if(i == 1 || i == 2 || i == 9 || i == 10) tilasto[i] /= 2;
            if(i == 3 || i == 4 || i == 11 || i == 12) tilasto[i] /= 3;
            if(i == 5 || i == 13) tilasto[i] /= 4;
        }
        int tulos = tilasto[1] * 10 - tilasto[9] * 10 + tilasto[2] * 50 - tilasto[10] * 50 + tilasto[3] * 100 - tilasto[11] * 100 
                + tilasto[4] * 1000 - tilasto[12] * 1000 + tilasto[5] * 1000 - tilasto[13] * 1000;
        return tulos;
    }
    
    /**
     * Metodi laskee, mik� on pisin annetun suuntainen suora, johon annetuissa koordinaateissa oleva pelimerkki sis�ltyy.
     * Metodi kertoo, mik� on pisin annettuun suuntaan jatkuva suora, jossa koordinaateissa oleva pelimerkki on osana.
     * Suunta, jota tutkitaan, annetaan parametrina (vaakasuora, pystysuora tai vasen- tai oikea vinosuora).
     * Suoran p��tepisteiden koordinaatit ja suoran pituus kirjoitetaan parametrina annettuun vastaus-taulukkoon.
     * @param x koordinaati.
     * @param y koordinaatti.
     * @param suunta mink� suuntainen suora tarkistetaan.
     * @param lauta pelilauta char[][]-taulukkona.
     * @param vastaus taulukko, johon saadut tulokset kirjoitetaan.
     */
    private void laskePisinSuora(int x, int y, int[] suunta, char[][] lauta, int[] vastaus){
        char merkki = lauta[x][y];
        int summa = 0;
        int alkux = x;
        int alkuy = y;
        while(alkux >= 0 && alkux < pituus && alkuy >= 0 && alkuy < pituus && lauta[alkux][alkuy] == merkki){
            summa++;
            alkux += suunta[0];
            alkuy += suunta[1];
        }
        vastaus[1] = alkux;
        vastaus[2] = alkuy;
        alkux = x + suunta[2];
        alkuy = y + suunta[3];
        while(alkux >= 0 && alkux < pituus && alkuy >= 0 && alkuy < pituus && lauta[alkux][alkuy] == merkki){
            summa++;
            alkux += suunta[2];
            alkuy += suunta[3];
        }
        vastaus[3] = alkux;
        vastaus[4] = alkuy;
        vastaus[0] = summa;
    }
    
    /**
     * Metodi saa parametrina laskePisinSuora-metodin tulostaulukon ja kertoo sen sis�ll�n perusteella, mink�lainen uhka taulukossa kuvailtu suora on.
     * Vastaus kirjoitetaan parametrina saatavaan tilasto-taulukkoon, jossa eri uhat on luokiteltuna.
     * Uhka tarkoittaa siis pelin termeill� esimerkiksi avointa kolmen suoraa tai puoliavointa nelj�n suoraa.
     * @param suora arvioitavan suoran tiedot.
     * @param lauta pelilauta char[][]-taulukkona.
     * @param botti true, jos kyseess� on botin oma uhka ja false muuten.
     * @param tilasto taulukko, johon arvioitu uhka tilastoidaan.
     * @param i lis�parametri yksinkertaistamaan metodia.
     * @param j lis�parametri yksinkertaistamaan metodia.
     * @see Aly#laskePisinSuora(int, int, int[], char[][], int[])
     */
    private void uhkaarvio(int[] suora, char[][] lauta, boolean botti, int[] tilasto, int i, int j){
        if (suora[0] >= 5) {
            if (botti) tilasto[7]++;
            else tilasto[15]++;
        }
        if(suora[0] == 1) {
            if (botti) tilasto[0] = Matikka.max(Matikka.min(i,(pituus -  1 - i)), Matikka.min(j, (pituus - 1 - j)));
            else tilasto[8] = -Matikka.max(Matikka.min(i,(pituus -  1 - i)), Matikka.min(j, (pituus - 1 - j)));
        }
        if ((laudalla(suora[1], suora[2]) && lauta[suora[1]][suora[2]] == '+') && (laudalla(suora[3], suora[4]) && lauta[suora[3]][suora[4]] == '+')){
            if(suora[0] == 2) {
                if(botti) tilasto[2]++;
                else tilasto[10]++;
            }
            if(suora[0] == 3) {
                if(botti) tilasto[4]++;
                else tilasto[12]++;
            }
            if(suora[0] == 4) {
                if(botti) tilasto[6]++;
                else tilasto[14]++;
            }
        } else if ((laudalla(suora[1], suora[2]) && lauta[suora[1]][suora[2]] == '+') || (laudalla(suora[3], suora[4]) && lauta[suora[3]][suora[4]] == '+')){
            if(suora[0] == 2) {
                if(botti) tilasto[1]++;
                else tilasto[9]++;
            }
            if(suora[0] == 3) {
                if(botti) tilasto[3]++;
                else tilasto[11]++;
            }
            if(suora[0] == 4) {
                if(botti) tilasto[5]++;
                else tilasto[13]++;
            }
        }
    }
    
    /**
     * Metodi kertoo, ovatko annetut koordinaatit pelilaudalla.
     * @param x koordinaatti
     * @param y koordinaatti
     * @return true, jos luku on pelilaudan sis�ll�, false muuten.
     */
    private boolean laudalla(int x, int y){
        if(x >= 0 && x < pituus && y >= 0 && y < pituus) return true;
        return false;
    }
    
    /**
     * Metodi kertoo, kannattaako parametreina annettuihin koordinaatteihin mahdollisesti sijoittaa pelimerkki.
     * Pelimerkki� ei kannata sijoittaa liian kauas muista pelimerkeist�. Botin oliomuuttuja sade m��ritt��, mik� on pisin et�isyys,
     * kuinka kauas toisesta pelimerkist� uusi pelimerkki kannattaa korkeintaan sijoittaa.
     * @param x koordinaatti
     * @param y koordinaatti
     * @param lauta pelilauta char[][]-taulukkona.
     * @return true, jos s�teen sis�ll� annetuista koordinaateista on toinen nappula, false muuten.
     * @see Aly#SADE
     */
    private boolean potentiaalinenSiirto(int x, int y, char[][] lauta) {
        if(lauta[x][y] != '+') return false;
        int mistax = Matikka.max(0, x - SADE);
        int mihinx = Matikka.min(x + SADE, pituus - 1);
        int mistay = Matikka.max(0, y - SADE);
        int mihiny = Matikka.min(y + SADE, pituus - 1);
        for (int i = mistax; i <= mihinx; i++) {
            for (int j = mistay; j <= mihiny; j++) {
                if (lauta[i][j] != '+') return true;
            }
        }
        return false;
    }
}