package com.camerakit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CameraKitViewTest {

    @Mock
    private Context mockContext;

    @Mock
    private AttributeSet mockAttributes;

    @Mock
    private TypedArray mockTypedArray;

    @Mock
    private CameraKitView.GestureListener mockGestureListener;

    @Mock
    private CameraKitView.CameraListener mockCameraListener;

    @Mock
    private CameraKitView.ErrorListener mockErrorListener;

    @Mock
    private CameraKitView.PreviewListener mockPreviewListener;

    private static final int TEST_WIDTH = 2;
    private static final int TEST_HEIGHT = 2;
    private static final int TEST_X = 2;
    private static final int TEST_Y = 2;
    private static final int TEST_DS = 2;
    private static final int TEST_DSX = 2;
    private static final int TEST_DSY = 2;
    private static final float TEST_ZOOM_FACTOR = 2.2f;

    private CameraKitView cameraKitView;
    private CameraKitView.Size testSize;
    private CameraKitView.PermissionsListener permissionsListener;

    @Before
    public void setupTests() {
        MockitoAnnotations.initMocks(this);
        when(mockContext.obtainStyledAttributes(mockAttributes, R.styleable.CameraKitView))
                .thenReturn(mockTypedArray);
        cameraKitView = new CameraKitView(mockContext, mockAttributes);
        testSize = new CameraKitView.Size(TEST_WIDTH, TEST_HEIGHT);

        permissionsListener = new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
            }

            @Override
            public void onPermissionsFailure() {
            }
        };
    }

    /**
     * Tests of CameraKitView member functions
     **/
    @Test
    public void onTapTest() {
        cameraKitView.setGestureListener(mockGestureListener);
        cameraKitView.onTap(TEST_X, TEST_Y);
    }

    @Test
    public void onLongTapTest() {
        cameraKitView.setGestureListener(mockGestureListener);
        cameraKitView.onLongTap(TEST_X, TEST_Y);
    }

    @Test
    public void onDoubleTapTest() {
        cameraKitView.setGestureListener(mockGestureListener);
        cameraKitView.onDoubleTap(TEST_X, TEST_Y);
    }

    @Test
    public void onPinchTest() {
        cameraKitView.setGestureListener(mockGestureListener);
        cameraKitView.onPinch(TEST_DS, TEST_DSX, TEST_DSY);
    }

    @Test
    public void onMeasureTest() {
        cameraKitView.onMeasure(TEST_WIDTH, TEST_HEIGHT);
    }

    @Test
    public void setPermissionsListenerTest() {
        cameraKitView.setPermissionsListener(permissionsListener);
    }

    @Test
    public void setAdjustViewBoundsTest() {
        cameraKitView.setAdjustViewBounds(true);
        assertEquals(true, cameraKitView.getAdjustViewBounds());
    }

    @Test
    public void setAspectRatioTest() {
        float testAspectRatio = 1.1f;
        cameraKitView.setAspectRatio(testAspectRatio);
        assertEquals(testAspectRatio, cameraKitView.getAspectRatio());
    }

    @Test
    public void setFacingTest() {
        cameraKitView.setFacing(CameraKit.FACING_BACK);
        assertEquals(CameraKit.FACING_BACK, cameraKitView.getFacing());
    }

    @Test
    public void toggleFacingTest() {
        cameraKitView.setFacing(CameraKit.FACING_BACK);
        cameraKitView.toggleFacing();
        assertEquals(cameraKitView.getFacing(), CameraKit.FACING_FRONT);
    }

    @Test
    public void setFlashTest() {
        cameraKitView.setFlash(CameraKit.FLASH_ON);
        assertEquals(cameraKitView.getFlash(), CameraKit.FLASH_ON);
    }

    @Test
    public void setFocusTest() {
        cameraKitView.setFocus(CameraKit.FOCUS_AUTO);
        assertEquals(cameraKitView.getFocus(), CameraKit.FOCUS_AUTO);
    }

    @Test
    public void setZoomFactorTest() {
        cameraKitView.setZoomFactor(TEST_ZOOM_FACTOR);
        assertEquals(cameraKitView.getZoomFactor(), TEST_ZOOM_FACTOR);
    }

    @Test
    public void setSensorPresetTest() {
        cameraKitView.setSensorPreset(CameraKit.SENSOR_PRESET_ACTION);
        assertEquals(cameraKitView.getSensorPreset(), CameraKit.SENSOR_PRESET_ACTION);
    }

    @Test
    public void setPreviewEffectTest() {
        cameraKitView.setPreviewEffect(CameraKit.PREVIEW_EFFECT_MONO);
        assertEquals(cameraKitView.getPreviewEffect(), CameraKit.PREVIEW_EFFECT_MONO);
    }

    @Test
    public void setPermissionsTest() {
        cameraKitView.setPermissions(CameraKitView.PERMISSION_CAMERA);
        assertEquals(cameraKitView.getPermissions(), CameraKitView.PERMISSION_CAMERA);
    }

    @Test
    public void setGestureListenerTest() {
        cameraKitView.setGestureListener(mockGestureListener);
        assertEquals(cameraKitView.getGestureListener(), mockGestureListener);
    }

    @Test
    public void setCameraListener() {
        cameraKitView.setCameraListener(mockCameraListener);
        assertEquals(cameraKitView.getCameraListener(), mockCameraListener);
    }

    @Test
    public void setPreviewListener() {
        cameraKitView.setPreviewListener(mockPreviewListener);
        assertEquals(cameraKitView.getPreviewListener(), mockPreviewListener);
    }

    @Test
    public void setErrorListener() {
        cameraKitView.setErrorListener(mockErrorListener);
        assertEquals(cameraKitView.getErrorListener(), mockErrorListener);
    }

    /**
     * Tests of CameraKitView.Size member functions
     */
    @Test
    public void getWidthTest() {
        assertEquals(testSize.getWidth(), TEST_WIDTH);
    }

    @Test
    public void getHeightTest() {
        assertEquals(testSize.getHeight(), TEST_HEIGHT);
    }

    @Test
    public void equalsTest() {
        CameraKitView.Size size = new CameraKitView.Size(TEST_WIDTH, TEST_HEIGHT);
        assert (testSize.equals(size));
    }

    @Test
    public void sizeToStringTest() {
        String testSizeSting = TEST_WIDTH + "x" + TEST_HEIGHT;
        assertEquals(testSize.toString(), testSizeSting);
    }

    @Test
    public void sizeHashCodeTest() {
        int testSizeHashCode = TEST_HEIGHT ^ ((TEST_WIDTH << (Integer.SIZE / 2)) | (TEST_WIDTH >>> (Integer.SIZE / 2)));
        assertEquals(testSize.hashCode(), testSizeHashCode);
    }

    @Test
    public void sizeCompareToTest() {
        int testCompareTo = 0;
        CameraKitView.Size size = new CameraKitView.Size(TEST_WIDTH, TEST_HEIGHT);
        assertEquals(testSize.compareTo(size), testCompareTo);
    }

}
