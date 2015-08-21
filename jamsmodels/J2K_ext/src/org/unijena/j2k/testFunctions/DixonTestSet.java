/*
 * DixonTestSet.java
 * Created on 06. May 2008, 14:25
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.j2k.testFunctions;

import jams.model.*;
import jams.data.*;
 
/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="Test Functions from Dixon Test",
        author="Christian Fischer",
        description="Test functions for optimizers from http://www-optima.amp.i.kyoto-u.ac.jp/member/student/hedar/Hedar_files/TestGO.htm"
        )
        
public class DixonTestSet extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X1"
            )
            public Attribute.Double paraX1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX2; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X3"
            )
            public Attribute.Double paraX3;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX4;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X1"
            )
            public Attribute.Double paraX5;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX6; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X3"
            )
            public Attribute.Double paraX7;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX8;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X1"
            )
            public Attribute.Double paraX9;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX10; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X3"
            )
            public Attribute.Double paraX11;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX12;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X1"
            )
            public Attribute.Double paraX13;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX14; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X3"
            )
            public Attribute.Double paraX15;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX16;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Integer function;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "function y"            
            )
            public Attribute.Double yVal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "function y"            
            )
            public Attribute.Double option;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "function y"            
            )
            public Attribute.Integer n;

    public void run() {
        if (function == null){
            this.getModel().getRuntime().sendHalt("parameter function must be specified!");
            return;
        }
        double x1=0,x2=0,x3=0,x4=0,x5=0,x6=0,x7=0,x8=0,
               x9=0,x10=0,x11=0,x12=0,x13=0,x14=0,x15=0,x16=0;
        int n=this.n.getValue();
        if (paraX1!=null){x1 = paraX1.getValue();}
        if (paraX2!=null){x2 = paraX2.getValue();}
        if (paraX3!=null){x3 = paraX3.getValue();}
        if (paraX4!=null){x4 = paraX4.getValue();}
        if (paraX5!=null){x5 = paraX5.getValue();}
        if (paraX6!=null){x6 = paraX6.getValue();}
        if (paraX7!=null){x7 = paraX7.getValue();}
        if (paraX8!=null){x8 = paraX8.getValue();}
        if (paraX9!=null){x9 = paraX9.getValue();}
        if (paraX10!=null){x10 = paraX10.getValue();}
        if (paraX11!=null){x11 = paraX11.getValue();}
        if (paraX12!=null){x12 = paraX12.getValue();}
        if (paraX13!=null){x13 = paraX13.getValue();}
        if (paraX14!=null){x14 = paraX14.getValue();}
        if (paraX15!=null){x15 = paraX15.getValue();}
        if (paraX16!=null){x16 = paraX16.getValue();}
        double x[] = new double[16];
        x[0] = x1;x[1] = x2;x[2] = x3;x[3] = x4;
        x[4] = x5;x[5] = x6;x[6] = x7;x[7] = x8;
        x[8] = x9;x[9] = x10;x[10] = x11;x[11] = x12;
        x[12] = x13;x[13] = x14;x[14] = x15;x[15] = x16;
        
        double r = 0;
        
        switch(function.getValue()){
            //Ackley Function
            //Minimum x = (0,0) y = 0
            //Bounds -15 < x < 30
            case 1: {               
                double a = 20, b = 0.2, c = 2*Math.PI;
                double s1 = 0, s2 = 0;
                for (int i=0;i<n;i++){
                    s1 += x[i]*x[i];
                    s2 += Math.cos(2.0*Math.PI*x[i]);
                }
                s1 /= n;
                s2 /= n;
                
                r = -a*Math.exp(-b*Math.sqrt(s1))-Math.exp(s2)+a+Math.exp(1);
                break;
            }//modified version not centered at zero
            case 1001: {
                double a = 20, b = 0.2, c = 2*Math.PI;
                double shift = 1.0/3.0;
                double s1 = 0, s2 = 0;
                for (int i=0;i<n;i++){
                    s1 += (x[i]+shift)*(x[i]+shift);
                    s2 += Math.cos(c*(x[i]+shift));
                }
                s1 /= n;
                s2 /= n;

                r = -a*Math.exp(-b*Math.sqrt(s1))-Math.exp(s2)+a+Math.exp(1);
                break;
            }
            //Beale's Function
            //Minimum x = (3,0.5) y = 0
            //Bounds -4.5 < x < 4.5
            case 2:  { 
                r = Math.pow((1.5-x1*(1-x2)),2)+Math.pow((2.25-x1*(1-x2*x2)),2)+Math.pow((2.625-x1*(1-x2*x2*x2)),2);
                break;
            }
            //Bohachevsky Function 1
            //Minimum x = (0,0.0) y = 0
            //Bounds -100 < x < 100
            case 3: {
                r=x1*x1+2*x2*x2-0.3*Math.cos(3*Math.PI*x1)-0.4*Math.cos(4*Math.PI*x2)+0.7;
                break;
            }
            //Bohachevsky Function 2
            //Minimum x = (0,0.0) y = 0
            //Bounds -100 < x < 100
            case 4: {
                r=x1*x1+2*x2*x2-0.3*Math.cos(3*Math.PI*x1)*Math.cos(4*Math.PI*x2)+0.3;
                break;
            }
            //Bohachevsky Function 3
            //Minimum x = (0,0.0) y = 0
            //Bounds -100 < x < 100
            case 5: {
                r = x1*x1+2*x2*x2-0.3*Math.cos(3*Math.PI*x1+4*Math.PI*x2)+0.3;
                break;
            }
            //Booth Function
            //Minimum x = (1,3.0) y = 0
            //Bounds -10 < x < 10
            case 6: {
                r = Math.pow(x1+2*x2-7,2.0)+Math.pow(2*x1+x2-5,2.0);
                break;
            }
            //Branin Function
            //Minimum x = (-Pi , 12.275), (Pi , 2.275), (9.42478, 2.475) y = 0.397887
            //Bounds -5 < x1 < 10; 0 < x2 < 15
            case 7: {
                r = Math.pow((x2-(5.1/(4*Math.PI*Math.PI))*x1*x1+5*x1/Math.PI-6),2.0)+10*(1-1/(8*Math.PI))*Math.cos(x1)+10;
                break;
            }
            //Colville Function
            //Minimum x = (1, 1, 1, 1) y = 0
            //Bounds -10 < x < 10;
            case 8: {
                r = 100*Math.pow((x1*x1-x2),2)+Math.pow((x1-1),2)+Math.pow((x3-1),2)
                        +90*Math.pow((x3*x3-x4),2)+10.1*(Math.pow((x2-1),2)+
                        Math.pow((x4-1),2))+19.8*(1-x2)*(x4-1);
                break;
            }
            //Dixon & Price Function
            //Minimum x = [1,1/2,1/4,1/8] y = 0
            //Bounds -10 < x < 10;
            case 9: {
                r = Math.pow((x1-1),2.0);
                for (int i=1;i<n;i++){
                    r += (i+1)*Math.pow((2*x[i]*x[i]-x[i-1]), 2);
                }                
                break;
            }
            //Easom Function
            //Minimum x = (Pi, Pi) y = -1
            //Bounds -100 < x < 100;
            case 10: {
                r = -1;
                double tmp = 0;
                for (int i=0;i<n;i++){
                    r *= Math.cos(x[i]);                    
                    tmp -= Math.pow(x[i]-Math.PI,2);
                }
                r *= Math.exp(tmp);                
                break;
            }
            //Goldstein Price Function
            //Minimum x = (0,1) y = 3
            //Bounds -2 < x < 2
            case 11:                
                r = (1+Math.pow(x1+x2+1,2.0)*(19-14*x1+3*x1*x1-14*x2+6*x1*x2+3*x2*x2))*
                            (30+Math.pow(2*x1-3*x2,2.0)*(18-32*x1+12*x1*x1+48*x2-36*x1*x2+27*x2*x2));                
                break;
            //Griewank Function
            //Minimum x = (0, 0) y = 0
            //Bounds -600 < x < 600;
            case 12: {
                double s = 0;
                double p = 1.0;
                double fr = 4000;
                for (int i=0;i<n;i++){
                    s += x[i]*x[i];
                    p+=Math.cos(x[i]/Math.sqrt(i+1));
                }
                //s = x1*x1 + x2*x2;
                //p = Math.cos(x1)*Math.cos(x2/2.0);
                r = s/fr-p+1;                
                break;
            }
            case 1012: {
                double s = 0;
                double p = 1.0;
                double fr = 4000;
                double shift = 100.0/33.0;
                for (int i=0;i<n;i++){
                    s += (x[i]+shift)*(x[i]+shift);
                    p+=Math.cos((x[i]+shift)/Math.sqrt(i+1));
                }
                //s = x1*x1 + x2*x2;
                //p = Math.cos(x1)*Math.cos(x2/2.0);
                r = s/fr-p+1;
                break;
            }
            //Hartmann Function (4,3)
            //Minimum x =  (0.114614, 0.555649, 0.852547) y = - 3.86278
            //Bounds 0 < x < 1;
            case 13: {
                double a[][] = new double[4][3];
                double c[]   = new double[4];
                double p[][] = new double[4][3];
                
                for (int i=0;i<2;i++){
                    a[2*i+0][0] = 3.0;  a[2*i+1][0]=0.1;
                    a[2*i+0][1] = 10.0; a[2*i+1][1] = 10.0; 
                    a[2*i+0][2] = 30.0; a[2*i+1][2]=35.0;
                }

                c[0] = 1.0; c[1]= 1.2; c[2]=3.0; c[3]=3.2;
                
                p[0][0]=0.36890;p[0][1]=0.11700;p[0][2]=0.26730;
                p[1][0]=0.46990;p[1][1]=0.43870;p[1][2]=0.74700;
                p[2][0]=0.10910;p[2][1]=0.87320;p[2][2]=0.55470;
                p[3][0]=0.03815;p[3][1]=0.57430;p[3][2]=0.88280;

                double s = 0;
                x = new double[3];
                x[0] = x1;  x[1] = x2; x[2] = x3;
                for (int i=0;i<4;i++){
                    double sm = 0;
                    for (int j=0;j<3;j++){
                        sm += a[i][j]*(x[j]-p[i][j])*(x[j]-p[i][j]);
                    }
                    s += c[i]*Math.exp(-sm);
                }
                r = -s;
                break;
            }
            //Hump Function
            //Minimum x =  (0.0898, -0.7126), (-0.0898, 0.7126) y = 0.0
            //Bounds -5 < x < 5;
            case 14:{
                r=1.0316285+4*x1*x1-2.1*Math.pow(x1,4.0)+Math.pow(x1,6.0)/3+x1*x2-4*x2*x2+4*Math.pow(x2, 4);
                break;
            }
            //Levy Function
            //Minimum x =  (1,... 1),  y = 0.0
            //Bounds -10 < x < 10;
            case 15:{
                double z[] = new double[n];
                for (int i=0;i<n;i++){
                    z[i] = 1+(x[i]-1.0)/4.0;
                }
                /*z[0] = 1+(x1-1.0)/4.0;
                z[1] = 1+(x2-1.0)/4.0;
                z[2] = 1+(x3-1.0)/4.0;
                z[3] = 1+(x4-1.0)/4.0;*/
                
                double s = Math.sin(Math.PI*z[0])*Math.sin(Math.PI*z[0]);
                for (int i=0;i<n-1;i++){
                    s += (z[i]-1)*(z[i]-1)*(1+10*Math.pow(Math.sin(Math.PI*z[i]+1),2.0));
                }
                r = s+(z[n-1]-1)*(z[n-1]-1)*2*(1+Math.pow(Math.sin(2*Math.PI*z[n-1]), 2));
                break;
            }   
            //Matjas Function
            //Minimum x =  (0,... 0),  y = 0.0
            //Bounds -10 < x < 10;
            case 16:{                
                r = 0.26*(x1*x1+x2*x2)-0.48*x1*x2;
                break;
            }     
            //Michalewics Function
            //Minimum x =  ?,  y = -1.8013
            //Bounds 0 < x < Pi;
            case 17:{    
                r = 0;
                for (int i=0;i<n;i++){
                    r -= Math.sin(x[i])*Math.pow((i+1)*Math.sin(x[i]*x[i]/Math.PI), 20);
                }                
                break;
            }  
            //Perm Functions
            //Minimum x =  (1,2,3,..n),  y = 0
            //Bounds -n < x < n;
            case 18:{                                
                double s_out = 0;
                double b = 0.0;
                                
                for (int k=0;k<n;k++){
                    double s_in = 0;
                    for (int j=0;j<n;j++){
                        s_in += ((Math.pow(j+1,k+1))+b)*(Math.pow(x[j]/(j+1),k+1)-1);
                    }
                    s_out += s_in*s_in;
                }
                r = s_out;
                break;
            }  
            //Powell Function
            //Minimum x =  (3,-1,0,1),  y = 0
            //Bounds -4 < x < 5;
            case 19:{   
                x = new double[4];                
                x[0] = x1;  x[1] = x2;  x[2] = x3; x[3] = x4;  
                
                double[] fvec = new double[4];
                fvec[0] = x[0]+10*x[1];
                fvec[1] = Math.sqrt(5)*(x[2]-x[3]);
                fvec[2] = (x[1]-2.0*x[2])*(x[1]-2.0*x[2]);
                fvec[3] = (Math.sqrt(10)*(x[0]-x[3]))*(Math.sqrt(10)*(x[0]-x[3]));
                r = fvec[0]*fvec[0] + fvec[1]*fvec[1] + fvec[2]*fvec[2] + fvec[3]*fvec[3];
                break;
            }  
            //Power Sum Function
            //Minimum x =  (1,2,3,4),  y = 0
            //Bounds 0 < x < 4;
            case 20:{   
                x = new double[4];                
                x[0] = x1;  x[1] = x2;  x[2] = x3; x[3] = x4;  
                                                
                double b[] = {8,18,44,114};
                double s_out = 0;
                for (int k=0;k<4;k++){
                    double s_in = 0;
                    for (int j=0;j<4;j++){
                        s_in += Math.pow(x[j],k+1);
                    }
                    s_out += (s_in-b[k])*(s_in-b[k]);
                }
                r = s_out;
                break;
            }  
            //Rosenbrock Function
            //Minimum x =  (1,..,1),  y = 0
            //Bounds -5 < x < 10;
            case 21:{   
                r = 100*((x1*x1-x2)*(x1*x1-x2)+(x1-1)*(x1-1));
                break;
            }    
            //Schwefel Function
            //Minimum x =  (1,..,1),  y = 0
            //Bounds -500 < x < 500;
            case 22:{   
                double s = 0;
                for (int i=0;i<n;i++){
                    s-=x[i]*Math.sin(Math.sqrt(Math.abs(x[i])));
                }
                r = 418.9829*n+s;
                break;
            } 
            //Shekel Functions
            //Minimum x =  (4,..,4),  y = -10.5364
            //Bounds 0 < x < 10;
            case 23:{   
                double m = 10;
                x = new double[4];                
                x[0] = x1;  x[1] = x2;  x[2] = x3; x[3] = x4;
                
                double a[][] = new double[10][4];
                a[0][0] = 4.0;  a[0][1] = 4.0;  a[0][2] = 4.0; a[0][3] = 4.0;
                a[1][0] = 1.0;  a[1][1] = 1.0;  a[1][2] = 1.0; a[1][3] = 1.0;
                a[2][0] = 8.0;  a[2][1] = 8.0;  a[2][2] = 8.0; a[2][3] = 8.0;
                a[3][0] = 6.0;  a[3][1] = 6.0;  a[3][2] = 6.0; a[3][3] = 6.0;
                a[4][0] = 3.0;  a[4][1] = 7.0;  a[4][2] = 3.0; a[4][3] = 7.0;
                a[5][0] = 2.0;  a[5][1] = 9.0;  a[5][2] = 2.0; a[5][3] = 9.0;
                a[6][0] = 5.0;  a[6][1] = 5.0;  a[6][2] = 3.0; a[6][3] = 3.0;
                a[7][0] = 8.0;  a[7][1] = 1.0;  a[7][2] = 8.0; a[7][3] = 1.0;
                a[8][0] = 6.0;  a[8][1] = 2.0;  a[8][2] = 6.0; a[8][3] = 2.0;
                a[9][0] = 7.0;  a[9][1] = 3.6;  a[9][2] = 7.0; a[9][3] = 3.6;
                
                double c[] = new double[10];
                c[0] = 0.1; c[1] = 0.2; c[2] = 0.2; c[3] = 0.4; c[4] = 0.4;
                c[5] = 0.6; c[6] = 0.3; c[7] = 0.7; c[8] = 0.5; c[9] = 0.5;

                double s = 0;
                for (int j=0;j<m;j++){
                    double p = 0;
                    for (int i=0;i<4;i++){
                        p += (x[i]-a[j][i])*(x[i]-a[j][i]);
                    }
                    s += 1.0 / (p+c[j]);
                }
                r = -s;
                break;
            } 
            //Shubert Function
            //Minimum x = 18 diff local minima,  y = -186.7309
            //Bounds -10 < x < 10;
            case 24:{
                double s1 = 0; 
                double s2 = 0;
                for (int i = 1;i<6;i++){
                    s1 += i*Math.cos((i+1)*x1+i);
                    s2 += i*Math.cos((i+1)*x2+i);
                }
                r = s1*s2;
                break;
            }
            //Sphere Function
            //Minimum x = 0 y = 0
            //Bounds -5.12 < x < 5.12
            case 25:{
                r = 0;
                for (int i=0;i<n;i++)
                    r += x[i]*x[i];
                break;
            }
            case 1025:{
                r = 0;
                double shift = 1.0 / 3.0;
                for (int i=0;i<n;i++)
                    r += (x[i]+shift)*(x[i]+shift);
                break;
            }
            //Sum Squares Function
            //Minimum x = 0 y = 0
            //Bounds -10 < x < 10
            case 26:{
                r = 0;
                for (int i=0;i<n;i++)
                    r += (i+1)*x[i]*x[i];
                break;
            }
            case 1026:{
                r = 0;
                double shift = 1.0 / 3.0;
                for (int i=0;i<n;i++)
                    r += (i+1)*(x[i]+shift)*(x[i]+shift);
                break;
            }
            //Trid Function
            //Minimum x = ? y = ?
            // -n² < x < n²
            case 27:{
                double s1 = 0,s2 = 0;                
                for (int i=0;i<n;i++)
                    s1 += (x[i]-1.0)*(x[i]-1.0);
                for (int i=1;i<n;i++)
                    s2 += x[i]*x[i-1];
                r = s1 - s2;
                break;
            }
            //Zakharov Function
            //Minimum x = 0 y = 0
            // -5 < x < 10
            case 28:{
                double s1 = 0,s2 = 0;
                for (int i=0;i<n;i++)
                    s1 += x[i]*x[i];
                for (int i=0;i<n;i++)
                    s2 += 0.5*(i+1)*x[i];
                
                double p2s2 = s2*s2;
                r = s1 + p2s2 + p2s2*p2s2;
                break;
            }
            case 1028:{
                double s1 = 0,s2 = 0;
                double shift = 1.0 / 3.0;

                for (int i=0;i<n;i++)
                    s1 += (x[i]+shift)*(x[i]+shift);
                for (int i=0;i<n;i++)
                    s2 += 0.5*(i+1)*(x[i]+shift);

                double p2s2 = s2*s2;
                r = s1 + p2s2 + p2s2*p2s2;
                break;
            }
            //simple linear test function
            case 29:{                
                r = x[0]+2*x[1];
                break;
            }
            //sobols g-function
            case 30:{                
                r = 1;
                for (int i=0;i<n;i++){
                    double ai = ((i+1)-2.) / 2.0;
                    double s = (Math.abs(4*x[i]-2)+ai) / (1+ai);
                    r*=s;
                }
                break;
            }
        }        
        yVal.setValue(r);
    }
}