package U5;

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

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Weichgezeichnetes Bild", "Hochpass", "Verstärkte Kanten"};


	public static void main(String[] args) {

    	IJ.open("/users/images/orchid.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U5 pw = new GRDM_U5();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
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
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}

			if (method.equals("Weichgezeichnetes Bild")) {

				for (int y=0; y<height; y++) { //y, x = 0;
					for (int x=0; x<width; x++) {
						int pos = y*width + x;

						double[][] filter = {
								{1.0/9.0, 1.0/9.0, 1.0/9.0},
								{1.0/9.0, 1.0/9.0, 1.0/9.0},
								{1.0/9.0, 1.0/9.0, 1.0/9.0}};

						// Werte begrenzen
						int x_neu1 = max(0, x-1);
						int x_neu2 = min(x+1, width-1);

						int y_neu1 = max(0, y-1);
						int y_neu2 = min(y+1, height-1);

						int[][] currentPixels = {
								{origPixels[(y_neu1)*width+(x_neu1)], origPixels[(y_neu1)*width+(x)], origPixels[(y_neu1)*width+(x_neu2)]},
								{origPixels[(y)*width+(x_neu1)], origPixels[(y)*width+(x)], origPixels[(y)*width+(x_neu2)]},
								{origPixels[(y_neu2)*width+(x_neu1)], origPixels[(y_neu2)*width+(x)], origPixels[(y_neu2)*width+(x_neu2)]}};


						double[][] newRed = new double[3][3];
						double[][] newGreen = new double[3][3];
						double[][] newBlue = new double[3][3];


						int additionR = 0;
						int additionG = 0;
						int additionB = 0;


						for(int i = 0; i < 3; i++) {
							for (int j = 0; j < 3; j++) {
								newRed[i][j] = (currentPixels[i][j] >> 16) & 0xff;
								newGreen[i][j] = (currentPixels[i][j] >> 8) & 0xff;
								newBlue[i][j] = currentPixels[i][j] & 0xff;

								double multR = filter[i][j]*newRed[i][j];
								additionR = (int) (additionR + multR);
								double multG = filter[i][j]*newGreen[i][j];
								additionG = (int) (additionG + multG);
								double multB = filter[i][j]*newBlue[i][j];
								additionB = (int) (additionB + multB);
							}
						}

						pixels[pos] = (0xFF<<24) | (additionR<<16) | (additionG << 8) | additionB;
					}
				}
			}



			if (method.equals("Hochpass")) {

				for (int y=0; y<height; y++) { //y, x = 0;
					for (int x=0; x<width; x++) {
						int pos = y*width + x;

						double[][] filter = {
								{-(1.0/9.0), -(1.0/9.0), -(1.0/9.0)},
								{-(1.0/9.0), 8.0/9.0, -(1.0/9.0)},
								{-(1.0/9.0), -(1.0/9.0), -(1.0/9.0)}};

						// Werte begrenzen
						int x_neu1 = max(0, x-1);
						int x_neu2 = min(x+1, width-1);

						int y_neu1 = max(0, y-1);
						int y_neu2 = min(y+1, height-1);

						int[][] currentPixels = {
								{origPixels[(y_neu1)*width+(x_neu1)], origPixels[(y_neu1)*width+(x)], origPixels[(y_neu1)*width+(x_neu2)]},
								{origPixels[(y)*width+(x_neu1)], origPixels[(y)*width+(x)], origPixels[(y)*width+(x_neu2)]},
								{origPixels[(y_neu2)*width+(x_neu1)], origPixels[(y_neu2)*width+(x)], origPixels[(y_neu2)*width+(x_neu2)]}};



						double[][] newRed = new double[3][3];
						double[][] newGreen = new double[3][3];
						double[][] newBlue = new double[3][3];


						int additionR = 128;
						int additionG = 128;
						int additionB = 128;


						for(int i = 0; i < 3; i++) {
							for (int j = 0; j < 3; j++) {
								newRed[i][j] = (currentPixels[i][j] >> 16) & 0xff;
								newGreen[i][j] = (currentPixels[i][j] >> 8) & 0xff;
								newBlue[i][j] = currentPixels[i][j] & 0xff;

								double multR = filter[i][j]*newRed[i][j];
								additionR = (int) (additionR + multR);
								double multG = filter[i][j]*newGreen[i][j];
								additionG = (int) (additionG + multG);
								double multB = filter[i][j]*newBlue[i][j];
								additionB = (int) (additionB + multB);
							}
						}

						pixels[pos] = (0xFF<<24) | (additionR<<16) | (additionG << 8) | additionB;
					}
				}
			}



			if (method.equals("Verstärkte Kanten")) {

				for (int y=0; y<height; y++) { //y, x = 0;
					for (int x=0; x<width; x++) {
						int pos = y*width + x;

						double[][] filter = {
								{-(1.0/9.0), -(1.0/9.0), -(1.0/9.0)},
								{-(1.0/9.0), 17.0/9.0, -(1.0/9.0)},
								{-(1.0/9.0), -(1.0/9.0), -(1.0/9.0)}};

						// Werte begrenzen
						int x_neu1 = max(0, x-1);
						int x_neu2 = min(x+1, width-1);

						int y_neu1 = max(0, y-1);
						int y_neu2 = min(y+1, height-1);

						int[][] currentPixels = {
								{origPixels[(y_neu1)*width+(x_neu1)], origPixels[(y_neu1)*width+(x)], origPixels[(y_neu1)*width+(x_neu2)]},
								{origPixels[(y)*width+(x_neu1)], origPixels[(y)*width+(x)], origPixels[(y)*width+(x_neu2)]},
								{origPixels[(y_neu2)*width+(x_neu1)], origPixels[(y_neu2)*width+(x)], origPixels[(y_neu2)*width+(x_neu2)]}};



						double[][] newRed = new double[3][3];

						double[][] newGreen = new double[3][3];
						double[][] newBlue = new double[3][3];


						int additionR = 0;
						int additionG = 0;
						int additionB = 0;


						for(int i = 0; i < 3; i++) {
							for (int j = 0; j < 3; j++) {
								newRed[i][j] = (currentPixels[i][j] >> 16) & 0xff;
								newGreen[i][j] = (currentPixels[i][j] >> 8) & 0xff;
								newBlue[i][j] = currentPixels[i][j] & 0xff;

								double multR = filter[i][j]*newRed[i][j];
								additionR = (int) (additionR + multR);
								double multG = filter[i][j]*newGreen[i][j];
								additionG = (int) (additionG + multG);
								double multB = filter[i][j]*newBlue[i][j];
								additionB = (int) (additionB + multB);
							}
						}

						additionR = min(max(additionR,0),255);
						additionG = min(max(additionG,0),255);
						additionB = min(max(additionB,0),255);

						pixels[pos] = (0xFF<<24) | (additionR<<16) | (additionG << 8) | additionB;
					}
				}
			}
		}
	} // CustomWindow inner class
} 
