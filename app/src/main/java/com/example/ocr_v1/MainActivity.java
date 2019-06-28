package com.example.ocr_v1;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText mResult;
    ImageView mPreview;
    private  static final int CAMERA_REQUEST_CODE=200;
    private  static final int STORAGE_REQUEST_CODE=400;
    private  static final int IMAGEN_PICK_GALLERY_CODE=1000;
    private  static final int IMAGEN_PICK_CAMERA_CODE=1001;

    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
            ActionBar actionBar=getSupportActionBar();
            actionBar.setSubtitle("Click+ Botón para insertar Imagen");
        //Validamos los campos con las ariables correspondidad
        mPreview=findViewById(R.id.imagenvista);
        mResult=findViewById(R.id.resultado);
        //Permiso para la camara
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }
    //action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflater menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.addImagen){
            showImagenImportarDialig();
        }
        if (id==R.id.setting){
            Toast.makeText(this,"Setting",Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showImagenImportarDialig() {

        String[] items={"Camara","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Seleccionar imagen");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //opcionbes al hacer click la camara
                    if (!checkCameraPermission()){
                        //Permiso para camara
                        requestCameraPermission();
                    }
                    else{
                        //permiso para  tomar foto
                        pickCamera();
                    }

                }
                if (which==1){
                    //galeria de fotos
                    if (!checkStoragePermission()){
                        //Permiso para almacenamiento
                        requestStoragePermission();
                    }
                    else{
                        //permiso para  tomar foto
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();

    }

    private void pickGallery() {
        //Con la intencion de  guarda la foto en la memoria interna
        Intent intent=new Intent(Intent.ACTION_PICK);
        //set intent type to  image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGEN_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //con la intención de tomar la imagen desde la cámara, también se guardará en el almacenamiento para obtener una imagen de alta calidad
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Nueva Foto");//titulo de la  foto
        values.put(MediaStore.Images.Media.DESCRIPTION,"Imagen a texto");//descripcion
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent camaeraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaeraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(camaeraIntent,IMAGEN_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_APN_SETTINGS)==(PackageManager.PERMISSION_GRANTED);
    return  result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //Manejar el resultado del permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }else {
                        Toast.makeText(this,"Permiso denegado",Toast.LENGTH_SHORT).show();
                    }

                }
                 break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length>0){

                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickGallery();
                    }else {
                        Toast.makeText(this,"Permiso denegado",Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }

    //Manejar el resultado de la imagen.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //obtengo la imagen de la cámara
        if (requestCode==RESULT_OK){
            if (requestCode==IMAGEN_PICK_GALLERY_CODE){
                //Consigo la  imagen de la galleria
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON) //habilitar lineamientos de imagen
                        .start(this);
            }
            if (requestCode==IMAGEN_PICK_GALLERY_CODE){
                //Consigo la  imagen de la camara
                CropImage.activity(image_uri).
                        setGuidelines(CropImageView.Guidelines.ON) //habilitar lineamientos de imagen
                        .start(this);

            }
        }
        //get croped iamge
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                Uri resultUri=result.getUri();//obetener    el elnace de la imagen
                //establecer imagen para  visualizar
                mPreview.setImageURI(resultUri);
                //obtener un mapa de bits dibujable para el reconocimiento de texto
                BitmapDrawable bitmapDrawable=(BitmapDrawable)mPreview.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();
                TextRecognizer recognizer=new TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()){
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();

                }else {
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=recognizer.detect(frame);
                    StringBuilder sb=new StringBuilder();
                    //obtener texto de sub hasta que no haya texto
                    for (int i=0;i<items.size();i++){
                        TextBlock myItem=items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");

                    }

                    //configurar texto para editar texto
                    mResult.setText(sb.toString());
                }
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //si hay algún error muéstralo
                Exception error=result.getError();
                Toast.makeText(this,"Muetrar error"+error,Toast.LENGTH_SHORT).show();

            }
        }
    }
}


