import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jtransforms.fft.DoubleFFT_2D;

/**
 * 
 * @author ¬ладимир
 *
 */
public class filters {

	picture Pic;
	double[][] filter;
	int N;
	int M;

	public filters(picture Pic) {
		this.Pic = Pic;
		N = Pic.getHeight();
		M = Pic.getWidth();
		filter = new double[N][M*2];
		result = new double[N][M*2];
		resultR = new double[N][M*2];
		resultG = new double[N][M*2];
		resultB = new double[N][M*2];
	}

	// Метод получения части изображения
	public void getPic(picture Pic) { 
		Pic.getColorPart(20, 40, 30, 50, Pic.getMonochrome());
	}

	double[][] result;
	double[][] resultR;
	double[][] resultG;
	double[][] resultB;
	
	public void matrtoIm(double kR, double kG, double kB, int n) throws IOException { //Получение изображения по матрицам яркостей
		File file = new File(Pic.getName() + ".jpg");
		BufferedImage source = ImageIO.read(file);
		BufferedImage res = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		for (int i = 0; i < source.getHeight(); i++) {
			for (int j = 0; j < source.getWidth(); j++) {
				int newRed = (int) ((resultR[i][j * 2] / kR) * (Math.pow(-1, i + j)));
				if (newRed < 0)
					newRed = 0;
				if (newRed > 255)
					newRed = 255;
				int newGreen = (int) ((resultG[i][j * 2] / kG) * (Math.pow(-1, i + j)));
				if (newGreen < 0)
					newGreen = 0;
				if (newGreen > 255)
					newGreen = 255;
				int newBlue = (int) ((resultB[i][j * 2] / kB) * (Math.pow(-1, i + j)));
				if (newBlue < 0)
					newBlue = 0;
				if (newBlue > 255)
					newBlue = 255;
				Color newColor = new Color(newRed, newGreen, newBlue);
				int grey = (int) (newRed * 0.299 + newGreen * 0.587 + newBlue * 0.114);
				res.setRGB(j, i, newColor.getRGB());
			}
		}
		File output = new File(Pic.getName() + n + ".jpg");
		ImageIO.write(res, "jpg", output);
	}


