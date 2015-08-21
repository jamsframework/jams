/*     */ package org.unijena.j2k.development;
/*     */ 
/*     */ import jams.data.*;
/*     */ import jams.model.*;
/*     */ 
/*     */ @JAMSComponentDescription(title="Title", author="Author", description="Description")
/*     */ public class J2KProcessReachRouting_D_decomp extends JAMSComponent
/*     */ {
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The reach collection")
/*     */   public JAMSEntityCollection entities;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute length")
/*     */   public JAMSDouble length;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute slope")
/*     */   public JAMSDouble slope;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute width")
/*     */   public JAMSDouble width;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="attribute roughness")
/*     */   public JAMSDouble roughness;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD1 inflow")
/*     */   public JAMSDouble inRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD2 inflow")
/*     */   public JAMSDouble inRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG1 inflow")
/*     */   public JAMSDouble inRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 inflow")
/*     */   public JAMSDouble inRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 inflow")
/*     */   public JAMSDouble inGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar additional inflow", defaultValue="0")
/*     */   public JAMSDouble inAddIn;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD1 outflow")
/*     */   public JAMSDouble outRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD2 outflow")
/*     */   public JAMSDouble outRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG1 outflow")
/*     */   public JAMSDouble outRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 outflow")
/*     */   public JAMSDouble outRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 outflow")
/*     */   public JAMSDouble outGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar additional outflow", defaultValue="0")
/*     */   public JAMSDouble outAddIn;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar simulated Runoff")
/*     */   public JAMSDouble simRunoff;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD1 storage")
/*     */   public JAMSDouble actRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RD2 storage")
/*     */   public JAMSDouble actRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG1 storage")
/*     */   public JAMSDouble actRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 storage")
/*     */   public JAMSDouble actRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar RG2 storage")
/*     */   public JAMSDouble actGW;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Reach statevar additional inflow storage", defaultValue="0")
/*     */   public JAMSDouble actAddIn;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.RUN, description="Channel storage")
/*     */   public JAMSDouble channelStorage;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="flow routing coefficient TA")
/*     */   public JAMSDouble flowRouteTA;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment outlet RD1 storage")
/*     */   public JAMSDouble catchmentRD1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment outlet RD2 storage")
/*     */   public JAMSDouble catchmentRD2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment outlet RG1 storage")
/*     */   public JAMSDouble catchmentRG1;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment outlet RG2 storage")
/*     */   public JAMSDouble catchmentRG2;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment additional input outlet storage", defaultValue="0")
/*     */   public JAMSDouble catchmentAddIn;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, update=JAMSVarDescription.UpdateType.RUN, description="Catchment outlet RG2 storage")
/*     */   public JAMSDouble catchmentSimRunoff;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="temporal resolution [d or h]")
/*     */   public JAMSString tempRes;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.INIT, description="mm ü. NN")
/*     */   public JAMSDouble baseHeigth;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.INIT, description="water Level in m ü. NN")
/*     */   public JAMSDouble waterTable_NN;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, update=JAMSVarDescription.UpdateType.INIT, description="water Level in m")
/*     */   public JAMSDouble waterDepth;
/*     */ 
/*     */   public void init()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/* 309 */     Attribute.Entity entity = this.entities.getCurrent();
/*     */ 
/* 311 */     double actualEntityID = entity.getDouble("ID");
/*     */ 
/* 313 */     JAMSEntity DestReach = (JAMSEntity)entity.getObject("to_reach");
/* 314 */     if (DestReach.getValue() == null) {
/* 315 */       DestReach = null;
/*     */     }
/* 317 */     JAMSEntity DestReservoir = null;
/*     */     try
/*     */     {
/* 320 */       DestReservoir = (JAMSEntity)entity.getObject("to_reservoir");
/*     */     } catch (Attribute.Entity.NoSuchAttributeException e) {
/* 322 */       DestReservoir = null;
/*     */     }
/*     */ 
/* 325 */     double run_width = this.width.getValue();
/* 326 */     double run_slope = this.slope.getValue();
/* 327 */     double rough = this.roughness.getValue();
/* 328 */     double run_length = this.length.getValue();
/*     */ 
/* 330 */     double RD1act = this.actRD1.getValue() + this.inRD1.getValue();
/* 331 */     double RD2act = this.actRD2.getValue() + this.inRD2.getValue();
/* 332 */     double RG1act = this.actRG1.getValue() + this.inRG1.getValue();
/* 333 */     double RG2act = this.actRG2.getValue() + this.inRG2.getValue();
/* 334 */     double GWact = this.actGW.getValue() + this.inGW.getValue();
/*     */ 
/* 336 */     double addInAct = this.actAddIn.getValue() + this.inAddIn.getValue();
/*     */ 
/* 338 */     this.inRD1.setValue(0.0D);
/* 339 */     this.inRD2.setValue(0.0D);
/* 340 */     this.inRG1.setValue(0.0D);
/* 341 */     this.inRG2.setValue(0.0D);
/* 342 */     this.inGW.setValue(0.0D);
/*     */ 
/* 344 */     this.inAddIn.setValue(0.0D);
/*     */ 
/* 346 */     this.actRD1.setValue(0.0D);
/* 347 */     this.actRD2.setValue(0.0D);
/* 348 */     this.actRG1.setValue(0.0D);
/* 349 */     this.actRG2.setValue(0.0D);
/* 350 */     this.actGW.setValue(0.0D);
/*     */ 
/* 352 */     this.actAddIn.setValue(0.0D);
/*     */ 
/* 354 */     double RD1DestIn = 0.0D;
/* 355 */     double RD2DestIn = 0.0D;
/* 356 */     double RG1DestIn = 0.0D;
/* 357 */     double RG2DestIn = 0.0D;
/* 358 */     double GWDestIn = 0.0D;
/* 359 */     double addInDestIn = 0.0D;
/*     */ 
/* 361 */     if ((DestReach == null) && (DestReservoir == null)) {
/* 362 */       RD1DestIn = 0.0D;
/* 363 */       RD2DestIn = 0.0D;
/* 364 */       RG1DestIn = 0.0D;
/* 365 */       RG2DestIn = 0.0D;
/* 366 */       GWDestIn = 0.0D;
/* 367 */       addInDestIn = 0.0D;
/*     */     }
/* 369 */     else if (DestReservoir != null) {
/* 370 */       RD1DestIn = DestReservoir.getDouble("compRD1");
/* 371 */       RD2DestIn = DestReservoir.getDouble("compRD2");
/* 372 */       RG1DestIn = DestReservoir.getDouble("compRG1");
/* 373 */       RG2DestIn = DestReservoir.getDouble("compRG2");
/*     */     }
/*     */     else {
/* 376 */       RD1DestIn = DestReach.getDouble("inRD1");
/* 377 */       RD2DestIn = DestReach.getDouble("inRD2");
/* 378 */       RG1DestIn = DestReach.getDouble("inRG1");
/* 379 */       RG2DestIn = DestReach.getDouble("inRG2");
/* 380 */       GWDestIn = DestReach.getDouble("inGW");
/*     */       try {
/* 382 */         addInDestIn = DestReach.getDouble("inAddIn");
/*     */       } catch (Attribute.Entity.NoSuchAttributeException e) {
/* 384 */         addInDestIn = 0.0D;
/*     */       }
/*     */     }
/*     */ 
/* 388 */     double q_act_tot = RD1act + RD2act + RG1act + RG2act + GWact + addInAct;
/*     */ 
/* 392 */     if (q_act_tot == 0.0D) {
/* 393 */       this.outRD1.setValue(0.0D);
/* 394 */       this.outRD2.setValue(0.0D);
/* 395 */       this.outRG1.setValue(0.0D);
/* 396 */       this.outRG2.setValue(0.0D);
/* 397 */       this.outGW.setValue(0.0D);
/*     */ 
/* 399 */       this.outAddIn.setValue(0.0D);
/*     */ 
/* 402 */       return;
/*     */     }
/*     */ 
/* 406 */     double RD1_part = RD1act / q_act_tot;
/* 407 */     double RD2_part = RD2act / q_act_tot;
/* 408 */     double RG1_part = RG1act / q_act_tot;
/* 409 */     double RG2_part = RG2act / q_act_tot;
/* 410 */     double GW_part = GWact / q_act_tot;
/* 411 */     double addInPart = addInAct / q_act_tot;
/*     */ 
/* 414 */     int sec_inTStep = 0;
/* 415 */     if (this.tempRes.getValue().equals("d"))
/* 416 */       sec_inTStep = 86400;
/* 417 */     else if (this.tempRes.getValue().equals("h"))
/* 418 */       sec_inTStep = 3600;
/* 419 */     double flow_veloc = calcFlowVelocity(q_act_tot, run_width, run_slope, rough, sec_inTStep);
/*     */ 
/* 423 */     double Rk = flow_veloc / run_length * this.flowRouteTA.getValue() * 3600.0D;
/*     */     double q_act_out;
/* 427 */     if (Rk > 0.0D)
/* 428 */       q_act_out = q_act_tot * Math.exp(-1.0D / Rk);
/*     */     else {
/* 430 */       q_act_out = 0.0D;
/*     */     }
/*     */ 
/* 433 */     double RD1out = q_act_out * RD1_part;
/* 434 */     double RD2out = q_act_out * RD2_part;
/* 435 */     double RG1out = q_act_out * RG1_part;
/* 436 */     double RG2out = q_act_out * RG2_part;
/* 437 */     double GWout = q_act_out * GW_part;
/* 438 */     double addInOut = q_act_out * addInPart;
/*     */ 
/* 441 */     RD1DestIn += RD1out;
/* 442 */     RD2DestIn += RD2out;
/* 443 */     RG1DestIn += RG1out;
/* 444 */     RG2DestIn += RG2out;
/* 445 */     GWDestIn += GWout;
/*     */ 
/* 447 */     addInDestIn += addInOut;
/*     */ 
/* 450 */     RD1act -= q_act_out * RD1_part;
/* 451 */     RD2act -= q_act_out * RD2_part;
/* 452 */     RG1act -= q_act_out * RG1_part;
/* 453 */     RG2act -= q_act_out * RG2_part;
/* 454 */     GWact -= q_act_out * GW_part;
/* 455 */     addInAct -= q_act_out * addInPart;
/*     */ 
/* 457 */     double run_channelStorage = RD1act + RD2act + RG1act + RG2act + GWact + addInAct;
/*     */ 
/* 460 */     double q_m = q_act_tot / (1000 * sec_inTStep);
/* 461 */     double run_waterDepth = q_m / flow_veloc / run_width;
/* 462 */     double run_waterTable_NN = this.baseHeigth.getValue() + run_waterDepth;
/*     */ 
/* 464 */     double cumOutflow = RD1out + RD2out + RG1out + RG2out + GWout + addInOut;
/*     */ 
/* 466 */     this.simRunoff.setValue(cumOutflow);
/* 467 */     this.channelStorage.setValue(run_channelStorage);
/*     */ 
/* 469 */     this.inRD1.setValue(0.0D);
/* 470 */     this.inRD2.setValue(0.0D);
/* 471 */     this.inRG1.setValue(0.0D);
/* 472 */     this.inRG2.setValue(0.0D);
/* 473 */     this.inGW.setValue(0.0D);
/*     */ 
/* 475 */     this.inAddIn.setValue(0.0D);
/*     */ 
/* 477 */     this.actRD1.setValue(RD1act);
/* 478 */     this.actRD2.setValue(RD2act);
/* 479 */     this.actRG1.setValue(RG1act);
/* 480 */     this.actRG2.setValue(RG2act);
/* 481 */     this.actGW.setValue(GWact);
/*     */ 
/* 483 */     this.actAddIn.setValue(addInAct);
/*     */ 
/* 485 */     this.outRD1.setValue(RD1out);
/* 486 */     this.outRD2.setValue(RD2out);
/* 487 */     this.outRG1.setValue(RG1out);
/* 488 */     this.outRG2.setValue(RG2out);
/* 489 */     this.outGW.setValue(GWout);
/* 490 */     this.outAddIn.setValue(addInOut);
/*     */ 
/* 492 */     this.waterDepth.setValue(run_waterDepth);
/* 493 */     this.waterTable_NN.setValue(run_waterTable_NN);
/*     */ 
/* 496 */     if ((DestReach != null) && (DestReservoir == null)) {
/* 497 */       DestReach.setDouble("inRD1", RD1DestIn);
/* 498 */       DestReach.setDouble("inRD2", RD2DestIn);
/* 499 */       DestReach.setDouble("inRG1", RG1DestIn);
/* 500 */       DestReach.setDouble("inRG2", RG2DestIn);
/* 501 */       DestReach.setDouble("inGW", GWDestIn);
/* 502 */       DestReach.setDouble("inAddIn", addInDestIn);
/*     */     }
/* 506 */     else if (DestReservoir != null) {
/* 507 */       DestReservoir.setDouble("compRD1", RD1DestIn);
/* 508 */       DestReservoir.setDouble("compRD2", RD2DestIn);
/* 509 */       DestReservoir.setDouble("compRG1", RG1DestIn);
/* 510 */       DestReservoir.setDouble("compRG2", RG2DestIn);
/*     */     }
/* 513 */     else if ((DestReach == null) && (DestReservoir == null)) {
/* 514 */       this.catchmentRD1.setValue(RD1out);
/* 515 */       this.catchmentRD2.setValue(RD2out);
/* 516 */       this.catchmentRG1.setValue(RG1out);
/* 517 */       RG2out += GWout;
/* 518 */       this.catchmentRG2.setValue(RG2out);
/*     */ 
/* 520 */       this.catchmentAddIn.setValue(addInOut);
/* 521 */       this.catchmentSimRunoff.setValue(cumOutflow);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/*     */   }
/*     */ 
/*     */   public static double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep)
/*     */   {
/* 540 */     double afv = 1.0D;
/* 541 */     double veloc = 0.0D;
/*     */ 
/* 546 */     double q_m = q / (1000 * secondsOfTimeStep);
/* 547 */     double rh = calcHydraulicRadius(afv, q_m, width);
/* 548 */     boolean cont = true;
/* 549 */     while (cont) {
/* 550 */       veloc = rough * Math.pow(rh, 0.6666666666666666D) * Math.sqrt(slope);
/* 551 */       if (Math.abs(veloc - afv) > 0.001D) {
/* 552 */         afv = veloc;
/* 553 */         rh = calcHydraulicRadius(afv, q_m, width); continue;
/*     */       }
/* 555 */       cont = false;
/* 556 */       afv = veloc;
/*     */     }
/*     */ 
/* 559 */     return afv;
/*     */   }
/*     */ 
/*     */   public static double calcHydraulicRadius(double v, double q, double width)
/*     */   {
/* 571 */     double A = q / v;
/*     */ 
/* 573 */     double rh = width + 2.0D * (A / width);
/*     */ 
/* 575 */     return rh;
/*     */   }
/*     */ }