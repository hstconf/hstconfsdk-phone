
package com.infowarelab.conference.ui.activity.inconf.view.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.hongshantongphone.ConfAPI;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("NewApi")
public class GLVideoDecoderView extends FrameLayout {

    private static final String TAG = "IFL.VideoDecoderView";
    public GLVideoDecoderView.GLRenderer renderer = null;
    private BaseFilter mCurrentFilter;
    private Context c;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture = null;
    private Surface mSurface = null;
    private float[] mSTMatrix = new float[16];
    private  FilterFactory.FilterType type;

    private boolean isSvc = false;
    private boolean isCreated = false;

    protected static final int VIDEO_SYNC = 8888;
    public static boolean isSoftDrawing = false;
    /** surface???????????? **/
    private SurfaceHolder holder;

    private Context activity;
    /** ???????????????channelID **/
    private int channelId;
    /** ????????????????????? **/
    private int userID;
    /** ??????SDK?????? **/
    protected CommonFactory commonFactory = CommonFactory.getInstance();
    private VideoCommonImpl videoCommon =  (VideoCommonImpl) commonFactory.getVideoCommon();;
    private UserCommonImpl userCommon = (UserCommonImpl) commonFactory.getUserCommon();
    private boolean isSupport = true;
    private boolean isWaiting = false;
    private GLSurfaceView surfaceView;
    private TextView userName;
    private Button deleteView;
    private LinearLayout relative;
    private TextView tvWait;
    private LinearLayout.LayoutParams cameraParames;
    private LinearLayout.LayoutParams delParames;
    private int screenW;
    private int screenH;
    private int pHeight;

    private IOnDeleteAndCameraClick onDeleteAndCameraClick;

