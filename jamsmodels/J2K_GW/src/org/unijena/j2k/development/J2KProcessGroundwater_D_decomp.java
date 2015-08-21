/*     */ package org.unijena.j2k.development;
/*     */ 
/*     */ import jams.data.*;
/*     */ import jams.model.*;
/*     */ import jams.runtime.JAMSRuntime;
/*     */ 
/*     */ @JAMSComponentDescription(title="J2KGroundwater", author="Peter Krause modifications Daniel Varga", description="Description")
/*     */ public class J2KProcessGroundwater_D_decomp extends JAMSComponent
/*     */ {
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute area")
/*     */   public JAMSDouble area;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute slope")
/*     */   public JAMSDouble slope;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="maximum RG1 storage")
/*     */   public JAMSDouble maxRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="maximum GW storage")
/*     */   public JAMSDouble maxGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="recision coefficient k RG1")
/*     */   public JAMSDouble kRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="recision coefficient k GW")
/*     */   public JAMSDouble kGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="actual RG1 storage")
/*     */   public JAMSDouble actRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="actual GW storage")
/*     */   public JAMSDouble actGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="RG1 inflow")
/*     */   public JAMSDouble inRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW inflow")
/*     */   public JAMSDouble inGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="RG1 outflow")
/*     */   public JAMSDouble outRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW outflow")
/*     */   public JAMSDouble outGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="RG1 generation")
/*     */   public JAMSDouble genRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW generation")
/*     */   public JAMSDouble genGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW saturation")
/*     */   public JAMSDouble satGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="actual GW storage")
/*     */   public JAMSDouble pot_actGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW outflow")
/*     */   public JAMSDouble pot_outGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="GW generation")
/*     */   public JAMSDouble pot_genGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="percolation")
/*     */   public JAMSDouble percolation;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="gwExcess")
/*     */   public JAMSDouble gwExcess;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="maximum soil storage")
/*     */   public JAMSDouble maxSoilStorage;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="actual soil storage")
/*     */   public JAMSDouble actSoilStorage;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="RG1 correction factor")
/*     */   public JAMSDouble gwRG1Fact;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="GW correction factor")
/*     */   public JAMSDouble gwGWFact;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="RG1 GW distribution factor")
/*     */   public JAMSDouble gwRG1GWdist;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="capilary rise factor")
/*     */   public JAMSDouble gwCapRise;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="Flow width between adjacent entities (Flie?breite)")
/*     */   public JAMSDouble gwFlowWidth;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Distance between adjacent entities")
/*     */   public JAMSDouble gwFlowLength;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Downstream hru entity")
/*     */   public JAMSEntity toPoly;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Downstream reach entity")
/*     */   public JAMSEntity toReach;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="estimated hydraulic conductivity in m/d")
/*     */   public JAMSDouble Kf_geo;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="estimated Porosity")
/*     */   public JAMSDouble Peff;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Thickness of the Aquifer")
/*     */   public JAMSDouble aqThickness;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Heigth of the Aquifer Base in m + NN")
/*     */   public JAMSDouble baseHeigth;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Groundwater Table")
/*     */   public JAMSDouble gwTable;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="potential GW-Level, real GW-Level would be calculated in the GWRouting-module")
/*     */   public JAMSDouble pot_gwTable;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The current hru entity")
/*     */   public JAMSEntityCollection entities;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="Reach Colmation Factor [0;1] = [0% ; 100%]")
/*     */   public JAMSDouble alphaC;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Calculation Factor for each HRU (static geographic variables) for use in GWRouting-module")
/*     */   public JAMSDouble calcFactor;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Inflow + Percolation - StorageDifference")
/*     */   public JAMSDouble preOutGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Inflow + Percolation - StorageDifference")
/*     */   public JAMSDouble preActGW;
/*     */   double run_maxRG1;
/*     */   double run_maxGW;
/*     */   double run_actRG1;
/*     */   double run_actGW;
/*     */   double run_inRG1;
/*     */   double run_inGW;
/*     */   double run_outRG1;
/*     */   double run_outGW;
/*     */   double run_genRG1;
/*     */   double run_genGW;
/*     */   double run_satGW;
/*     */   double run_k_RG1;
/*     */   double run_k_GW;
/*     */   double run_RG1_rec;
/*     */   double run_GW_rec;
/*     */   double run_maxSoilStor;
/*     */   double run_actSoilStor;
/*     */   double run_slope;
/*     */   double run_area;
/*     */   double run_percolation;
/*     */   double run_interflow;
/*     */   double run_pot_RG1;
/*     */   double run_pot_GW;
/*     */   double run_gwExcess;
/*     */   double maxOutflow;
/*     */   double run_gwDepthUpper;
/*     */   double run_gwDepthLower;
/*     */   double run_gwTableUpper;
/*     */   double run_gwTableLower;
/*     */   double run_Peff;
/*     */   double run_Kf_geo;
/*     */   double run_gwFlowWidth;
/*     */   double run_aqThickness;
/*     */   double run_gwFlowLength;
/*     */   double run_baseHeigth;
/*     */   double run_ARPosition;
/*     */   double run_cf;
/*     */   double old_gwTable;
/*     */   double run_pre_outGW;
/*     */   double run_pre_actGW;
/*     */   double oR;
/*     */   double actualEntityID;
/*     */ 
/*     */   public void init()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 261 */     Attribute.Entity entity = this.entities.getCurrent();
/*     */ 
/* 264 */     this.run_percolation = this.percolation.getValue();
/*     */ 
/* 267 */     this.run_slope = this.slope.getValue();
/* 268 */     this.run_inRG1 = this.inRG1.getValue();
/* 269 */     this.run_gwExcess = this.gwExcess.getValue();
/* 270 */     this.run_maxSoilStor = this.maxSoilStorage.getValue();
/* 271 */     this.run_actSoilStor = this.actSoilStorage.getValue();
/*     */ 
/* 274 */     this.run_maxGW = this.maxGW.getValue();
/* 275 */     this.run_actGW = this.actGW.getValue();
/* 276 */     this.run_inGW = this.inGW.getValue();
/* 277 */     this.run_k_GW = this.kGW.getValue();
/* 278 */     this.run_Peff = this.Peff.getValue();
/* 279 */     this.run_Kf_geo = this.Kf_geo.getValue();
/* 280 */     this.run_gwFlowLength = this.gwFlowLength.getValue();
/* 281 */     this.run_gwFlowWidth = this.gwFlowWidth.getValue();
/* 282 */     this.run_aqThickness = this.aqThickness.getValue();
/* 283 */     this.run_baseHeigth = this.baseHeigth.getValue();
/*     */ 
/* 285 */     this.actualEntityID = entity.getDouble("ID");
/*     */ 
/* 288 */     this.run_outRG1 = 0.0D;
/* 289 */     this.run_genRG1 = 0.0D;
/*     */ 
/* 291 */     this.run_outGW = 0.0D;
/* 292 */     this.run_genGW = 0.0D;
/*     */ 
/* 294 */     this.pot_actGW.setValue(this.run_actGW);
/* 295 */     this.pot_outGW.setValue(this.run_outGW);
/* 296 */     this.pot_genGW.setValue(this.run_genGW);
/*     */ 
/* 302 */     if (this.toPoly.getValue() != null)
/* 303 */       this.run_gwTableLower = this.toPoly.getDouble("gwTable");
/*     */     else {
/* 305 */       this.run_gwTableLower = this.toReach.getDouble("waterTable_NN");
/*     */     }
/*     */ 
/* 309 */     this.run_gwTableUpper = this.gwTable.getValue();
/*     */ 
/* 312 */     this.old_gwTable = this.run_gwTableUpper;
/*     */ 
/* 314 */     redistRG1_GW_in();
/*     */ 
/* 316 */     replenishSoilStor();
/* 317 */     distRG1_GW();
/*     */ 
/* 320 */     updategwTable();
/*     */ 
/* 326 */     calcDarcyGWout();
/*     */ 
/* 329 */     updategwTable();
/*     */ 
/* 331 */     this.run_satGW = (this.run_actGW / this.run_maxGW);
/*     */ 
/* 333 */     this.actRG1.setValue(this.run_actRG1);
/* 334 */     this.outRG1.setValue(this.run_outRG1);
/* 335 */     this.genRG1.setValue(this.run_genRG1);
/* 336 */     this.inRG1.setValue(this.run_inRG1);
/*     */ 
/* 341 */     if (entity.getDouble("type") == 3.0D) {
/* 342 */       this.actGW.setValue(this.run_actGW);
/* 343 */       this.inGW.setValue(this.run_inGW);
/* 344 */       this.outGW.setValue(this.run_outGW);
/* 345 */       this.genGW.setValue(this.run_genGW);
/* 346 */       this.gwTable.setValue(this.run_gwTableUpper);
/*     */     }
/*     */     else
/*     */     {
/* 355 */       this.actGW.setValue(this.run_actGW);
/* 356 */       this.satGW.setValue(this.run_satGW);
/* 357 */       this.inGW.setValue(this.run_inGW);
/*     */ 
/* 361 */       this.outGW.setValue(this.run_outGW);
/* 362 */       this.genGW.setValue(this.run_genGW);
/* 363 */       this.gwTable.setValue(this.run_gwTableUpper);
/* 364 */       this.pot_actGW.setValue(this.run_actGW);
/* 365 */       this.pot_outGW.setValue(this.run_outGW);
/* 366 */       this.pot_genGW.setValue(this.run_genGW);
/* 367 */       this.pot_gwTable.setValue(this.run_gwTableUpper);
/*     */     }
/*     */ 
/* 371 */     this.gwExcess.setValue(this.run_gwExcess);
/* 372 */     this.actSoilStorage.setValue(this.run_actSoilStor);
/* 373 */     this.calcFactor.setValue(this.run_cf);
/*     */   }
/*     */ 
/*     */   public void cleanup() {
/*     */   }
/*     */ 
/*     */   public boolean replenishSoilStor() {
/* 380 */     double deltaSoilStor = this.run_maxSoilStor - this.run_actSoilStor;
/* 381 */     double sat_SoilStor = 0.0D;
/* 382 */     double inSoilStor = 0.0D;
/* 383 */     if ((this.run_actSoilStor > 0.0D) && (this.run_maxSoilStor > 0.0D))
/* 384 */       sat_SoilStor = this.run_actSoilStor / this.run_maxSoilStor;
/*     */     else {
/* 386 */       sat_SoilStor = 1.0E-006D;
/*     */     }
/* 388 */     if (this.run_actGW > deltaSoilStor) {
/* 389 */       double alpha = this.gwCapRise.getValue();
/* 390 */       inSoilStor = deltaSoilStor * (1.0D - Math.exp(-1.0D * alpha / sat_SoilStor));
/*     */     }
/*     */ 
/* 393 */     this.run_actSoilStor += inSoilStor;
/* 394 */     this.run_actGW -= inSoilStor;
/*     */ 
/* 396 */     return true;
/*     */   }
/*     */ 
/*     */   private boolean redistRG1_GW_in() {
/* 400 */     if (this.run_inRG1 > 0.0D) {
/* 401 */       double deltaRG1 = this.run_maxRG1 - this.run_actRG1;
/* 402 */       if (this.run_inRG1 <= deltaRG1) {
/* 403 */         this.run_actRG1 += this.run_inRG1;
/* 404 */         this.run_inRG1 = 0.0D;
/*     */       } else {
/* 406 */         this.run_actRG1 = this.run_maxRG1;
/* 407 */         this.run_outRG1 = (this.run_outRG1 + this.run_inRG1 - deltaRG1);
/* 408 */         this.run_inRG1 = 0.0D;
/*     */       }
/*     */     }
/*     */ 
/* 412 */     if (this.run_inGW > 0.0D) {
/* 413 */       double deltaGW = this.run_maxGW - this.run_actGW;
/* 414 */       if (this.run_inGW <= deltaGW) {
/* 415 */         this.run_actGW += this.run_inGW;
/*     */ 
/* 417 */         this.run_inGW = 0.0D;
/*     */       } else {
/* 419 */         this.run_actGW = this.run_maxGW;
/* 420 */         this.run_outGW = (this.run_outGW + this.run_inGW - deltaGW);
/*     */ 
/* 423 */         this.run_inGW = 0.0D;
/*     */       }
/*     */     }
/*     */ 
/* 427 */     return true;
/*     */   }
/*     */ 
/*     */   private boolean distRG1_GW() {
/* 431 */     double slope_weight = Math.tan(this.run_slope * 0.0174532925199433D);
/* 432 */     double gradh = 1.0D;
/*     */ 
/* 434 */     if (gradh < 0.0D)
/* 435 */       gradh = 0.0D;
/* 436 */     else if (gradh > 1.0D) {
/* 437 */       gradh = 1.0D;
/*     */     }
/*     */ 
/* 440 */     this.run_pot_RG1 = ((1.0D - gradh) * this.run_percolation);
/* 441 */     this.run_pot_GW = (gradh * this.run_percolation);
/*     */ 
/* 443 */     this.run_actRG1 += this.run_pot_RG1;
/* 444 */     this.run_actGW += this.run_pot_GW;
/*     */ 
/* 447 */     double delta_GW = this.run_actGW - this.run_maxGW;
/* 448 */     if (delta_GW > 0.0D)
/*     */     {
/* 458 */       this.run_gwExcess += delta_GW;
/* 459 */       this.run_actGW = this.run_maxGW;
/*     */     }
/*     */ 
/* 462 */     this.run_pre_outGW = this.run_outGW;
/* 463 */     this.run_pre_actGW = this.run_actGW;
/*     */ 
/* 465 */     return true;
/*     */   }
/*     */ 
/*     */   private boolean calcDarcyGWout()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 471 */     double k_rg1 = 1.0D / this.run_RG1_rec;
/* 472 */     if (k_rg1 > 1.0D) {
/* 473 */       k_rg1 = 1.0D;
/*     */     }
/* 475 */     double rg1_out = k_rg1 * this.run_actRG1;
/* 476 */     this.run_actRG1 -= rg1_out;
/* 477 */     this.run_outRG1 += rg1_out;
/*     */ 
/* 479 */     double GW_out_m3 = 0.0D;
/* 480 */     double GW_out = 0.0D;
/*     */ 
/* 482 */     double gwDifference = this.run_gwTableUpper - this.run_gwTableLower;
/*     */ 
/* 486 */     if (this.toPoly.getValue() != null) {
/* 487 */       if (gwDifference < 0.0D) {
/* 488 */         gwDifference = 0.0D;
/*     */       }
/* 491 */       double A1 = this.run_area * this.run_Peff;
/* 492 */       double A2 = this.toPoly.getDouble("area") * this.toPoly.getDouble("Peff");
/*     */ 
/* 494 */       double newHeight = gwDifference * (A1 / (A1 + A2));
/*     */ 
/* 496 */       this.maxOutflow = ((gwDifference - newHeight) * A1);
/*     */ 
/* 501 */       double gwFlowArea = this.run_gwFlowWidth * (this.run_gwTableUpper - this.run_baseHeigth);
/* 502 */       this.run_cf = (gwFlowArea * this.run_Kf_geo / this.run_gwFlowLength);
/*     */ 
/* 504 */       GW_out_m3 = this.run_cf * gwDifference;
/*     */ 
/* 506 */       if (GW_out_m3 > this.maxOutflow) {
/* 507 */         GW_out_m3 = this.maxOutflow;
/* 508 */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 517 */       double reachArea = this.toReach.getDouble("width") * this.toReach.getDouble("length");
/* 518 */       if (gwDifference < 0.0D) {
/* 519 */         gwDifference = 0.0D;
/*     */       }
/* 522 */       double A1 = this.run_area * this.run_Peff;
/* 523 */       double A2 = reachArea;
/*     */ 
/* 525 */       double newHeight = gwDifference * (A1 / (A1 + A2));
/*     */ 
/* 527 */       this.maxOutflow = ((gwDifference - newHeight) * A1);
/*     */ 
/* 540 */       double gwFlowArea = this.run_gwFlowWidth * gwDifference;
/* 541 */       this.run_cf = (gwFlowArea * this.run_Kf_geo / this.run_gwFlowLength);
/*     */ 
/* 543 */       GW_out_m3 = this.alphaC.getValue() * this.run_cf * gwDifference;
/*     */ 
/* 562 */       if (GW_out_m3 > this.maxOutflow) {
/* 563 */         GW_out_m3 = this.maxOutflow;
/* 564 */       }
/* 566 */       if (GW_out_m3 < 0.0D) {
/* 567 */         GW_out_m3 = 0.0D;
/* 568 */       }
/*     */ 
/*     */     }
/*     */ 
/* 580 */     GW_out = GW_out_m3 * 1000.0D;
/*     */ 
/* 582 */     if (this.run_actGW >= GW_out) {
/* 583 */       this.run_actGW -= GW_out;
/*     */     } else {
/* 585 */       GW_out = this.run_actGW;
/* 586 */       this.run_actGW = 0.0D;
/*     */     }
/*     */ 
/* 589 */     this.run_outGW += GW_out;
/*     */ 
/* 591 */     this.run_genGW = GW_out;
/*     */ 
/* 593 */     return true;
/*     */   }
/*     */ 
/*     */   private double calcMaxGWOutflow(double gwDifference) throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 598 */     this.maxOutflow = (this.run_area / (this.run_area + this.toPoly.getDouble("area")) * gwDifference);
/* 599 */     return this.maxOutflow;
/*     */   }
/*     */ 
/*     */   private boolean updategwTable()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 619 */     this.run_area = this.area.getValue();
/* 620 */     double gwVolume = this.run_actGW / 1000.0D;
/*     */ 
/* 622 */     this.run_gwDepthUpper = (gwVolume / this.run_area / this.run_Peff);
/* 623 */     this.run_gwTableUpper = (this.run_gwDepthUpper + this.run_baseHeigth);
/*     */ 
/* 626 */     return true;
/*     */   }
/*     */ }
