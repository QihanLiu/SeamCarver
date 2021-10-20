/**
 *
 * @author Qihan Liu
 * Reference: https://github.com/jonike/SeamCarving/blob/master/SeamCarver.java
 */

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Picture;
import java.awt.Color;

public class SeamCarver {
    private double[][] energyMatrix;
    private int[][] colorMatrix;
    private int picW;
    private int picH;
    private static final double BORDER_ENERGY = 1000.0;
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        checkNull(picture, "Cant take null input as picture");
        // get width and height
        picW = picture.width();
        picH = picture.height();
        // initial energy matrix
        energyMatrix = new double[picW][picH];
        // picture is in index of col-row. so w is 1st ind, h is 2nd.
        // w is vertical index, h is horizontal index.
        // get color as int.
        colorMatrix = new int[picW][picH];
        for (int w = 0; w < picW; w++) {
            for (int h = 0; h < picH; h++) {
                colorMatrix[w][h] = picture.get(w, h).getRGB();
            }
        }
        // update/initial full matrix;
        updateEnergy(null, true);
    }

    // current picture
    public Picture picture() {
        Picture picture = new Picture(picW, picH);
        for (int w = 0; w < picW; w++) {
            for (int h = 0; h < picH; h++) {
                picture.set(w, h, new Color(colorMatrix[w][h]));
            }
        }
        return picture;
    }
    // width of current picture
    public int width() {
        return picW;
    }
    // height of current picture
    public int height() {
        return picH;
    }

    private void updateEnergy(int[] seam, boolean isVertical) {
        // initial the whole map
        if (seam == null) {
            for (int w = 0; w < picW; w++) {
                for (int h = 0; h < picH; h++) {
                    energyMatrix[w][h] = energy(w, h);
                }
            }
            return;
        }
        // seam is validated in remove functions.
        // vertical in the sense of matrix. seam provides h.
        // noted that matrix is transpose from picture.
        // so the line is a horizontal line in picture.
        else if (isVertical) {
            for (int w = 0; w < picW; w++) {
                // update every matrix block that contacts the seam
                int h = seam[w];
                updateEnergy(w, h);
                updateEnergy(w, h-1);
                updateEnergy(w, h+1);
            }
        }
        else {
            for (int h = 0; h < picH; h++) {
                // update every matrix block that contacts the seam
                int w = seam[h];
                updateEnergy(w, h);
                updateEnergy(w-1, h);
                updateEnergy(w+1, h);
            }
        }
    }

    private void updateEnergy(int w, int h) {
        if (w >= 0 && w < picW && h >= 0 && h < picH) {
            energyMatrix[w][h] = energy(w, h);
        }
    }
    // energy of pixel at column x and row y
    public double energy(int w, int h) {
        checkRange(w, h, picW, picH, "w, h out of range!");
        if (w == 0 || h == 0 || w == picW - 1 || h == picH - 1) {
            return BORDER_ENERGY;
        }
        double gradient = 0;
        // Needs to transpose the picture.
        Color colorup = new Color(colorMatrix[w-1][h]);
        Color colordown = new Color(colorMatrix[w+1][h]);
        gradient += gradient(colorup, colordown);
        Color colorleft = new Color(colorMatrix[w][h-1]);
        Color colorright = new Color(colorMatrix[w][h+1]);
        gradient += gradient(colorleft, colorright);
        return Math.sqrt(gradient);
    }
    // Calculate energy of 2 color block
    private double gradient(Color x, Color y) {
        return Math.pow(x.getRed() - y.getRed(), 2) + Math.pow(x.getGreen() - y.getGreen(), 2)
                + Math.pow(x.getBlue() - y.getBlue(), 2);
    }

    // sequence of indices for horizontal seam
    // horizontal in the sense of pictures.
    // use Dynamic Programming to update each line
    // find vertical seam in matrix
    // picture is transpose -> find horizontal seam in pic.
    public int[] findHorizontalSeam() {
        // minimal energy to current point
        double[][] energyTo = new double[picW][picH];
        // initial first line and others
        for (int w = 0; w < picW; w++) {
            for (int h = 0; h < picH; h++) {
                if (w == 0) {
                    energyTo[w][h] = BORDER_ENERGY;
                    continue;
                }
                energyTo[w][h] = Double.POSITIVE_INFINITY;
            }
        }
        // minimal path from last layer to current.
        int[][] edgeFrom = new int[picW][picH];
        double energyTemp = 0;
        // Go through each layer
        for (int w = 1; w < picW; w++) {
            // calculate minimal distance to this point
            for (int h = 0; h < picH; h++) {
                // it can come from 3 directions, find minimal path
                for (int i = h - 1; i < h + 2; i++) {
                    if (i < 0 || i >= picH) {
                        continue;
                    }
                    energyTemp = energyMatrix[w][h] + energyTo[w - 1][i];
                    // record if it is the shortes among 3 direction.
                    if (energyTemp < energyTo[w][h]) {
                        energyTo[w][h] = energyTemp;
                        edgeFrom[w][h] = (w - 1) * picH + i;
                    }
                }
            }
        }
        // find the end point of shortest path
        energyTemp = Double.POSITIVE_INFINITY;
        int ind = -1;
        for (int j = 0; j < picH; j++) {
            if (energyTo[picW - 1][j] < energyTemp) {
                energyTemp = energyTo[picW - 1][j];
                ind = j;
            }
        }
        
        // read from last layer, trace to top
        int[] seam = new int[picW];
        seam[picW - 1] = ind;
        for (int i = picW - 1; i > 0; i--) {
            seam[i - 1] = edgeFrom[i][seam[i]] % picH;
        }
        return seam;
    }

    // sequence of indices for vertical seam
    // vertical in the sense of pictures.
    // Can also implemented by transpose energy matrix and use find horizontal
    public int[] findVerticalSeam() {
        // minimal energy to current point
        double[][] energyTo = new double[picW][picH];
        // initial first line and others
        for (int w = 0; w < picW; w++) {
            for (int h = 0; h < picH; h++) {
                // initial left row
                if (h == 0) {
                    energyTo[w][h] = BORDER_ENERGY;
                    continue;
                }
                energyTo[w][h] = Double.POSITIVE_INFINITY;
            }
        }
        // minimal path from last layer to current.
        int[][] edgeFrom = new int[picW][picH];
        double energyTemp = 0;
        // Go through each layer
        for (int h = 1; h < picH; h++) {
            // calculate minimal distance to this point
            for (int w = 0; w < picW; w++) {
                // it can come from 3 directions, find minimal path
                for (int i = w - 1; i < w + 2; i++) {
                    if (i < 0 || i >= picW) {
                        continue;
                    }
                    energyTemp = energyMatrix[w][h] + energyTo[i][h-1];
                    // record if it is the shortes among 3 direction.
                    if (energyTemp < energyTo[w][h]) {
                        energyTo[w][h] = energyTemp;
                        edgeFrom[w][h] = (h - 1) * picW + i;
                    }
                }
            }
        }
        // find the end point of shortest path
        energyTemp = Double.POSITIVE_INFINITY;
        int ind = -1;
        for (int j = 0; j < picW; j++) {
            if (energyTo[j][picH-1] < energyTemp) {
                energyTemp = energyTo[j][picH-1];
                ind = j;
            }
        }
        
        // read from last layer, trace to top
        int[] seam = new int[picH];
        seam[picH - 1] = ind;
        for (int i = picH - 1; i > 0; i--) {
            seam[i - 1] = edgeFrom[seam[i]][i] % picW;
        }
        return seam;
    }


    // remove horizontal seam from current picture
    public void removeVerticalSeam() {
        int[] seam = findVerticalSeam();
        checkSeam(seam, false);
        int[][] newColorMatrix = new int[picW-1][picH];
        double[][] newEnergyMatrix = new double[picW-1][picH];
        // copy matrix by col
        for (int h = 0; h < picH; h++) {
            for (int w = 0; w < picW; w++) {
                if (w < seam[h]) {
                    newColorMatrix[w][h] = colorMatrix[w][h];
                    newEnergyMatrix[w][h] = energyMatrix[w][h];
                }
                else if (w > seam[h]) {
                    newColorMatrix[w-1][h] = colorMatrix[w][h];
                    newEnergyMatrix[w-1][h] = energyMatrix[w][h];
                }
            }
        }
        picW -= 1;
        colorMatrix = newColorMatrix;
        energyMatrix = newEnergyMatrix;
        // update energy matrix with the blocks contact with seam
        updateEnergy(seam, false);
    }
    // remove vertical seam from current matrix
    // it is the horinzontal seam in picture
    public void removeHorizontalSeam() {
        int[] seam = findHorizontalSeam();
        checkSeam(seam, true);
        int[][] newColorMatrix = new int[picW][picH-1];
        double[][] newEnergyMatrix = new double[picW][picH-1];
        // copy the color matrix and energy matrix
        for (int w = 0; w < picW; w++) {
            // copy from left end to seam-1
            System.arraycopy(colorMatrix[w], 0, newColorMatrix[w], 0, seam[w]);
            System.arraycopy(energyMatrix[w], 0, newEnergyMatrix[w], 0, seam[w]);
            // copy from seam to right end
            System.arraycopy(colorMatrix[w], seam[w]+1, newColorMatrix[w], seam[w], picH-seam[w]-1);
            System.arraycopy(energyMatrix[w], seam[w]+1, newEnergyMatrix[w], seam[w], picH-seam[w]-1);
        }
        picH -= 1;
        colorMatrix = newColorMatrix;
        energyMatrix = newEnergyMatrix;
        // update energy matrix with the blocks contact with seam
        updateEnergy(seam, true);
    }

    private void checkSeam(int[] seam, boolean isVertical) {
        checkNull(seam, "Cant take null seam");
        int seamlen = picH, seamrange = picW;
        // vertical is for matrix
        if (isVertical) {
            seamlen = picW;
            seamrange = picH;
        }
        if (seamrange <= 1) {
            throw new IllegalArgumentException("Cannot remove seam since maplen is 1");
        }
        int len = seam.length;
        if (len != seamlen) {
            throw new IllegalArgumentException("V Seam length is not Height");
        }
        int prev = -1;
        for (int i : seam) {
            if (i < 0 || i >= seamrange) {
                throw new IllegalArgumentException("V seam out of range");
            }
            // 1st round
            if (prev == -1) {
                prev = i;
                continue;
            }
            else if (Math.abs(i - prev) > 1) {
                throw new IllegalArgumentException("Seam items are not connected");
            }
            prev = i;
        }
    }
    private static void checkNull(Object any, String message) {
        if (any == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkRange(int x, int y, int xmax, int ymax, String message) {
        if (x < 0 || x >= xmax || y < 0 || y >= ymax) {
            throw new IllegalArgumentException(message);
        }
    }

    public void printmatrix(double[][] matrix) {
        int matW = matrix.length;
        int matH = matrix[0].length;
        StdOut.println("W: " + matW + ", H: " + matH);
        for (int w = 0; w < matW; w++) {
            for (int h = 0; h < matH; h++) {
                StdOut.print(matrix[w][h] + ", ");
            }
            StdOut.println("");
        }
    }

    public void printmatrix(int[][] matrix) {
        int matW = matrix.length;
        int matH = matrix[0].length;
        StdOut.println("W: " + matW + ", H: " + matH);
        for (int w = 0; w < matW; w++) {
            for (int h = 0; h < matH; h++) {
                StdOut.print(matrix[w][h] + ", ");
            }
            StdOut.println("");
        }
    }

    // unit testing (optional)
    public static void main(String[] args) {
        Picture pic = new Picture("HJocean.png");
        pic.show();
        SeamCarver seampic = new SeamCarver(pic);
        // seampic.printmatrix(seampic.energyMatrix);
        // int[] seam = seampic.findHorizontalSeam();
        // for (int i : seam) {
        //     StdOut.print(i + ",");
        // }
        // StdOut.println("");
        // seampic.removeHorizontalSeam();
        // seampic.printmatrix(seampic.energyMatrix);
        // seampic.removeVerticalSeam();
        // seampic.printmatrix(seampic.energyMatrix);
        for (int i = 0; i < 200; i++) {
            seampic.removeHorizontalSeam();
        }
        for (int i = 0; i < 500; i++) {
            seampic.removeVerticalSeam();
        }
        seampic.picture().show();
    }

}