    public GLVideoDecoderView(Context context) {
        super(context);
        initParames(context,0,0,0,0);
    }
    public GLVideoDecoderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = context;
        initParames(context,0,0,0,0);
    }
    public GLVideoDecoderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.activity = context;
        initParames(context,0,0,0,0);
    }

    /**
     * ???????????????
     * ????????????12sp,white???
     */
    private void initParames(Context context,int textSizeSp,int textColor,int cameraSrc,int deleteSrc){

        this.c = context;
        activity = context;

    }

    private PaintFlagsDrawFilter pfdf = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private ByteBuffer buffer;
    private Bitmap videoBit;
    private int bitWidth=640;
    private int bitHeight=320;

    /**
     * ?????????????????????
     * @param width
     * @param height
     */
    public void resetSize(int width,int height) {
        Log.d(TAG, "VideoDecoderView.resetSize:" + width + "x" + height);
        if(!videoCommon.isHardDecode()){
            dataQueue.clear();
        }
        bitWidth = width;
        bitHeight = height;
        if(bitWidth>0&&bitHeight>0){
            videoBit = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }
        if( bitHeight * bitWidth > 1920 * 1080 && width > 0&&!videoCommon.isHardDecode()){
            setBackgroundResource(R.drawable.bg_video_nosupport);
            isSupport = false;
        }else{
            setBackgroundResource(0);
            isSupport = true;
        }

//        mCurrentFilter.setTextureSize(new Size(width,height));

//		setParams(screenW, screenH);
//		setSurface(width, height);
//		invalidate();
    }
    public void setSurface(int w, int h) {

        Log.d(TAG, "VideoDecoderView.setSurface:" + w + "x" + h);

        if (screenH != 0 && screenW != 0 && surfaceView != null) {
            if (w>0&&h>0) {
                LayoutParams p = (LayoutParams) surfaceView.getLayoutParams();
                float ss = 1f * screenH / screenW;
                float vs = 1f * h / w;
                if (ss == vs) {
                    p.width = screenW;
                    p.height = screenH;
                } else if (ss > vs) {
                    p.width = screenW;
                    p.height = (int) (screenW * vs);
                } else {
                    p.height = screenH;
                    p.width = (int) (1f * screenH / vs);
                }
                surfaceView.setLayoutParams(p);
            } else {
                LayoutParams p = (LayoutParams) surfaceView.getLayoutParams();
                p.width = LayoutParams.MATCH_PARENT;
                p.height = LayoutParams.MATCH_PARENT;
                surfaceView.setLayoutParams(p);
            }
        }
    }

    /**
     * ????????????/????????????
     * @param channelID
     * @param isOpen
     */
    public void changeStatus(int channelID,boolean isOpen) {

        Log.d(TAG, "VideoDecoderView.changeStatus:" + channelID + ":" + isOpen);
        if(videoCommon.isHardDecode()){
            if (isOpen) {
                this.channelId = channelID;
                if (null != surfaceView) {
                    //surfaceView.getHolder().getSurface().release();
                    Log.d(TAG, "VideoDecoderView.changeStatus: Removed old surfaceView");
                    if (mSurface != null) mSurface.release();
                    //surfaceView.setRenderer(null);
                    removeView(surfaceView);
                    surfaceView = null;
                    removeNameCamera();
                }

                Log.d(TAG, "VideoDecoderView.create new GLSurfaceView: " + this.activity.toString());
                surfaceView = new GLSurfaceView(this.activity);
                surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, Gravity.CENTER));

                Log.d(TAG, "VideoDecoderView.addView: surfaceView");
                addView(surfaceView);

                surfaceView.setEGLContextClientVersion(3);

                type = FilterFactory.FilterType.Original;

                Log.d(TAG, "VideoDecoderView.create new GLRenderer");
                renderer = new GLVideoDecoderView.GLRenderer(surfaceView,type);
                surfaceView.setRenderer(renderer);
                surfaceView.setRenderMode(surfaceView.RENDERMODE_WHEN_DIRTY);

//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                setTag(channelID);
                showWait();
                addName();
            } else {
                if (this.channelId != 0) {

                    Log.d(TAG, "VideoDecoderView.videoCommon.closeVideo: " + channelId);
                    videoCommon.closeVideo(this.channelId);
                }
                this.channelId = 0;
                if (null != surfaceView) {
                    Log.d(TAG, "VideoDecoderView.destroy the surfaceView: " + channelId);
                    //surface.getHolder().getSurface().release();
                    if (mSurface != null) mSurface.release();
                    mSurfaceTexture = null;
                    removeView(surfaceView);
                    surfaceView = null;
                    removeNameCamera();
                }
            }
        }else {
            if(isOpen){
                this.channelId = channelID;
                if (null != surfaceView) {
                    removeNameCamera();
                    // ??????
                    setTag(channelID);
                    showWait();
                    addName();
                    if (videoCommon.getDeviceMap().containsKey(channelId)) {
                        this.userID = videoCommon.getDeviceMap().get(
                                channelId);
                        setUserName();
                    }
                    HandlerThread surfaceThread = new HandlerThread(
                            "SurfaceHandler");
                    surfaceThread.start();
                    videoCommon.openVideo(channelId, mSurface);
                    videoCommon.addSurfaceHanlder(surfaceThread,
                            new SurfaceHandler(surfaceThread.getLooper()),
                            channelId);
                    dataQueue.clear();
                } else {
                    surfaceView = new GLSurfaceView(this.activity);

                    addView(surfaceView);

                    surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, Gravity.CENTER));

                    surfaceView.setEGLContextClientVersion(2);
                    //holder = surface.getHolder();
                    //holder.addCallback(this);

                    type = FilterFactory.FilterType.Original;
                    renderer = new GLVideoDecoderView.GLRenderer(surfaceView,type);
                    surfaceView.setRenderer(renderer);
                    surfaceView.setRenderMode(surfaceView.RENDERMODE_WHEN_DIRTY);

                    //surfaceView.setZOrderMediaOverlay(false);

                    setTag(channelID);
                    showWait();
                    addName();
                }
            }else {
                videoCommon.closeVideo(channelID);
            }
        }
    }
    private void showWait() {
        this.isWaiting = true;
        if (null != surfaceView) {
            if (ConfAPI.getInstance().getConfType() == 0)
                surfaceView.setBackgroundResource(R.drawable.bg_video_wait);
            else
                surfaceView.setBackgroundResource(R.drawable.bg_sharedt_wait);
        }

    }

    public void showSupport(boolean isSupport) {
        this.isSupport = isSupport;
        showSupport();
    }

    private void showSupport() {
        if (isSupport) {
            if (isWaiting) {
                showWait();
            } else {
                showSurface();
            }
        } else {
            surfaceView.setBackgroundResource(R.drawable.bg_nosupport);
        }
    }

    public void showSupportReady() {
        this.isWaiting = false;
        if (isSupport) {
            showSurface();
        } else {
            if (surfaceView != null)
                surfaceView.setBackgroundResource(R.drawable.bg_nosupport);
        }
    }

    private void showSurface() {
        if (surfaceView != null)
            surfaceView.setBackgroundColor(0);
    }

    private void addName() {
        int txtSize = 12;
        int txtColor = Color.WHITE;// 0????????????????????????
        // **???????????????**//
        userName = new TextView(activity);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize);
        userName.setTextColor(txtColor);
        userName.setBackgroundColor(Color.TRANSPARENT);
        userName.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        lp.leftMargin = 10;
        lp.bottomMargin = 10
