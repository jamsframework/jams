/*     */ package org.unijena.j2k.development;
/*     */ 
/*     */ import jams.data.*;
/*     */ import jams.model.*;
/*     */ import jams.runtime.JAMSRuntime;
/*     */ 
/*     */ @JAMSComponentDescription(title="J2KProcessRouting", author="Peter Krause", description="Passes the output of the entities as input to the respective reach or unit")
/*     */ public class J2KProcessRouting_D_decomp extends JAMSComponent
/*     */ {
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The current hru entity")
/*     */   public JAMSEntityCollection entities;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Collection of reach objects")
/*     */   public JAMSEntityCollection reaches;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Collection of reservoir objects")
/*     */   public JAMSEntityCollection reservoirs;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RD1 inflow")
/*     */   public JAMSDouble inRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RD2 inflow")
/*     */   public JAMSDouble inRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RG1 inflow")
/*     */   public JAMSDouble inRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RG2 inflow")
/*     */   public JAMSDouble inRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar groundwater excess", defaultValue="0")
/*     */   public JAMSDouble inGWExcess;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RD1 outflow")
/*     */   public JAMSDouble outRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RD2 outflow")
/*     */   public JAMSDouble outRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RG1 outflow")
/*     */   public JAMSDouble outRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RG2 outflow")
/*     */   public JAMSDouble outRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="HRU statevar RG2 outflow")
/*     */   public JAMSDouble outGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Downstream hru entity")
/*     */   public Attribute.Entity toPoly;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Downstream reach entity")
/*     */   public Attribute.Entity toReach;
/*     */ 
/*     */   public void init()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 158 */     Attribute.Entity entity = this.entities.getCurrent();
/*     */ 
/* 167 */     Attribute.Entity toReservoir = null;
/*     */     try {
/* 169 */       toReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
/*     */     } catch (Attribute.Entity.NoSuchAttributeException e) {
/* 171 */       toReservoir = null;
/*     */     }
/* 173 */     double RD1out = this.outRD1.getValue();
/* 174 */     double RD2out = this.outRD2.getValue();
/* 175 */     double RG1out = this.outRG1.getValue();
/* 176 */     double RG2out = this.outRG2.getValue();
/* 177 */     double GWout = this.outGW.getValue();
/*     */ 
/* 179 */     if (this.toPoly.getValue() != null)
/*     */     {
/* 181 */       double RG2in = this.toPoly.getDouble("inRG2");
/* 182 */       double GWin = this.toPoly.getDouble("inGW");
/* 183 */       GWin += GWout;
/* 184 */       GWout = 0.0D;
/* 185 */       this.outGW.setValue(0.0D);
/* 186 */       this.toPoly.setDouble("inGW", GWin);
/*     */ 
/* 189 */       double RD1in = this.toPoly.getDouble("inRD1");
/* 190 */       double RD2in = this.toPoly.getDouble("inRD2");
/* 191 */       double RG1in = this.toPoly.getDouble("inRG1");
/*     */ 
/* 193 */       RD1in += RD1out;
/* 194 */       RD2in += RD2out;
/* 195 */       RG1in += RG1out;
/*     */ 
/* 197 */       RD2in += this.inGWExcess.getValue();
/*     */ 
/* 199 */       RD1out = 0.0D;
/* 200 */       RD2out = 0.0D;
/* 201 */       RG1out = 0.0D;
/*     */ 
/* 203 */       this.outRD1.setValue(0.0D);
/* 204 */       this.outRD2.setValue(0.0D);
/* 205 */       this.outRG1.setValue(0.0D);
/*     */ 
/* 207 */       this.inGWExcess.setValue(0.0D);
/*     */ 
/* 209 */       this.toPoly.setDouble("inRD1", RD1in);
/* 210 */       this.toPoly.setDouble("inRD2", RD2in);
/* 211 */       this.toPoly.setDouble("inRG1", RG1in);
/*     */     }
/* 213 */     else if (this.toReach.getValue() != null) {
/* 214 */       double RD1in = this.toReach.getDouble("inRD1");
/* 215 */       double RD2in = this.toReach.getDouble("inRD2");
/* 216 */       double RG1in = this.toReach.getDouble("inRG1");
/* 217 */       double RG2in = this.toReach.getDouble("inRG2");
/* 218 */       double GWin = this.toReach.getDouble("inGW");
/*     */ 
/* 220 */       RD1in += RD1out;
/* 221 */       RD2in += RD2out;
/* 222 */       RG1in += RG1out;
/* 223 */       RG2in += RG2out;
/* 224 */       GWin += GWout;
/* 225 */       RD2in += this.inGWExcess.getValue();
/*     */ 
/* 227 */       RD1out = 0.0D;
/* 228 */       RD2out = 0.0D;
/* 229 */       RG1out = 0.0D;
/* 230 */       RG2out = 0.0D;
/* 231 */       GWout = 0.0D;
/*     */ 
/* 233 */       this.outRD1.setValue(RD1out);
/* 234 */       this.toReach.setDouble("inRD1", RD1in);
/* 235 */       this.outRD2.setValue(RD2out);
/* 236 */       this.toReach.setDouble("inRD2", RD2in);
/* 237 */       this.outRG1.setValue(RG1out);
/* 238 */       this.toReach.setDouble("inRG1", RG1in);
/* 239 */       this.outRG2.setValue(RG2out);
/* 240 */       this.toReach.setDouble("inGW", GWin);
/* 241 */       this.outGW.setValue(GWout);
/*     */ 
/* 243 */       this.inGWExcess.setValue(0.0D);
/* 244 */       this.toReach.setDouble("inRG2", RG2in);
/*     */     }
/* 246 */     else if (toReservoir != null) {
/* 247 */       double resRD1 = toReservoir.getDouble("compRD1");
/* 248 */       double resRD2 = toReservoir.getDouble("compRD2");
/* 249 */       double resRG1 = toReservoir.getDouble("compRG1");
/* 250 */       double resRG2 = toReservoir.getDouble("compRG2");
/*     */ 
/* 252 */       resRD1 += RD1out;
/* 253 */       resRD2 += RD2out;
/* 254 */       resRG1 += RG1out;
/* 255 */       resRG2 += RG2out;
/*     */ 
/* 257 */       RD1out = 0.0D;
/* 258 */       RD2out = 0.0D;
/* 259 */       RG1out = 0.0D;
/* 260 */       RG2out = 0.0D;
/*     */ 
/* 262 */       this.outRD1.setValue(RD1out);
/* 263 */       toReservoir.setDouble("compRD1", resRD1);
/* 264 */       this.outRD2.setValue(RD2out);
/* 265 */       toReservoir.setDouble("compRD2", resRD2);
/* 266 */       this.outRG1.setValue(RG1out);
/* 267 */       toReservoir.setDouble("compRG1", resRG1);
/* 268 */       this.outRG2.setValue(RG2out);
/* 269 */       toReservoir.setDouble("compRG2", resRG2);
/*     */     }
/*     */     else {
/* 272 */       getModel().getRuntime().println("Current entity ID: " + (int)entity.getDouble("ID") + " has no receiver.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/*     */   }
/*     */ }