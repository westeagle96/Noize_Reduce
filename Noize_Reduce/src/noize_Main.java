import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class noize_Main {
	

	public static void main(String[] args) throws IOException {
		
		picture Pic = new picture();
		Pic.setPic("p1__Vp657ZY");
		filters f = new filters(Pic);
		/*picture P = Pic.getPart(300, 350, 300, 400);
		P.matrtoIm(1, 1, 1, 3);
		filters f = new filters(Pic);
		f.filterBut(30,2);
		f.process(2);*/
		
		
		/*for (int i = 1; i<20; i++) {
			f.FilterT(20000,21000);
			f.process(i);
		}*/
		//f.filterBut(70,2);
		f.FilterT3();
		f.process(2);
		
	}

}
