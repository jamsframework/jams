/*     */ package org.unijena.j2k.development;
/*     */ 
/*     */ import jams.data.*;
/*     */ import jams.model.*;
/*     */ import jams.runtime.JAMSRuntime;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ @JAMSComponentDescription(title="J2KGroundwater", author="Peter Krause modifications Daniel Varga", description="Description")
/*     */ public class J2KProcessGWRoutingPreparator_decomp extends JAMSComponent
/*     */ {
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The current hru entity")
/*     */   public JAMSEntityCollection hrus;
/*     */ 
/*     */   @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ, update=JAMSVarDescription.UpdateType.RUN, description="The current reach entity")
/*     */   public JAMSEntityCollection reaches;
/*     */ 
/*     */   public void init()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */     throws Attribute.Entity.NoSuchAttributeException
/*     */   {
/*  61 */     HashMap routingMap = new HashMap();
/*     */ 
/*  66 */     Iterator hruIterator = this.hrus.getEntities().iterator();
/*  67 */     while (hruIterator.hasNext()) {
/*  68 */       Attribute.Entity e = (Attribute.Entity)hruIterator.next();
/*  69 */       if (e.getDouble("type") < 3.0D) {
/*  70 */         Attribute.Entity f = (JAMSEntity)e.getObject("to_poly");
/*     */ 
/*  72 */        
/*  74 */         if (!routingMap.containsKey(f)) {
/*  75 */           routingMap.put(f, new ArrayList());
/*     */         }
/*  77 */         ArrayList senderPolys = (ArrayList)routingMap.get(f);
/*  78 */         senderPolys.add(e);
/*     */       }
/*     */     }
/*     */ 
/*  82 */     Iterator routingIterator = this.hrus.getEntities().iterator();
/*  83 */     while (routingIterator.hasNext()) {
/*  84 */       Attribute.Entity r = (Attribute.Entity)routingIterator.next();
/*  85 */       ArrayList senderPolys = (ArrayList)routingMap.get(r);
/*  87 */       if (senderPolys != null)
/*     */       {
/*  90 */         JAMSEntity[] from_poly_Array = (JAMSEntity[])senderPolys.toArray(new JAMSEntity[senderPolys.size()]);
/*     */ 
/*  93 */         r.setObject("from_poly", from_poly_Array);
/*  94 */         continue;
/*  95 */       }r.setObject("from_poly", new JAMSEntity[0]);
/*     */     }
/*     */ 
/* 100 */     hruIterator = this.hrus.getEntities().iterator();
/* 101 */     while (hruIterator.hasNext()) {
/* 102 */       Attribute.Entity e = (Attribute.Entity)hruIterator.next();
/* 103 */       if (e.getDouble("type") == 3.0D) {
/* 104 */         Attribute.Entity f = (JAMSEntity)e.getObject("to_reach");
/*     */ 
/* 106 */        
/* 108 */         if (!routingMap.containsKey(f)) {
/* 109 */           routingMap.put(f, new ArrayList());
/*     */         }
/* 111 */         ArrayList senderPolys = (ArrayList)routingMap.get(f);
/* 112 */         senderPolys.add(e);
/*     */       }
/*     */     }
/*     */ 
/* 116 */     routingIterator = this.reaches.getEntities().iterator();
/* 117 */     while (routingIterator.hasNext()) {
/* 118 */       Attribute.Entity r = (Attribute.Entity)routingIterator.next();
/* 119 */       ArrayList senderPolys = (ArrayList)routingMap.get(r);
/* 121 */       if (senderPolys != null)
/*     */       {
/* 124 */         JAMSEntity[] from_poly_Array = (JAMSEntity[])senderPolys.toArray(new JAMSEntity[senderPolys.size()]);
/*     */ 
/* 127 */         r.setObject("from_poly", from_poly_Array);
/* 128 */         continue;
/* 129 */       }r.setObject("from_poly", new JAMSEntity[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/*     */   }
/*     */ }