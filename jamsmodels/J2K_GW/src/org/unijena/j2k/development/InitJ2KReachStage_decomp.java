/*    */ package org.unijena.j2k.development;
/*    */ 
/*    */ import jams.data.*;
/*    */ import jams.model.*;

/*    */ 
/*    */ @JAMSComponentDescription(title="J2KGroundwater", author="Peter Krause modifications Daniel Varga", description="Description")
/*    */ public class InitJ2KReachStage_decomp extends JAMSComponent
/*    */ {
/*    */ 
/*    */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READWRITE, description="river bed height")
/*    */   public JAMSDouble reachBed;
/*    */ 
/*    */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE, description="Reach Water Level")
/*    */   public JAMSDouble waterTable_NN;
/*    */ 
/*    */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="reachBed adaptation")
/*    */   public JAMSDouble rB_adapt;
/*    */   double run_rB;
/*    */   double run_wT;
/*    */   double run_rB_adapt;
/*    */ 
/*    */   public void init()
/*    */     throws Attribute.Entity.NoSuchAttributeException
/*    */   {
/*    */   }
/*    */ 
/*    */   public void run()
/*    */     throws Attribute.Entity.NoSuchAttributeException
/*    */   {
/* 70 */     this.run_rB = this.reachBed.getValue();
/* 71 */     this.run_rB_adapt = this.rB_adapt.getValue();
/*    */ 
/* 73 */     this.run_rB -= this.run_rB_adapt;
/*    */ 
/* 75 */     this.run_wT = (this.run_rB + 0.1D);
/*    */ 
/* 77 */     this.waterTable_NN.setValue(this.run_wT);
/* 78 */     this.reachBed.setValue(this.run_rB);
/*    */   }
/*    */ 
/*    */   public void cleanup()
/*    */   {
/*    */   }
/*    */ }
