package U3;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Rot-Kanal", "Negativ", "Graustufen", "Binär 1", "5 Graustufen", "27 Graustufen", "Binär 2", "Sepia", "9 Farben"};


	public static void main(String args[]) {

    	IJ.open("/users/images/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		if (imp == null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int[]) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			}

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[]) ip.getPixels();

			if (method.equals("Original")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;

						pixels[pos] = origPixels[pos];
					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Rot-Kanal")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						// int g = (argb >>  8) & 0xff;
						// int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max(rn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Negativ")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max(rn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// U und V fallen weg
						double Y = 0.299 * r + 0.587 * g + 0.114 * b;
						double U = 0.493 * (b - Y);
						double V = 0.877 * (r - Y);

						Y = (r + g + b) / 3;
						U = 0;
						V = 0;

						int rn = (int) (Y + V / 0.877);
						int bn = (int) (Y + U / 0.493);
						int gn = (int) (1 / 0.587 * Y - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn); //nach unten, da Bezug auf 'bn'

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max(rn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Binär 1")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);

						if (Y > 127) {
							Y = 255;
						} else {
							Y = 0;
						}

						pixels[pos] = (0xFF << 24) | (Y << 16) | (Y << 8) | Y;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("5 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);

						double spaces = 255 / 5.0;
						int grey = (int) (Y / spaces);
						Y = (int) (grey * spaces + (spaces / 2)); //spaces/2 --> damit kein s/w

						pixels[pos] = (0xFF << 24) | (Y << 16) | (Y << 8) | Y;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("27 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);

						double spaces = 255 / 26.0;
						double grey_tones = 255 / 27.0;
						int grey = (int) (Y / grey_tones);
						Y = (int) (grey * spaces);

						pixels[pos] = (0xFF << 24) | (Y << 16) | (Y << 8) | Y;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Binär 2")) {
				int error = 0;

				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 255;
						int gn = 255;
						int bn = 255;

						if ((r + g + b + error) < 380) {
							rn = 0;
							gn = 0;
							bn = 0;
						}
						error = ((r + g + b + error) - (rn + gn + bn));


						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("Sepia")) { // Graustufenbild --> Unbunten-Gerade im Raum verschieben --> Richtung R, G

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
						int rn = Y;
						int gn = Y;
						int bn = Y;

						// Verschieben im Raum:
						rn += 40;
						gn += 20;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max(rn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

					}
				}
			}

			// ---------------------------------------------------------------------------

			if (method.equals("9 Farben")) {

				/*
				9 Farben bel. raussuchen und für jeden einzelnen Pixel
				den Abstand zu den 9 Punkten im Raum (Würfel) ausrechnen (S.d.P.)
				und den kürzesten Abstand wählen und als neuen Wert für den
				aktuellen Pixel einsetzen
				am besten mit Array, der die 9 Farben enthält, arbeiten
				 */


				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 0;
						int gn = 0;
						int bn = 0;

						if (r < 40) {
							rn = 30;
							gn = 35;
							bn = 33;
						} else if (((r > 40 && r <= 60) && g > 80) || ((r > 60 && r <= 100) && b > 100)) {
							rn = 50;
							gn = 105;
							bn = 140;
						} else if ((r > 40 && r <= 60) && g < 80) {
							rn = 30;
							gn = 28;
							bn = 32;
						} else if (r > 60 && r <= 95) {
							rn = 80;
							gn = 70;
							bn = 65;
						} else if (r > 95 && r <= 130) {
							rn = 115;
							gn = 105;
							bn = 90;
						} else if (r > 130 && r <= 170) {
							rn = 135;
							gn = 120;
							bn = 110;
						} else if (r > 170 && r <= 180) {
							rn = 150;
							gn = 150;
							bn = 150;
						} else if (r > 180 && r <= 220) {
							rn = 240;
							gn = 235;
							bn = 220;
						} else if (r > 220 && r <= 255) {
							rn = 255;
							gn = 255;
							bn = 255;
						}

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

					}
				}
			} // CustomWindow inner class
		}
	}
}

