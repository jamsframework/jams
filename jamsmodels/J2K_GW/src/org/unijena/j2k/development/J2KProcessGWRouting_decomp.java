/*     */ package org.unijena.j2k.development;
/*     */ 
/*     */ import jams.data.*;
/*     */ import jams.model.*;
/*     */ import jams.runtime.JAMSRuntime;
/*     */ 
/*     */ @JAMSComponentDescription(title="J2KProcessRouting", author="Peter Krause", description="Passes the output of the entities as input to the respective reach or unit")
/*     */ public class J2KProcessGWRouting_decomp extends JAMSComponent
/*     */ {
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar GW inflow")
/*     */   public JAMSDouble inGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar GW outflow")
/*     */   public JAMSDouble outGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="sum attribute")
/*     */   public JAMSEntity[] fP;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute area")
/*     */   public JAMSDouble area;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Heigth of the Aquifer Base in m + NN")
/*     */   public JAMSDouble baseHeigth;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="estimated Porosity")
/*     */   public JAMSDouble Peff;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="actual GW storage")
/*     */   public JAMSDouble actGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Groundwater Level")
/*     */   public JAMSDouble gwTable;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Groundwater Level")
/*     */   public JAMSDouble calcFactor;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Reduction of outflows, 0 = off, 1 = average, 2 = exponential")
/*     */   public JAMSInteger outflowReduction;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Number of sender-HRUs")
/*     */   public JAMSDouble sender;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The current hru entity")
/*     */   public JAMSEntityCollection entities;
/*     */   double[] gradientNew;
/*     */   double gwVolume;
/*     */   double run_area;
/*     */   double run_Peff;
/*     */   double run_gwTableUpper;
/*     */   double run_gwTableLower;
/*     */   double run_gwDepthUpper;
/*     */   double run_gwDepthLower;
/*     */   double run_baseHeigth;
/*     */   double pot_gwTable;
/*     */   double sumGWin;
/*     */   double sumGWin_new;
/*     */   double run_GWact;
/*     */   double run_outGW;
/*     */   double old_GWact;
/*     */   int oR;
/*     */ 
/*     */   public void init()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 102 */     Attribute.Entity entity = this.entities.getCurrent();
/* 103 */     /*     */ 
/* 107 */     this.fP = ((JAMSEntity[])(JAMSEntity[])entity.getObject("from_poly"));
/*     */ 
/* 110 */     this.run_GWact = this.actGW.getValue();
/* 111 */     this.run_area = this.area.getValue();
/* 112 */     this.run_Peff = this.Peff.getValue();
/* 113 */     
/* 116 */     this.run_baseHeigth = this.baseHeigth.getValue();
/*     */ 
/* 121 */     this.gradientNew = new double[this.fP.length];
/* 122 */     this.oR = this.outflowReduction.getValue();
/*     */ 
/* 124 */     if (this.fP.length != 0) {
/* 125 */       this.sumGWin_new = 0.0D;
/*     */ 
/* 128 */       for (int i = 0; i < this.fP.length; i++) {
/* 129 */         this.sumGWin_new = (this.fP[i].getDouble("pot_outGW") + this.sumGWin_new);
/*     */       }
/*     */ 
/* 138 */       this.old_GWact = this.run_GWact;
/* 139 */       this.run_GWact += this.sumGWin_new;
/*     */ 
/* 142 */       boolean flag = false;
/*     */ 
/* 144 */       updateGWTable(flag);
/*     */ 
/* 146 */       if (this.oR != 0)
/* 147 */         this.gradientNew = calcGradientReduction();
/*     */       else {
/* 149 */         for (int i = 0; i < this.fP.length; i++)
/*     */         {
/* 151 */           this.gradientNew[i] = (this.fP[i].getDouble("gwTable") - this.gwTable.getValue());
/* 152 */           
/*     */         }
/*     */       }
/*     */ 
/* 158 */       this.run_GWact = recalcDarcyGWOut(this.gradientNew);
/*     */ 
/* 161 */       flag = true;
/*     */ 
/* 163 */       updateGWTable(flag);
/*     */ 
/* 179 */       this.actGW.setValue(this.run_GWact);
/* 180 */       this.inGW.setValue(this.sumGWin_new);
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean updateGWTable(boolean flag) throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 186 */     for (int i = 0; i < this.fP.length; i++) {
/* 187 */       if (flag) {
/* 188 */         this.gwVolume = (this.fP[i].getDouble("actGW") / 1000.0D);
/* 189 */         this.run_gwDepthUpper = (this.gwVolume / this.fP[i].getDouble("area") / this.fP[i].getDouble("Peff"));
/* 190 */         this.run_gwTableUpper = (this.run_gwDepthUpper + this.fP[i].getDouble("baseHeigth"));
/* 191 */         this.fP[i].setDouble("gwTable", this.run_gwTableUpper);
/* 192 */         
/*     */       }
/*     */     }
/*     */ 
/* 198 */     this.gwVolume = (this.run_GWact / 1000.0D);
/*     */ 
/* 200 */     this.run_gwDepthLower = (this.gwVolume / this.run_area / this.run_Peff);
/* 201 */     this.run_gwTableLower = (this.run_gwDepthLower + this.run_baseHeigth);
/*     */ 
/* 203 */     
/*     */ 
/* 207 */     if (flag)
/* 208 */       this.gwTable.setValue(this.run_gwTableLower);
/*     */     else {
/* 210 */       this.pot_gwTable = this.run_gwTableLower;
/*     */     }
/*     */ 
/* 213 */     return true;
/*     */   }
/*     */ 
/*     */   private double[] calcGradientReduction()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 220 */     for (int i = 0; i < this.fP.length; i++) {
/* 221 */       double gradientPre = this.fP[i].getDouble("gwTable") - this.gwTable.getValue();
/* 222 */       double gradientPost = this.fP[i].getDouble("pot_gwTable") - this.pot_gwTable;
/*     */ 
/* 224 */       if (gradientPost < 0.0D) {
/* 225 */         gradientPost = 0.0D;
/*     */       }
/*     */ 
/* 229 */       if (this.oR == 1) {
/* 230 */         this.gradientNew[i] = ((gradientPre + gradientPost) / 2.0D);
/*     */       } else {
/* 232 */         double a = gradientPre;
/* 233 */         double m = (gradientPost - gradientPre) / 1.0D;
/* 234 */         if (m > 0.0D) {
/* 235 */           m *= -1.0D;
/* 236 */           double c = m / a;
/* 237 */           this.gradientNew[i] = (a + (a - (a / c * Math.exp(c) - a / c)));
/* 238 */           if (this.gradientNew[i] < 0.0D) {
/* 239 */             this.gradientNew[i] = 0.0D;
/*     */           }
/* 242 */         } else if (m < 0.0D) {
/* 243 */           double c = m / a;
/* 244 */           this.gradientNew[i] = (a / c * Math.exp(c) - a / c);
/* 245 */           if (this.gradientNew[i] < 0.0D) {
/* 246 */             this.gradientNew[i] = 0.0D;
/*     */           }
/*     */         } else {
/* 250 */           this.gradientNew[i] = 0.0D;
/*     */         }
/*     */       }
/*     */     }
/* 254 */     return this.gradientNew;
/*     */   }
/*     */ 
/*     */   private double recalcDarcyGWOut(double[] gradientNew)
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 262 */     this.sumGWin_new = 0.0D;
/* 263 */     if (this.run_gwTableUpper >= this.run_gwTableLower) {
/* 264 */       this.sumGWin_new = 0.0D;
/* 265 */       double GWout_sum = 0.0D;
/*     */ 
/* 267 */       for (int i = 0; i < this.fP.length; i++)
/*     */       {
/*     */         double GWout_new;
/* 268 */         if (this.fP[i].getDouble("hgeoID") != 1.0D) {
/* 269 */           GWout_new = this.fP[i].getDouble("outRG2");
/*     */         }
/*     */         else {
/* 272 */           GWout_new = this.fP[i].getDouble("calcFactor") * gradientNew[i] * 1000.0D;
/* 273 */           if (GWout_new < 0.0D) {
/* 274 */             GWout_new = 0.0D;
/*     */           }
/*     */ 
/* 280 */           GWout_sum = this.fP[i].getDouble("preOutGW") + GWout_new;
/*     */         }
/*     */ 
/* 283 */         if (GWout_new > this.fP[i].getDouble("actGW")) {
/* 284 */           GWout_new = this.fP[i].getDouble("actGW");
/*     */         }
/* 286 */         double GWact_new = this.fP[i].getDouble("actGW") - GWout_new;
/*     */ 
/* 288 */         this.fP[i].setDouble("actGW", GWact_new);
/* 289 */         this.fP[i].setDouble("outGW", GWout_sum);
/* 290 */         this.fP[i].setDouble("genGW", GWout_new);
/*     */ 
/* 292 */         this.sumGWin_new += GWout_sum;
/*     */       }
/*     */     }
/*     */ 
/* 298 */     double GWact_new = this.old_GWact + this.sumGWin_new;
/*     */ 
/* 300 */     return GWact_new;
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/*     */   }
/*     */ }
