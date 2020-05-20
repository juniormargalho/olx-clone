package com.juniormargalho.olx.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.juniormargalho.olx.R;
import com.juniormargalho.olx.helper.ConfiguracaoFirebase;
import com.juniormargalho.olx.helper.Permissoes;
import com.juniormargalho.olx.model.Anuncio;
import com.santalu.maskedittext.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CadastrarAnuncioActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private ImageView imagem1, imagem2, imagem3;
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private Spinner campoEstado, campoCategoria;
    private Anuncio anuncio;
    private StorageReference storage;
    private List<String> listaUrlFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //configuracoes iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();

        carregarDadosSpinner();
    }

    public void salvarAnuncio() {
        for(int i = 0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            salvarFotoStorage(urlImagem, listaFotosRecuperadas.size(), i);
        }
    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){
        final StorageReference imagemAnuncio = storage
                .child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + contador);
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlConvertida = uri.toString();
                        listaUrlFotos.add(urlConvertida);
                        if(totalFotos == listaUrlFotos.size()){
                            anuncio.setFotos(listaUrlFotos);
                            anuncio.salvar();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao enviar imagem!");
            }
        });
    }

    private Anuncio configurarAnuncio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = String.valueOf(campoValor.getRawValue());
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    public void validarDadosAnuncio(View view){

        anuncio = configurarAnuncio();

        String fone = "";
        if(campoTelefone.getRawText() != null){
            fone = campoTelefone.getRawText().toString();
        }

        if(listaFotosRecuperadas.size() != 0){
            if(!anuncio.getEstado().isEmpty()){
                if(!anuncio.getCategoria().isEmpty()){
                    if(!anuncio.getTitulo().isEmpty()){
                        if(!anuncio.getValor().isEmpty() && !anuncio.getValor().equals("0")){
                            if(!anuncio.getTelefone().isEmpty() && fone.length() >= 11){
                                if(!anuncio.getDescricao().isEmpty()){
                                    salvarAnuncio();
                                }else {
                                    exibirMensagemErro("Preencha o campo descrição!");
                                }
                            }else {
                                exibirMensagemErro("Preencha o campo telefone!");
                            }
                        }else {
                            exibirMensagemErro("Preencha o campo valor!");
                        }
                    }else {
                        exibirMensagemErro("Preencha o campo título!");
                    }
                }else {
                    exibirMensagemErro("Selecione uma categoria!");
                }
            }else {
                exibirMensagemErro("Selecione um estado!");
            }
        }else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void exibirMensagemErro(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();
            if (requestCode == 1) {
                imagem1.setImageURI(imagemSelecionada);
            } else if (requestCode == 2) {
                imagem2.setImageURI(imagemSelecionada);
            } else if (requestCode == 3) {
                imagem3.setImageURI(imagemSelecionada);
            }
            listaFotosRecuperadas.add(caminhoImagem);
        }
    }

    private void carregarDadosSpinner(){
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapterEstado);

        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);
    }

    private void inicializarComponentes() {
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);

        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o App é necessário aceitar todas as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
