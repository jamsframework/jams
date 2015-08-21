/*
 * Styled3DMapPane.java
 *
 * Created on 7. Mai 2007, 18:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.gui;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.GraphicsConfiguration;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.AWTEvent;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import javax.media.j3d.*;
import javax.vecmath.*;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

/**
 *
 * @author Christian(web)
 */
public class Styled3DMapPane extends Applet {
    TransformGroup objRotate = null;
    TransformGroup objScale = null;
    BufferedImage img = null;
    BranchGroup scene = null;
    SimpleUniverse simpleU = null;
//    LiteRenderer liteRenderer = null;
    AffineTransform affineTransform = null;
    Rectangle bound = null;
    Envelope envelope = null;
    Canvas3D canvas3D = null;
    JAMSAscGridReader agr = null;
    MyOrbitBehavior orbit = null;
    
    public double   textureHeight = 256.0,
            textureWidth  = 256.0;
    
    public int xRes = 256,yRes = 256;
    public float scale = 1.2f;
    public float height = 0.5f;
    public float hScale = 1.0f;
    public boolean light = true;
    
    /** Creates a new instance of Styled3DMapPane */
    public Styled3DMapPane() {
        setLayout(new BorderLayout());
    }
    
    @Override
    public void init() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        
        simpleU = new SimpleUniverse(canvas3D);
        simpleU.getViewer().getView().setFrontClipDistance(0.001);
        simpleU.getViewer().getView().setBackClipDistance(10.0);
        scene = createSceneGraph(simpleU);
        simpleU.addBranchGraph(scene);
    }
    
    @Override
    public void destroy(){
        super.destroy();
        simpleU.cleanup();
    }
    
    class MyScaleBehavior extends Behavior {
        public void initialize() {
            wakeupOn(new WakeupOnElapsedFrames(0));
        }
        public void processStimulus(Enumeration enumeration) {
            //update TransformGroup
            Transform3D transform = new Transform3D();
            objScale.getTransform(transform);
            Matrix4d matrix = new Matrix4d();
            transform.get(matrix);
            matrix.m22 = hScale*1.0;
            transform.set(matrix);
            objScale.setTransform(transform);
            
            wakeupOn(new WakeupOnElapsedFrames(0));
        }
    }
    
    //this moves the grid
    class MyMouseRotateBehavior extends Behavior {
        double rotX = 0.0,rotZ = 0.0;
        int x,x_last,y,y_last;
        int x2,x2_last,y2,y2_last;
        boolean init,initb2;
        double xFactor = 0.0065,yFactor = 0.0065;
        double xTranslationFactor = 0.01,yTranslationFactor = 0.01;
        
        TransformGroup transformGroup;
        
        private WakeupOr mouseCriterion;
        boolean bOnClick = false;
        boolean bOnClickb2 = false;
        
        MyMouseRotateBehavior() {
            init = false;
            initb2 = false;
            
            WakeupCriterion mouseEvents[] = new WakeupCriterion[3];
            mouseEvents[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
            mouseEvents[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
            mouseEvents[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
            
            mouseCriterion = new WakeupOr(mouseEvents);
        }
        
        public void initialize() {
            Transform3D trans = new Transform3D();
            trans.setTranslation(new Vector3d(0,0,0));
            rotX = 0.7853;
            trans.rotX(rotX);
            trans.rotY(0.0);
            trans.rotZ(0.0);
            transformGroup.setTransform(trans);
            init = false;
            
            wakeupOn(mouseCriterion);
        }
        
        public void processMouseEvent(MouseEvent mouseEvent) {
            if (mouseEvent.getButton() == mouseEvent.BUTTON2)
                return;
            
            //System.out.println(mouseEvent.getID() + " " + mouseEvent.getButton());
            if (mouseEvent.getID() == mouseEvent.MOUSE_PRESSED) {
                if (mouseEvent.getButton() == mouseEvent.BUTTON1) {
                    init = false;
                    this.bOnClick = true;
                }
            }
            if (mouseEvent.getID() == mouseEvent.MOUSE_RELEASED) {
                if (mouseEvent.getButton() == mouseEvent.BUTTON1) {
                    this.bOnClick = false;
                    //return;
                }
            }
            
            if (mouseEvent.getID() == mouseEvent.MOUSE_PRESSED) {
                if (mouseEvent.getButton() == mouseEvent.BUTTON3) {
                    initb2 = false;
                    this.bOnClickb2 = true;
                }
            }
            if (mouseEvent.getID() == mouseEvent.MOUSE_RELEASED) {
                if (mouseEvent.getButton() == mouseEvent.BUTTON3) {
                    this.bOnClickb2 = false;
                    //return;
                }
            }
            /*if (bOnClick && bOnClickb2) {
                bOnClick = false;
                bOnClickb2 = false;
                init = false;
            }*/
            
            if ((mouseEvent.getModifiersEx() & mouseEvent.BUTTON1_DOWN_MASK) != 0) {
                x = mouseEvent.getX();
                y = mouseEvent.getY();
                if (!init) {
                    x_last = x;
                    y_last = y;
                    init = true;
                }
            }
            
            if ((mouseEvent.getModifiersEx() & mouseEvent.BUTTON3_DOWN_MASK) != 0) {
                x2 = mouseEvent.getX();
                y2 = mouseEvent.getY();
                if (!initb2) {
                    x2_last = x2;
                    y2_last = y2;
                    initb2 = true;
                }
            }
            
            //scrap ... this because sometimes no release event is received
            //is there any better method to init the last_positions?
            if (x2 - x2_last < -40 || x2 - x2_last > 40)
                x2_last = x2;
            if (x - x_last < -40   || x - x_last > 40)
                x_last = x;
            if (y2 - y2_last < -40 || y2 - y2_last > 40)
                y2_last = y2;
            if (y - y_last < -40 || y - y_last > 40)
                y_last = y;
            
            if ((mouseEvent.getModifiersEx() & mouseEvent.BUTTON1_DOWN_MASK) != 0) {
                rotZ += ((double)(this.x - this.x_last)) * xFactor;
                rotX += ((double)(this.y - this.y_last)) * yFactor;
                
                if (rotX < -1.57)	rotX = -1.57;
                if (rotX >  0)	rotX =  0;
                
                Transform3D trans = new Transform3D();
                
                Transform3D transRotX = new Transform3D();
                transRotX.rotX(rotX);
                Transform3D transRotZ = new Transform3D();
                transRotZ.rotZ(rotZ);
                transRotX.mul(transRotZ);
                trans = transRotX;
                this.transformGroup.setTransform(trans);
            }
            
            if ((mouseEvent.getModifiersEx() & mouseEvent.BUTTON3_DOWN_MASK) != 0 && !((mouseEvent.getModifiersEx() & mouseEvent.BUTTON1_DOWN_MASK) != 0)) {
                Point3d point = new Point3d();
                orbit.getRotationCenter(point);
                
                Transform3D trans = new Transform3D();
                this.transformGroup.getTransform(trans);
                Matrix4d mat = new Matrix4d();
                trans.get(mat);
                
                //rotierte ebene berechnen
                Matrix4d mat2 = new Matrix4d();
                mat2.setZero();
                mat2.m20 = 1.0;//((double)(this.x - this.x_last)) * xFactor;
                mat.mul(mat2);
                //gradient auf rotierter ebene!
                Vector3d grad = new Vector3d(mat.m00,mat.m10,mat.m20);
                
                //Ziel: Bewegung in x bzw. y Richtung im Bildraum unter der Nebenbedingung dass
                //wir uns in der rotierten Ebene bewegen
                Vector3d vTransX = new Vector3d();
                Vector3d vTransY = new Vector3d();
                if (grad.z != 0) {
                    vTransX.x = 1.0;
                    vTransX.y = 0.0;
                    vTransX.z = -grad.x / grad.z;
                    vTransX.normalize();
                    vTransX.x *= -((double)(this.x2 - this.x2_last)) * xFactor;
                    vTransX.z *= -((double)(this.x2 - this.x2_last)) * xFactor;
                }
                if (grad.x != 0) {
                    vTransX.x = -grad.z / grad.x;
                    vTransX.y = 0.0;
                    vTransX.z = 1.0;
                    vTransX.normalize();
                    vTransX.x *= -((double)(this.x2 - this.x2_last)) * xFactor;
                    vTransX.z *= -((double)(this.x2 - this.x2_last)) * xFactor;
                }
                
                if (grad.z != 0) {
                    vTransY.x = 0.0;
                    vTransY.y = 1.0;
                    vTransY.z = -grad.y / grad.z;
                    vTransY.normalize();
                    vTransY.y *= -((double)(this.y2 - this.y2_last)) * yFactor;
                    vTransY.z *= -((double)(this.y2 - this.y2_last)) * yFactor;
                }
                if (grad.y != 0) {
                    vTransY.x = 0.0;
                    vTransY.y = -grad.z / grad.y;
                    vTransY.z = 1.0;
                    vTransY.normalize();
                    vTransY.y *= -((double)(this.y2 - this.y2_last)) * yFactor;
                    vTransY.z *= -((double)(this.y2 - this.y2_last)) * yFactor;
                }
                
                point.x += vTransX.x + vTransY.x;
                point.y += vTransX.y + vTransY.y;
                point.z += vTransX.z + vTransY.z;
                
                orbit.setRotationCenter(point);
            }
            x_last = x;
            y_last = y;
            x2_last = x2;
            y2_last = y2;
        }
        
        public void setTransformGroup(TransformGroup gp) {
            this.transformGroup = gp;
        }
        
        public void processStimulus(Enumeration enumeration) {
            WakeupCriterion wakeup = (WakeupCriterion) enumeration.nextElement();
            if (wakeup instanceof WakeupOnAWTEvent) {
                AWTEvent event[] = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
                for (int i=0; i<event.length; i++) {
                    processMouseEvent((MouseEvent) event[i]);
                }
            }
            wakeupOn(mouseCriterion);
        }
    }
    
    class MyOrbitBehavior extends OrbitBehavior {
        MyOrbitBehavior(Canvas3D c3d) {
            super(c3d);
            
            this.setRotationCenter(new Point3d(0,0,0));
            this.setTranslateEnable(false);
            this.setRotYFactor(0.0);
            this.setRotXFactor(0.0);
            this.setZoomFactor(0.5);
            setMinRadius(100.0);
        }
        
        public void mouseMoved(MouseEvent mouseEvent) {
            super.mouseMoved(mouseEvent);
        }
        
        public void mouseWheelMoved(MouseWheelEvent m) {
            super.mouseWheelMoved(m);
        }
    }
    
    class UpdateTextureBehavior extends Behavior implements ImageComponent2D.Updater{
        
        WakeupOnElapsedFrames w = null;
        
        public UpdateTextureBehavior(){
            w = new WakeupOnElapsedFrames(0);
        }
        
        public void initialize(){
            wakeupOn(w);
        }
        
        public void updateData(ImageComponent2D imageComponent, int x, int y, int width, int height) {
            imageComponent.set(img);
        }
        
        public void processStimulus(Enumeration critiria){
            Grid3D grid = (Grid3D)objScale.getChild(0);
            
            ((ImageComponent2D)grid.getAppearance().getTexture().getImage(0)).updateData(this,0,0,0,0);
            
            wakeupOn(w);
        }
    }
    
    
    public BranchGroup createSceneGraph(SimpleUniverse su) {
        BranchGroup objRoot = new BranchGroup();
        TransformGroup vpTrans = null;
        BoundingSphere mouseBounds = null;
        
        vpTrans = su.getViewingPlatform().getViewPlatformTransform();
        
        mouseBounds = new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY);
        
        Background bg = new Background(new Color3f(0.8f,0.8f,0.85f));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0f));
        
        objRoot.addChild(bg);
        
        objScale = new TransformGroup();
        objScale.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objScale.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        Transform3D scale = new Transform3D();
        Matrix4d matrix = new Matrix4d();
        matrix.setIdentity();
        matrix.m00 = 1.0;
        matrix.m11 = 1.0;
        matrix.m22 = 1.0;
        matrix.m33 = 1.0;
        
        scale.set(matrix);
        objScale.setTransform(scale);
        objScale.addChild(new Grid3D());
        
        objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        MyMouseRotateBehavior myMouseRotate = new MyMouseRotateBehavior();
        myMouseRotate.setTransformGroup(objRotate);
        myMouseRotate.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
        objRoot.addChild(myMouseRotate);
        
        UpdateTextureBehavior utb = new UpdateTextureBehavior();
        utb.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000.0));
        objRotate.addChild(objScale);
        objRotate.addChild(utb);
        
        objRoot.addChild(objRotate);
        
        MyScaleBehavior myScaleBehavior = new MyScaleBehavior();
        myScaleBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
        objRoot.addChild(myScaleBehavior);
        
        if (this.light) {
            AmbientLight lightA = new AmbientLight();
            lightA.setColor(new Color3f(0,0,0));
            
            lightA.setInfluencingBounds(new BoundingSphere());
            objRoot.addChild(lightA);
            
            DirectionalLight lightD1 = new DirectionalLight();
            lightD1.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
            Vector3f direction1 = new Vector3f(-1.0f, -1.0f, -0.5f);
            direction1.normalize();
            lightD1.setDirection(direction1);
            lightD1.setColor(new Color3f(0.1f, 0.1f, 0.1f));
            objRoot.addChild(lightD1);
        }
        
        orbit = new MyOrbitBehavior(su.getCanvas());
        orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
        
        su.getViewingPlatform().setViewPlatformBehavior(orbit);
        
        objRoot.compile();
        
        int x = su.getCanvas().getBounds().x + 20;
        int y = su.getCanvas().getBounds().y + 20;
        MouseWheelEvent m = new MouseWheelEvent(this, 507, 0, 0 , x, y, 0, false, 0, 3, 1);
        
        orbit.mouseWheelMoved(m);
        orbit.mouseWheelMoved(m);
        orbit.mouseWheelMoved(m);
        orbit.mouseWheelMoved(m);
        orbit.mouseWheelMoved(m);
        
        return objRoot;
    }
    
    public void setHeightMap(JAMSAscGridReader agr) {
        this.agr = agr;
    }
    
    @SuppressWarnings("unchecked")
    public void setContext(org.geotools.map.MapContext map) {
        double width = this.textureWidth;
        double height = this.textureHeight;
        
        img = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = img.createGraphics();
        
        double xs = this.agr.GetX11Corner();
        double ys = this.agr.GetY11Corner();
        double xe = xs + this.agr.GetNumberOfColums()*this.agr.GetCellSize();
        double ye = ys + this.agr.GetNumberOfRows()*this.agr.GetCellSize();
        double cellsize = this.agr.GetCellSize();
        envelope = new Envelope(/*xs,xe,ys,ye*/);
        /*envelope.init(xs,xe,ys,ye);	*/
        for (MapLayer mapLayer : map.getLayers()) {
            try {
                envelope.expandToInclude(mapLayer.getFeatureSource().getFeatures().getBounds());
            } catch (Exception e) {
                System.out.println("Error because: " + e.toString());
            }
        }
        
        xs = (int)(envelope.getMinX()/cellsize)*cellsize;
        xe = (int)(envelope.getMaxX()/cellsize)*cellsize;
        ys = (int)(envelope.getMinY()/cellsize)*cellsize;
        ye = (int)(envelope.getMaxY()/cellsize)*cellsize;
        envelope = new Envelope(xs,xe,ys,ye);
        bound = new Rectangle();
        bound.x = 0;
        bound.y = 0;
        bound.width = (int)width;
        bound.height = (int)height;
        
        GTRenderer myRender = new StreamingRenderer();
        myRender.setContext(map);
        HashMap hints = new HashMap();
        //hints.put("optimizedDataLoadingEnabled", Boolean.TRUE);
        myRender.setRendererHints(hints);
        myRender.paint(graphics2D, bound , envelope);
        
        try {
            //ImageIO.write(img,"BMP",new File("E:\\test.bmp"));
        } catch(Exception e) {
        }
    }
    
    public class Grid3D extends Shape3D{
        
        public Grid3D() {
            Init();
        }
        public void Init() {
            this.setCapability(this.ALLOW_GEOMETRY_WRITE);
            this.setCapability(this.ALLOW_GEOMETRY_READ);
            this.setGeometry(createGeometry());
            
            Appearance appear = new Appearance();
            
            PolygonAttributes polyAttr = new PolygonAttributes();
            polyAttr.setCullFace(polyAttr.CULL_NONE);
            appear.setPolygonAttributes(polyAttr);
            
            BufferedImage bImage = new BufferedImage((int)textureWidth, (int)textureWidth ,BufferedImage.TYPE_INT_ARGB);
            ImageComponent2D image = new ImageComponent2D(ImageComponent2D.FORMAT_RGB,bImage,true,true);
            image.setCapability(image.ALLOW_IMAGE_WRITE | image.ALLOW_IMAGE_READ | image.ALLOW_FORMAT_READ);
            Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,image.getWidth(), image.getHeight());
            texture.setImage(0, image);
            appear.setTexture(texture);
            appear.setCapability(appear.ALLOW_TEXTURE_WRITE | appear.ALLOW_TEXTURE_READ | appear.ALLOW_TEXGEN_READ | appear.ALLOW_TEXGEN_WRITE | appear.ALLOW_TEXTURE_ATTRIBUTES_READ | appear.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
            texture.setCapability(texture.ALLOW_ENABLE_WRITE | texture.ALLOW_ENABLE_READ | texture.ALLOW_IMAGE_WRITE);
            
            Material material = new Material();
            material.setDiffuseColor(0.0f,0.0f,0.0f);
            material.setAmbientColor(0,0,0);
            material.setEmissiveColor(0,0,0);
            material.setShininess(0.0f);
            material.setLightingEnable(true);
            
            appear.setMaterial(material);
            
            this.setAppearance(appear);
        }
        
        public Geometry createGeometry(){
            double scalex = envelope.getWidth();
            double scaley = envelope.getHeight();
            
            if (scalex > scaley) {
                scaley = (scaley/scalex)*scale;
                scalex = scale;
            } else {
                scalex = (scalex/scaley)*scale;
                scaley = scale;
            }
            
            double offsetx = 0.5*scalex;
            double offsety = 0.5*scaley;
            double invxRes = scalex/(double)(xRes-1);
            double invyRes = scaley/(double)(yRes-1);
            
            //go through and determine minheight and maxheight
            float minHeight = Float.POSITIVE_INFINITY,maxHeight = Float.NEGATIVE_INFINITY;
            float hMap[][] = new float[xRes][yRes];
            
            for (int x=0;x<xRes;x++) {
                for (int y=0;y<yRes;y++) {
                    float fx = x,fy = y;
                    if (agr != null && envelope != null) {
                        double posX = envelope.getMinX() + fx*(invxRes/scalex)*envelope.getWidth();
                        double posY = envelope.getMinY() + fy*(invyRes/scaley)*envelope.getHeight();
                        float value = (float)agr.getValue(posX,posY);
                        hMap[x][y] = value;
                        if (value != -1.0) {
                            if (minHeight > value)
                                minHeight = value;
                            if (maxHeight < value)
                                maxHeight = value;
                        }
                    }
                }
            }
            //scale height to 5 units
            float hScale = height / (maxHeight -  minHeight);
            
            int indexCounter = 0;
            
            IndexedQuadArray grid = new IndexedQuadArray(xRes*yRes,IndexedQuadArray.NORMALS | IndexedQuadArray.TEXTURE_COORDINATE_2 | IndexedQuadArray.COORDINATES,4*(xRes-1)*(yRes-1));
            for (int x=0;x<xRes;x++) {
                for (int y=0;y<yRes;y++) {
                    float fx = x,fy = y;
                    if (agr == null) {
                        grid.setCoordinate( x*xRes+y, new Point3f((float)((fx*invxRes)-offsetx),(float)((fy*invyRes)-offsety),0.0f));
                    } else {
                        float value = hMap[x][y];
                        value -= minHeight;
                        grid.setCoordinate( x*xRes+y, new Point3f((float)((fx*invxRes)-offsetx),(float)((fy*invyRes)-offsety),(float)hScale*value - 0.5f*height));
                    }
                    //y axes is mirrored
                    grid.setTextureCoordinate(0,x*xRes+y,new TexCoord2f((float)(fx*invxRes/scalex),1.0f-(float)(fy*invyRes/scaley)));
                    
                    if (x != xRes-1 && y != yRes-1 && hMap[x][y] >= 0.0 && hMap[x+1][y] >= 0.0 && hMap[x][y+1] >= 0.0 && hMap[x+1][y+1] >= 0.0) {
                        
                        /*grid.setCoordinateIndex(4*(x*(xRes-1)+y)+0,x*xRes+y);
                        grid.setCoordinateIndex(4*(x*(xRes-1)+y)+1,(x+1)*xRes+y);
                        grid.setCoordinateIndex(4*(x*(xRes-1)+y)+2,(x+1)*xRes+y+1);
                        grid.setCoordinateIndex(4*(x*(xRes-1)+y)+3,(x)*xRes+y+1);
                         
                        grid.setTextureCoordinateIndex(0,4*(x*(xRes-1)+y)+0,x*xRes+y);
                        grid.setTextureCoordinateIndex(0,4*(x*(xRes-1)+y)+1,(x+1)*xRes+y);
                        grid.setTextureCoordinateIndex(0,4*(x*(xRes-1)+y)+2,(x+1)*xRes+y+1);
                        grid.setTextureCoordinateIndex(0,4*(x*(xRes-1)+y)+3,(x)*xRes+y+1);
                         
                        grid.setNormalIndex(4*(x*(xRes-1)+y)+0,x*xRes+y);
                        grid.setNormalIndex(4*(x*(xRes-1)+y)+1,(x+1)*xRes+y);
                        grid.setNormalIndex(4*(x*(xRes-1)+y)+2,(x+1)*xRes+y+1);
                        grid.setNormalIndex(4*(x*(xRes-1)+y)+3,(x)*xRes+y+1);*/
                        
                        grid.setCoordinateIndex(indexCounter+0,x*xRes+y);
                        grid.setCoordinateIndex(indexCounter+1,(x+1)*xRes+y);
                        grid.setCoordinateIndex(indexCounter+2,(x+1)*xRes+y+1);
                        grid.setCoordinateIndex(indexCounter+3,(x)*xRes+y+1);
                        
                        grid.setTextureCoordinateIndex(0,indexCounter+0,x*xRes+y);
                        grid.setTextureCoordinateIndex(0,indexCounter+1,(x+1)*xRes+y);
                        grid.setTextureCoordinateIndex(0,indexCounter+2,(x+1)*xRes+y+1);
                        grid.setTextureCoordinateIndex(0,indexCounter+3,(x)*xRes+y+1);
                        
                        grid.setNormalIndex(indexCounter+0,x*xRes+y);
                        grid.setNormalIndex(indexCounter+1,(x+1)*xRes+y);
                        grid.setNormalIndex(indexCounter+2,(x+1)*xRes+y+1);
                        grid.setNormalIndex(indexCounter+3,(x)*xRes+y+1);
                        
                        indexCounter+=4;
                    }
                    /***************************************************************************
                     ***
                     * this represents part of a heightmap:
                     * v11   v12   v13
                     * x-----x-----x
                     * |\    |	   /|
                     * |  \  |	 /  |
                     * v21   v22   v23
                     * x-----x-----x
                     * |    /|\    |
                     * | /   |	 \  |
                     * v31   v32   v33
                     * x-----x-----x
                     ***
                     ****************************************************************************/
                    //calculate normals
                    Vector3f	v11 = new Vector3f(),
                            v12 = new Vector3f(),
                            v13 = new Vector3f(),
                            v21 = new Vector3f(),
                            v22 = new Vector3f(),
                            v23 = new Vector3f(),
                            v31 = new Vector3f(),
                            v32 = new Vector3f(),
                            v33 = new Vector3f(),
                            vN1 = new Vector3f(),
                            vN2 = new Vector3f(),
                            vN3 = new Vector3f(),
                            vN4 = new Vector3f(),
                            vN5 = new Vector3f(),
                            vN6 = new Vector3f(),
                            vN7 = new Vector3f(),
                            vN8 = new Vector3f(),
                            vtmp1 = new Vector3f(),
                            vtmp2 = new Vector3f(),
                            vResult = new Vector3f();
                    /***************************************************************************
                     ***create a vector to every point in heightmap***
                     ****************************************************************************/
                    //v22
                    v22.x = (float)((((float)x)*invxRes)-offsetx);
                    v22.y = (float)((((float)y)*invyRes)-offsety);
                    v22.z = hScale*hMap[x][y] - 2.5f;
                    //v11
                    v11.x = (float)((((float)(x-1))*invxRes)-offsetx);
                    v11.y = (float)((((float)(y-1))*invxRes)-offsety);
                    if (x>0&&y>0)
                        v11.z = hScale*hMap[x-1][y-1] - 2.5f;
                    else
                        v11.z = v22.z;
                    //v12
                    v12.x = (float)((((float)(x))*invxRes)-offsetx);
                    v12.y = (float)((((float)(y-1))*invxRes)-offsety);
                    if (y>0)
                        v12.z = hScale*hMap[x][y-1] - 2.5f;
                    else
                        v12.z = v22.z;
                    //v13
                    v13.x = (float)((((float)(x+1))*invxRes)-offsetx);
                    v13.y = (float)((((float)(y-1))*invxRes)-offsety);
                    if (x<xRes-1 && y>0)
                        v13.z = hScale*hMap[x+1][y-1] - 2.5f;
                    else
                        v13.z = v22.z;
                    //v21
                    v21.x = (float)((((float)(x-1))*invxRes)-offsetx);
                    v21.y = (float)((((float)(y))*invxRes)-offsety);
                    if (x>0)
                        v21.z = hScale*hMap[x-1][y] - 2.5f;
                    else
                        v21.z = v22.z;
                    //v23
                    v23.x = (float)((((float)(x+1))*invxRes)-offsetx);
                    v23.y = (float)((((float)(y))*invxRes)-offsety);
                    if (x<xRes-1)
                        v23.z = hScale*hMap[x+1][y] - 2.5f;
                    else
                        v23.z = v22.z;
                    //v31
                    v31.x = (float)((((float)(x-1))*invxRes)-offsetx);
                    v31.y = (float)((((float)(y+1))*invxRes)-offsety);
                    if (x>0 && y < yRes-1)
                        v31.z = hScale*hMap[x-1][y+1] - 2.5f;
                    else
                        v31.z = v22.z;
                    //v32
                    v32.x = (float)((((float)(x))*invxRes)-offsetx);
                    v32.y = (float)((((float)(y+1))*invxRes)-offsety);
                    if (y < yRes-1)
                        v32.z = hScale*hMap[x][y+1] - 2.5f;
                    else
                        v32.z = v22.z;
                    //v33
                    v33.x = (float)((((float)(x+1))*invxRes)-offsetx);
                    v33.y = (float)((((float)(y+1))*invxRes)-offsety);
                    if (x < xRes-1 && y < yRes-1)
                        v33.z = hScale*hMap[x+1][y+1] - 2.5f;
                    else
                        v33.z = v22.z;
                    /***************************************************************************
                     ***
                     * zwei vektoren spannen eine ebene auf in der ein dreieck liegt,
                     * berechnen des Normalenvektors zu der Ebene
                     ***
                     ****************************************************************************/
                    vtmp1.sub(v22,v11);
                    vtmp2.sub(v21,v22);
                    
                    vN1.cross(vtmp1,vtmp2);
                    vN1.normalize();
                    
                    vtmp1.sub(v22,v12);
                    vtmp2.sub(v11,v22);
                    vN2.cross(vtmp1,vtmp2);
                    vN2.normalize();
                    
                    vtmp1.sub(v22,v13);
                    vtmp2.sub(v12,v22);
                    vN3.cross(vtmp1,vtmp2);
                    vN3.normalize();
                    
                    vtmp1.sub(v22,v23);
                    vtmp2.sub(v13,v22);
                    vN4.cross(vtmp1,vtmp2);
                    vN4.normalize();
                    
                    vtmp1.sub(v22,v33);
                    vtmp2.sub(v23,v22);
                    vN5.cross(vtmp1,vtmp2);
                    vN5.normalize();
                    
                    vtmp1.sub(v22,v32);
                    vtmp2.sub(v33,v22);
                    vN6.cross(vtmp1,vtmp2);
                    vN6.normalize();
                    
                    vtmp1.sub(v22,v31);
                    vtmp2.sub(v32,v22);
                    vN7.cross(vtmp1,vtmp2);
                    vN7.normalize();
                    
                    vtmp1.sub(v22,v21);
                    vtmp2.sub(v31,v22);
                    vN8.cross(vtmp1,vtmp2);
                    vN8.normalize();
                    /***************************************************************************
                     ***der resultierende normalenvektor ist der mittelwert aller anliegenden
                     ****************************************************************************/
                    vN1.add(vN2);
                    vN1.add(vN3);
                    vN1.add(vN4);
                    vN1.add(vN5);
                    vN1.add(vN6);
                    vN1.add(vN7);
                    vN1.add(vN8);
                    //vN1.negate();
                    vResult = vN1;
                    vResult.normalize();
                    
                    grid.setNormal(x*xRes+y,vResult);
                }
            }
            
            return grid;
        }
    }
}
