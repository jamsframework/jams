/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.experimental;

import optas.core.SampleLimitException;
import jams.JAMS;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerDescription;

/**
 *
 * @author Christian Fischer
 */
public class ParticleSwarm extends Optimizer {
    
    static public class Particle{
        SampleSO particle;
        SampleSO local_best;
        double[] velocity;
        
        Particle(SampleSO particle,double[] velocity){
            this.particle = particle;
            this.local_best = particle;
            this.velocity = velocity;
        }                
    }

    public OptimizerDescription getDescription() {
        return OptimizerLibrary.getDefaultOptimizerDescription(ParticleSwarm.class.getSimpleName(), ParticleSwarm.class.getName(), 500, false);
    }

    int numberOfParticles = 1000;
    Particle particles[] = new Particle[numberOfParticles];            
    Particle bestParticle = null;
    
    double w = 0.9;
    double c1 = 0.7;
    double c2 = 1.4;
    double c3 = 0.0;
    
    boolean feasible(double nextPosition[]){
        for (int i=0;i<n;i++){
            if (nextPosition[i]<this.lowBound[i] || nextPosition[i]>this.upBound[i])
                return false;
        }
        return true;
    }
             
    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException{
        System.out.println(JAMS.i18n("start_optimization_of"));

        for (int i=0;i<numberOfParticles;i++){
            SampleSO rndSample = null;
            
            if (x0 != null && i<x0.length){
                rndSample = this.getSampleSO(x0[i]);
            }else
                rndSample = this.getSampleSO(this.randomSampler());
            
            particles[i] = new Particle(rndSample,new double[n]);
                        
            if (bestParticle!=null){
                if (bestParticle.particle.f() > particles[i].particle.f())
                    bestParticle = particles[i];
            }else
                bestParticle = particles[i];            
        }
        
        while(true){
            //randomly generate r1 and r2
            double r1[] = new double[n];
            double r2[] = new double[n];
            double r3[] = new double[n];
            for (int i=0;i<n;i++){
                r1[i] = generator.nextDouble();
                r2[i] = generator.nextDouble();
                r3[i] = generator.nextDouble();                
            }            
            for (int i=0;i<numberOfParticles;i++){
                //update velocities
                double nextPosition[] = new double[n];
                for (int j=0;j<n;j++){
                    particles[i].velocity[j] = w*particles[i].velocity[j];                    
                    particles[i].velocity[j] -= c1*r1[j]*(particles[i].particle.x[j]-particles[i].local_best.x[j]);
                    particles[i].velocity[j] -= c2*r2[j]*(particles[i].particle.x[j]-bestParticle.local_best.x[j]);
                    
                    int rndParticle = generator.nextInt(numberOfParticles);
                    
                    particles[i].velocity[j] -= c3*r3[j]*(particles[i].particle.x[j]-particles[rndParticle].local_best.x[j]);
                    
                    nextPosition[j] = particles[i].particle.x[j] + particles[i].velocity[j];                                                            
                }
                double beta = 0.5;
                while(!feasible(nextPosition)){
                    for (int j=0;j<n;j++){
                        nextPosition[j] -= beta*particles[i].velocity[j];     
                    }
                }    
                //evaluate
                particles[i].particle = this.getSampleSO(nextPosition);
                if (particles[i].particle.f() < particles[i].local_best.f())
                    particles[i].local_best = particles[i].particle;
                if (particles[i].particle.f() < bestParticle.local_best.f()){
                    bestParticle = particles[i];
                }
            }
            log(JAMS.i18n("current_best")+":" + bestParticle.local_best.toString() + " " + JAMS.i18n("after") + " " +  this.getIterationCounter());
        }
        //System.out.println(JAMS.i18n("finished_optimization"));
    }
}
