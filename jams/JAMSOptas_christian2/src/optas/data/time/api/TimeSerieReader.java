/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time.api;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 */
public interface TimeSerieReader<T extends TimeSerie> {
    T getData();
    
}
