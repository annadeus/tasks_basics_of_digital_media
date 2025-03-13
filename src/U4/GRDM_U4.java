package U4;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;


public class GRDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wisch-Blende", "Weiche Blende", "Overlay_AB", "Overlay_BA", "Schieb-Blende", "Chroma-Keying", "Eigene Überblendung"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB + STACK_REQUIRED;
	}

	public static void main(String[] args) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);

    	IJ.open("/users/images/orchid.jpg");

		GRDM_U4 sd = new GRDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();

		int length = stack_B.getSize();
		int width = B_ip.getWidth();
		int height = B_ip.getHeight();

		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
    	IJ.open("/users/images/orchid.jpg");
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height) {
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}

		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length, stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode", choices, "");
		gd.showDialog();

		int methode = 0;
		String s = gd.getNextChoice();
		if (s.equals("Wisch-Blende")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Overlay_AB")) methode = 3;
		if (s.equals("Overlay_BA")) methode = 4;
		if (s.equals("Schieb-Blende")) methode = 5;
		if (s.equals("Chroma-Keying")) methode = 6;
		if (s.equals("Eigene Überblendung")) methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z = 1; z <= length; z++) {
			pixels_B = (int[]) stack_B.getPixels(z);
			pixels_A = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++, pos++) {
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);


					// -----------------------------------------------------------------------------------
					// Wisch-Blende:

					if (methode == 1) {
						if (y + 1 > (z - 1) * (double) height / (length - 1)) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							pixels_Erg[pos] = pixels_A[pos];
						}
					}


					// -----------------------------------------------------------------------------------
					// Weiche Blende:

					if (methode == 2) {
						double currentFrame = z - 1;
						double numberOfFrames = length - 1;
						double alpha = (((currentFrame) / (numberOfFrames - 1)));

						int r = (int) (((alpha * rA) + (rB * (1 - alpha))));
						int g = (int) (((alpha * gA) + (gB * (1 - alpha))));
						int b = (int) (((alpha * bA) + (bB * (1 - alpha))));

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}


					// -----------------------------------------------------------------------------------
					// Overlay A-B:

					if (methode == 3) {
						int r, g, b;

						if (rB <= 128) {
							r = (rA * rB) / 128;
						} else {
							r = 255 - ((255 - rA) * (255 - rB) / 128);
						}

						if (gB <= 128) {
							g = (gA * gB) / 128;
						} else {
							g = 255 - ((255 - gA) * (255 - gB) / 128);
						}

						if (bB <= 128) {
							b = (bA * bB) / 128;
						} else {
							b = 255 - ((255 - bA) * (255 - bB) / 128);
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}


					// -----------------------------------------------------------------------------------
					// Overlay B-A:

					if (methode == 4) {
						int r, g, b;

						if (rA <= 128) {
							r = (rB * rA) / 128;
						} else {
							r = 255 - ((255 - rB) * (255 - rA) / 128);
						}

						if (gA <= 128) {
							g = (gB * gA) / 128;
						} else {
							g = 255 - ((255 - gB) * (255 - gA) / 128);
						}

						if (bA <= 128) {
							b = (bB * bA) / 128;
						} else {
							b = 255 - ((255 - bB) * (255 - bA) / 128);
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}


					// -----------------------------------------------------------------------------------
					// Schieb-Blende:

					if (methode == 5) {
						/*
						 divisionLine: horizontal shift in pixels for the current frame
						 determines how far the transition has progressed
						 as z increases, divisionLine increases from 0 to width
						 --> creates the "line"
						*/
						int divisionLine = (int) ((z - 1) * (double) width / (length - 1));

						/*
						 if: checks whether the current pixel position x is to the right of the transition point divisionLine
						 if it is: the pixel from image A should be taken
						 if it is not: the pixel from image B should be taken
						*/
						if ((x + 1) > divisionLine) {

							int a = pos - divisionLine;
							if (a > pixels_A.length - 1) {
								a = pixels_A.length - 1;
							} else {
								a = Math.max(a, 0);
							}
							pixels_Erg[pos] = pixels_A[a];

						} else {
							int a = y * width - divisionLine + x;
							if (a > pixels_B.length - 1) {
								a = pixels_B.length - 1;
							} else {
								a = Math.max(a, 0);
							}
							pixels_Erg[pos] = pixels_B[a];
						}
					}


					// -----------------------------------------------------------------------------------
					// Chroma-Keying:
					// if current pixel in A is similar to the key-color, the pixel of B will be taken, if not the one from A instead
					if (methode == 6) {
						if (rA > 100 && gA > 100 && bA < 125) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							pixels_Erg[pos] = pixels_A[pos];
						}
					}


					// -----------------------------------------------------------------------------------
					// Eigene Überarbeitung:

					if (methode == 7) {

						double currentFrame = z - 1;
						double numberOfFrames = length - 1;
						double alpha = (((currentFrame) / (numberOfFrames - 1)));

						int r = (int) (((alpha * rA) + (rB * (1 - alpha))));
						int g = (int) (((alpha * gA) + (gB * (1 - alpha))));
						int b = (int) (((alpha * bA) + (bB * (1 - alpha))));


						if (rA > 100 && gA > 100 && bA < 130) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							r = ((255 / (length - 1)) * (z - 1)) * rA + (1 - ((255 / (length - 1)) * (z - 1))) * rB;
							g = ((255 / (length - 1)) * (z - 1)) * gA + (1 - ((255 / (length - 1)) * (z - 1))) * gB;
							b = ((255 / (length - 1)) * (z - 1)) * bA + (1 - ((255 / (length - 1)) * (z - 1))) * bB;
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}
				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();
	}
}
