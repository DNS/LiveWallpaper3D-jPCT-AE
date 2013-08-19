package com.example.LiveWallpaper3D;

import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.Scene;
import com.threed.jpct.*;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;
import com.threed.jpct.util.SkyBox;
import net.rbgrn.android.glwallpaperservice.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.Vector;

public class MyWallpaperService extends GLWallpaperService {
    private static MyWallpaperService master = null;

    private GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null;
    private World world = null;
    private RGBColor back = new RGBColor(50, 50, 100);

    private Vector<EditorObject> objects;

    private float touchTurn = 0;
    private float touchTurnUp = 0;

    private float xpos = -1;
    private float ypos = -1;

    private Object3D cube = null;
    private int fps = 0;

    private Light sun = null;

    public MyWallpaperService() {
        super();
    }


    public Engine onCreateEngine() {
        MyEngine engine = new MyEngine();
        return engine;
    }

    class MyEngine extends GLEngine {
        MyRenderer renderer;
        /*public MyEngine() {
            super();
            // handle prefs, other initialization
            renderer = new MyRenderer();
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                //renderer.release();
            }
            renderer = null;
        }*/
        public MyEngine() {
            super();
            renderer = new MyRenderer();

            setRenderer(renderer);
            //setEGLConfigChooser(8, 8, 8, 8, 0, 0);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
/*
        public Bundle onCommand (String paramString, int paramInt1, int paramInt2, int paramInt3, Bundle paramBundle, boolean paramBoolean)
        {
            if (paramString.equals("android.wallpaper.tap"))
                MyWallpaperService.this.renderer.onTouch_(null);
            return null;
        }
*/
        @Override
        public void onTouchEvent(MotionEvent me)
        {

            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                xpos = me.getX();
                ypos = me.getY();
            }

            if (me.getAction() == MotionEvent.ACTION_UP) {
                xpos = -1;
                ypos = -1;
                touchTurn = 0;
                touchTurnUp = 0;
            }

            if (me.getAction() == MotionEvent.ACTION_MOVE) {
                float xd = me.getX() - xpos;
                float yd = me.getY() - ypos;

                xpos = me.getX();
                ypos = me.getY();

                touchTurn = xd / -100f;
                touchTurnUp = yd / -100f;
            }

            try {
                Thread.sleep(15);
            } catch (Exception e) {
                // No need for this...
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
        }
    }


/*
    class MyRenderer implements GLWallpaperService.Renderer {
        public void onDrawFrame(GL10 gl) {
            // Your rendering code goes here

            gl.glClearColor(0.2f, 0.4f, 0.2f, 1f);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        }


        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }


        public void release() {
        }
    }
*/


    class MyRenderer implements GLWallpaperService.Renderer {

        private long time = System.currentTimeMillis();
        private SkyBox sky;
        private AssetManager assetManager = getAssets();

        public MyRenderer() {
        }



        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) {
                fb.dispose();
            }
            fb = new FrameBuffer(gl, w, h);

            if (master == null) {

                world = new World();
                world.setAmbientLight(20, 20, 20);

                sun = new Light(world);
                sun.setIntensity(250, 250, 250);

                // Create a texture out of the icon...:-)
                Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.icon)), 64, 64));
                Texture sky_tex = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.sky)), 64, 64));
                TextureManager.getInstance().addTexture("texture", texture);
                TextureManager.getInstance().addTexture("sky", sky_tex);

				/*cube = Primitives.getCube(10);
				cube.calcTextureWrapSpherical();
				cube.setTexture("texture");
				cube.strip();
				cube.build();
*/
                //world.addObject(cube);

                //sky = new SkyBox("sky", "sky", "sky", "sky", "sky", "sky", 90.0f);


                objects = Scene.loadLevelAE("test.txt", objects, world, assetManager);
                cube = Scene.findObject("barrel", objects);
                cube.scale(10.0f);
                world.addObject(cube);

                Camera cam = world.getCamera();
                cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
                cam.lookAt(cube.getTransformedCenter());

                SimpleVector sv = new SimpleVector();
                sv.set(cube.getTransformedCenter());
                sv.y -= 100;
                sv.z -= 100;
                sun.setPosition(sv);
                MemoryHelper.compact();

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    master = MyWallpaperService.this;
                }
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        public void onDrawFrame(GL10 gl) {
            if (touchTurn != 0) {
                cube.rotateY(touchTurn);
                touchTurn = 0;
            }

            if (touchTurnUp != 0) {
                cube.rotateX(touchTurnUp);
                touchTurnUp = 0;
            }

            fb.clear(back);

            //sky.render(world, fb);
            world.renderScene(fb);

            world.draw(fb);

            fb.display();

            if (System.currentTimeMillis() - time >= 1000) {
                Logger.log(fps + "fps");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }

       /*
        public boolean onTouch_(MotionEvent me) {

            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                xpos = me.getX();
                ypos = me.getY();
                return true;
            }

            if (me.getAction() == MotionEvent.ACTION_UP) {
                xpos = -1;
                ypos = -1;
                touchTurn = 0;
                touchTurnUp = 0;
                return true;
            }

            if (me.getAction() == MotionEvent.ACTION_MOVE) {
                float xd = me.getX() - xpos;
                float yd = me.getY() - ypos;

                xpos = me.getX();
                ypos = me.getY();

                touchTurn = xd / -100f;
                touchTurnUp = yd / -100f;
                return true;
            }

            try {
                Thread.sleep(15);
            } catch (Exception e) {
                // No need for this...
            }

            return false;
        }
        */

        public void release() {
        }

    }
}


