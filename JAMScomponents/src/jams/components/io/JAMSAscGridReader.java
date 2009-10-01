/*
 * JAMSArcGridReader.java
 *
 * Created on 16. Mai 2007, 11:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 *
 * @author Christian(web)
 */
public class JAMSAscGridReader {
    int ncols;
    int nrows;
    
    double x11corner;
    double y11corner;
    
    double cellsize;
    
    double nodatavalue = 0.0;
    
    double grid[][];
    double lowest_value = Double.POSITIVE_INFINITY;
    
    public JAMSAscGridReader(String path) {
	try {
	    //search for ncols, nrows, x11corner, y11corner, cellsize, nodatevalue
	    BufferedReader reader = new BufferedReader(new FileReader(path));		    	    	    
	    int datafound = 0;
	    String line;
	    StringTokenizer st;
	    
	    while (datafound < 5) {		
		line = reader.readLine();	    
		st = new StringTokenizer(line);
		while (st.hasMoreTokens()) {
		    String tok = st.nextToken();
		    if (tok.contains("ncols")) {
			ncols = new Integer(st.nextToken()).intValue();
			datafound++;
		    }
		    if (tok.contains("nrows")) {
			nrows = new Integer(st.nextToken()).intValue();
			datafound++;
		    }
		    if (tok.contains("xllcorner")) {
			x11corner = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }
		    if (tok.contains("yllcorner")) {
			y11corner = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }
		    if (tok.contains("cellsize")) {
			cellsize = new Double(st.nextToken()).doubleValue();
			datafound++;
		    }		    
		}	    
	    }	    
	    //now read grid
	    grid = new double[ncols][nrows];
	    int x = 0;
	    int y = 0;
	    boolean firsttoken = true;	    
	    while ( (line = reader.readLine()) != null) {		
		st = new StringTokenizer(line);	
		
		while (st.hasMoreTokens()) {
		    if (y >= nrows) {			
			System.out.println("error to many entrys");
			break;
		    }
		    String tok = st.nextToken();
		    //look for optional components
		    if (firsttoken) {
			firsttoken = false;
			//optional
			if (tok.contains("NODATA_value")) {		    
			    nodatavalue = new Double(st.nextToken()).doubleValue();
			}
			if (st.hasMoreTokens()) {
			    tok = st.nextToken();
			}
			else
			    continue;
		    }
		    grid[x][y] = new Double(tok).doubleValue();
		    
		    if (Math.abs(grid[x][y]-this.nodatavalue) < 0.0001) {
			grid[x][y] = -1.0;
		    }		    		    
		    if (grid[x][y] != -1.0 && grid[x][y] < lowest_value) {
			lowest_value = grid[x][y];
		    }
		    x++;
		    if (x >= ncols) {
			x = 0;
			y++;
		    }		    
		}
	    }
	}
	catch (Exception e) {
	    System.out.println(e.toString());
	}		
	//repair grid
	/*boolean stillchange = true;
	while (stillchange) {
	    stillchange = false;
	    for (int x=0;x<ncols;x++) {
		for (int y=0;y<nrows;y++) {
		    if (grid[x][y] == -1.0) {
			if (x < ncols-1 && y > 0 && grid[x+1][y] != -1.0)
			    grid[x][y] = grid[x+1][y-1];			
			else if (x < ncols-1 && grid[x+1][y] != -1.0)
			    grid[x][y] = grid[x+1][y];
			else if (x < ncols-1 && y < nrows - 1 && grid[x+1][y+1] != -1.0)
			    grid[x][y] = grid[x+1][y+1];			
			else if (y < nrows - 1 && grid[x][y+1] != -1.0)
			    grid[x][y] = grid[x][y+1];			
			else if (y > 0  && grid[x][y-1] != -1.0)
			    grid[x][y] = grid[x][y-1];			
			else if (x > 0  && y > 0 && grid[x-1][y] != -1.0)
			    grid[x][y] = grid[x-1][y-1];			
			else if (x > 0  && grid[x-1][y] != -1.0)
			    grid[x][y] = grid[x-1][y];
			else if (x > 0  && y < nrows - 1 && grid[x-1][y+1] != -1.0)
			    grid[x][y] = grid[x-1][y+1];
			
			stillchange = true;
		    }
		}
	    }
	}*/
    }
    public double getValue(double x, double y) {
	int East,West,South,North;
	
	y = y11corner + nrows*cellsize - (y - y11corner);
	
	East = (int)( (x - x11corner) / this.cellsize);
	West = East + 1;
	
	North = (int)( (y - y11corner) / this.cellsize);
	South = North + 1;
		
	if (East < 0 || East >= this.ncols || North < 0 || North >= this.nrows)
	    return -1.0;
	if (West >= this.ncols || South >= this.nrows)
	    return grid[East][North];
	
	if (grid[East][North] == -1 || grid[West][North] == -1 || grid[East][South] == -1 || grid[West][South] == -1) {
	    return -1.0;
	}
	    
	double xA = East*cellsize + x11corner;
	double yA = North*cellsize + y11corner;
	double zA = grid[East][North];
		
	double xB = (East+1)*cellsize + x11corner;
	double yB = North*cellsize + y11corner;
	double zB = grid[East+1][North];
	
	double xC = (East+1)*cellsize + x11corner;
	double yC = (North+1)*cellsize + y11corner;
	double zC = grid[East+1][North+1];
	
	double xD = East*cellsize + x11corner;
	double yD = (North+1)*cellsize + y11corner;
	double zD = grid[East][North+1];
		
	/*
	A              B
	 --------------
	 |             |
	 |             |
	 --------------
	D             C
	*/
	//bilinear interpolation
	double rx = x - xD, ry = y - yD;
	double result = 0.0;
	if ( (xA - xC)*rx + (yA - yC)*ry > 0) {
	    //befinden uns in dreieck ABD
	    double lambda = (x - xA) / (xB - xA);
	    double theta  = (y - yA) / (yD - yA);
	    
	    result =  zA + lambda*(zB-zA) + theta*(zD - zA);
	}
	else {
	    //befinden uns in dreieck DBC
	    double lambda = (x - xC) / (xD - xC);
	    double theta  = (y - yC) / (yB - yC);
	    
	    result =  zC + lambda*(zD-zC) + theta*(zB - zC);
	}
	
	return result;
    }
    
    public double GetX11Corner(){   return this.x11corner;  }
    public double GetY11Corner(){   return this.y11corner;  }
    public int GetNumberOfColums(){   return this.ncols;  }
    public int GetNumberOfRows(){   return this.nrows;  }
    public double GetCellSize() {return this.cellsize;}
}
