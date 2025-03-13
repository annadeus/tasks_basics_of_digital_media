package U1;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild", 
		"Belgische Fahne",
		"Horiz. Verlauf Schwarz/Weiß",
		"Diag. Verlauf Weiß/Schwarz",
		"Horiz. Schwarz/Rot Verlauf + vert. Schwarz/Blau Verlauf",
		"USA Fahne",
		"Tschechische Fahne"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1 imageGeneration = new GLDM_U1();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			generateBlackImage(width, height, pixels);
		}

		if ( choice.equals("Gelbes Bild") ) {
			generateYellowImage(width, height, pixels);
		}

		if ( choice.equals("Belgische Fahne") ) {
			generateBelgianFlag(width, height, pixels);
		}

		if ( choice.equals("Horiz. Verlauf Schwarz/Weiß") ) {
			generateBlackWhite(width, height, pixels);
		}

		if ( choice.equals("Diag. Verlauf Weiß/Schwarz") ) {
			generateWhiteBlack(width, height, pixels);
		}

		if ( choice.equals("Horiz. Schwarz/Rot Verlauf + vert. Schwarz/Blau Verlauf") ) {
			generateBlackRed(width, height, pixels);
		}

		if ( choice.equals("USA Fahne") ) {
			generateUSA(width, height, pixels);
		}

		if ( choice.equals("Tschechische Fahne") ) {
			generateTschech(width, height, pixels);
		}

		
		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen
				
				int r = 0;
				int g = 0;
				int b = 0;
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateYellowImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 255;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateBelgianFlag(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<(width/3); x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 0;
				int g = 0;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
			for (int x=(width/3); x<(2*(width/3)); x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 255;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
			for (int x=(2*(width/3)); x<(3*(width/3)); x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 0;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateBlackWhite(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = (255*x)/width;
				int g = (255*x)/width;
				int b = (255*x)/width;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateWhiteBlack(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = (int) (255-((((255)*Math.sqrt((x^2)+(y^2)))/(Math.sqrt((height^2)+(width^2))))));
				int g = r;
				int b = r;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateBlackRed(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = (255*x)/width;
				int g = 0;
				int b = (255*y)/height;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateUSA(int width, int height, int[] pixels) { //modulo-Funktion ??
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) {
				int pos = y * width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 255;
				int b = 255;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}


		for (int a = 1; a <= 13; a++) {
			for (int y = (a-1)*(height/13); y < (a * (height / 13)); y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					int pos = y * width + x; // Arrayposition bestimmen

					int r = 255;
					int g = 255;
					int b = 255;

					if((a%2)!=0) {
						r = 255;
						g = 0;
						b = 0;
					}

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		}


		for (int y=0; y<(7*(height/13)); y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<(width*(0.4)); x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 0;
				int g = 0;
				int b = 255;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	private void generateTschech(int width, int height, int[] pixels) {
		//Koordiatensystem steht immer auf dem Kopf --> wichtig für Berechnung
		// bei Diagonale nach unten --> x<y
		// Schleife ueber die y-Werte
		for (int y=0; y<(height/2); y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 255;
				int b = 255;

				if(x<=((y*(width/2))/(height/2))){
					r = 0;
					g = 0;
					b = 255;
				} else {
					r = 255;
					g = 255;
					b = 255;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}


		for (int y=(height/2); y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 255;
				int g = 0;
				int b = 0;

				if(x<=(((height-y)*(width/2))/(height/2))){
					r = 0;
					g = 0;
					b = 255;
				} else {
					r = 255;
					g = 0;
					b = 0;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}


	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

