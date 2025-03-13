package U2;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GRDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	IJ.open("/users/images/orchid.jpg");
		
		GRDM_U2 pw = new GRDM_U2();
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
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;

		private double brightness;
		private double contrast;
		private double saturation;
		private double hue;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", -128, 128, 0);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 10, 5);
			jSliderSaturation = makeTitledSilder("Sättigung", 0, 8, 4);
			jSliderHue = makeTitledSilder("Hue", 0, 360, 0);

            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
			panel.add(jSliderSaturation);
			panel.add(jSliderHue);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			if(maxVal-minVal>=10) {
				slider.setMajorTickSpacing((maxVal - minVal) / 10);
			} else {
				slider.setMajorTickSpacing((maxVal - minVal) / 8);
			}
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue();
				String str = "Helligkeit " + brightness;
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				contrast = slider.getValue();
				if(contrast<=5){
					contrast *= 0.2;
					contrast = Math.round(contrast*10.0)/10.0;
				} else{
					contrast = (2*contrast)-10;
				}
				String str = "Kontrast " + contrast;
				setSliderTitle(jSliderContrast, str);
			}

			if (slider == jSliderSaturation) {
				saturation = slider.getValue();
				if(saturation<=4){
					saturation *= 0.25;
				} else{
					saturation -= 3;
				}
				String str = "Sättigung " + saturation;
				setSliderTitle(jSliderSaturation, str);
			}

			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue " + hue;
				setSliderTitle(jSliderHue, str);
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array für den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					// Farben aus dem Pixel einzeln herausziehen
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;


					// RGB zu YUV
					double Y = 0.299 * r + 0.587 * g + 0.114 * b;
					double U = 0.493 * (b - Y);
					double V = 0.877 * (r - Y);


					// Helligkeit
					Y = Y + brightness;

					// Kontrast
					Y = (Y - 128) * contrast + 128;
					U = U * contrast;
					V = V * contrast;

					// Sättigung
					U = U * saturation;
					V = V * saturation;

					// Hue
					double hue2 = Math.toRadians(hue);
					U = U * Math.cos(hue2) - V * Math.sin(hue2);
					V = U * Math.sin(hue2) + V * Math.cos(hue2);



					// YUV zu RGB (nachdem YUV-Werte verändert worden sind)
					int rn = (int) (Y + V/0.877);
					int bn = (int) (Y + U/0.493);
					int gn = (int) (1/0.587 * Y - 0.299/0.587 * rn - 0.114/0.587 * bn); //nach unten, da Bezug auf 'bn'

					
					
					// RGB-Werte auf den Bereich von 0 bis 255 begrenzt werden (damit kein Überlauf passiert)
					rn = Math.min(Math.max(rn,0),255);
					gn = Math.min(Math.max(gn,0),255);
					bn = Math.min(Math.max(bn,0),255);


					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
		
    } // CustomWindow inner class
} 
