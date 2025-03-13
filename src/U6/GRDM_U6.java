package U6;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class GRDM_U6 implements PlugInFilter {

	ImagePlus imp; // ImagePlus object

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
		{showAbout(); return DONE;}
		return DOES_RGB+NO_CHANGES;
		// kann RGB-Bilder und verändert das Original nicht
	}

	public static void main(String[] args) {

    	IJ.open("/users/images/orchid.jpg");

		GRDM_U6 pw = new GRDM_U6();
		pw.imp = IJ.getImage();
		ImageProcessor ip = pw.imp.getProcessor();
		pw.run(ip);
	}

	public void run(ImageProcessor ip) {

		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Hoehe:",500,0);
		gd.addNumericField("Breite:",400,0);

		gd.showDialog();
		
		int height_n = (int)gd.getNextNumber(); // _n fuer das neue skalierte Bild
		int width_n =  (int)gd.getNextNumber();
		
		int width  = ip.getWidth();  // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		//height_n = height;
		//width_n  = width;
		
		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild",
		                   width_n, height_n, 1, NewImage.FILL_BLACK);
		
		ImageProcessor ip_n = neu.getProcessor();

		
		int[] pix = (int[])ip.getPixels();
		int[] pix_n = (int[])ip_n.getPixels();

		String method = gd.getNextChoice(); // neu

		// Schleife ueber das neue Bild
		for (int y_n=0; y_n<height_n; y_n++) {
			for (int x_n=0; x_n<width_n; x_n++) {
				int y = y_n;
				int x = x_n;

				if (method.equals("Kopie")) {

					if (y < height && x < width) { // ensure the new coordinates are within the original image bounds
						int pos_n = y_n * width_n + x_n; // position in new image
						int pos = y * width + x; // corresponding position in original image

						pix_n[pos_n] = pix[pos]; // copy pixel value
					}
				}


				if(method.equals("Pixelwiederholung")) {
					// Skalierungsfaktor - vom Original zum skalierten Bild
					double scaleFactorX = (double) width / width_n; // horizontal scaling factor
					double scaleFactorY = (double) height / height_n; // vertical scaling factor

					int pos_n = y_n * width_n + x_n; // position in new image

					// Pixel in Relation zum Faktor setzen
					// Originalpixel auslesen
					int xOld = (int) (x_n * scaleFactorX); // map new x to original x
					int yOld = (int) (y_n * scaleFactorY); // map new y to original y

					int pos = yOld * width + xOld; // corresponding position in original image
					pix_n[pos_n] = pix[pos]; // nearest pixel value
				}



				if(method.equals("Bilinear")){
					// Skalierungsfaktor zwischen altem und neuem Bild berechnen
					double scaleX = (double)(width) / (width_n); // horizontal scaling factor
					double scaleY = (double)(height) / (height_n); //  vertical scaling factor

					// Pixelpositionen initialisieren, (für die vier umliegenden Bildpunkte)
					int A = 0;
					int B = 0;
					int C = 0;
					int D = 0; // variables for surrounding pixel values

					int pos_n = y_n * width_n + x_n; // position in new image

					int xOld = (int) (scaleX * x_n); // original x coordinate
					int yOld = (int) (scaleY * y_n); // original y coordinate

					// Abstand vom alten zum neuen Pixel bestimmen (v und h aus der Formel)
					double h = (scaleX * x_n) - xOld; // horizontal distance from old pixel
					double v = (scaleY * y_n) - yOld; // vertical distance from old pixel


					int pos = xOld + yOld * width; // position in original image

					// pixel  A, B, C, D around the original coordinate
					// frist assume that B, C and D are the same as A before checking if they "legally" exist
					A = pix[pos]; // top-left pixel; pixel at (xOld, yOld)
					B = pix[pos]; // top-right pixel
					C = pix[pos]; // bottom-left pixel
					D = pix[pos]; // bottom-right pixel

					// Randbehandlung
					// 1) safely get B and handle D in right edge condition
					if (xOld != width-1) { // if x not at the right edge
						B = pix[pos+1]; // then take the right pixel --> B
						if (yOld == height-1) { // if y is at the bottom edge --> no pixel below
							D = pix[pos+1]; // bottom-right pixel same as right pixel (B or A)
						} else {
							D = pix[pos+1+width]; // bottom-right pixel --> D
						}
					}

					// 2) safely get C and handle D in right edge condition
					if (yOld != height-1) { // if y is not at the bottom edge
						C = pix[pos+width]; // C as bottom pixel
						if (xOld == width-1) { // if x is at the right edge
							D = pix[pos+width]; // bottom-right pixel same as bottom pixel
						} else {
							D = pix[pos+1+width]; // bottom-right pixel
						}
					}

					// RGB-Werte von A, B, C, D extrahieren
					// jeweils rot, grün und blau aus allen vier Punkten nehmen
					int rA = (A >> 16) & 0xff;
					int gA = (A >> 8) & 0xff;
					int bA = A & 0xff;

					int rB = (B >> 16) & 0xff;
					int gB = (B >> 8) & 0xff;
					int bB = B & 0xff;

					int rC = (C >> 16) & 0xff;
					int gC = (C >> 8) & 0xff;
					int bC = C & 0xff;

					int rD = (D >> 16) & 0xff;
					int gD = (D >> 8) & 0xff;
					int bD = D & 0xff;

					// Formel aus VL für bilineare Interpolation
					int rn = (int) (rA*(1-h)*(1-v) + rB*h*(1-v) + rC*(1-h)*v + rD*h*v);
					int gn = (int) (gA*(1-h)*(1-v) + gB*h*(1-v) + gC*(1-h)*v + gD*h*v);
					int bn = (int) (bA*(1-h)*(1-v) + bB*h*(1-v) + bC*(1-h)*v + bD*h*v);

					rn = Math.min(Math.max(rn, 0), 255);
					gn = Math.min(Math.max(gn, 0), 255);
					bn = Math.min(Math.max(bn, 0), 255);

					pix_n[pos_n] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}
		}


		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}

	void showAbout() {
		IJ.showMessage("");
	}
}











/*
1. Scaling Factors: Calculate how much each pixel in the new image maps to the original image.
2. Mapping New to Old: For each pixel in the new image, determine where it maps in the original image.
3. Relative Position: Find the fractional distances within the surrounding pixels.
4. Surrounding Pixels: Identify the four surrounding pixels in the original image.
5. Boundary Conditions: Handle cases where pixels might be at the edge of the image.
6. Extract and Interpolate RGB: Extract the color values of the surrounding pixels and compute the interpolated color.
7. Clamping and Storing: Ensure that the RGB values are valid and store them in the new image's pixel array.
 */

