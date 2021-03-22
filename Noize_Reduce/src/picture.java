import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jtransforms.fft.DoubleFFT_2D;

public class picture {

	private double[][] pic; //Матрица яркостей монохромного изображения
	private double[][] red; //Матрица яркостей красной составляющей изображения
	private double[][] green; //Матрица яркостей зеленой составляющей изображения
	private double[][] blue; //Матрица яркостей синей составляющей изображения
	private int N; // Ширина изображения
	private int M; // Длинна изображения
	private String picName; 
	/*Средние яркости
	 * для всех цветов
	 */
	private double sum;  
	private double sumR;
	private double sumG;
	private double sumB;
	// Матрицы частотного представления изображения
	private double [][] complexRed;
	private double [][] complexGreen;
	private double [][] complexBlue;
	
	public picture() {
		
	}
	
	
	// Метод для получения части изображения
	public picture getPart(int minW, int maxW, int minH, int maxH) {
		picture P = new picture(maxH-minH, (maxW-minW)*2);
		P.setRed(this.getColorPart(minW, maxW, minH, maxH, red));
		P.setGreen(this.getColorPart(minW, maxW, minH, maxH, green));
		P.setBlue(this.getColorPart(minW, maxW, minH, maxH, blue));
		return P;
	}
	
	// Метод получения изображения из матриц яркостей
	public void matrtoIm(double kR, double kG, double kB, int n) throws IOException {
		BufferedImage res = new BufferedImage(M/2, N, 5);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < (M/2); j++) {
				int newRed = (int) ((red[i][j * 2] / kR) * (Math.pow(-1, i + j)));
				if (newRed < 0)
					newRed = 0;
				if (newRed > 255)
					newRed = 255;
				int newGreen = (int) ((green[i][j * 2] / kG) * (Math.pow(-1, i + j)));
				if (newGreen < 0)
					newGreen = 0;
				if (newGreen > 255)
					newGreen = 255;
				int newBlue = (int) ((blue[i][j * 2] / kB) * (Math.pow(-1, i + j)));
				if (newBlue < 0)
					newBlue = 0;
				if (newBlue > 255)
					newBlue = 255;
				Color newColor = new Color(newRed, newGreen, newBlue);
				int grey = (int) (newRed * 0.299 + newGreen * 0.587 + newBlue * 0.114);
				res.setRGB(j, i, newColor.getRGB());
			}
		}
		File output = new File(this.getName() + n + ".jpg");
		ImageIO.write(res, "jpg", output);
	}

	
	public picture(int N, int M) {
		this.pic = new double[N][M*2];
		this.red = new double[N][M*2];
		this.green = new double[N][M*2];
		this.blue = new double[N][M*2];
		this.complexRed = new double[N][M*2];
		this.complexGreen = new double[N][M*2];
		this.complexBlue = new double[N][M*2];
		this.N = N;
		this.M = M;
	}
	
	/* Метод используется для получения части изображения
	по каждой из составляющих цвета*/
	public double[][] getColorPart(int minW, int maxW, int minH, int maxH, double[][] pic) {
		double[][] part = new double[maxH-minH][(maxW-minW)*2];
		for (int i = 0; i<maxH-minH; i++) {
			for (int j = 0; j<maxW-minW; j++) {
				part[i][j*2] = pic[i+minH][j*2];
			}
		}
		return part;
	}
	
	
	// Метод для подготовки изображения к обработке
	public void setPic(String picName) throws IOException {
		this.picName = picName;
		File file = new File(picName + ".jpg");
		BufferedImage source = ImageIO.read(file);
		this.N = source.getHeight();
		this.M = source.getWidth();

		this.pic = new double[N][M * 2];
		this.red = new double[N][M * 2];
		this.green = new double[N][M * 2];
		this.blue = new double[N][M * 2];

		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				Color color = new Color(source.getRGB(x, y));
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
				pic[y][x * 2] = grey;
				red[y][x * 2] = (double) (r) * (Math.pow(-1, x + y));
				red[y][x * 2 + 1] = 0.0;
				green[y][x * 2] = (double) (g) * (Math.pow(-1, x + y));
				green[y][x * 2 + 1] = 0.0;
				blue[y][x * 2] = (double) (b) * (Math.pow(-1, x + y));
				blue[y][x * 2 + 1] = 0.0;
			}
		}

		sum = sum(pic);
		sumR = sum(red);
		sumG = sum(green);
		sumB = sum(blue);

		sum = Math.sqrt(sum / (source.getWidth() * source.getHeight()));
		sumR = Math.sqrt(sumR / (source.getWidth() * source.getHeight()));
		sumG = Math.sqrt(sumG / (source.getWidth() * source.getHeight()));
		sumB = Math.sqrt(sumB / (source.getWidth() * source.getHeight()));
		
		complexColor();
	}
	
	public void Copy(double[][] M1, double[][] M2) {
		//double[][] M2 = new double[M1.length][M1[0].length];
		for (int i = 0; i < M2.length; i++) {
			for (int j = 0; j < M2[0].length; j++) {
				M2[i][j] = M1[i][j];
			}
		}
	}
	
	/* Метод используется для получения частотного 
	представления изображения по каждой из составляющих цвета*/
	public void complexColor() {
		DoubleFFT_2D fft = new DoubleFFT_2D(N, M);
		complexRed = new double[red.length][red[0].length];
		complexGreen = new double[green.length][green[0].length];
		complexBlue = new double[blue.length][blue[0].length];
		Copy(red,complexRed);
		Copy(green,complexGreen);
		Copy(blue,complexBlue);
		fft.complexForward(complexRed);
		fft.complexForward(complexGreen);
		fft.complexForward(complexBlue);
		
	}
	
	
	/* Метод используется для получения суммы ярокстей
	по каждой из составляющих цвета*/
	public double sum(double[][] pic) {
		double res = 0;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				res = res + pic[i][j * 2] * pic[i][j * 2];
			}
		}
		return res;
	}


	public double[][] getRed() {
		return this.complexRed;
	}

	public double[][] getGreen() {
		return this.complexGreen;
	}

	public double[][] getBlue() {
		return this.complexBlue;
	}
	
	public void setRed(double[][] red) {
		this.red = red;
	}
	
	public void setGreen(double[][] green) {
		this.green = green;
	}
	
	public void setBlue(double[][] blue) {
		this.blue = blue;
	}

	public double[][] getMonochrome() {
		return this.pic;
	}

	public int getWidth() {
		return this.M;
	}

	public int getHeight() {
		return this.N;
	}

	public double getsum() {
		return this.sum;
	}

	public double getsumR() {
		return this.sumR;
	}

	public double getsumG() {
		return this.sumG;
	}

	public double getsumB() {
		return this.sumB;
	}

	public String getName() {
		return this.picName;
	}

}