//				+getResources().getDimensionPixelSize(R.dimen.view_inconf_bottom_height)
        ;
        userName.setLayoutParams(lp);
        addView(userName);
        userName.setVisibility(View.GONE);
    }

    private void removeNameCamera() {
        if (userName != null) {
            removeView(userName);
            userName = null;
        }
        if (relative != null) {
            removeView(relative);
            relative = null;
        }
    }


/*	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		screenW=this.getWidth();
		screenH=this.getHeight();
		if (videoCommon.isHardDecode()) {
			if (isSvc) {
				DisplayMetrics dm = getResources().getDisplayMetrics();
				videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
			}
			if (videoCommon.getDeviceMap().containsKey(channelId)) {
				this.userID = videoCommon.getDeviceMap().get(channelId);
				setUserName();
			}
			videoCommon.openVideo(channelId, surface);
		} else {
			if (videoCommon.getDeviceMap().containsKey(channelId)) {
				this.userID = videoCommon.getDeviceMap().get(channelId);
				setUserName();
			}
			HandlerThread surfaceThread = new HandlerThread(
					"SurfaceHandler");
			surfaceThread.start();
			videoCommon.openVideo(channelId, surface);
			videoCommon.addSurfaceHanlder(surfaceThread,
					new SurfaceHandler(surfaceThread.getLooper()),
					channelId);
			drawImage = null;
		}
		isCreated = true;
	}*/
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//							   int height) {
//		screenW = this.getWidth();
//		screenH = this.getHeight();
////		if (isSvc) {
////			DisplayMetrics dm = getResources().getDisplayMetrics();
////			videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
////		}
//		this.holder = holder;
//	}
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		//if(!CallbackManager.IS_LEAVED) return;
//		isDrawing = false;
//		videoCommon.closeVideo(channelId);
//	}

    private boolean isUnlock4release = false;

    public void unlock2release() {
        if (videoCommon.isHardDecode()) {
            if (drawImage != null) {
                isDrawing = false;
                isUnlock4release = true;
            } else {
                Message m = videoCommon.getHandler().obtainMessage(9999);
                videoCommon.getHandler().sendMessage(m);
            }
        } else {
            Message m = videoCommon.getHandler().obtainMessage(9999);
            videoCommon.getHandler().sendMessage(m);
        }
    }

    class SurfaceHandler extends Handler {
        public SurfaceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_SYNC:
                    if(!isSupport)return;
                    try {
                        dataQueue.put((byte[])msg.obj);
                        if(drawImage==null){
                            isDrawing = true;
                            isWatching = true;
                            drawImage = new DrawImage();
                            drawImage.start();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>(8);
    private DrawImage drawImage = null;
    /** ???????????????????????? */
    private boolean isDrawing = false;
    /** ???????????????????????? */
    private boolean isWatching = false;
    private Canvas canvas1;
    class DrawImage extends Thread {
        public void run() {
            while (dataQueue!=null&&isDrawing) {
                if(dataQueue.size()>5){
                    dataQueue.poll();
                }else {
                    try {
                        if(!dataQueue.isEmpty()&&isWatching){
                            isSoftDrawing = true;
                            canvas1 = holder.lockCanvas();  // ????????????
                            byte[] bitBuffer = dataQueue.take();
                            if(null==canvas1||null==bitBuffer){
                                continue;
                            }
                            startSyncDraw(canvas1,bitBuffer);
                            holder.unlockCanvasAndPost(canvas1);  // ???????????????????????????
                            isSoftDrawing = false;
                        }
                        sleep(30);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    };


    /**
     * ??????????????????
     * @param canvas2
     * @param data
     */
    protected void startSyncDraw(Canvas canvas2,byte[] data) {
//	    canvas2.setDrawFilter(pfdf); //?????????
        buffer = ByteBuffer.wrap(data);
        try {
            videoBit.copyPixelsFromBuffer(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Matrix m = new Matrix();
        float scaleX , scaleY;
        int xOff = 0;
        scaleX = (float)screenW/ bitWidth;
        scaleY = (float)screenH/ bitHeight;
        m.postScale(scaleX, scaleY);
        m.postTranslate(xOff, 0);
        drawPartBitmap(canvas2, scaleX, scaleY);
    }
    /**
     * ????????????
     * scaleX < scaleY ??????????????????
     * scaleX > scaleY ??????????????????
     * @param canvas
     * @param scaleX
     * @param scaleY
     */
    private void drawPartBitmap(Canvas canvas, float scaleX, float scaleY){
        Rect src = new Rect();
        RectF dst = new RectF();
        int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        if(isSvc){
            x1=0;y1=0;x2=bitWidth;y2=bitHeight;
            src.set(x1, y1, x2, y2);
            dst.set(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
        }else {
            if(scaleX > scaleY){
                x1 = 0; x2 = bitWidth;
                y1 = (int) ((bitHeight*scaleX-screenH)/(2*scaleX));
                y2 = bitHeight - y1;
            } else {
                y1 = 0; y2 = bitHeight;
                x1 = (int) ((bitWidth*scaleY-screenW)/(2*scaleY));
                x2 = videoBit.getWidth() - x1;
            }
            src.set(x1, y1, x2, y2);
            dst.set(0, 0, screenW, screenH);
        }
        canvas.drawBitmap(videoBit, src, dst, null);
    }

    public void stopDrawThread(){
        isDrawing = false;
    }
    public void changeWatchingState(boolean isWatching){
        this.isWatching = isWatching;
    }


    /**
     * ????????????
     * @param width
     * @param height
     */
    public void setParams(int width, int height) {
        screenW = width;
        screenH = height;
    }

    public void setUserName(){
        String name = userCommon.getUser(this.userID).getUsername();
        if(this.userID==userCommon.getSelf().getUid())
            name = "[???]"+name;
        userName.setText(name);
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
        this.deleteView.setTag(channelId);
    }

    public int getUserID() {
        return userID;
    }

    public int getChannelId() {
        return channelId;
    }

    public SurfaceView getSurfaceView(){
        return surfaceView;
    }

    public interface IOnDeleteAndCameraClick{
        public void onClickDelete();
        public void onClickCamera();
    }
    public  void  setOnDeleteAndCameraClick(IOnDeleteAndCameraClick onclick){
        this.onDeleteAndCameraClick = onclick;
    }
    private void clickDelete(){
        if(this.onDeleteAndCameraClick!=null)onDeleteAndCameraClick.onClickDelete();
    }
    private void clickCamera(){
        if(this.onDeleteAndCameraClick!=null)onDeleteAndCameraClick.onClickCamera();
    }
    public void show(){
        setVisibility(View.VISIBLE);
    }
    public void hide(){
        setVisibility(View.GONE);
    }

    public void changeSize(int w, int h) {
        if (screenW != w || screenH != h) {
            screenW = w;
            screenH = h;
            if (isSvc) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
            }
            setSurface(bitWidth, bitHeight);
            invalidate();
        }

    }

    public void setCurSVCLvl(int w, int h){
        if (screenW != w || screenH != h) {
            screenW = w;
            screenH = h;
            if (isSvc) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
            }
            if(surfaceView!=null&&isCreated){
                setSurface(bitWidth, bitHeight);
            }
        }
    }

    public void setwh11(){
        screenW = 1;
        screenH = 1;
        if(surfaceView!=null&&isCreated){
            setSurface(0, 0);
        }
    }

    public void setSvc(boolean svc) {
        isSvc = svc;
    }

    public class GLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

        GLSurfaceView surfaceView;

        public GLRenderer(GLSurfaceView surfaceView, FilterFactory.FilterType type) {

            this.surfaceView = surfaceView;

            mCurrentFilter = FilterFactory.createFilter(c,type);

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            Log.d(TAG,"VideoDecoderView.onSurfaceCreated");

            screenW=getWidth();
            screenH=getHeight();


            mCurrentFilter.createProgram();
            //mCurrentFilter.onInputSizeChanged(getWidth(),getHeight());

            mTextureId = BaseFilter.bindTexture();

            Log.d(TAG,"VideoDecoderView.bindTexture = " + mTextureId);

            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurface = new Surface(mSurfaceTexture);
            mSurfaceTexture.setOnFrameAvailableListener(this);

            if (videoCommon.isHardDecode()) {
//				if (isSvc) {
//					DisplayMetrics dm = getResources().getDisplayMetrics();
//					videoCommon.changeSvcLevel(channelId, 0, 1.0f * screenW / dm.xdpi, 1.0f * screenH / dm.ydpi);
//				}
                if (videoCommon.getDeviceMap().containsKey(channelId)) {
                    userID = videoCommon.getDeviceMap().get(channelId);
                    setUserName();
                }
                Log.d(TAG,"videoCommon.openVideo = " + channelId);
                videoCommon.openVideo(channelId, mSurface);
            } else {
                if (videoCommon.getDeviceMap().containsKey(channelId)) {
                    userID = videoCommon.getDeviceMap().get(channelId);
                    setUserName();
                }
                HandlerThread surfaceThread = new HandlerThread(
                        "SurfaceHandler");
                surfaceThread.start();
                videoCommon.openVideo(channelId, mSurface);
                videoCommon.addSurfaceHanlder(surfaceThread,
                        new SurfaceHandler(surfaceThread.getLooper()),
                        channelId);
                drawImage = null;
            }
            isCreated = true;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            Log.d(TAG,"VideoDecoderView.onSurfaceChanged: " + width + "x" + height);

            GLES20.glViewport(0, 0, width, height);

            mCurrentFilter.onInputSizeChanged(width,height);

            //mCameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
//			mCurrentFilter.createProgram();
//			mCurrentFilter.onInputSizeChanged(getWidth(),getHeight());
//
//			mTextureId = BaseFilter.bindTexture();
//			mSurfaceTexture = new SurfaceTexture(mTextureId);
//			mSurface = new Surface(mSurfaceTexture);
//			mSurfaceTexture.setOnFrameAvailableListener(this);

//            RectF surfaceDimensions = new RectF(0,0,width,height);
//            RectF previewRect = new RectF(0, 0, cameraHeight, cameraWidth);
//            Matrix matrix = new Matrix();
//            matrix.setRectToRect(previewRect, surfaceDimensions, Matrix.ScaleToFit.FILL);
//            mSurfaceTexture.getTransformMatrix(mSTMatrix);

            //mCameraHelper.startPreview(mSurfaceTexture);
        }

        /**
         * ????????????????????????????????????????????????????????????:
         * 1.????????????????????????????????????
         * 2.??????opengl?????????????????????????????????????????????
         * ????????????????????????
         */

        @Override
        public void onDrawFrame(GL10 gl) {

            //runAll(runOnDraw);
            mSurfaceTexture.updateTexImage();

            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mCurrentFilter.draw(mTextureId,mSTMatrix);

            //runAll(runOnDrawEnd);

        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            //Log.v("aaaaa","avaible");

            Log.d(TAG,"VideoDecoderView.onFrameAvailable");

            if (isWaiting == true){
                showSupportReady();
                isWaiting = false;
            }

            if (surfaceView != null)
                surfaceView.requestRender();
        }

    }

}