	public void complexMult(double[][] res, double[][] arg) { //Комплексное умножение 
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				res[i][j*2] = (arg[i][j*2]*filter[i][j*2]-arg[i][j*2+1]*filter[i][j*2+1]);
				res[i][j*2+1] = (filter[i][j*2]*arg[i][j*2+1]+arg[i][j*2]*filter[i][j*2+1]);
			}
		}
	}
	
	public void complexDiv(double[][] res, double[][] arg) { // Комплексное деление
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				res[i][j*2] = (arg[i][j*2]*filter[i][j*2]+arg[i][j*2+1]*filter[i][j*2+1])/
						(filter[i][j*2]*filter[i][j*2]+filter[i][j*2+1]*filter[i][j*2+1]);
				res[i][j*2+1] = (filter[i][j*2]*arg[i][j*2+1]-arg[i][j*2]*filter[i][j*2+1])/
						(filter[i][j*2]*filter[i][j*2]+filter[i][j*2+1]*filter[i][j*2+1]);
			}
		}
	}

	public double complexSum(double[][] arg) { // Комплексная сумма
		double sum = 0;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				sum = sum + Math.hypot(arg[i][j*2],arg[i][j*2+1])*Math.hypot(arg[i][j*2],arg[i][j*2+1]);
			}
		}
		return sum;
	}
	
	public void filterBut(int arg, int n) { //Формирование Фильтра Баттерворта
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				double D = Math.pow(((i-N/2)*(i-N/2)+(j-M/2)*(j-M/2)),1./2.);
				filter[i][j*2] = 1-(1 / (1 + Math.pow((D / arg), n))) + 0.1;
			}
		}
	}
	
	
	public void FilterT(int min, int max) { //Формирование фильтра для имитации эффекта турбулентности атмосферы
		int a = min;
		int b = max;
		int x = a + (int) (Math.random() * ((b - a) + 1));
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				filter[i][j*2] = Math.exp(-1* Math.pow(((i-M/2)*(i-M/2)+(j-N/2)*(j-N/2))/(x),5./6.));
			}
		}
	}
	
	public void FilterT2(int min, int max) { //Формирование фильтра для имитации эффекта турбулентности атмосферы
		double rom = 5.92/(2*Math.PI*0.01);
		int a = min;
		int b = max;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				double x = (Math.random());
				double ro = (i-M/2)*(i-M/2)+(j-N/2)*(j-N/2);
				filter[i][j*2] = Math.sqrt((0.023*(Math.exp(-(ro)/(rom*rom))))/(Math.pow(0.01, 5./3.)*Math.pow((ro+0.01),11./6.)));
			}
		}
		DoubleFFT_2D fft = new DoubleFFT_2D(N,M);
		fft.complexForward(filter);
	}
	
	public void FilterT3() { //Формирование фильтра для имитации эффекта турбулентности атмосферы
		double lam = 0.535E-6;
		double Cn = 42E-14;
		double z = 280;
		double r0 = 0.185*Math.pow(((lam*lam)/(Cn*z)),3./5.);
		double d = 0.1;
		double l = 0.24;
		double D = 0.184;
		double roc = 1/(lam*(l/D));
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				double r = (i-M/2)*(i-M/2)+(j-N/2)*(j-N/2);
				if (r<roc)
					filter[i][j*2] = (2/Math.PI)*(Math.acos(r/(2*roc))-(r/(2*roc))*Math.sqrt(1-((r/(2*roc))*(r/(2*roc)))));
					else
						filter[i][j*2] = 0;
				filter[i][j*2] =filter[i][j*2]* Math.exp(-3.44*Math.pow((lam*r*l)/r0, 5./3.) * (1-Math.pow((lam*r*l)/(D),1./3.)));
			}
		}
		
	}
	
	public void filterGauss(int arg) { //Формирование Гаусова фильтра
		for (int i = 0; i<N; i++) { 
			for (int j = 0;j<M; j++) { 
				double D =Math.pow(((i-N/2)*(i-N/2)+(j-M/2)*(j-M/2)),1./2.);
				filter[i][j*2] =1+3*(1-Math.exp(-((D*D)/(arg*arg*2))));
				  
				  } 
			}
	}
	
	public void filterBut2(int arg) { // Второй вариант фильтра Баттерворта
		for (int i = 0; i<N; i++) { for (int j = 0;
				 j<M; j++) { 
			double D = Math.pow(((N/2-i)*(N/2-i)+(j-M/2)*(j-M/2)),1./2.); 
			filter[i][j*2] = 1-(1/(1+(Math.sqrt(2)-1)*Math.pow((D/arg), 2)))+0.2; 
			} 
		}
	}
	
	public void filterGauss2(int arg, int n) { //Второй вариант Гаусова фильтра
		for (int i = 0; i<N; i++) { 
			for (int j = 0;j<M; j++) { 
				double D = Math.pow(((i-N/2)*(i-N)+(j-M/2)*(j-M/2)),1./2.);
				filter[i][j*2] = 1-Math.exp(-(Math.PI*5*5*(i*i+j*j))/(N*M));
			} 
		}
	} 

	public void process(int n) throws IOException { // Метод для обработки изображения
		
		complexMult(result, Pic.getMonochrome()); // Комплексное умножение ИЗображения на фильтра
		complexMult(resultR, Pic.getRed());      // для каждого из трех цветов и монохромного изображения
		complexMult(resultG, Pic.getGreen());
		complexMult(resultB, Pic.getBlue());

		DoubleFFT_2D fft = new DoubleFFT_2D(N,M);

		fft.complexInverse(result, true); // Превод изображения из частотной в действительную область
		fft.complexInverse(resultR, true);
		fft.complexInverse(resultG, true);
		fft.complexInverse(resultB, true);

		double sum2 = 0;
		double sum2R = 0;
		double sum2G = 0;
		double sum2B = 0;

		sum2 = complexSum(result); 
		sum2R = complexSum(resultR);
		sum2G = complexSum(resultG);
		sum2B = complexSum(resultB);
		
		picture P2 = new picture(N,M*2);
		P2.setRed(resultR);
		P2.setGreen(resultG);
		P2.setBlue(resultB);

		sum2 = Math.sqrt(sum2 / (N* M)); // Средняя яркость изображения для каждого цвета
		sum2R = Math.sqrt(sum2R / (N*M));
		sum2G = Math.sqrt(sum2G / (N*M));
		sum2B = Math.sqrt(sum2B / (N*M));

		double k = sum2 / Pic.getsum();
		double kR = sum2R / Pic.getsum();
		double kG = sum2G / Pic.getsum();
		double kB = sum2B / Pic.getsum();

		P2.matrtoIm(kR, kG, kB, n); // Получение изображения из матриц яркостей

	}
}
