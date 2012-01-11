/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.groovy


import java.io.*
import jams.*
import jams.tools.*
import jams.runtime.*
import jams.meta.*
import jams.io.*
import jams.model.*

// class needed lateron, could also use null instead..
class ExHandler implements ExceptionHandler {
    
    public void handle(JAMSException ex) {
        println ex
    }

    public void handle(ArrayList<JAMSException> exList) {
        for (JAMSException jex : exList) {
            println ex
        }
    }
}

// set some variables first
propertyFile = "D:/jamsapplication/nsk.jap"
modelFile = "D:/jamsapplication/JAMS-Gehlberg/j2k_gehlberg.jam"
defaultWorkspacePath = new File(modelFile).getParent()

// create some property object
properties = JAMSProperties.createProperties()
properties.load(propertyFile)
//properties.setProperty(JAMSProperties.GUICONFIG_IDENTIFIER, "0")
//properties.setProperty(JAMSProperties.WINDOWENABLE_IDENTIFIER, "0")
//properties.setProperty(JAMSProperties.VERBOSITY_IDENTIFIER, "1")
//properties.setProperty(JAMSProperties.ERRORDLG_IDENTIFIER, "0")

// tweak localization
JAMSTools.configureLocaleEncoding(properties)

// create XML document from model file
modelDoc = XMLTools.getDocument(modelFile)

// do some preprocessing on the XML
ParameterProcessor.preProcess(modelDoc);

// create a runtime object
runtime = new StandardRuntime(properties)

// create a ModelDescriptor object, i.e. a representation of the XML for further tweaking etc.
modelIO = ModelIO.getStandardModelIO()
modelDescriptor = modelIO.loadModel(modelDoc, runtime.getClassLoader(), false, new ExHandler())

// set the workspace explicitly if needed
modelDescriptor.setWorkspacePath(defaultWorkspacePath)

// load the model into the runtime and execute it
runtime.loadModel(modelDescriptor, defaultWorkspacePath)
runtime.runModel()

